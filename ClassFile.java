import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;
import java.util.*;

public class ClassFile implements Constants {
  private InstructionFactory _factory;
  private ConstantPoolGen    _cp;
  private ClassGen           _cg;
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

    il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    
    System.out.println(il);
    
    il.dispose();
  }
  
  private void process(SyntaxTreeNode statement, InstructionList il) {

	if (ifFlag) {
		ifFlag2 = true;
	}
	  
	if (statement.toString().equals("=")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		// Process Right Side
		process(interior.getChild(1), il);
		// Process Left Side
		if (!symbolTable.containsKey(interior.getChild(0).toString())) {
			symbolTable.put(interior.getChild(0).toString(), registers);
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
	    BranchInstruction go_to = _factory.createBranchInstruction(Constants.GOTO, null);
	    il.append(go_to);
	    
	    bodyFlag = true; // For grabbing backtrack target
		for (int i = 0; i < body.numChildren(); i++) {
			process(body.getChild(i), il); // Body
		}	
		go_toFlag = true; // For grabbing goto target
	    process(interior.getChild(0), il); // Condition
	    
	    go_to.setTarget(go_toTarget);
	}
	
	else if (statement.toString().equals("if")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		SyntaxTreeNode.Interior body1 = (SyntaxTreeNode.Interior) interior.getChild(1);
		SyntaxTreeNode.Interior body2 = null;
		if (interior.numChildren() == 3) {
			body2 = (SyntaxTreeNode.Interior) interior.getChild(2);
		}
	    
	    process(interior.getChild(0), il); // Condition
	    
		for (int i = 0; i < body1.numChildren(); i++) {
			process(body1.getChild(i), il); // Body
		}
		
		BranchInstructions.add(_factory.createBranchInstruction(Constants.GOTO, null));
	    il.append(BranchInstructions.get(BranchInstructions.size() - 1));
	    
		if (body2 != null) {
			elseFlag = true;
			for (int i = 0; i < body2.numChildren(); i++) {
				process(body2.getChild(i), il); // Body Else
			}	
		}

	    ifFlag = true;
	}
	
	else if (statement.toString().equals("<")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
		BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPLT, bodyTarget));
		il.append(BranchInstructions.get(BranchInstructions.size() - 1));
	}
	
	else if (statement.toString().equals(">")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
		BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPGT, bodyTarget));
		il.append(BranchInstructions.get(BranchInstructions.size() - 1));
	}
	
	else if (statement.toString().equals("<=")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
		BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPLE, bodyTarget));
		il.append(BranchInstructions.get(BranchInstructions.size() - 1));
		
	}
	
	else if (statement.toString().equals(">=")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
		BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPGE, bodyTarget));
		il.append(BranchInstructions.get(BranchInstructions.size() - 1));
	}	
	
	else if (statement.toString().equals("==")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
		BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPEQ, bodyTarget));
		il.append(BranchInstructions.get(BranchInstructions.size() - 1));
	}

	else if (statement.toString().equals("<>")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
		BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPNE, bodyTarget));
		il.append(BranchInstructions.get(BranchInstructions.size() - 1));
	}
	
	else if (statement.toString().equals("+")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
		il.append(InstructionConstants.IADD);
	}
	
	else if (statement.toString().equals("-")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
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
		int var = (int)symbolTable.get(interior.getChild(0).toString());
		il.append(_factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Constants.GETSTATIC));
		printTarget = il.getEnd();
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
		} else {
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
  
  public void ilappend(InstructionList il, InstructionHandle ih) {
	  
  }
  
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