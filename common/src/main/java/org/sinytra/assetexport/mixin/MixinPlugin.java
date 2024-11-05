package org.sinytra.assetexport.mixin;

import org.objectweb.asm.Label;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.sinytra.assetexport.CommonClass;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.objectweb.asm.Opcodes.*;

public class MixinPlugin implements IMixinConfigPlugin {
    @Override
    public void onLoad(String mixinPackage) {

    }

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        if (mixinClassName.startsWith("org.sinytra.assetexport.mixin.conditional.") && System.getProperty(CommonClass.CONFIG_FILE) == null) {
            return false;
        }
        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {

    }

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
    }

    @Override
    public void postApply(String targetClassName, org.objectweb.asm.tree.ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {
        if (mixinClassName.startsWith("org.sinytra.assetexport.mixin.conditional.") && (targetClass.name.equals("com/mojang/blaze3d/systems/RenderSystem") || targetClass.name.equals("com/mojang/blaze3d/platform/GlStateManager"))) {
            var newFields = targetClass.fields.stream()
                    .filter(f -> f.name.startsWith("$"))
                    .collect(Collectors.toMap(
                            f -> f.name.substring(1),
                            f -> f.desc
                    ));

            for (var newField : newFields.entrySet()) {
                var fname = newField.getKey();
                var fdesc = newField.getValue();
                var ftype = Type.getType(fdesc);

                {
                    var methodVisitor = targetClass.visitMethod(ACC_PRIVATE | ACC_STATIC, "$" + newField.getKey(), "()" + newField.getValue(), null, null);
                    methodVisitor.visitCode();
                    Label label0 = new Label();
                    methodVisitor.visitLabel(label0);
                    methodVisitor.visitLineNumber(42, label0);
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
                    methodVisitor.visitFieldInsn(GETSTATIC, "org/sinytra/assetexport/CommonClass", "mainTh", "Ljava/lang/Thread;");
                    Label label1 = new Label();
                    methodVisitor.visitJumpInsn(IF_ACMPNE, label1);
                    methodVisitor.visitFieldInsn(GETSTATIC, targetClass.name, "$" + newField.getKey(), newField.getValue());
                    methodVisitor.visitInsn(ftype.getOpcode(IRETURN));
                    methodVisitor.visitLabel(label1);
                    methodVisitor.visitLineNumber(43, label1);
                    methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                    methodVisitor.visitFieldInsn(GETSTATIC, targetClass.name, newField.getKey(), newField.getValue());
                    methodVisitor.visitInsn(ftype.getOpcode(IRETURN));
                    methodVisitor.visitMaxs(2, 0);
                    methodVisitor.visitEnd();
                }

                {
                    var methodVisitor = targetClass.visitMethod(ACC_PRIVATE | ACC_STATIC, "$" + fname, "(" + fdesc + ")V", null, null);
                    methodVisitor.visitCode();
                    Label label0 = new Label();
                    methodVisitor.visitLabel(label0);
                    methodVisitor.visitLineNumber(47, label0);
                    methodVisitor.visitMethodInsn(INVOKESTATIC, "java/lang/Thread", "currentThread", "()Ljava/lang/Thread;", false);
                    methodVisitor.visitFieldInsn(GETSTATIC, "org/sinytra/assetexport/CommonClass", "mainTh", "Ljava/lang/Thread;");
                    Label label1 = new Label();
                    methodVisitor.visitJumpInsn(IF_ACMPNE, label1);
                    methodVisitor.visitVarInsn(ftype.getOpcode(ILOAD), 0);
                    methodVisitor.visitFieldInsn(PUTSTATIC, targetClass.name, "$" + fname, fdesc);
                    Label label2 = new Label();
                    methodVisitor.visitJumpInsn(GOTO, label2);
                    methodVisitor.visitLabel(label1);
                    methodVisitor.visitLineNumber(48, label1);
                    methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                    methodVisitor.visitVarInsn(ftype.getOpcode(ILOAD), 0);
                    methodVisitor.visitFieldInsn(PUTSTATIC, targetClass.name, fname, fdesc);
                    methodVisitor.visitLabel(label2);
                    methodVisitor.visitLineNumber(49, label2);
                    methodVisitor.visitFrame(Opcodes.F_SAME, 0, null, 0, null);
                    methodVisitor.visitInsn(RETURN);
                    Label label3 = new Label();
                    methodVisitor.visitLabel(label3);
                    methodVisitor.visitLocalVariable("pmx", fdesc, null, label0, label3, 0);
                    methodVisitor.visitMaxs(2, 1);
                    methodVisitor.visitEnd();
                }
            }

            for (MethodNode method : targetClass.methods) {
                if (method.name.startsWith("$") || method.name.equals("<clinit>")) return;
                for (int i = 0; i < method.instructions.size(); i++) {
                    var abstractInsnNode = method.instructions.get(i);
                    if (abstractInsnNode instanceof FieldInsnNode fsns && newFields.containsKey(fsns.name)) {
                        if (abstractInsnNode.getOpcode() == Opcodes.GETSTATIC) {
                            method.instructions.set(abstractInsnNode, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    targetClass.name,
                                    "$" + fsns.name,
                                    "()" + fsns.desc
                            ));
                        } else if (abstractInsnNode.getOpcode() == Opcodes.PUTSTATIC) {
                            method.instructions.set(abstractInsnNode, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    targetClass.name,
                                    "$" + fsns.name,
                                    "(" + fsns.desc + ")V"
                            ));
                        }
                    }
                }
            }
        }
    }
}
