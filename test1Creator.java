import org.apache.bcel.generic.*;
import org.apache.bcel.classfile.*;
import org.apache.bcel.*;
import java.io.*;

public class test1Creator implements Constants {
  private InstructionFactory _factory;
  private ConstantPoolGen    _cp;
  private ClassGen           _cg;

  public test1Creator() {
    _cg = new ClassGen("test3", "java.lang.Object", "test3.java", ACC_PUBLIC | ACC_SUPER, new String[] {  });

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
    MethodGen method = new MethodGen(ACC_PUBLIC, Type.VOID, Type.NO_ARGS, new String[] {  }, "<init>", "test3", il, _cp);

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
    MethodGen method = new MethodGen(ACC_PUBLIC | ACC_STATIC, Type.VOID, new Type[] { new ArrayType(Type.STRING, 1) }, new String[] { "arg0" }, "main", "test3", il, _cp);

    InstructionHandle ih_0 = il.append(new PUSH(_cp, 21));
    il.append(_factory.createStore(Type.INT, 1));
    InstructionHandle ih_3 = il.append(new PUSH(_cp, 15));
    il.append(_factory.createStore(Type.INT, 2));
    InstructionHandle ih_6 = il.append(new PUSH(_cp, 2));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(InstructionConstants.IMUL);
    il.append(new PUSH(_cp, 1));
    il.append(InstructionConstants.IADD);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(InstructionConstants.IDIV);
    il.append(new PUSH(_cp, 3));
    il.append(InstructionConstants.IADD);
        BranchInstruction if_icmple_16 = _factory.createBranchInstruction(Constants.IF_ICMPLE, null);
    il.append(if_icmple_16);
    InstructionHandle ih_19 = il.append(new PUSH(_cp, 1));
    il.append(_factory.createStore(Type.INT, 1));
    il.append(new PUSH(_cp, 2));
    il.append(_factory.createStore(Type.INT, 2));
        BranchInstruction goto_23 = _factory.createBranchInstruction(Constants.GOTO, null);
    il.append(goto_23);
    InstructionHandle ih_26 = il.append(new PUSH(_cp, 10));
    il.append(_factory.createStore(Type.INT, 1));
    il.append(new PUSH(_cp, 20));
    il.append(_factory.createStore(Type.INT, 2));
    InstructionHandle ih_32 = il.append(new PUSH(_cp, 3));
    il.append(_factory.createLoad(Type.INT, 1));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createLoad(Type.INT, 2));
    il.append(InstructionConstants.IMUL);
    il.append(_factory.createStore(Type.INT, 3));
    InstructionHandle ih_38 = il.append(_factory.createFieldAccess("java.lang.System", "out", new ObjectType("java.io.PrintStream"), Constants.GETSTATIC));
    il.append(_factory.createLoad(Type.INT, 3));
    il.append(_factory.createInvoke("java.io.PrintStream", "println", Type.VOID, new Type[] { Type.INT }, Constants.INVOKEVIRTUAL));
    InstructionHandle ih_45 = il.append(_factory.createReturn(Type.VOID));
    if_icmple_16.setTarget(ih_26);
    goto_23.setTarget(ih_32);
    method.setMaxStack();
    method.setMaxLocals();
    _cg.addMethod(method.getMethod());
    System.out.println(il);
    il.dispose();
  }

  public static void main(String[] args) throws Exception {
    test1Creator creator = new test1Creator();
    creator.create(new FileOutputStream("test3.class"));
  }
}