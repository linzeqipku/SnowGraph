package exps.codepattern.code.ir;

import exps.codepattern.code.ir.statement.IRAbstractStatement;
import exps.codepattern.code.ir.statement.IRLabel;

public class VisitorContext implements VisitorResult {
    private IRScope block;
    private IRLabel continueLabel;
    private IRLabel breakLabel;

    public VisitorContext(IRScope block, IRLabel continueLabel, IRLabel breakLabel) {
        this.block = block;
        this.continueLabel = continueLabel;
        this.breakLabel = breakLabel;
    }

    void addStatement(IRAbstractStatement statement) {
        block.addStatement(statement);
    }

    public VisitorContext replaceScope(IRScope newScope) {
        return new VisitorContext(newScope, continueLabel, breakLabel);
    }

    public VisitorContext replaceLabel(IRLabel newContinueLabel, IRLabel newBreakLabel) {
        return new VisitorContext(block, newContinueLabel, newBreakLabel);
    }

    public IRLabel getContinueLabel() {
        return continueLabel;
    }

    public IRLabel getBreakLabel() {
        return breakLabel;
    }
}
