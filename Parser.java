import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.LinkedList;
import java.util.ArrayList;

public class Parser {
	private Lexer lex = new Lexer();
	public SyntaxTree syntaxTree = new SyntaxTree();
	private Token lookahead = null;
	private Token token = null;
	private static Hashtable<String, List<String>> FIRST = new Hashtable<String, List<String>>();
	private static Hashtable<String, List<String>> FOLLOW = new Hashtable<String, List<String>>();

	private String currentName, currentFuncName, currentType, currentValue;
	private SyntaxTree currentFuncBody = null;
	
	private void initializeFIRST() {
        FIRST.put("program", Arrays.asList("def", "int", "double", "if", "while", "print", "return", "ID"));
        FIRST.put("fdecls", Arrays.asList("def", "EPSILON"));
        FIRST.put("fdec", Arrays.asList("def"));
        FIRST.put("fdec_r", Arrays.asList("def", "EPSILON"));
        FIRST.put("params", Arrays.asList("int", "double", "EPSILON"));
		FIRST.put("params_r", Arrays.asList(",", "EPSILON"));
		FIRST.put("fname", Arrays.asList("ID"));
		FIRST.put("declarations", Arrays.asList("int", "double", "EPSILON"));
        FIRST.put("decl", Arrays.asList("int", "double"));
        FIRST.put("decl_r", Arrays.asList("int", "double", "EPSILON"));
        FIRST.put("type", Arrays.asList("int", "double"));
		FIRST.put("varlist", Arrays.asList("ID"));
		FIRST.put("varlist_r", Arrays.asList(",", "EPSILON"));
		FIRST.put("statement_seq", Arrays.asList("if", "while", "print", "return", "ID", "EPSILON"));
        FIRST.put("statement", Arrays.asList("if","while","print","return","ID","EPSILON"));
        FIRST.put("statement_seq_r", Arrays.asList(";", "EPSILON"));
		FIRST.put("opt_else", Arrays.asList("else", "EPSILON"));
		FIRST.put("expr", Arrays.asList("ID", "NUMBER", "("));
		FIRST.put("term", Arrays.asList("ID", "NUMBER", "("));
		FIRST.put("term_r", Arrays.asList("+", "-", "EPSILON"));
		FIRST.put("var_r", Arrays.asList("[","EPSILON"));
        FIRST.put("var", Arrays.asList("ID"));
        FIRST.put("comp", Arrays.asList("<", ">", "==", "<=", ">=", "<>"));
        FIRST.put("bfactor_r_p", Arrays.asList("(", "not", "ID", "NUMBER", "EPSILON"));
        FIRST.put("bfactor", Arrays.asList("(", "not"));
        FIRST.put("bfactor_r", Arrays.asList("and", "EPSILON"));
        FIRST.put("bterm", Arrays.asList("(", "not"));
        FIRST.put("bterm_r", Arrays.asList("or", "EPSILON"));
        FIRST.put("bexpr", Arrays.asList("(", "not"));
        FIRST.put("exprseq_r", Arrays.asList(",", "EPSILON"));
        FIRST.put("exprseq", Arrays.asList("(", "ID", "NUMBER"));
        FIRST.put("factor", Arrays.asList("(", "ID", "NUMBER"));
        FIRST.put("factor_r", Arrays.asList("*", "/", "%", "EPSILON"));
        FIRST.put("factor_r_p", Arrays.asList("(","EPSILON"));
		
		// Missing grt_opt, less_opt, id, id_r, integer, integer_r, double, double_r, decimal, decimal_r, exponent, letter, digit
		// Might only be relevant for lexer. Will add in later if actually needed.
	}
	
