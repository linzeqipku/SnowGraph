package searcher.codepattern.code.ir;

import com.github.javaparser.ast.body.ReceiverParameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.*;
import com.github.javaparser.ast.stmt.*;
import com.github.javaparser.ast.visitor.GenericVisitorAdapter;
import searcher.codepattern.code.ir.IRExpression.*;
import searcher.codepattern.code.ir.statement.*;
import searcher.codepattern.utils.Predicates;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class IRVisitor extends GenericVisitorAdapter<VisitorResult, VisitorContext> {
    private static Logger logger = LoggerFactory.getLogger(IRVisitor.class);

    private IRRepresentation block;

    IRVisitor(IRRepresentation block) {
        this.block = block;
    }

    // - Body ----------------------------------------------
    @Override
    public VisitorResult visit(ReceiverParameter n, VisitorContext arg) {
        throw new UnsupportedSyntaxException();
    }

    @Override
    public VisitorResult visit(VariableDeclarator n, VisitorContext arg) {
        n.getInitializer().ifPresent(x -> arg.addStatement(new IRAssignment((IRExpression) x.accept(this, arg), block.getVariableOrCreate(n.getName().getIdentifier()))));
        return null;
    }

    // - Statements ----------------------------------------
    @Override
    public VisitorResult visit(ExplicitConstructorInvocationStmt n, VisitorContext arg) {
        if (n.isThis()) {
            List<IRExpression> args = n.getArguments().stream().map(x -> (IRExpression) x.accept(this, arg)).collect(Collectors.toList());
            IRTemp target = block.createTempVariable();
            arg.addStatement(new IRMethodInvocation(null, "this", args, target));
        } else {
            List<IRExpression> args = n.getArguments().stream().map(x -> (IRExpression) x.accept(this, arg)).collect(Collectors.toList());
            IRTemp target = block.createTempVariable();
            arg.addStatement(new IRMethodInvocation(null, "super", args, target));
        }
        return arg;
    }

    @Override
    public VisitorResult visit(AssertStmt n, VisitorContext arg) {
        IRExpression check = (IRExpression) n.getCheck().accept(this, arg);
        arg.addStatement(new IRAssert(check));
        return arg;
    }

    @Override
    public VisitorResult visit(BlockStmt n, VisitorContext arg) {
        n.getStatements().forEach(i -> i.accept(this, arg));
        return arg;
    }

    @Override
    public VisitorResult visit(LabeledStmt n, VisitorContext arg) {
        throw new UnsupportedSyntaxException();
    }

    @Override
    public VisitorResult visit(SwitchStmt n, VisitorContext arg) {
        IRLabel breakLabel = block.createLabel();
        IRExpression expression = (IRExpression) n.getSelector().accept(this, arg);
        IRSwitch irSwitch = new IRSwitch(expression);
        n.getEntries().forEach(x -> {
            if (!x.getLabel().isPresent()) return;
            IRExpression caseExp = (IRExpression) x.getLabel().get().accept(this, arg);
            irSwitch.addCase(caseExp);
        });
        arg.addStatement(irSwitch);

//        breakStack.push(breakLabel);
        n.getEntries().stream().forEach(x -> {
            IRScope scope = x.getLabel().isPresent() ? irSwitch.getRegularScope() : irSwitch.getDefaultScope();
            // TODO: switch
//            x.getStatements().forEach(s -> s.accept(this, new VisitorContext(scope)));
        });
//        breakStack.pop();
        return arg;
    }

    @Override
    public VisitorResult visit(BreakStmt n, VisitorContext arg) {
        arg.addStatement(new IRGoto(arg.getBreakLabel()));
        return arg;
    }

    @Override
    public VisitorResult visit(ReturnStmt n, VisitorContext arg) {
        IRExpression result = n.getExpression().map(x -> (IRExpression) x.accept(this, arg)).orElse(null);
        arg.addStatement(new IRReturn(result));
        return arg;
    }

    @Override
    public VisitorResult visit(IfStmt n, VisitorContext arg) {
        IRExpression condition = (IRExpression) n.getCondition().accept(this, arg);
        IRIf irIf = new IRIf(condition);
        arg.addStatement(irIf);
        n.getThenStmt().accept(this, arg.replaceScope(irIf.getThenScope()));
        n.getElseStmt().ifPresent(x -> x.accept(this, arg.replaceScope(irIf.getElseScope())));
        return arg;
    }

    @Override
    public VisitorResult visit(WhileStmt n, VisitorContext arg) {
        IRLabel loopLabel = block.createLabel();
        IRLabel breakLabel = block.createLabel();

        // condition
        arg.addStatement(loopLabel);
        IRExpression condition = (IRExpression) n.getCondition().accept(this, arg);
        IRIf irIf = new IRIf(condition);
        arg.addStatement(irIf);

        // body
        n.getBody().accept(this, new VisitorContext(irIf.getThenScope(), loopLabel, breakLabel));
        irIf.getThenScope().addStatement(new IRGoto(loopLabel));
        arg.addStatement(breakLabel);
        return arg;
    }

    @Override
    public VisitorResult visit(ContinueStmt n, VisitorContext arg) {
        if (n.getLabel().isPresent()) throw new UnsupportedSyntaxException();
        arg.addStatement(new IRGoto(arg.getContinueLabel()));
        return arg;
    }

    @Override
    public VisitorResult visit(DoStmt n, VisitorContext arg) {
        IRLabel loopLabel = block.createLabel();
        IRLabel breakLabel = block.createLabel();

        arg.addStatement(loopLabel);

        n.getBody().accept(this, arg.replaceLabel(loopLabel, breakLabel));
        IRExpression condition = (IRExpression) n.getCondition().accept(this, arg.replaceLabel(loopLabel, breakLabel));
        IRIf irIf = new IRIf(condition);
        arg.addStatement(irIf);
        irIf.getThenScope().addStatement(new IRGoto(loopLabel));

        arg.addStatement(breakLabel);
        return arg;
    }

    @Override
    public VisitorResult visit(ForeachStmt n, VisitorContext arg) {
        IRLabel loopLabel = block.createLabel();
        IRLabel breakLabel = block.createLabel();

        // init
        IRExpression expression = (IRExpression) n.getIterable().accept(this, arg);
        IRTemp ite = block.createTempVariable();
        arg.addStatement(new IRMethodInvocation(expression, "iterator", new ArrayList<>(), ite));
        arg.addStatement(loopLabel);

        // condition
        IRTemp condition = block.createTempVariable();
        arg.addStatement(new IRMethodInvocation(ite, "hasNext", new ArrayList<>(), condition));
        IRIf irIf = new IRIf(condition);
        arg.addStatement(irIf);

        // body
        irIf.getThenScope().addStatement(new IRMethodInvocation(ite, "next", new ArrayList<>(), block.getVariableOrCreate(n.getVariable().getVariable(0).toString())));
        n.getBody().accept(this, new VisitorContext(irIf.getThenScope(), loopLabel, breakLabel));

        irIf.getThenScope().addStatement(new IRGoto(loopLabel));

        arg.addStatement(breakLabel);
        return arg;
    }

    @Override
    public VisitorResult visit(ForStmt n, VisitorContext arg) {
        IRLabel loopLabel = block.createLabel();
        IRLabel continueLabel = block.createLabel();
        IRLabel breakLabel = block.createLabel();

        // init
        n.getInitialization().forEach(i -> i.accept(this, arg));
        arg.addStatement(loopLabel);

        IRExpression condition = n.getCompare().map(x -> (IRExpression) x.accept(this, arg)).orElse(IRExpression.TRUE);
        IRIf irIf = new IRIf(condition);
        arg.addStatement(irIf);

        // body
        n.getBody().accept(this, new VisitorContext(irIf.getThenScope(), continueLabel, breakLabel));

        // update
        irIf.getThenScope().addStatement(continueLabel);
        n.getUpdate().forEach(i -> i.accept(this, new VisitorContext(irIf.getThenScope(), continueLabel, breakLabel)));
        irIf.getThenScope().addStatement(new IRGoto(loopLabel));

        arg.addStatement(breakLabel);
        return arg;
    }

    @Override
    public VisitorResult visit(ThrowStmt n, VisitorContext arg) {
        arg.addStatement(new IRThrow((IRExpression) n.getExpression().accept(this, arg)));
        return arg;
    }

    @Override
    public VisitorResult visit(SynchronizedStmt n, VisitorContext arg) {
        n.getBody().accept(this, arg);
        return arg;
    }

    @Override
    public VisitorResult visit(TryStmt n, VisitorContext arg) {
        // TODO: auto-close
        // TODO: catches
        List<IRExpression> resources = n.getResources().stream().map(x -> (IRExpression) x.accept(this, arg)).collect(Collectors.toList());
        IRTry irTry = new IRTry(resources);
        arg.addStatement(irTry);
        n.getTryBlock().accept(this, arg.replaceScope(irTry.getTryScope()));
        n.getFinallyBlock().ifPresent(x -> x.accept(this, arg.replaceScope(irTry.getFinallyScope())));
        return arg;
    }

    @Override
    public VisitorResult visit(CatchClause n, VisitorContext arg) {
        // TODO: catches
        return arg;
    }

    @Override
    public VisitorResult visit(UnparsableStmt n, VisitorContext arg) {
        logger.warn("An unparsable statement met. {}", n);
        return arg;
    }

    // - Expression ----------------------------------------
    @Override
    public VisitorResult visit(ArrayAccessExpr n, VisitorContext arg) {
        IRExpression array = (IRExpression) n.getName().accept(this, arg);
        IRExpression index = (IRExpression) n.getIndex().accept(this, arg);
        IRTemp target = block.createTempVariable();
        arg.addStatement(new IRArrayAccess(array, index, target));
        return target;
    }

    @Override
    public VisitorResult visit(ArrayCreationExpr n, VisitorContext arg) {
        List<IRExpression> sizes = n.getLevels().stream().map(x -> (IRExpression) x.accept(this, arg)).filter(Predicates.notNull()).collect(Collectors.toList());

        // TODO: initializers
//        List<IRExpression> initializers = null;
//        if (node.getInitializer() != null) {
//            initializers = new ArrayList<>();
//            initializers.addAll((List<IRExpression>)
//                node.getInitializer().expressions().stream().map(arg -> {
//                    ((Expression) arg).accept(this);
//                    return ret;
//                }).collect(Collectors.toList())
//            );
//        }
        IRTemp target = block.createTempVariable();
        arg.addStatement(new IRArrayCreation(n.getElementType().toString(), sizes, Lists.newArrayList(), target));
        return target;
    }

    @Override
    public VisitorResult visit(ArrayInitializerExpr n, VisitorContext arg) {
        // TODO: initializers
        return null;
    }

    @Override
    public VisitorResult visit(AssignExpr n, VisitorContext arg) {
        IRExpression rhs = (IRExpression) n.getValue().accept(this, arg);
        IRExpression ret = (IRExpression) n.getTarget().accept(this, arg);
        IRAbstractVariable lhs;
        if (ret instanceof IRAbstractVariable) lhs = (IRAbstractVariable) ret;
        else lhs = block.getVariableOrCreate(ret.toString());
        IRStatement irAssignment = null;
        if (n.getOperator() == AssignExpr.Operator.ASSIGN)
            irAssignment = new IRAssignment(rhs, lhs);
        else if (n.getOperator() == AssignExpr.Operator.PLUS)
            irAssignment = new IRBinaryOperation(IRBinaryOperation.Operator.PLUS, lhs.clone(), rhs, lhs);
        else if (n.getOperator() == AssignExpr.Operator.MINUS)
            irAssignment = new IRBinaryOperation(IRBinaryOperation.Operator.MINUS, lhs.clone(), rhs, lhs);
        else if (n.getOperator() == AssignExpr.Operator.MULTIPLY)
            irAssignment = new IRBinaryOperation(IRBinaryOperation.Operator.TIMES, lhs.clone(), rhs, lhs);
        else if (n.getOperator() == AssignExpr.Operator.DIVIDE)
            irAssignment = new IRBinaryOperation(IRBinaryOperation.Operator.DIVIDE, lhs.clone(), rhs, lhs);
        else if (n.getOperator() == AssignExpr.Operator.AND)
            irAssignment = new IRBinaryOperation(IRBinaryOperation.Operator.AND, lhs.clone(), rhs, lhs);
        else if (n.getOperator() == AssignExpr.Operator.OR)
            irAssignment = new IRBinaryOperation(IRBinaryOperation.Operator.OR, lhs.clone(), rhs, lhs);
        else if (n.getOperator() == AssignExpr.Operator.XOR)
            irAssignment = new IRBinaryOperation(IRBinaryOperation.Operator.XOR, lhs.clone(), rhs, lhs);
        else if (n.getOperator() == AssignExpr.Operator.REMAINDER)
            irAssignment = new IRBinaryOperation(IRBinaryOperation.Operator.REMAINDER, lhs.clone(), rhs, lhs);
        else if (n.getOperator() == AssignExpr.Operator.LEFT_SHIFT)
            irAssignment = new IRBinaryOperation(IRBinaryOperation.Operator.LEFT_SHIFT, lhs.clone(), rhs, lhs);
        else if (n.getOperator() == AssignExpr.Operator.SIGNED_RIGHT_SHIFT)
            irAssignment = new IRBinaryOperation(IRBinaryOperation.Operator.RIGHT_SHIFT_SIGNED, lhs.clone(), rhs, lhs);
        else if (n.getOperator() == AssignExpr.Operator.UNSIGNED_RIGHT_SHIFT)
            irAssignment = new IRBinaryOperation(IRBinaryOperation.Operator.RIGHT_SHIFT_UNSIGNED, lhs.clone(), rhs, lhs);
        arg.addStatement(irAssignment);
        return lhs;
    }

    @Override
    public VisitorResult visit(BinaryExpr n, VisitorContext arg) {
        IRExpression left = (IRExpression) n.getLeft().accept(this, arg);
        IRExpression right = (IRExpression) n.getRight().accept(this, arg);
        IRTemp target = block.createTempVariable();
        arg.addStatement(new IRBinaryOperation(IRBinaryOperation.Operator.toOperator(n.getOperator().asString()), left, right, target));
        return target;
    }

    @Override
    public VisitorResult visit(ClassExpr n, VisitorContext arg) {
        return new IRType(n.getType().asString());
    }

    @Override
    public VisitorResult visit(ConditionalExpr n, VisitorContext arg) {
        IRExpression condition = (IRExpression) n.getCondition().accept(this, arg);
        IRExpression thenExp = (IRExpression) n.getThenExpr().accept(this, arg);
        IRExpression elseExp = (IRExpression) n.getElseExpr().accept(this, arg);
        IRTemp target = block.createTempVariable();
        IRIf irIf = new IRIf(condition);
        irIf.getThenScope().addStatement(new IRAssignment(thenExp, target));
        irIf.getElseScope().addStatement(new IRAssignment(elseExp, target));
        arg.addStatement(irIf);
        return target;
    }

    @Override
    public VisitorResult visit(FieldAccessExpr n, VisitorContext arg) {
        IRExpression receiver = (IRExpression) n.getScope().accept(this, arg);
        IRTemp target = block.createTempVariable();
        arg.addStatement(new IRFieldAccess(receiver, n.getName().getIdentifier(), target));
        return target;
    }

    @Override
    public VisitorResult visit(InstanceOfExpr n, VisitorContext arg) {
        IRExpression left = (IRExpression) n.getExpression().accept(this, arg);
        IRExpression right = block.getVariableOrExtern(n.getType().toString());
        IRTemp target = block.createTempVariable();
        arg.addStatement(new IRBinaryOperation(IRBinaryOperation.Operator.INSTANCE_OF, left, right, target));
        return target;
    }

    @Override
    public VisitorResult visit(StringLiteralExpr n, VisitorContext arg) {
        return new IRString(n.asString());
    }

    @Override
    public VisitorResult visit(IntegerLiteralExpr n, VisitorContext arg) {
        return new IRNumber(n.getValue());
    }

    @Override
    public VisitorResult visit(LongLiteralExpr n, VisitorContext arg) {
        return new IRNumber(n.getValue());
    }

    @Override
    public VisitorResult visit(CharLiteralExpr n, VisitorContext arg) {
        return new IRChar(n.asChar());
    }

    @Override
    public VisitorResult visit(DoubleLiteralExpr n, VisitorContext arg) {
        return new IRNumber(n.getValue());
    }

    @Override
    public VisitorResult visit(BooleanLiteralExpr n, VisitorContext arg) {
        return n.getValue() ? IRExpression.TRUE : IRExpression.FALSE;
    }

    @Override
    public VisitorResult visit(NullLiteralExpr n, VisitorContext arg) {
        return IRExpression.NULL;
    }

    @Override
    public VisitorResult visit(MethodCallExpr n, VisitorContext arg) {
        Optional<Expression> receiver = n.getScope();
        IRExpression receiverExp = receiver.map(x -> (IRExpression) x.accept(this, arg)).orElse(IRExpression.THIS);
        List<IRExpression> args = n.getArguments().stream().map(x -> (IRExpression) x.accept(this, arg)).collect(Collectors.toList());
        IRTemp target = block.createTempVariable();
        arg.addStatement(new IRMethodInvocation(receiverExp, n.getName().getIdentifier(), args, target));
        return target;
    }

    @Override
    public VisitorResult visit(NameExpr n, VisitorContext arg) {
        return n.getName().accept(this, arg);
    }

    @Override
    public VisitorResult visit(ObjectCreationExpr n, VisitorContext arg) {
        List<IRExpression> args = n.getArguments().stream().map(x -> (IRExpression) x.accept(this, arg)).collect(Collectors.toList());
        IRTemp target = block.createTempVariable();
        arg.addStatement(new IRMethodInvocation(null, n.getType().toString() + ".<init>", args, target));
        return target;
    }

    @Override
    public VisitorResult visit(ThisExpr n, VisitorContext arg) {
        return IRExpression.THIS;
    }

    @Override
    public VisitorResult visit(SuperExpr n, VisitorContext arg) {
        // TODO: more specific super
        return IRExpression.SUPER;
    }

    @Override
    public VisitorResult visit(UnaryExpr n, VisitorContext arg) {
        IRExpression operand = (IRExpression) n.getExpression().accept(this, arg);
        IRAbstractVariable ope = block.getVariableOrCreate(operand.toString());
        IRTemp target = block.createTempVariable();
        if (n.getOperator().isPostfix()) {
            arg.addStatement(new IRAssignment(ope.clone(), target));
            IRBinaryOperation.Operator operator;
            if (n.getOperator() == UnaryExpr.Operator.POSTFIX_INCREMENT) operator = IRBinaryOperation.Operator.PLUS;
            else if (n.getOperator() == UnaryExpr.Operator.POSTFIX_DECREMENT) operator = IRBinaryOperation.Operator.MINUS;
            else throw new RuntimeException("Unknown Postfix operator!");
            arg.addStatement(new IRBinaryOperation(operator, ope.clone(), new IRNumber("1"), ope.clone()));
        } else {
            IRBinaryOperation.Operator operator;
            if (n.getOperator() == UnaryExpr.Operator.PREFIX_INCREMENT) operator = IRBinaryOperation.Operator.PLUS;
            else if (n.getOperator() == UnaryExpr.Operator.PREFIX_DECREMENT) operator = IRBinaryOperation.Operator.MINUS;
            else {
                arg.addStatement(new IRPrefixOperation(n.getOperator(), operand, target));
                return target;
            }
            arg.addStatement(new IRBinaryOperation(operator, ope.clone(), new IRNumber("1"), ope.clone()));
            arg.addStatement(new IRAssignment(ope.clone(), target));
        }
        return target;
    }

    @Override
    public VisitorResult visit(VariableDeclarationExpr n, VisitorContext arg) {
        n.getVariables().stream().forEach(x -> x.accept(this, arg));
        return null;
    }

    // - Special ----------------------------------------
    @Override
    public VisitorResult visit(LambdaExpr n, VisitorContext arg) {
        throw new UnsupportedSyntaxException();
    }

    @Override
    public VisitorResult visit(MethodReferenceExpr n, VisitorContext arg) {
        throw new UnsupportedSyntaxException();
    }

    @Override
    public VisitorResult visit(TypeExpr n, VisitorContext arg) {
        throw new UnsupportedSyntaxException();
    }

    @Override
    public VisitorResult visit(Name n, VisitorContext arg) {
        Optional<Name> q = n.getQualifier();
        if (q.isPresent()) {
            IRExpression receiver = (IRExpression) q.get().accept(this, arg);
            IRTemp target = block.createTempVariable();
            arg.addStatement(new IRFieldAccess(receiver, n.getIdentifier(), target));
            return target;
        } else {
            return block.getVariableOrExtern(n.getIdentifier());
        }
    }

    @Override
    public VisitorResult visit(SimpleName n, VisitorContext arg) {
        return block.getVariableOrExtern(n.getIdentifier());
    }


//    public boolean visit(CreationReference node) {
//        IRTemp target = block.createTempVariable();
//        IRExpression operand = block.getVariableOrExtern(node.getType().toString());
//        scopeStack.peek().addStatement(new IRReference(operand, "new", target));
//        return false;
//    }

//    public boolean visit(ExpressionMethodReference node) {
//        node.getExpression().accept(this);
//        IRTemp target = block.createTempVariable();
//        scopeStack.peek().addStatement(new IRReference((IRExpression) ret, node.getName().toString(), target));
//        return false;
//    }


//    public boolean visit(SuperFieldAccess node) {
//        IRTemp target = block.createTempVariable();
//        scopeStack.peek().addStatement(new IRFieldAccess(IRExpression.SUPER, node.getName().toString(), target));
//        ret = target;
//        return true;
//    }

//    public boolean visit(SuperMethodInvocation node) {
//        List<IRExpression> args = new ArrayList<>();
//        node.arguments().forEach(arg -> {
//            ((Expression) arg).accept(this);
//            args.add((IRExpression) ret);
//        });
//        IRTemp target = block.createTempVariable();
//        scopeStack.peek().addStatement(new IRMethodInvocation(IRExpression.SUPER, node.getName().toString(), args, target));
//        ret = target;
//        return true;
//    }

//    public boolean visit(SuperMethodReference node) {
//        IRTemp target = block.createTempVariable();
//        scopeStack.peek().addStatement(new IRReference(IRExpression.SUPER, node.getName().toString(), target));
//        return false;
//    }

//    public boolean visit(TypeMethodReference node) {
//        IRTemp target = block.createTempVariable();
//        scopeStack.peek().addStatement(new IRReference(block.getVariableOrExtern(node.getType().toString()), node.getName().toString(), target));
//        return false;
//    }

}
