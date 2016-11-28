
import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ClassFile implements Constants {
	private InstructionFactory _factory;
	private ConstantPoolGen _cp;
	private ClassGen _cg;
	private Number numberHandler = new Number();
	private InstructionList il = new InstructionList();
	private static final List<InstructionHandle> InstructionHandles = new ArrayList<InstructionHandle>();
	private static final List<BranchInstruction> BranchInstructions = new ArrayList<BranchInstruction>();

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
		InstructionList il0 = new InstructionList();
		MethodGen method0 = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<init>", "test", il0, _cp);

		InstructionHandle ih_0 = il0.append(_factory.createLoad(Type.OBJECT, 0));
		il0.append(_factory.createInvoke("java.lang.Object", "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
		InstructionHandle ih_4 = il0.append(_factory.createReturn(Type.VOID));
		method0.setMaxStack();
		method0.setMaxLocals();
		_cg.addMethod(method0.getMethod());
		il0.dispose();
	}

	private void createMethod_1() throws IOException {
		MethodGen method = new MethodGen(ACC_PUBLIC | ACC_STATIC, Type.VOID, new Type[] { new ArrayType(Type.STRING, 1) }, new String[] { "arg0" }, "main", "test", il, _cp);
		
		// Get Tree
		Parser parser = new Parser();
		parser.program();
		LinkedList<SyntaxTreeNode> treeList = parser.syntaxTree.getTraversalList();
		System.out.println("Syntax Tree Traversal List: " + treeList);
		
		// Process Tree & Make Class
		SyntaxTreeNode.Interior statements = (SyntaxTreeNode.Interior) treeList.getFirst();
		statement_seq(statements);
		
		System.out.println(il);
		
		il.append(_factory.createReturn(Type.VOID));
		
		// Final Cleanup
	    method.setMaxStack();
	    method.setMaxLocals();
	    _cg.addMethod(method.getMethod());
	    il.dispose();
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

				InstructionHandles.add(il.append(new PUSH(_cp, (Integer)right)));
				il.append(_factory.createStore(Type.INT, InstructionHandles.size()));
			}
			
			else if (node.toString().equals("+")) {
				process(interior.getChild(0));
				process(interior.getChild(1));
				Object left = resolve(interior.getChild(0));
				Object right = resolve(interior.getChild(1));
				node.setValue(numberHandler.operation("+", right, left));
			}
			
			else if (node.toString().equals("while")) {
				process(interior.getChild(0));
				while ((boolean)interior.getChild(0).getValue() == true) {
					statement_seq((SyntaxTreeNode.Interior)interior.getChild(1));
					process(interior.getChild(0));
				}
				
			    BranchInstructions.add(_factory.createBranchInstruction(Constants.GOTO, null));
			    InstructionHandles.add(il.append(BranchInstructions.get(BranchInstructions.size() - 1)));
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
				
			    InstructionHandle ih_21 = il.append(_factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Constants.GETSTATIC));
			    il.append(_factory.createLoad(Type.INT, 1));	
			    il.append(_factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
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

	public static void main(String[] args) throws Exception {
		ClassFile creator = new ClassFile();
		creator.create(new FileOutputStream("test.class"));
	}
}