	private void initializeFOLLOW() {
        FOLLOW.put("program", Arrays.asList("$"));
        FOLLOW.put("fdecls", Arrays.asList("int", "double", "if", "while", "print", "return", "ID"));
        FOLLOW.put("fdec", Arrays.asList(";"));
        FOLLOW.put("fdec_r", Arrays.asList(";"));
        FOLLOW.put("params", Arrays.asList(")"));
		FOLLOW.put("params_r", Arrays.asList(")"));
		FOLLOW.put("fname", Arrays.asList("("));
		FOLLOW.put("declarations", Arrays.asList("if","while","print","return","ID"));
        FOLLOW.put("decl", Arrays.asList(";"));
        FOLLOW.put("decl_r", Arrays.asList(";"));
        FOLLOW.put("type", Arrays.asList("ID"));
		FOLLOW.put("varlist", Arrays.asList(";",",",".","(",")","]","[","then","+","-","*","/","%","==","<>","<",">"));
		FOLLOW.put("varlist_r", Arrays.asList(";",",",".","(",")","]","[","then","+","-","*","/","%","==","<>","<",">"));
		FOLLOW.put("statement_seq", Arrays.asList(".","fed","fi","od","else"));
        FOLLOW.put("statement", Arrays.asList(".",";","fed","fi","od","else"));
        FOLLOW.put("statement_seq_r", Arrays.asList(".",";","fed","fi","od","else"));
		FOLLOW.put("opt_else", Arrays.asList("fi"));
		FOLLOW.put("expr", Arrays.asList(".",";","fed","fi","od","else",")","=",">","<","]"));
		FOLLOW.put("term", Arrays.asList(".",";","fed","fi","od","else",")","=",">","<","]","+","-","*","/"));
		FOLLOW.put("term_r", Arrays.asList(".",";","fed","fi","od","else",")","=",">","<","]","+","-","*","/"));
		FOLLOW.put("var_r", Arrays.asList(";",",",".","(",")","]","[","then","+","-","*","/","%","==","<>","<",">"));
        FOLLOW.put("var", Arrays.asList(";",",",".","(",")","]","[","then","+","-","*","/","%","==","<>","<",">"));
        FOLLOW.put("comp", Arrays.asList(""));
        FOLLOW.put("bfactor_r_p", Arrays.asList("then","do",")","or","and"));
        FOLLOW.put("bfactor", Arrays.asList("then","do",")","or","and"));
        FOLLOW.put("bfactor_r", Arrays.asList("then","do",")","or","and"));
        FOLLOW.put("bterm", Arrays.asList("then","do",")","or","and"));
        FOLLOW.put("bterm_r", Arrays.asList("then","do",")","or","and"));
        FOLLOW.put("bexpr", Arrays.asList("then","do",")","or"));
        FOLLOW.put("exprseq_r", Arrays.asList(")"));
        FOLLOW.put("exprseq", Arrays.asList(")"));
        FOLLOW.put("factor", Arrays.asList(".",";","fed","fi","od","else",")","=",">","<","]","+","-","*","/"));
        FOLLOW.put("factor_r", Arrays.asList(".",";","fed","fi","od","else",")","=",">","<","]","+","-","*","/"));
        FOLLOW.put("factor_r_p", Arrays.asList(".",";","fed","fi","od","else",")","=",">","<","]","+","-","*","/"));
	}
	
	public Parser() throws IOException {
		initializeFIRST();
		initializeFOLLOW();
		consumeToken(); consumeToken(); // Twice to initialize token & lookahead
	}
	
	// RECURSIVE FUNCTIONS
	
	public SyntaxTreeNode program() {
		String first = checkFIRST("program");
		if(first != null) {
			SyntaxTreeNode.Interior statements = syntaxTree.makeInterior("statement_seq");
			fdecls(); declarations(); statement_seq(statements); match('.');
			return statements;
		} else
			error();
			return null;
	}
	
	public void fdecls() {
		String first = checkFIRST("fdecls");
		if(first != null) {
			fdec(); match(';'); fdec_r();
		}
	}
	
	public void fdec() {
		String first = checkFIRST("fdec");
		if(first != null) {
			currentFuncBody = new SyntaxTree();
			SyntaxTreeNode.Interior currentFuncRoot = currentFuncBody.makeInterior("statement_seq");
			
			match("def"); type(); fname(); match("("); params(); match(")"); declarations(); statement_seq(currentFuncRoot); match("fed");
		}
	}
	
	public void fdec_r() {
		String first = checkFIRST("fdec_r");
		if(first != null) {
			fdec(); match(";"); fdec_r();
		}
	}

	public void params() {
		String first = checkFIRST("params");
		if (first != null) {
			type(); var(); params_r();
		}
	}
	
	public void params_r() {
		String first = checkFIRST("params_r");
		if (first != null) {
			match(","); params();
		}
	}
	
