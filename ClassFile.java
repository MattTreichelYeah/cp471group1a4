import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;
import java.util.*;

public class ClassFile implements Constants {
  private InstructionFactory _factory;
  private ConstantPoolGen    _cp;
  private ClassGen           _cg;
  private boolean ifSymbolFlag = false;
  private boolean go_toFlag = false;
  private boolean bodyFlag = false;
  private boolean ifFlag = false;
  private boolean ifFlag2 = false;
  private boolean elseFlag = false;
  private InstructionHandle go_toTarget = null;
  private InstructionHandle bodyTarget = null;
  private InstructionHandle printTarget = null;
  private int registers = 1;
  private List<BranchInstruction> BranchInstructions = new ArrayList<BranchInstruction>();
  private Hashtable symbolTable = new Hashtable();

  public ClassFile() {
    _cg = new ClassGen("test", "java.lang.Object", "test.java", ACC_PUBLIC | ACC_SUPER, new String[] {  });

    _cp = _cg.getConstantPool();
    _factory = new InstructionFactory(_cg, _cp);
  }

  public void create(OutputStream out) throws IOException {
    createMethod_0();
    createMethod_1();
    _cg.getJavaClass().dump(out);
  }

  private void createMethod_0() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<init>", "test", il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke("java.lang.Object", "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_1() throws IOException {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC | ACC_STATIC, Type.VOID, new Type[] { new ArrayType(Type.STRING, 1) }, new String[] { "arg0" }, "main", "test", il, _cp);

	// Get Tree
	Parser parser = new Parser();
	parser.program();
	LinkedList<SyntaxTreeNode> treeList = parser.syntaxTree.getTraversalList();
	System.out.println("Syntax Tree Traversal List: " + treeList);
	
	// Process Tree & Make Class
	SyntaxTreeNode.Interior statements = (SyntaxTreeNode.Interior) treeList.getFirst();
	
	for (int i = 0; i < statements.numChildren(); i++) {
		process(statements.getChild(i), il);
	}

    // Cleanup
    il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    
    System.out.println(il);
    
