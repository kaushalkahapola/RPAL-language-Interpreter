package Parser;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import Lexer.Token;
import Lexer.TokenType;

public class Parser {
	private List<Token> tokens;
	private List<Node> AST;
	private ArrayList<String> stringAST;
	
	public Parser(List<Token> tokens) {
		this.tokens=tokens;
		AST = new ArrayList<>();
		stringAST = new ArrayList<>();
	}
	
	public List<Node> parse(){
		tokens.add(new Token(TokenType.EndOfTokens,""));
		E();
		if(tokens.get(0).type.equals(TokenType.EndOfTokens)) {
			return AST;
		}
		else {
			System.out.println("Parsing Failed");
			System.out.println("Remaining Unparsed Tokens:");
			for (Token token : tokens) {
	            System.out.println("<" + token.type + ", " + token.value + ">");
	        }
			return null;
		}
	}
	
	public ArrayList<String> ASTtoString(){
		
		String dots = "";
		List<Node> stack= new ArrayList<Node>();
		
		while(!AST.isEmpty()) {
			if(stack.isEmpty()) {
				if(AST.get(AST.size()-1).childrenCount==0) {
					addStrings(dots,AST.remove(AST.size()-1));
				}
				else {
					Node node = AST.remove(AST.size()-1);
					stack.add(node);
				}
			}
			else {
				if(AST.get(AST.size()-1).childrenCount>0) {
					Node node = AST.remove(AST.size()-1);
					stack.add(node);
					dots += ".";
				}
				else {
					stack.add(AST.remove(AST.size()-1));
					dots += ".";
					while(stack.get(stack.size()-1).childrenCount==0) {
						addStrings(dots,stack.remove(stack.size()-1));
						if(stack.isEmpty()) break;
						dots = dots.substring(0, dots.length() - 1);
						Node node =stack.remove(stack.size()-1);
						node.childrenCount--;
						stack.add(node);
						
					}
				}
				
			}
		}
		
        Collections.reverse(stringAST);
		return stringAST;
	}
	
	void addStrings(String dots,Node node) {
			switch(node.type) {
				case identifier:
					stringAST.add(dots+"<ID:"+node.value+">");
					break;
				case integer:
					stringAST.add(dots+"<INT:"+node.value+">");
					break;
				case string: 
					stringAST.add(dots+"<STR:"+node.value+">");
					break;	
				case true_value:
					stringAST.add(dots+"<"+node.value+">");
					break;
				case false_value:
					stringAST.add(dots+"<"+node.value+">");
					break;
				case nil:
					stringAST.add(dots+"<"+node.value+">");
					break;
				case dummy:
					stringAST.add(dots+"<"+node.value+">");
					break;
				case fcn_form:
					stringAST.add(dots+"function_form");
					break;
				default :
					stringAST.add(dots+node.value);
			}		
	}
	
	void E() {
	    int n=0;
		Token token=tokens.get(0);
		if(token.type.equals(TokenType.KEYWORD) && Arrays.asList("let", "fn").contains(token.value)) { 
			if(token.value.equals("let")) {
				tokens.remove(0);
				D();
				if(!tokens.get(0).value.equals("in")) {
					System.out.println("Parse error at E: 'in' Expected");
				}
				tokens.remove(0);
				E();
				AST.add(new Node(NodeType.let,"let",2));
				
			}
			else {
				tokens.remove(0);
				do {
					Vb();
					n++;
				} while(tokens.get(0).type.equals(TokenType.IDENTIFIER) || tokens.get(0).value.equals("(")); 
				if(!tokens.get(0).value.equals(".")) {
					System.out.println("Parse error at E: '.' Expected");
				}
				tokens.remove(0);
				E();
				AST.add(new Node(NodeType.lambda,"lambda",n+1));							
			}
		}	
		else
			Ew();	
	}
	
	void Ew() {
		T();
		if(tokens.get(0).value.equals("where")){
			tokens.remove(0);
			Dr();
			AST.add(new Node(NodeType.where,"where",2));
	    }
	}
	
	void T() {
		Ta();
		int n = 1;
		while (tokens.get(0).value.equals(",")){
			tokens.remove(0);
			Ta();
			++n;
	    }
	    if (n > 1) {
			AST.add(new Node(NodeType.tau,"tau",n));		
	    }
	}
	
	void Ta(){
		Tc();
		while(tokens.get(0).value.equals("aug")){
			tokens.remove(0);
			Tc();
			AST.add(new Node(NodeType.aug,"aug",2));		
		} 
	}	
	
