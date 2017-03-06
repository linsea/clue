package com.github.linsea.clue;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.util.ASMifier;
import org.objectweb.asm.util.CheckClassAdapter;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

public class BytecodeUtil implements Opcodes {

    static final String realCallMethodName = "log";

    //android.util.Log constant
    public static final int VERBOSE = 2;
    public static final int DEBUG = 3;
    public static final int INFO = 4;
    public static final int WARN = 5;
    public static final int ERROR = 6;
    public static final int ASSERT = 7;

    public static boolean needInject(String filename) {
        return filename.endsWith(".class")
                && !filename.equals("R.class")
                && !filename.equals("R$string.class")
                && !filename.equals("R$bool.class")
                && !filename.equals("BuildConfig.class")
                && !filename.equals("R$dimen.class")
                && !filename.equals("R$anim.class")
                && !filename.equals("R$layout.class")
                && !filename.equals("R$integer.class")
                && !filename.equals("R$styleable.class")
                && !filename.equals("R$plurals.class")
                && !filename.equals("R$style.class")
                && !filename.equals("R$raw.class")
                && !filename.equals("R$id.class")
                && !filename.equals("R$drawable.class")
                && !filename.equals("R$attr.class")
                && !filename.equals("R$color.class")
                && !filename.equals("R$array.class");
    }

    /** change {@code com.github.linsea.Clue.internalLog(...) } methods from private to public
     * @param clueClassfile clueClassfile
     * @param outputFile outputFile
     * @throws IOException IOException
     */
    public static void injectClueClass(File clueClassfile, File outputFile) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(clueClassfile);
        ClassReader cr = new ClassReader(fileInputStream);
        ClassWriter cw = new ClassWriter(0);
        ClassVisitor cv = new ClassVisitor(Opcodes.ASM5, cw) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                if (realCallMethodName.equals(name)) {
                    access = Opcodes.ACC_PUBLIC + ACC_STATIC;
                }
                return super.visitMethod(access, name, desc, signature, exceptions);
            }
        };
        cr.accept(cv, 0);
        byte[] bytes = cw.toByteArray();
        fileInputStream.close();
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        fileOutputStream.write(bytes);
    }


    public static boolean injectClass(File originalClassfile, File outputClassfile) throws IOException {
        final boolean[] found = {false};
        FileInputStream fis = new FileInputStream(originalClassfile);
        ClassReader cr = new ClassReader(fis);
        ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS);
//        ClassWriter cw = new ClassWriter(0);
        ClassVisitor checker = new CheckClassAdapter(cw);

//        ASMifier asmifier = new ASMifier();
//        TraceClassVisitor traceClassVisitor = new TraceClassVisitor(checker, asmifier, new PrintWriter(System.out));
//        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM5, traceClassVisitor) {  //for test trace purpose

        ClassVisitor visitor = new ClassVisitor(Opcodes.ASM5, checker) {

            String internalClassName = "unknown";// e.g. com/github/linsea/personal/MainActivity
            String sourceFilename = "unknown";// ".java" not included, e.g. MainActivity
            String methodName = "unknown";

            @Override
            public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                int idx = name.indexOf("$");
                if (idx < 0) {
                    internalClassName = name;
                } else {
                    internalClassName = name.substring(0, idx);
                }
                super.visit(version, access, name, signature, superName, interfaces);
            }

            @Override
            public void visitSource(String source, String debug) {
                if (source != null) {
                    sourceFilename = source.replaceAll("\\.java", "");
                }
                super.visitSource(source, debug);
            }

            @Override
            public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                methodName = name;
                final MethodVisitor nextMv = super.visitMethod(access, name, desc, signature, exceptions);
                return new MethodVisitor(Opcodes.ASM5, nextMv) {

                    private int lineNumber = 0;
                    private int hit = 0;

                    @Override
                    public void visitLineNumber(int line, Label start) {
                        lineNumber = line;
                        super.visitLineNumber(line, start);
                    }

                    @Override
                    public void visitMaxs(int maxStack, int maxLocals) {
//                        super.visitMaxs(0, 0);
                        if (hit > 0) {
                            super.visitMaxs(maxStack + 4, maxLocals);
                        } else {
                            super.visitMaxs(maxStack, maxLocals);
                        }
                    }

                    private void insertInsn(MethodVisitor mv, int priority, String desc) {
                        mv.visitIntInsn(Opcodes.SIPUSH, priority);
                        mv.visitLdcInsn(Type.getType("L" + internalClassName + ";"));
                        mv.visitLdcInsn(methodName);
                        mv.visitIntInsn(Opcodes.SIPUSH, lineNumber);
                        String newDesc = desc.replace(")V", "ILjava/lang/Class;Ljava/lang/String;I)V");
                        mv.visitMethodInsn(INVOKESTATIC, "com/github/linsea/clue/Clue", realCallMethodName, newDesc, false);
                    }

                    @Override
                    public void visitMethodInsn(int opcode, String owner, String name1, String desc, boolean itf) {
                        if (opcode == INVOKESTATIC && owner.equals("com/github/linsea/clue/Clue")) {
                            // desc = .....[Ljava/lang/Object;)V    =>  .....[Ljava/lang/Object;ILjava/lang/Class;Ljava/lang/String;I)V
                            // ie: )V => ILjava/lang/Class;Ljava/lang/String;I)V
                            if (name1.equals("i") || name1.equals("it")) {//INFO = 4
                                hit++;
                                found[0] = true;
                                insertInsn(mv, INFO, desc);
                            } else if (name1.equals("d") || name1.equals("dt")) { //DEBUG = 3
                                hit++;
                                found[0] = true;
                                insertInsn(mv, DEBUG, desc);
                            } else if (name1.equals("v") || name1.equals("vt")) { //VERBOSE = 2
                                hit++;
                                found[0] = true;
                                insertInsn(mv, VERBOSE, desc);
                            } else if (name1.equals("w") || name1.equals("wt")) { //WARN = 5
                                hit++;
                                found[0] = true;
                                insertInsn(mv, WARN, desc);
                            } else if (name1.equals("e") || name1.equals("et")) { //ERROR = 6
                                hit++;
                                found[0] = true;
                                insertInsn(mv, ERROR, desc);
                            } else if (name1.equals("wtf") || name1.equals("wtft")) { //ASSERT = 7
                                hit++;
                                found[0] = true;
                                insertInsn(mv, ASSERT, desc);
                            } else {
                                super.visitMethodInsn(opcode, owner, name1, desc, itf);
                            }

                        } else {
                            super.visitMethodInsn(opcode, owner, name1, desc, itf);
                        }
                    }

                };
            }
        };
        cr.accept(visitor, 0);

        FileOutputStream fos = new FileOutputStream(outputClassfile);
        fos.write(cw.toByteArray());
        fis.close();
        fos.close();

        return found[0];
    }

}
