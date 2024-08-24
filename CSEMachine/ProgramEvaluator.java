package CSEMachine;

import java.util.ArrayList;
import java.util.List;
import Parser.Node;
import Parser.Parser;
import AST.AST;
import AST.ASTNode;
import Lexer.Lexer;
import Lexer.Token;
import Symbols.*;

// Class responsible for evaluating RPal programs
public class ProgramEvaluator {
    private static E initialEnvironment = new E(0); // Initial environment
    private static int lambdaIndex = 1; // Index for lambda expressions
    private static int deltaIndex = 0; // Index for delta expressions

    // Convert the list of strings into an Abstract Syntax Tree (AST)
    private static AST getAST(ArrayList<String> data) {
        ASTNode root = ASTNode.getNode(data.get(0), 0);
        ASTNode prevASTNode = root;
        int currDepth = 0;

        for (String s : data.subList(1, data.size())) {
            int i = 0;
            int d = 0;

            while (s.charAt(i) == '.') {
                d++;
                i++;
            }

            ASTNode current_ASTNode = ASTNode.getNode(s.substring(i), d);

            if (currDepth < d) {
                prevASTNode.children.add(current_ASTNode);
                current_ASTNode.setParent(prevASTNode);
            } else {
                while (prevASTNode.getDepth() != d) {
                    prevASTNode = prevASTNode.getParent();
                }
                prevASTNode.getParent().children.add(current_ASTNode);
                current_ASTNode.setParent(prevASTNode.getParent());
            }

            prevASTNode = current_ASTNode;
            currDepth = d;
        }
        return new AST(root);
    }

    // Evaluate the RPal program
    public static String evaluate(String input, boolean isPrintAST) {
        Lexer scanner = new Lexer(input);
        List<Token> tokens;
        List<Node> AST;
        try {
            tokens = scanner.tokenize();
            if (tokens.isEmpty()) {
                System.out.println("Program is Empty!, Try a different file");
                return "";
            }
            Parser parser = new Parser(tokens);
            AST = parser.parse();
            ArrayList<String> ASTString = parser.ASTtoString();
            if (isPrintAST) {
                for (String string : ASTString) {
                    System.out.println(string);
                }
            } else {
                AST ast = getAST(ASTString);

                ast.standardize();

                CSEMachine csemachine = createCSEMachine(ast);
                return csemachine.getAnswer();
            }
            return null;
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return null;
    }

    // Create a CSEMachine instance for the given AST
    private static CSEMachine createCSEMachine(AST ast) {
        return new CSEMachine(createControl(ast), createStack(), createEnvironment());
    }

    // Create the control structure for the CSEMachine
    private static ArrayList<Symbol> createControl(AST ast) {
        ArrayList<Symbol> control = new ArrayList<>();
        control.add(initialEnvironment);
        control.add(createDelta(ast.getRoot()));
        return control;
    }

    // Create the stack for the CSEMachine
    private static ArrayList<Symbol> createStack() {
        ArrayList<Symbol> stack = new ArrayList<>();
        stack.add(initialEnvironment);
        return stack;
    }

    // Create the environment for the CSEMachine
    private static ArrayList<E> createEnvironment() {
        ArrayList<E> environment = new ArrayList<>();
        environment.add(initialEnvironment);
        return environment;
    }

    // Create delta expression from the ASTNode
    private static Delta createDelta(ASTNode node) {
        Delta delta = new Delta(deltaIndex++);
        delta.symbols = traversePreOrder(node);
        return delta;
    }

    // Traverse the AST in pre-order and create symbols accordingly
    private static ArrayList<Symbol> traversePreOrder(ASTNode node) {
        ArrayList<Symbol> symbols = new ArrayList<>();
        if ("lambda".equals(node.getData())) {
            symbols.add(createLambda(node));
        } else if ("->".equals(node.getData())) {
            symbols.add(createDelta(node.children.get(1)));
            symbols.add(createDelta(node.children.get(2)));
            symbols.add(new Beta());
            symbols.add(createB(node.children.get(0)));
        } else {
            symbols.add(getSymbol(node));
            for (ASTNode child : node.children) {
                symbols.addAll(traversePreOrder(child));
            }
        }
        return symbols;
    }

    // Get the corresponding Symbol for the ASTNode
    private static Symbol getSymbol(ASTNode node) {
        // Various cases based on different node types
        switch (node.getData()) {
            case "not":
            case "neg":
                return new Uop(node.getData());
            case "+":
            case "-":
            case "*":
            case "/":
            case "**":
            case "&":
            case "or":
            case "eq":
            case "ne":
            case "ls":
            case "le":
            case "gr":
            case "ge":
            case "aug":
                return new Bop(node.getData());
            case "gamma":
                return new Gamma();
            case "tau":
                return new Tau(node.children.size());
            case "<Y*>":
                return new Ystar();
            default:
                // Handling literals and identifiers
                if (node.getData().startsWith("<ID:")) {
                    return new Id(node.getData().substring(4, node.getData().length() - 1));
                } else if (node.getData().startsWith("<INT:")) {
                    return new Int(node.getData().substring(5, node.getData().length() - 1));
                } else if (node.getData().startsWith("<STR:")) {
                    return new Str(node.getData().substring(6, node.getData().length() - 2));
                } else if (node.getData().startsWith("<nil")) {
                    return new Tup();
                } else if (node.getData().startsWith("<true>")) {
                    return new Bool("true");
                } else if (node.getData().startsWith("<false>")) {
                    return new Bool("false");
                } else if (node.getData().startsWith("<dummy>")) {
                    return new Dummy();
                } else {
                    System.out.println("Err node: " + node.getData());
                    return new Err();
                }
        }
    }

    // Create a B expression from the ASTNode
    private static B createB(ASTNode node) {
        B b = new B();
        b.symbols = traversePreOrder(node);
        return b;
    }

    // Create a Lambda expression from the ASTNode
    private static Lambda createLambda(ASTNode node) {
        Lambda lambda = new Lambda(lambdaIndex++);
        lambda.setDelta(createDelta(node.children.get(1)));
        if (",".equals(node.children.get(0).getData())) {
            for (ASTNode identifier : node.children.get(0).children) {
                lambda.identifiers.add(new Id(identifier.getData().substring(4, identifier.getData().length() - 1)));
            }
        } else {
            lambda.identifiers.add(new Id(node.children.get(0).getData().substring(4, node.children.get(0).getData().length() - 1)));
        }
        return lambda;
    }
}
