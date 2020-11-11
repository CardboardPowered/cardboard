/**
 * Cardboard - Bukkit/Spigot/Paper API for Fabric
 * Copyright (C) 2020, CardboardPowered.org
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package com.javazilla.bukkitfabric.nms;

import java.util.ArrayList;

import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public class ReflectionMethodVisitor extends MethodVisitor {

    public static ArrayList<String> SKIP = new ArrayList<>();
    static {
        SKIP.add("vault");
        SKIP.add("worldguard");
        SKIP.add("essentials");
    }
    private String pln;

    public ReflectionMethodVisitor(int api, MethodVisitor visitMethod, String pln) {
        super(api, visitMethod);
        this.pln = pln;
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        for (Provider p : Remapper.providers) {
            boolean b = p.visitFieldInsn( opcode, owner, name, desc );
            if (b) return;
        }
        super.visitFieldInsn( opcode, owner, name, desc );
    }

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        for (String str : SKIP) {
            if (this.pln.equalsIgnoreCase(str) || owner.startsWith("org/bukkit")) {
                // Skip Vault cause weird things happen
                super.visitMethodInsn( opcode, owner, name, desc, itf );
                return;
            }
        }

        for (Provider p : Remapper.providers) {
            boolean b = p.visitMethodInsn( opcode, owner, name, desc, itf );
            if (b) return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("forName") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/Class;"))
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "mapClassName", "(Ljava/lang/String;)Ljava/lang/String;", false);

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getField") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/reflect/Field;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getFieldByName", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false );
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getDeclaredField") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/reflect/Field;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getDeclaredFieldByName", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false );
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getMethod") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/reflect/Method;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getMethodByName", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Method;", false );
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getDeclaredMethod") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/reflect/Method;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getDeclaredMethodByName", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Method;", false );
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

        if (owner.startsWith("net/minecraft/class_")) {
            if (!name.startsWith("method_"))
                name = MappingsReader.METHODS2.getOrDefault(name + desc, MappingsReader.getIntermedMethod(owner.replace('/', '.'), name));

            if (owner.equalsIgnoreCase("net/minecraft/class_3176") && name.equalsIgnoreCase("getVersion")) {
                // Add MinecraftServer#getVersion
                super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getMinecraftServerVersion", "()Ljava/lang/String;", false);
                return;
            }
        }

        super.visitMethodInsn( opcode, owner, name, desc, itf );
    }

}
