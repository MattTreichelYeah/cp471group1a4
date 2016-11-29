import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;
import java.util.*;

public class ClassFile implements Constants {
  private InstructionFactory _factory;
  private ConstantPoolGen    _cp;
  private ClassGen           _cg;
  private List<InstructionHandle> InstructionHandles = new ArrayList<InstructionHandle>();

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
		SyntaxTreeNode.Interior statement = (SyntaxTreeNode.Interior) statements.getChild(i);
		
		if (statement.toString().equals("=")) {
		    InstructionHandles.add(il.append(new PUSH(_cp, Integer.valueOf(statement.getChild(1).toString()))));
		    il.append(_factory.createStore(Type.INT, InstructionHandles.size()));
		}
		
		if (statement.toString().equals("print")) {
			InstructionHandles.add(il.append(_factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Constants.GETSTATIC)));
		    il.append(_factory.createLoad(Type.INT, 2));
		    il.append(_factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
		}
	}

    il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    
    System.out.println(il);
    
    il.dispose();
  }

  public static void main(String[] args) throws Exception {
    ClassFile creator = new ClassFile();
    creator.create(new FileOutputStream("test.class"));
  }
}