import java.io.IOException;
import java.util.LinkedList;
import java.util.Stack;

public class Interpreter {
	private SyntaxTree intermediate;
	private Stack runtimeStack = new Stack();
	private Number numberHandler = new Number();
	
	// Only handles INTs, not DOUBLEs, and doesn't handle functions
	// Passes Test1.txt and Test2.txt (with b=1r5 corrected)
	public static void main(String[] args) throws IOException {
		Parser parser = new Parser();
		parser.program();
		
	//	System.out.println("\nValid Parse: true");
	//	System.out.println("");
	//	SymbolTableTree.getInstance().printSymbolTables();
	
		LinkedList<SyntaxTreeNode> treeList = parser.syntaxTree.getTraversalList();
		
		System.out.println("");
		System.out.println("Syntax Tree Traversal List: " + treeList);
		
		System.out.println("Interpretation: ");
		Interpreter interpreter = new Interpreter(parser.syntaxTree);
		interpreter.interpret();
	}
	
	public Interpreter(SyntaxTree tree) {
		 intermediate = tree;
	}
	
	public void interpret() {
		LinkedList<SyntaxTreeNode> treeNodes = intermediate.getTraversalList();
		SyntaxTreeNode.Interior statements = (SyntaxTreeNode.Interior) treeNodes.getFirst();
		statement_seq(statements);
	}
	
	public void statement_seq(SyntaxTreeNode.Interior statements) {
		for (int i = 0; i < statements.numChildren(); i++) {
			process(statements.getChild(i));
		}		
	}
	
	public void process (SyntaxTreeNode node) {
		// If Leaf, doesn't need to do anything, value is computed
		if (node instanceof SyntaxTreeNode.Interior) {
			SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) node;
			
			if (node.toString().equals("=")) {
				process(interior.getChild(1));
				Object right = resolve(interior.getChild(1));
				SymbolTableTree.getInstance().updateValue(interior.getChild(0).toString(), right);
			}
			
			else if (node.toString().equals("+")) {
				process(interior.getChild(0));
				process(interior.getChild(1));
				Object left = resolve(interior.getChild(0));
				Object right = resolve(interior.getChild(1));
				node.setValue(numberHandler.operation("+", right, left));
			}

			else if (node.toString().equals("-")) {
				process(interior.getChild(0));
				process(interior.getChild(1));
				Object left = resolve(interior.getChild(0));
				Object right = resolve(interior.getChild(1));
				node.setValue(numberHandler.operation("-", right, left));
			}			
			
			else if (node.toString().equals("*")) {
				process(interior.getChild(0));
				process(interior.getChild(1));
				Object left = resolve(interior.getChild(0));
				Object right = resolve(interior.getChild(1));
				node.setValue(numberHandler.operation("*", right, left));
			}
	
			else if (node.toString().equals("/")) {
				process(interior.getChild(0));
				process(interior.getChild(1));
				Object left = resolve(interior.getChild(0));
				Object right = resolve(interior.getChild(1));
				node.setValue(numberHandler.operation("/", right, left));
			}			

			else if (node.toString().equals("%")) {
				process(interior.getChild(0));
				process(interior.getChild(1));
				Object left = resolve(interior.getChild(0));
				Object right = resolve(interior.getChild(1));
				node.setValue(numberHandler.operation("%", right, left));
			}
			
			else if (node.toString().equals("while")) {
				process(interior.getChild(0));
				while ((boolean)interior.getChild(0).getValue() == true) {
					statement_seq((SyntaxTreeNode.Interior)interior.getChild(1));
					process(interior.getChild(0));
				}
			}
			
			else if (node.toString().equals("if")) {
				process(interior.getChild(0));
				if ((boolean)interior.getChild(0).getValue() == true) {
					statement_seq((SyntaxTreeNode.Interior)interior.getChild(1));
				} else {
					statement_seq((SyntaxTreeNode.Interior)interior.getChild(2));
				}
			}
			
			else if (node.toString().equals("print")) {
				process(interior.getChild(0));
				Object child = resolve(interior.getChild(0));
				System.out.println(child);
			}
			
			else if (node.toString().equals("<")) {
				process(interior.getChild(0));
				process(interior.getChild(1));
				Object left = resolve(interior.getChild(0));
				Object right = resolve(interior.getChild(1));
				if (numberHandler.comparison("<", left, right))
					interior.setValue(true);
				else
					interior.setValue(false);
			}
			
			else if (node.toString().equals(">")) {
				process(interior.getChild(0));
				process(interior.getChild(1));
				Object left = resolve(interior.getChild(0));
				Object right = resolve(interior.getChild(1));
				if (numberHandler.comparison(">", left, right))
					interior.setValue(true);
				else
					interior.setValue(false);
			}
			
			else if (node.toString().equals("<=")) {
				process(interior.getChild(0));
				process(interior.getChild(1));
				Object left = resolve(interior.getChild(0));
				Object right = resolve(interior.getChild(1));
				if (numberHandler.comparison("<=", left, right))
					interior.setValue(true);
				else
					interior.setValue(false);
			}
			
			else if (node.toString().equals(">=")) {
				process(interior.getChild(0));
				process(interior.getChild(1));
				Object left = resolve(interior.getChild(0));
				Object right = resolve(interior.getChild(1));
				if (numberHandler.comparison(">=", left, right))
					interior.setValue(true);
				else
					interior.setValue(false);
			}
			
			else if (node.toString().equals("==")) {
				process(interior.getChild(0));
				process(interior.getChild(1));
				Object left = resolve(interior.getChild(0));
				Object right = resolve(interior.getChild(1));
				if (numberHandler.comparison("==", left, right))
					interior.setValue(true);
				else
					interior.setValue(false);
			}
			
			else if (node.toString().equals("<>")) {
				process(interior.getChild(0));
				process(interior.getChild(1));
				Object left = resolve(interior.getChild(0));
				Object right = resolve(interior.getChild(1));
				if (numberHandler.comparison("<>", left, right))
					interior.setValue(true);
				else
					interior.setValue(false);
			}
			
//			else if (node.toString().equals("return")) {
//				runtimeStack.push(interior.getValue());
//			}
			
//			else { //function
//				SymbolTableEntry entry = SymbolTableTree.getInstance().getEntry(interior.toString());
//				if (entry.getIdType().equals("function")) {
//					SyntaxTreeNode.Interior params = (SyntaxTreeNode.Interior) interior.getChild(0);
//					System.out.println(entry);
//					System.out.println(((SyntaxTree)entry.getValue()).getTraversalList());
//					for (int i = 0; i < params.numChildren(); i++) {
//						runtimeStack.push(params.getChild(i));
//						//SymbolTableTree.getInstance().updateValue(x, val, interior.toString());
//					}
//					
//					//function(entry, params);
//					interior.setValue(runtimeStack.pop());
//				}
//			}
		}
	}
	
	// Get value from node, whether it's Symbol Table value or Constant
	public Object resolve(SyntaxTreeNode node) {
		Object value;
		if (node.getValue() instanceof SymbolTableEntry) {
			String symbol = node.toString();
			value = (Object)SymbolTableTree.getInstance().getEntry(symbol).getValue();
		} else {
			value = (Object)node.getValue();
		}
		return value;
	}

}