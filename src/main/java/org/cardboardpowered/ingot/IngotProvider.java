package org.cardboardpowered.ingot;

import java.io.File;
import java.util.ArrayList;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.javazilla.bukkitfabric.nms.Provider;

public class IngotProvider implements Provider {

    @Override
    public boolean remap(File file) {
        return !IngotReader.finishedSetup; // Disable SpecialSource
    }

    @Override
    public boolean runSpecialSource(File mappingsFile, File inJar, File outJar) {
        //if (mappingsFile.getName().contains("deversionify"))
            return false;

        /*String[] args = {"-q", "-i", inJar.getAbsolutePath(), "-o", outJar.getAbsolutePath(), "-m", IngotReader.outFile.getAbsolutePath()};
        try {
            SpecialSource.main(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    */}

    @Override
    public boolean shouldReplaceASM() {
        return true;
    }

    public static ArrayList<String> SKIP = new ArrayList<>();
    static {
        SKIP.add("vault");
        SKIP.add("worldguard");
        SKIP.add("essentials"); // What crazy things does EssentialsX do now?
    }

    @Override
    public MethodVisitor newMethodVisitor(int arg0, MethodVisitor arg1, String pln) {
        return new MethodVisitor(arg0, arg1) {

            @Override
            public void visitFieldInsn(int opcode, String owner, String name, String desc) {
                if (owner.startsWith("net/minecraft")) {
                    String toGet = owner.replace('/','.') + "|" + name;
                    if (IngotReader.fields_S2F.containsKey(toGet)) {
                        owner = IngotReader.classes_S2F.getOrDefault(owner.replace('/','.'), owner).replace('.', '/');
                        name = IngotReader.fields_S2F.get(toGet);
                    }
                }
                super.visitFieldInsn(opcode, owner, name, desc);
            }

            @Override
            public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean isInterface) {
                for (String str : SKIP) {
                    if (pln.equalsIgnoreCase(str) || owner.startsWith("org/bukkit") || owner.startsWith("com/earth2me/essentials/utils")) {
                        // Skip cause weird things happen
                        super.visitMethodInsn( opcode, owner, name, desc, isInterface );
                        return;
                    }
                }

                if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("forName") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/Class;"))
                    super.visitMethodInsn(Opcodes.INVOKESTATIC, "org/cardboardpowered/ingot/ReflectionRemapper", "mapClassName", "(Ljava/lang/String;)Ljava/lang/String;", false);

                if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getField") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/reflect/Field;")) {
                    super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/ingot/ReflectionRemapper", "getFieldByName", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false );
                    return;
                }

                if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getDeclaredField") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/reflect/Field;")) {
                    super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/ingot/ReflectionRemapper", "getDeclaredFieldByName", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false );
                    return;
                }

                if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getMethod") && desc.equalsIgnoreCase("(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;")) {
                    super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/ingot/ReflectionRemapper", "getMethodByName", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false );
                    return;
                }

                if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getDeclaredMethod") && desc.equalsIgnoreCase("(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;")) {
                    super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/ingot/ReflectionRemapper", "getDeclaredMethodByName", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false );
                    return;
                }

                if (owner.equalsIgnoreCase("java/lang/Package") && name.equalsIgnoreCase("getName") && desc.equalsIgnoreCase("()Ljava/lang/String;")) {
                    super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getPackageName", "(Ljava/lang/Package;)Ljava/lang/String;", false);
                    return;
                }

                if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getName") && desc.equalsIgnoreCase("()Ljava/lang/String;")) {
                    super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getClassName", "(Ljava/lang/Class;)Ljava/lang/String;", false);
                    return;
                }


                if (owner.startsWith("net/minecraft")) {
                    if (owner.equalsIgnoreCase("net/minecraft/server/MinecraftServer") && name.equalsIgnoreCase("getVersion")) {
                        // Add MinecraftServer#getVersion
                        super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getMinecraftServerVersion", "()Ljava/lang/String;", false);
                        return;
                    }
                    if (owner.equalsIgnoreCase("net/minecraft/class_3176") && name.equalsIgnoreCase("getVersion")) {
                        // Add MinecraftServer#getVersion
                        super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getMinecraftServerVersion", "()Ljava/lang/String;", false);
                        return;
                    }

                    if (owner.equalsIgnoreCase("net/minecraft/server/MinecraftServer") && name.equalsIgnoreCase("getServer")) {
                        // Add MinecraftServer#getServer
                        super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/ingot/ReflectionRemapper", "getNmsServer", "()Lnet/minecraft/server/MinecraftServer;", false);
                        return;
                    }
                    if (owner.equalsIgnoreCase("net/minecraft/class_3176") && name.equalsIgnoreCase("getServer")) {
                        // Add MinecraftServer#getServer
                        super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/ingot/ReflectionRemapper", "geNmsServer", "()Lnet/minecraft/server/MinecraftServer;", false);
                        return;
                    }


                    String toGet = owner.replace('/','.') + "|" + name;
                    if (IngotReader.methods_S2F.containsKey(toGet)) {
                        IngotMethodInfo mi = IngotReader.methods_S2F.get(toGet);
                        owner = mi.fabricClassName.replace('.','/');
                        name = mi.fabricMethodName;
                        desc = mi.fabricDescriptor;
                    }
                }
                super.visitMethodInsn(opcode, owner, name, desc, isInterface);
            }

        };
    }

}
