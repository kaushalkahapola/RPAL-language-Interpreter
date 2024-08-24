package AST;

import java.util.ArrayList;

public class ASTNode {

    private String data;
    private int depth;
    private ASTNode parent;
    public ArrayList<ASTNode> children;
    public boolean isStandardized = false;

    public ASTNode() {

    }

    public void setData(String data) {
        this.data = data;
    }

    public String getData() {
        return this.data;
    }

    public int getDegree() {
        return children.size();
    }

    public void setDepth(int depth) {
        this.depth = depth;
    }

    public int getDepth() {
        return this.depth;
    }

    public void setParent(ASTNode parent) {
        this.parent = parent;
    }

    public ASTNode getParent() {
        return this.parent;
    }

    public static ASTNode getNode(String data, int depth) {
        ASTNode ASTNode = new ASTNode();
        ASTNode.setData(data);
        ASTNode.setDepth(depth);
        ASTNode.children = new ArrayList<ASTNode>();
        return ASTNode;
    }

    public static ASTNode getNode(String data, int depth, ASTNode parent, ArrayList<ASTNode> children, boolean isStandardize) {
        ASTNode ASTNode = new ASTNode();
        ASTNode.setData(data);
        ASTNode.setDepth(depth);
        ASTNode.setParent(parent);
        ASTNode.children = children;
        ASTNode.isStandardized = isStandardize;
        return ASTNode;
    }

    public void standardize() {
        if (!this.isStandardized) {
            for (ASTNode child : this.children) {
                child.standardize();
            }
            switch (this.getData()) {
                case "let":
                    ASTNode temp1 = this.children.get(0).children.get(1);
                    temp1.setParent(this);
                    temp1.setDepth(this.depth + 1);
                    ASTNode temp2 = this.children.get(1);
                    temp2.setParent(this.children.get(0));
                    temp2.setDepth(this.depth + 2);
                    this.children.set(1, temp1);
                    this.children.get(0).setData("lambda");
                    this.children.get(0).children.set(1, temp2);
                    this.setData("gamma");
                    break;
                case "where":
                    ASTNode temp = this.children.get(0);
                    this.children.set(0, this.children.get(1));
                    this.children.set(1, temp);
                    this.setData("let");
                    this.standardize();
                    break;
                case "function_form":
                    ASTNode Ex = this.children.get(this.children.size() - 1);
                    ASTNode currentLambda = this.getNode("lambda", this.depth + 1, this, new ArrayList<ASTNode>(), true);
                    this.children.add(1, currentLambda);
                    while (!this.children.get(2).equals(Ex)) {
                        ASTNode V = this.children.get(2);
                        this.children.remove(2);
                        V.setDepth(currentLambda.depth + 1);
                        V.setParent(currentLambda);
                        currentLambda.children.add(V);
                        if (this.children.size() > 3) {
                            currentLambda = this.getNode("lambda", currentLambda.depth + 1, currentLambda, new ArrayList<ASTNode>(), true);
                            currentLambda.getParent().children.add(currentLambda);
                        }
                    }
                    currentLambda.children.add(Ex);
                    this.children.remove(2);
                    this.setData("=");
                    break;
                case "lambda":
                    if (this.children.size() > 2) {
                        ASTNode Ey = this.children.get(this.children.size() - 1);
                        ASTNode currentLambdax = this.getNode("lambda", this.depth + 1, this, new ArrayList<ASTNode>(), true);
                        this.children.add(1, currentLambdax);
                        while (!this.children.get(2).equals(Ey)) {
                            ASTNode V = this.children.get(2);
                            this.children.remove(2);
                            V.setDepth(currentLambdax.depth + 1);
                            V.setParent(currentLambdax);
                            currentLambdax.children.add(V);
                            if (this.children.size() > 3) {
                                currentLambdax = this.getNode("lambda", currentLambdax.depth + 1, currentLambdax, new ArrayList<ASTNode>(), true);
                                currentLambdax.getParent().children.add(currentLambdax);
                            }
                        }
                        currentLambdax.children.add(Ey);
                        this.children.remove(2);
                    }
                    break;
                case "within":
                    ASTNode X1 = this.children.get(0).children.get(0);
                    ASTNode X2 = this.children.get(1).children.get(0);
                    ASTNode E1 = this.children.get(0).children.get(1);
                    ASTNode E2 = this.children.get(1).children.get(1);
                    ASTNode gamma = this.getNode("gamma", this.depth + 1, this, new ArrayList<ASTNode>(), true);
                    ASTNode lambda = this.getNode("lambda", this.depth + 2, gamma, new ArrayList<ASTNode>(), true);
                    X1.setDepth(X1.depth + 1);
                    X1.setParent(lambda);
                    X2.setDepth(X1.depth - 1);
                    X2.setParent(this);
                    E1.setDepth(E1.depth);
                    E1.setParent(gamma);
                    E2.setDepth(E2.depth + 1);
                    E2.setParent(lambda);
                    lambda.children.add(X1);
                    lambda.children.add(E2);
                    gamma.children.add(lambda);
                    gamma.children.add(E1);
                    this.children.clear();
                    this.children.add(X2);
                    this.children.add(gamma);
                    this.setData("=");
                    break;
                case "@":
                    ASTNode gamma1 = this.getNode("gamma", this.depth + 1, this, new ArrayList<ASTNode>(), true);
                    ASTNode e1 = this.children.get(0);
                    e1.setDepth(e1.getDepth() + 1);
                    e1.setParent(gamma1);
                    ASTNode n = this.children.get(1);
                    n.setDepth(n.getDepth() + 1);
                    n.setParent(gamma1);
                    gamma1.children.add(n);
                    gamma1.children.add(e1);
                    this.children.remove(0);
                    this.children.remove(0);
                    this.children.add(0, gamma1);
                    this.setData("gamma");
                    break;
                case "and":
                    ASTNode comma = this.getNode(",", this.depth + 1, this, new ArrayList<ASTNode>(), true);
                    ASTNode tau = this.getNode("tau", this.depth + 1, this, new ArrayList<ASTNode>(), true);
                    for (ASTNode equal : this.children) {
                        equal.children.get(0).setParent(comma);
                        equal.children.get(1).setParent(tau);
                        comma.children.add(equal.children.get(0));
                        tau.children.add(equal.children.get(1));
                    }
                    this.children.clear();
                    this.children.add(comma);
                    this.children.add(tau);
                    this.setData("=");
                    break;
                case "rec":
                    ASTNode X = this.children.get(0).children.get(0);
                    ASTNode E = this.children.get(0).children.get(1);
                    ASTNode F = this.getNode(X.getData(), this.depth + 1, this, X.children, true);
                    ASTNode G = this.getNode("gamma", this.depth + 1, this, new ArrayList<ASTNode>(), true);
                    ASTNode Y = this.getNode("<Y*>", this.depth + 2, G, new ArrayList<ASTNode>(), true);
                    ASTNode L = this.getNode("lambda", this.depth + 2, G, new ArrayList<ASTNode>(), true);
                    X.setDepth(L.depth + 1);
                    X.setParent(L);
                    E.setDepth(L.depth + 1);
                    E.setParent(L);
                    L.children.add(X);
                    L.children.add(E);
                    G.children.add(Y);
                    G.children.add(L);
                    this.children.clear();
                    this.children.add(F);
                    this.children.add(G);
                    this.setData("=");
                    break;
                default:
                    break;
            }
        }
        this.isStandardized = true;
    }
}

