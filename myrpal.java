
import CSEMachine.ProgramEvaluator;


public class myrpal {
    
    // Main method, entry point of the program
    public static void main(String[] args) {  

        
        String prog;
        boolean isPrintAST = false; 

        // Check the number of command line arguments
        if(args.length == 0){
            // If no arguments provided, set prog to a default file
            prog = "sample.txt"; 
        }
        else if(args.length == 2){
            // If two arguments provided
            prog = args[1]; // Set prog to the second argument
            if(args[0].equalsIgnoreCase("-ast")){
                // If the first argument is "-ast", set isPrintAST to true
                isPrintAST = true;
            }
            else{
                // If the first argument is not "-ast", print an error message and exit
                System.out.println("Invalid Argument");
                return;
            }
        }
        else if(args.length == 1){
            // If only one argument provided
            prog = args[0]; // Set prog to the argument
        }
        else{
            // If more than two arguments provided, print an error message and exit
            System.out.println("Invalid Argument");
            return;
        }

        // Evaluate the program
        if(isPrintAST){
            // If isPrintAST is true, only print the Abstract Syntax Tree (AST)
            ProgramEvaluator.evaluate(prog, isPrintAST);
        }
        else{
            // If isPrintAST is false, evaluate the program and print the result
            System.out.println(ProgramEvaluator.evaluate(prog, isPrintAST));
        }
    }
}
