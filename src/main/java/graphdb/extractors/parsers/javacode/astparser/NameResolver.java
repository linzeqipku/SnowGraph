package graphdb.extractors.parsers.javacode.astparser;

import java.io.File;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ImportDeclaration;

/**
 * Evaluates fully qualified name of TypeDeclaration, Type and Name objects.
 */
public class NameResolver {

    public static String getSrcDir() {
        return srcDir;
    }

    public static void setSrcDir(String srcDir) {
        NameResolver.srcDir = srcDir;
    }


    private static String srcDir = "";

    /**
     * Evaluates fully qualified name of the TypeDeclaration object.
     */
    public static String getFullName(TypeDeclaration decl) {
        String name = decl.getName().getIdentifier();
        ASTNode parent = decl.getParent();
        // resolve full name e.g.: A.B
        while (parent != null && parent.getClass() == TypeDeclaration.class) {
            name = ((TypeDeclaration) parent).getName().getIdentifier() + "." + name;
            parent = parent.getParent();
        }
        // resolve fully qualified name e.g.: some.package.A.B
        if (decl.getRoot().getClass() == CompilationUnit.class) {
            CompilationUnit root = (CompilationUnit) decl.getRoot();
            if (root.getPackage() != null) {
                PackageDeclaration pack = root.getPackage();
                name = pack.getName().getFullyQualifiedName() + "." + name;
            }
        }
        return name;
    }

    /**
     * Evaluates fully qualified name of the Type object.
     */
    public static String getFullName(Type t) {
        if (t == null)
            return null;
        if (t.isParameterizedType()) {
            ParameterizedType t0 = (ParameterizedType) t;
            return getFullName(t0.getType());
        } else if (t.isQualifiedType()) {
            QualifiedType t0 = (QualifiedType) t;
            return getFullName(t0.getQualifier()) + "." + t0.getName().getIdentifier();
        } else if (t.isSimpleType()) {
            SimpleType t0 = (SimpleType) t;
            return getFullName(t0.getName());
        } else {
            return "?";
        }
    }

    /**
     * Evaluates fully qualified name of the Name object.
     */
    public static String getFullName(Name name) {
        // check if the root node is a CompilationUnit
        if (name.getRoot().getClass() != CompilationUnit.class) {
            // cannot resolve a full name, CompilationUnit root node is missing
            return name.getFullyQualifiedName();
        }
        // get the root node
        CompilationUnit root = (CompilationUnit) name.getRoot();
        // check if the name is declared in the same file
        TypeDeclVisitor tdVisitor = new TypeDeclVisitor(name.getFullyQualifiedName());
        root.accept(tdVisitor);
        if (tdVisitor.getFound()) {
            // the name is the use of the TypeDeclaration in the same file
            return getFullName(tdVisitor.getTypeDecl());
        }
        // check if the name is declared in the same package or imported
        PckgImprtVisitor piVisitor = new PckgImprtVisitor(name.getFullyQualifiedName());
        root.accept(piVisitor);
        if (piVisitor.getFound()) {
            // the name is declared in the same package or imported
            return piVisitor.getFullName();
        }
        // could be a class from the java.lang (String) or a param name (T, E,...)
        return name.getFullyQualifiedName();
    }


    private static class PckgImprtVisitor extends ASTVisitor {
        private boolean found = false;
        private String fullName;
        private String name;
        private String[] nameParts;

        PckgImprtVisitor(String aName) {
            super();
            name = aName;
            nameParts = name.split("\\.");
        }

        private void checkInDir(String dirName) {
            File path = getPath(dirName);
            String fileName = nameParts[0] + ".java";
            File f = new File(path, fileName);
            if (path.isDirectory() && f.isFile()) {
                fullName = dirName;
                for (String namePart : nameParts) {
                    fullName += "." + namePart;
                }
                found = true;
            }
        }

        public boolean visit(PackageDeclaration node) {
            String pckgName = node.getName().getFullyQualifiedName();
            checkInDir(pckgName);
            return true;
        }

        public boolean visit(ImportDeclaration node) {
            if (node.isOnDemand()) {
                String pckgName = node.getName().getFullyQualifiedName();
                checkInDir(pckgName);
            } else {
                String importName = node.getName().getFullyQualifiedName();
                if (importName.endsWith("." + nameParts[0])) {
                    fullName = importName;
                    for (int i = 1; i < nameParts.length; i++) {
                        fullName += "." + nameParts[i];
                    }
                    found = true;
                }
            }
            return true;
        }

        public boolean getFound() {
            return found;
        }

        public String getFullName() {
            return fullName;
        }

        public File getPath(String name) {
            String[] parts = name.split("\\.");
            File path = new File(srcDir);
            for (String part : parts) {
                if (part.equals("")) {
                    break;
                } else if (Character.isUpperCase(part.charAt(0))) {
                    path = new File(path, part + ".java");
                    break;
                } else {
                    path = new File(path, part);
                }
            }
            return path;
        }

    }


    private static class TypeDeclVisitor extends ASTVisitor {
        private boolean found = false;
        private TypeDeclaration typeDecl;
        private String name;

        TypeDeclVisitor(String aName) {
            super();
            name = aName;
        }

        public boolean visit(TypeDeclaration node) {
            if (getFullName(node).endsWith("." + name)) {
                found = true;
                typeDecl = node;
            }
            return true;
        }

        public boolean getFound() {
            return found;
        }

        public TypeDeclaration getTypeDecl() {
            return typeDecl;
        }
    }
}