	void Tc(){
		B();
		if(tokens.get(0).value.equals("->")){
			tokens.remove(0);
			Tc();
			if(!tokens.get(0).value.equals("|")){
				System.out.println("Parse error at Tc: conditional '|' expected");
			}
			tokens.remove(0);
			Tc();
			AST.add(new Node(NodeType.conditional,"->",3));		
		}
	}
	
	void B(){ 
		Bt();
		while(tokens.get(0).value.equals("or")){
			tokens.remove(0);
			Bt();
			AST.add(new Node(NodeType.op_or,"or",2));		
		} 
	}
	
	void Bt(){
		Bs();
		while(tokens.get(0).value.equals("&")){
			tokens.remove(0);
			Bs();
			AST.add(new Node(NodeType.op_and,"&",2));		
		}
	}
	
	void Bs(){
		if(tokens.get(0).value.equals("not")){
			tokens.remove(0);
			Bp();
			AST.add(new Node(NodeType.op_not,"not",1));		
		}
		else Bp();
	}
	
	void Bp() {
		A();
		Token token = tokens.get(0);
		if(Arrays.asList(">", ">=", "<", "<=").contains(token.value)
				|| Arrays.asList("gr", "ge", "ls", "le", "eq", "ne").contains(token.value)){
			tokens.remove(0);
			A();
			switch(token.value){
				case ">":
					AST.add(new Node(NodeType.op_compare,"gr",2));		
					break;
				case ">=":
					AST.add(new Node(NodeType.op_compare,"ge",2));		
					break;
				case "<":
					AST.add(new Node(NodeType.op_compare,"ls",2));		
					break;
				case "<=":
					AST.add(new Node(NodeType.op_compare,"le",2));		
					break;
				default:
					AST.add(new Node(NodeType.op_compare,token.value,2));	
					break;
			}
		}
	}

	void A(){
		if (tokens.get(0).value.equals("+")) {
			tokens.remove(0);
			At();
	    } else if (tokens.get(0).value.equals("-")) {
			tokens.remove(0);
			At();
			AST.add(new Node(NodeType.op_neg,"neg",1));	
	    } else {
	        At();
	    }
	    while (Arrays.asList("+", "-").contains(tokens.get(0).value)) {
	    	Token currentToken = tokens.get(0);
			tokens.remove(0);
			At();
			if(currentToken.value.equals("+")) AST.add(new Node(NodeType.op_plus,"+",2));
			else AST.add(new Node(NodeType.op_minus,"-",2));
	    }
		
	}
	
	void At(){
		Af();
		while(Arrays.asList("*", "/").contains(tokens.get(0).value)){
			Token currentToken = tokens.get(0);
			tokens.remove(0);
			Af();
			if(currentToken.value.equals("*")) AST.add(new Node(NodeType.op_mul,"*",2));
			else AST.add(new Node(NodeType.op_div,"/",2));
		}		
	}
	
	void Af(){
		Ap();
		if(tokens.get(0).value.equals("**")){
			tokens.remove(0);
			Af();
			AST.add(new Node(NodeType.op_pow,"**",2));	
		}
	}
	
	void Ap(){
		R();
		while(tokens.get(0).value.equals("@")){
			tokens.remove(0);
			if(!tokens.get(0).type.equals(TokenType.IDENTIFIER)){
				System.out.println("Parsing error at Ap: IDENTIFIER EXPECTED");
			}
			AST.add(new Node(NodeType.identifier,tokens.get(0).value,0));	
			tokens.remove(0);
			R();
			AST.add(new Node(NodeType.at,"@",3));	
		}
	}
	
	void R(){
		Rn();
		while((Arrays.asList(TokenType.IDENTIFIER, TokenType.INTEGER, TokenType.STRING).contains(tokens.get(0).type))
				||(Arrays.asList("true", "false", "nil", "dummy").contains(tokens.get(0).value))
				||(tokens.get(0).value.equals("("))) {
			
			Rn();
			AST.add(new Node(NodeType.gamma,"gamma",2));
		}
	}
	
