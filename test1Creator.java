import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;

public class test1Creator implements Constants {
  private InstructionFactory _factory;
  private ConstantPoolGen    _cp;
  private ClassGen           _cg;

  public test1Creator() {
    _cg = new ClassGen("test1", "java.lang.Object", "test1.java", ACC_PUBLIC | ACC_SUPER, new String[] {  });

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
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<init>", "test1", il, _cp);

    InstructionHandle ih_0 = il.append(_factory.createLoad(Type.OBJECT, 0));
    il.append(_factory.createInvoke("java.lang.Object", "<init>", Type.VOID, Type.NO_ARGS, Constants.INVOKESPECIAL));
    InstructionHandle ih_4 = il.append(_factory.createReturn(Type.VOID));
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    il.dispose();
  }

  private void createMethod_1() {
    InstructionList il = new InstructionList();
    MethodGen method = new MethodGen(ACC_PUBLIC | ACC_STATIC, Type.VOID, new Type[] { new ArrayType(Type.STRING, 1) }, new String[] { "arg0" }, "main", "test1", il, _cp);

    InstructionHandle ih_0 = il.append(new PUSH(_cp, 21));
    il.append(_factory.createStore(Type.INT, 1));
    InstructionHandle ih_3 = il.append(new PUSH(_cp, 15));
    il.append(_factory.createStore(Type.INT, 2));
    InstructionHandle ih_6;
    BranchInstruction goto_6 = _factory.createBranchInstruction(Constants.GOTO, null);
    ih_6 = il.append(goto_6);
    InstructionHandle ih_9 = il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IREM);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_13 = il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createStore(Type.INT, 1));
    InstructionHandle ih_15 = il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createStore(Type.INT, 2));
    InstructionHandle ih_17 = il.append(_factory.createLoad(Type.INT, 2));
        BranchInstruction ifne_18 = _factory.createBranchInstruction(Constants.IFNE, ih_9);
    il.append(ifne_18);
    InstructionHandle ih_21 = il.append(_factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Constants.GETSTATIC));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(_factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_28 = il.append(_factory.createReturn(Type.VOID));
    goto_6.setTarget(ih_17);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    
    System.out.println(il);
    
    il.dispose();
  }

  public static void main(String[] args) throws Exception {
    test1Creator creator = new test1Creator();
    creator.create(new FileOutputStream("test1.class"));
  }
}