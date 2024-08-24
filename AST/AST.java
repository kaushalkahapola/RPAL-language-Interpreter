package AST;

import java.util.ArrayList;

public class AST {
    private ASTNode root;
    
    public AST(ASTNode root) {
        this.setRoot(root);
    }
    
    public void setRoot(ASTNode root) {
        this.root = root;
    }
    
    public ASTNode getRoot() {
        return this.root;
    }
    
    public void standardize() {  
        if (!this.root.isStandardized) {
            this.root.standardize();
        }
    }
    
    private void preTraverse(ASTNode ASTNode, int i) {
        for (int n = 0; n < i; n++) {
            System.out.print(".");
        }
        System.out.println(ASTNode.getData());
        ASTNode.children.forEach((child) -> preTraverse(child, i + 1));
    }
    
    public void printAst() {
        this.preTraverse(this.getRoot(), 0);
    }
}