	void Rn(){
		switch(tokens.get(0).type){
			case IDENTIFIER:
				AST.add(new Node(NodeType.identifier,tokens.get(0).value,0));	
				tokens.remove(0);
				break;
			case INTEGER:
				AST.add(new Node(NodeType.integer,tokens.get(0).value,0));	
				tokens.remove(0);
				break;
			case STRING:
				AST.add(new Node(NodeType.string,tokens.get(0).value,0));	
				tokens.remove(0);
				break;
			case KEYWORD:
				switch(tokens.get(0).value){
					case "true":
						AST.add(new Node(NodeType.true_value,tokens.get(0).value,0));
						tokens.remove(0);
						break;
					case "false":
						AST.add(new Node(NodeType.false_value,tokens.get(0).value,0));	
						tokens.remove(0);
						break;
					case "nil":
						AST.add(new Node(NodeType.nil,tokens.get(0).value,0));
						tokens.remove(0);
						break;
					case "dummy":
						AST.add(new Node(NodeType.dummy,tokens.get(0).value,0));
						tokens.remove(0);
						break;
					default:
						System.out.println("Parse Error at Rn: Unexpected KEYWORD");
						break;
				}
				break;
			case PUNCTUATION:
				if(tokens.get(0).value.equals("(")) {
					tokens.remove(0);
					E();
					if(!tokens.get(0).value.equals(")")) {
						System.out.println("Parsing error at Rn: Expected a matching ')'");
					}
					tokens.remove(0);
				}
				else System.out.println("Parsing error at Rn: Unexpected PUNCTUATION");
				break;
			default:
				System.out.println("Parsing error at Rn: Expected a Rn, but got different");
				break;
		}			
	}
	
	void D(){
		Da();
		if(tokens.get(0).value.equals("within")){
			tokens.remove(0);
			D();
			AST.add(new Node(NodeType.within,"within",2));	
		}
	}
	
	void Da(){
		Dr();
		int n = 1;
		while(tokens.get(0).value.equals("and")){
			tokens.remove(0);
			Dr();
			n++;
		}
		if(n>1) AST.add(new Node(NodeType.and,"and",n));	
	}
	
	void Dr(){
		boolean isRec = false;
		if(tokens.get(0).value.equals("rec")){
			tokens.remove(0);
	        isRec = true;
	    }
	    Db();
	    if (isRec) {
			AST.add(new Node(NodeType.rec,"rec",1));	
	    }
	}
	
	void Db() {
		if( tokens.get(0).type.equals(TokenType.PUNCTUATION) && tokens.get(0).value.equals("(")){
			tokens.remove(0);
			D();
			if(!tokens.get(0).value.equals(")")) {
				System.out.println("Parsing error at Db #1");
			}
			tokens.remove(0);
		}
		else if(tokens.get(0).type.equals(TokenType.IDENTIFIER)){
			if(tokens.get(1).value.equals("(") || tokens.get(1).type.equals(TokenType.IDENTIFIER)) {
				AST.add(new Node(NodeType.identifier,tokens.get(0).value,0));
				tokens.remove(0);
				int n = 1;
				do {
					Vb();
					n++;
				} while(tokens.get(0).type.equals(TokenType.IDENTIFIER) || tokens.get(0).value.equals("("));
				if(!tokens.get(0).value.equals("=")) {
					System.out.println("Parsing error at Db #2");
				}
				tokens.remove(0);
				E();
				AST.add(new Node(NodeType.fcn_form,"fcn_form",n+1));		
				
			}
			else if (tokens.get(1).value.equals("=")) {
				AST.add(new Node(NodeType.identifier,tokens.get(0).value,0));
				tokens.remove(0);
				tokens.remove(0);
				E();
				AST.add(new Node(NodeType.equal,"=",2));		
			}
			else if (tokens.get(1).value.equals(",")){
				Vl();
				if(!tokens.get(0).value.equals("=")) {
					System.out.println("Parsing error at Db");
				}
				tokens.remove(0);
				E();
				
				AST.add(new Node(NodeType.equal,"=",2));		
			}
		}
	}

		
	void Vb(){
		if(tokens.get(0).type.equals(TokenType.PUNCTUATION) && tokens.get(0).value.equals("(")) {
			tokens.remove(0);
			boolean isVl=false;
			
			if(tokens.get(0).type .equals(TokenType.IDENTIFIER) ){
				Vl();
				isVl = true;
			}
			if(!tokens.get(0).value.equals(")")){
				System.out.println("Parse error unmatch )");
			}
			tokens.remove(0);
			if(!isVl) AST.add(new Node(NodeType.empty_params,"()",0));	
			
		} else if(tokens.get(0).type .equals(TokenType.IDENTIFIER) ){
			AST.add(new Node(NodeType.identifier,tokens.get(0).value,0));
			tokens.remove(0);
	    }
		
	}
	
	void Vl() {
		int n = 0;
		do {
			if(n>0) {
				tokens.remove(0);
			}
			if(!tokens.get(0).type.equals(TokenType.IDENTIFIER)) {
				System.out.println("Parse error: a ID was expected )");
			}
			AST.add(new Node(NodeType.identifier,tokens.get(0).value,0));
			tokens.remove(0);
			n++;
		} while (tokens.get(0).value.equals(","));
		AST.add(new Node(NodeType.comma,",",n));	
	}

}