    il.dispose();
  }
  
  private void process(SyntaxTreeNode statement, InstructionList il) {

	//This helps catch branching backtracking for 'if' structures 
	//(it has to loop around a 2nd time to process the next instruction to fill in the goto)
	if (ifFlag) {
		ifFlag2 = true;
	}
	  
	if (statement.toString().equals("=")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		
		// Process Right Side
		process(interior.getChild(1), il);
		
		// Process Left Side
		if (!symbolTable.containsKey(interior.getChild(0).toString())) {
			symbolTable.put(interior.getChild(0).toString(), registers); // Note: ad-hoc symbolTable used to just map {varName:register#}
			il.append(_factory.createStore(Type.INT, registers));
			registers += 1;
		} else {
			int var = (int)symbolTable.get(interior.getChild(0).toString());
			il.append(_factory.createStore(Type.INT, var));
		}
	}
	
	else if (statement.toString().equals("while")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		SyntaxTreeNode.Interior body = (SyntaxTreeNode.Interior) interior.getChild(1);
		
		// Setup goto to be backtracked later
	    BranchInstruction go_to = _factory.createBranchInstruction(Constants.GOTO, null);
	    il.append(go_to);
	    
	    bodyFlag = true; // For grabbing backtrack target used in conditional
		for (int i = 0; i < body.numChildren(); i++) {
			process(body.getChild(i), il); // Body
		}	
		go_toFlag = true; // For grabbing backtrack target
	    process(interior.getChild(0), il); // Condition
	    
	    // Set backtrack target, the other is set within the conditional
	    go_to.setTarget(go_toTarget);
	}
	
	else if (statement.toString().equals("if")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		SyntaxTreeNode.Interior body1 = (SyntaxTreeNode.Interior) interior.getChild(1);
		SyntaxTreeNode.Interior body2 = null;
		// Only process this part if 'else' part exists
		if (interior.numChildren() == 3) {
			body2 = (SyntaxTreeNode.Interior) interior.getChild(2);
		}
	    
		ifSymbolFlag = true; // Symbols put into byte-code are opposite for 'if' branching
	    process(interior.getChild(0), il); // Condition
	    ifSymbolFlag = false;
	    
		for (int i = 0; i < body1.numChildren(); i++) {
			process(body1.getChild(i), il); // Body
		}
		
		// Setup goto to be backtracked later
		BranchInstructions.add(_factory.createBranchInstruction(Constants.GOTO, null));
	    il.append(BranchInstructions.get(BranchInstructions.size() - 1));
	    
	    // Process 'else' if necessary
		if (body2 != null) {
			elseFlag = true; // For grabbing backtrack target used in else
			for (int i = 0; i < body2.numChildren(); i++) {
				process(body2.getChild(i), il); // Body Else
			}	
		}

	    ifFlag = true; //Since this flag comes last without recursing deeper, ifFlag2 is needed on the next loop around to catch the next Instruction
	}
	
	else if (statement.toString().equals("<")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
		if (!ifSymbolFlag) BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPLT, bodyTarget));
		else BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPGE, bodyTarget));
		il.append(BranchInstructions.get(BranchInstructions.size() - 1));
	}
	
	else if (statement.toString().equals(">")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
		if (!ifSymbolFlag) BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPGT, bodyTarget));
		else BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPLE, bodyTarget));
		il.append(BranchInstructions.get(BranchInstructions.size() - 1));
	}
	
	else if (statement.toString().equals("<=")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
		if (!ifSymbolFlag) BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPLE, bodyTarget));
		else BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPGT, bodyTarget));
		il.append(BranchInstructions.get(BranchInstructions.size() - 1));
	}
	
	else if (statement.toString().equals(">=")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
		if (!ifSymbolFlag) BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPGE, bodyTarget));
		else BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPLT, bodyTarget));
		il.append(BranchInstructions.get(BranchInstructions.size() - 1));
	}	
	
	else if (statement.toString().equals("==")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
		if (!ifSymbolFlag) BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPEQ, bodyTarget));
		else BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPNE, bodyTarget));
		il.append(BranchInstructions.get(BranchInstructions.size() - 1));
	}

	else if (statement.toString().equals("<>")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
		if (!ifSymbolFlag) BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPNE, bodyTarget));
		else BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPEQ, bodyTarget));
		il.append(BranchInstructions.get(BranchInstructions.size() - 1));
	}
	
	else if (statement.toString().equals("+")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(1), il);
		process(interior.getChild(0), il);
		il.append(InstructionConstants.IADD);
	}
	
	else if (statement.toString().equals("-")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(1), il);
		process(interior.getChild(0), il);
		il.append(InstructionConstants.ISUB);
	}
	
	else if (statement.toString().equals("*")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(1), il);
		process(interior.getChild(0), il);
		il.append(InstructionConstants.IMUL);
	}
	
	else if (statement.toString().equals("/")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(1), il);
		process(interior.getChild(0), il);
		il.append(InstructionConstants.IDIV);
	}
	
	else if (statement.toString().equals("%")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(1), il);
		process(interior.getChild(0), il);
		il.append(InstructionConstants.IREM);
	}	
	
	else if (statement.toString().equals("print")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		int var = (int)symbolTable.get(interior.getChild(0).toString()); // Print grabs the variable's register number from symbolTable to pull from stack
		il.append(_factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Constants.GETSTATIC));
		printTarget = il.getEnd(); // Need to grab backtrack target here at start of print Instruction
		il.append(_factory.createLoad(Type.INT, var));
	    il.append(_factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
	}
	
	else { // Leaf Nodes: ie. Constants & Variables
		Object value = resolve(statement);
		if (value instanceof Integer) {
			il.append(new PUSH(_cp, (int)value));
		} else if (value instanceof String) {
			int var = (int)symbolTable.get(value);
			il.append(_factory.createLoad(Type.INT, var));
		}
	}
	
	// Backpatching
	// The first two (for 'while') are kind of inconsistently done than the later two (for 'if')
    if (bodyFlag)  { // for while backpatching
    	bodyTarget = il.getEnd(); // ie. the first body instruction
    	bodyFlag = false;
    }
	if (go_toFlag) { // for while backpatching
		go_toTarget = il.getEnd(); // ie. the first condition instruction
		go_toFlag = false;
	}
	if (ifFlag && ifFlag2) { // for if backpatching
		if (printTarget == null) {
			BranchInstructions.get(BranchInstructions.size() - 1).setTarget(il.getEnd());
		} else { // Special case for 'print' instruction, since 3 Instructions instead of 1
			BranchInstructions.get(BranchInstructions.size() - 1).setTarget(printTarget);
		}
	    ifFlag = false;
	    ifFlag2 = false;
	}
	if (elseFlag) { // for else backpatching
		BranchInstructions.get(BranchInstructions.size() - 2).setTarget(il.getEnd());
	    elseFlag = false;
	}
	printTarget = null;
  }
  
  // Returns the leaf node value, ie. Number or Variable Name
	public Object resolve(SyntaxTreeNode node) {
		Object value;
		if (node.getValue() instanceof SymbolTableEntry) {
			value = node.toString();
		} else {
			value = (Object)node.getValue();
		}
		return value;
	}

  public static void main(String[] args) throws Exception {
    ClassFile creator = new ClassFile();
    creator.create(new FileOutputStream("test.class"));
  }
}