	public void fname() {
		String first = checkFIRST("fname");
		if (first != null) {
			currentName = lookahead.getRepresentation();
			currentFuncName = currentName;
			SymbolTableTree.getInstance().addEntry(new SymbolTableEntry(currentName, SymbolTableEntry.FUNCTION, currentType, null));
			match(TokenType.ID);
		}
		else
			error();
	}
	
	public void declarations() {
		String first = checkFIRST("declarations");
		if(first != null) {
			decl(); match(';'); decl_r();
		}
	}
	
	public void decl() {
		String first = checkFIRST("decl");
		if(first != null) {
			type(); varlist();
		} else
			error();
	}
	
	public void decl_r() {
		String first = checkFIRST("decl_r");
		if(first != null) {
			decl(); match(";"); decl_r();
		}
	}
	
	public void type() {
		String first = checkFIRST("type");
		
		switch(first) {
			case "int":
				currentType = SymbolTableEntry.INT;
				match("int");
				return;
			case "double":
				currentType = SymbolTableEntry.DOUBLE;
				match("double");
				return;
			default:
				error();
		}
	}
	
	public void statement_seq(SyntaxTreeNode.Interior seqList) {
		String first = checkFIRST("statement_seq");
		SyntaxTreeNode statementNode;
		
		if(first != null) {
			seqList.addChild(statement()); statement_seq_r(seqList);
		}
	}
	
	public void statement_seq_r(SyntaxTreeNode.Interior seqList) {
		String first = checkFIRST("statement_seq_r");
		if(first != null) {
			match(";"); statement_seq(seqList);
		}
	}
	
	public SyntaxTreeNode statement() {
		String first = checkFIRST("statement");
		SyntaxTreeNode varNode, exprNode, bexprNode, elseNode;
		SyntaxTreeNode.Interior statements;
		
		if (currentFuncBody == null)
			statements = syntaxTree.makeInterior("statement_seq");
		else
			statements = currentFuncBody.makeInterior("statement_seq");
		
		switch(first) {
			case "ID":
				varNode = var(); match("="); exprNode = expr();
				
				if (currentFuncBody == null)
					return syntaxTree.makeInterior("=", varNode, exprNode);
				else
					return currentFuncBody.makeInterior("=", varNode, exprNode);
			case "if":
				match("if"); bexprNode = bexpr(); match("then"); statement_seq(statements); elseNode = opt_else(); match("fi");
				
				if (elseNode != null)
				{
					if (currentFuncBody == null)
						return syntaxTree.makeInterior("if", bexprNode, statements, elseNode);
					else
						return currentFuncBody.makeInterior("if", bexprNode, statements, elseNode);
				}
				else
				{
					if (currentFuncBody == null)
						return syntaxTree.makeInterior("if", bexprNode, statements);
					else
						return currentFuncBody.makeInterior("if", bexprNode, statements);
				}
			case "while":
				match("while"); bexprNode = bexpr(); match("do"); statement_seq(statements); match("od");
				
				if (currentFuncBody == null)
					return syntaxTree.makeInterior("while", bexprNode, statements);
				else
					return currentFuncBody.makeInterior("while", bexprNode, statements);
			case "print":
				match("print"); exprNode = expr();
				
				if (currentFuncBody == null)
					return syntaxTree.makeInterior("print", exprNode);
				else
					return currentFuncBody.makeInterior("print", exprNode);
			case "return":
				match("return"); exprNode = expr();
				
				if (currentFuncBody == null)
					return syntaxTree.makeInterior("return", exprNode);
				else
					return currentFuncBody.makeInterior("return", exprNode);
			default: //Epsilon
				return null;
		}
	}

	public void varlist() {
		String first = checkFIRST("varlist");
		
		if (first != null) {
			var(); varlist_r();
		} else {
			error();
		}
	}
	
	public void varlist_r() {
		String first = checkFIRST("varlist_r");
		if (first != null) {
			match(","); varlist();
		}
	}
	
	public SyntaxTreeNode opt_else() {
		String first = checkFIRST("opt_else");
	
		if (first != null) {
			SyntaxTreeNode.Interior statements;
			
			if (currentFuncBody == null)
				statements = syntaxTree.makeInterior("statement_seq");
			else
				statements = currentFuncBody.makeInterior("statement_seq");
			
			match("else"); statement_seq(statements);
			return statements;
		} else {
			return null;
		}
	}
	
