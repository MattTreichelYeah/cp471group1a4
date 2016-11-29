import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;
import java.util.*;

public class ClassFile implements Constants {
  private InstructionFactory _factory;
  private ConstantPoolGen    _cp;
  private ClassGen           _cg;
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
	    BranchInstruction go_to = _factory.createBranchInstruction(Constants.GOTO, null);
	    il.append(go_to);
	    int bodyTarget = il.size();
	    process(interior.getChild(1), il); // Loop Body
		int go_toTarget = il.size(); // ie. the first condition instruction
	    process(interior.getChild(0), il); // Condition
		BranchInstructions.add(_factory.createBranchInstruction(Constants.IF_ICMPLT, il.findHandle(bodyTarget)));
		il.append(BranchInstructions.get(BranchInstructions.size() - 1));
	    go_to.setTarget(il.findHandle(go_toTarget));
	}
	
	else if (statement.toString().equals("<")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
	}
	
	else if (statement.toString().equals("+")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
		il.append(InstructionConstants.IADD);
	}
	
	else if (statement.toString().equals("*")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		process(interior.getChild(0), il);
		process(interior.getChild(1), il);
		il.append(InstructionConstants.IMUL);
	}
	
	else if (statement.toString().equals("print")) {
		SyntaxTreeNode.Interior interior = (SyntaxTreeNode.Interior) statement;
		int var = (int)symbolTable.get(interior.getChild(0).toString());
		il.append(_factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Constants.GETSTATIC));
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