	public SyntaxTreeNode expr()
	{
		String first = checkFIRST("expr");
		SyntaxTreeNode termNode, term_rNode;
		
		if (first != null) {
			termNode = term(); term_rNode = term_r();
			
			if (termNode != null)
			{
				if (term_rNode instanceof SyntaxTreeNode.Interior)
				{
					((SyntaxTreeNode.Interior)term_rNode).addChild(termNode);
					return term_rNode;
				}
				else
				{
					return termNode;
				}
			}
			else
			{
				return null;
			}
		} else {
			error();
			return null;
		}
	}
	
	public SyntaxTreeNode term_r() {
		String first = checkFIRST("term_r");
		SyntaxTreeNode termNode, term_rNode;
		
		if (first != null) {
			if (first.equals("+")) {
				match("+"); termNode = term(); term_rNode = term_r();
				
				if (currentFuncBody == null){
					return syntaxTree.makeInterior("+", termNode, term_rNode);
				}
				else
					return currentFuncBody.makeInterior("+", termNode, term_rNode);
			} else if (first.equals("-")) {
				match("-"); termNode = term(); term_rNode = term_r();
				
				if (currentFuncBody == null)
					return syntaxTree.makeInterior("-", termNode, term_rNode);
				else
					return currentFuncBody.makeInterior("-", termNode, term_rNode);
			} else {
				error();
				return null;
			}
		} else {
			return null;
		}
	}
	
	public SyntaxTreeNode term() {
		String first = checkFIRST("term");
		SyntaxTreeNode factorNode, factor_rNode;
		
		if (first != null) {
			factorNode = factor(); factor_rNode = factor_r();
			
			if (factorNode != null)
			{
				if (factorNode instanceof SyntaxTreeNode.Interior)
				{
					((SyntaxTreeNode.Interior)factorNode).addChild(factor_rNode);
					return factorNode;
				}
				else if (factorNode instanceof SyntaxTreeNode.Leaf && factor_rNode != null)
				{
					((SyntaxTreeNode.Interior)factor_rNode).addChild(factorNode);
					return factor_rNode;
				}
				else if (factorNode == null && factor_rNode != null)
				{
					return factor_rNode;
				}
				else
				{
					return factorNode;
				}
			}
			else
			{
				return null;
			}
		} else {
			error();
			return null;
		}
	}
	
	public SyntaxTreeNode factor_r() {
		String first = checkFIRST("factor_r");
		SyntaxTreeNode factorNode, factor_rNode;
		
		if (first != null) {
			switch(first) {
				case "*":
					match("*"); factorNode = factor(); factor_rNode = factor_r();

					if (currentFuncBody == null)
					{
						return syntaxTree.makeInterior("*", factorNode, factor_rNode);
					}
					else
						return currentFuncBody.makeInterior("*", factorNode, factor_rNode);
				case "/":
					match("/"); factorNode = factor(); factor_rNode = factor_r();

					if (currentFuncBody == null)
						return syntaxTree.makeInterior("/", factorNode, factor_rNode);
					else
						return currentFuncBody.makeInterior("/", factorNode, factor_rNode);
				case "%":
					match("%"); factorNode = factor(); factor_rNode = factor_r();

					if (currentFuncBody == null)
						return syntaxTree.makeInterior("%", factorNode, factor_rNode);
					else
						return currentFuncBody.makeInterior("%", factorNode, factor_rNode);
				default:
					error();
					return null;
			}
		} else {
			return null;
		}
	}
	
	// Careful
	 public SyntaxTreeNode factor() {
		String first = checkFIRST("factor");
		SyntaxTreeNode exprNode, idNode, funcParams;
		
		if (first != null) {
			if (first.equals("ID")) { // Either a function call or a variable usage
				idNode = match(TokenType.ID); funcParams = factor_r_p();
				
				if (funcParams != null) {
					if (currentFuncBody == null)
						return syntaxTree.makeInterior(idNode.toString(), funcParams);
					else
						return currentFuncBody.makeInterior(idNode.toString(), funcParams);
				} else {
					return idNode;
				}
			} else if (first.equals("NUMBER")) {
				return match(TokenType.INT); //Technically INT too
			} else if (first.equals("(")) {
				match("("); exprNode = expr(); match(")");
				return exprNode;
			} else if (first.equals("ID")) {
				return var();
			} else {
				error();
				return null;
			}
		} else {
			error();
			return null;
		}
	}
 
	public SyntaxTreeNode factor_r_p() {
		String first = checkFIRST("factor_r_p");
		if (first != null) {
			if (first.equals("(")) {
				SyntaxTreeNode.Interior params;
				
				if (currentFuncBody == null)
					params = syntaxTree.makeInterior("params");
				else
					params = currentFuncBody.makeInterior("params");
				
				match("("); exprseq(params); match(")");
				return params;
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public void exprseq(SyntaxTreeNode.Interior params) {
		String first = checkFIRST("exprseq");
		SyntaxTreeNode exprNode;
		if (first != null) {
			exprNode = expr(); 
			params.addChild(exprNode);
			exprseq_r(params);
		}
	}
	
	public void exprseq_r(SyntaxTreeNode.Interior params) {
		String first = checkFIRST("exprseq_r");
		if (first != null) {
			match(","); exprseq(params);
		}
	}
	
	public SyntaxTreeNode bexpr() {
		String first = checkFIRST("bexpr");
		SyntaxTreeNode btermNode, bterm_rNode;
		
		if (first != null) {
			btermNode = bterm(); bterm_rNode = bterm_r();
			((SyntaxTreeNode.Interior)btermNode).addChild(bterm_rNode);
			return btermNode;
		} else {
			return null;
		}
	}
	
	public SyntaxTreeNode bterm_r() {
		String first = checkFIRST("bterm_r");
		SyntaxTreeNode btermNode, bterm_rNode;
		
		if (first != null) {
			match("or"); btermNode = bterm(); bterm_rNode = bterm_r();
			
			if (currentFuncBody == null)
				return syntaxTree.makeInterior("or", btermNode, bterm_rNode);
			else
				return currentFuncBody.makeInterior("or", btermNode, bterm_rNode);
		}
		else{
			return null;
		}
	}
	
	public SyntaxTreeNode bterm() {
		String first = checkFIRST("bterm");
		SyntaxTreeNode bfactorNode, bfactor_rNode;
		
		if (first != null) {
			bfactorNode = bfactor(); bfactor_rNode = bfactor_r();
			((SyntaxTreeNode.Interior)bfactorNode).addChild(bfactor_rNode);
			return bfactorNode;
		} else
			error();
			return null;
	}
	
	public SyntaxTreeNode bfactor_r() {
		String first = checkFIRST("bfactor_r");
		SyntaxTreeNode bfactorNode, bfactor_rNode;
		
		if (first != null) {
			match("and"); bfactorNode = bfactor(); bfactor_rNode = bfactor_r();
			
			if (currentFuncBody == null)
				return syntaxTree.makeInterior("and", bfactorNode, bfactor_rNode);
			else
				return currentFuncBody.makeInterior("and", bfactorNode, bfactor_rNode);
		} else {
			return null;
		}
	}
	
	public SyntaxTreeNode bfactor() {
		String first = checkFIRST("bfactor");
		SyntaxTreeNode bfactor_r_p_node, bfactorNode;
		
		switch (first) {
			case "(":
				match("("); bfactor_r_p_node = bfactor_r_p(); match(")");
				return bfactor_r_p_node;
			case "not":
				match("not"); bfactorNode = bfactor();
				
				if (currentFuncBody == null)
					return syntaxTree.makeInterior("not", bfactorNode);
				else
					return currentFuncBody.makeInterior("not", bfactorNode);
			default:
				error();
				return null;
		}
	}
	
	// Careful
	public SyntaxTreeNode bfactor_r_p() {
		String first = checkFIRST("bfactor_r_p");
		SyntaxTreeNode e1Node, e2Node, compNode;
		
		if (FIRST.get("bfactor_r_p").contains(first) && token.getType() == TokenType.COMP) {
			e1Node = expr(); compNode = comp(); e2Node = expr();
			((SyntaxTreeNode.Interior)compNode).addChild(e1Node);
			((SyntaxTreeNode.Interior)compNode).addChild(e2Node);
			return compNode;
		} else if (FIRST.get("bfactor_r_p").contains(first)) {
			return bexpr();
		} else {
			error();
			return null;
		}
	}
	
	public SyntaxTreeNode comp() {
		String first = checkFIRST("comp");
		if (first != null) {
			return match(TokenType.COMP);
		} else {
			error();
			return null;
		}
	}
	
	public SyntaxTreeNode var() {
		String first = checkFIRST("var");
		SyntaxTreeNode nodeToReturn = null;
		
		if (first != null)
		{
			currentName = lookahead.getRepresentation();
			
			if (currentFuncName != null)
				SymbolTableTree.getInstance().addEntry(new SymbolTableEntry(currentName, SymbolTableEntry.VARIABLE, currentType, null), currentFuncName);
			else
				SymbolTableTree.getInstance().addEntry(new SymbolTableEntry(currentName, SymbolTableEntry.VARIABLE, currentType, null));
			
			nodeToReturn = match(TokenType.ID); var_r();
			return nodeToReturn;
		}
		else {
			error();
			return nodeToReturn;
		}
	}
	
	public void var_r() {
		String first = checkFIRST("var_r");
		if (first != null) {
			match("["); expr(); match("]");
		}
	}
	
	// UTILITY FUNCTIONS
	
	public void consumeToken() {
		lookahead = token;
		try {
			if (token == null || (token != null && token.getType() != TokenType.END)) {
				lookahead = token;
				token = lex.getNextToken();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public String checkFIRST(String nonterminal) {
		List<String> first = FIRST.get(nonterminal);
		if (first != null) {
			if (lookahead.getType() == TokenType.ID && first.contains("ID")) {
				return "ID";
			} else if ((lookahead.getType() == TokenType.INT || lookahead.getType() == TokenType.DOUBLE) && first.contains("NUMBER")) {
				return "NUMBER";
			} else if (first.contains(lookahead.getRepresentation())) {
				return lookahead.getRepresentation();
			} else {
				return null;
			}
		} else {
			return null;
		}
	}
	
	public void match() {
		consumeToken();
	}
	
	public void match(char c) {
		boolean isMatch = lookahead.getRepresentation().equals(String.valueOf(c));
		if (isMatch) consumeToken();
		else error();
	}
	
	public void match(String s) {
		boolean isMatch = lookahead.getRepresentation().equals(s);
		if (isMatch) 
		{
			if (s.equals("fed"))
			{
				SymbolTableTree.getInstance().updateValue(currentFuncName, currentFuncBody);
				currentFuncName = null;
				currentFuncBody = null;
			}
			consumeToken();
		}
		else error();
	}
	
	public SyntaxTreeNode match(TokenType type) {
		boolean isMatch = false;
		SyntaxTreeNode node = null;

		if (type == TokenType.INT) { 
			isMatch = true;
			
			if (lookahead.getType() == TokenType.INT) {
				if (currentFuncBody == null)
					node = syntaxTree.makeLeaf(lookahead.getRepresentation(), Integer.parseInt(lookahead.getRepresentation()));
				else
					node = currentFuncBody.makeLeaf(lookahead.getRepresentation(), Integer.parseInt(lookahead.getRepresentation()));
			} else if (lookahead.getType() == TokenType.DOUBLE) {
				if (currentFuncBody == null)
					node = syntaxTree.makeLeaf(lookahead.getRepresentation(), Double.parseDouble(lookahead.getRepresentation()));
				else
					node = currentFuncBody.makeLeaf(lookahead.getRepresentation(), Double.parseDouble(lookahead.getRepresentation()));
			}
		} else if (type == TokenType.ID){
			isMatch = true;
			
			if (currentFuncBody == null)

				node = syntaxTree.makeLeaf(lookahead.getRepresentation(), SymbolTableTree.getInstance().getEntry(lookahead.getRepresentation()));
			else
				node = currentFuncBody.makeLeaf(lookahead.getRepresentation(), SymbolTableTree.getInstance().getEntry(lookahead.getRepresentation()));
		} else if (type == TokenType.COMP){
			isMatch = true;
			
			if (currentFuncBody == null)
				node = syntaxTree.makeInterior(lookahead.getRepresentation());
			else
				node = currentFuncBody.makeInterior(lookahead.getRepresentation());
		} else {
			isMatch = type == lookahead.getType();
		}

		if (isMatch)
		{
			consumeToken();
		}
		else error();

		return node;
	}
	
	public void error() {
		System.out.println("\nValid Parse: false");
		System.out.println("Error on Line " + lex.getLineNum() + " at token " + lookahead.getRepresentation());
		StackTraceElement[] stackTraceElements = Thread.currentThread().getStackTrace();
		System.out.println(Arrays.toString(stackTraceElements));
		System.exit(0);
	}
}