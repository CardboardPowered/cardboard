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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;

import org.bukkit.Material;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.minecraft.block.BlockState;

public class ReflectionMethodVisitor extends MethodVisitor {

    public static ArrayList<String> SKIP = new ArrayList<>();
    static {
        SKIP.add("vault");
        SKIP.add("worldguard");
        //SKIP.add("essentials");
    }
    private String pln;
    private MappingResolver mr;
    
    public static HashMap<String,String> spigot2obf;

    public ReflectionMethodVisitor(int api, MethodVisitor visitMethod, String pln) {
        super(api, visitMethod);
        this.pln = pln;
        this.mr = FabricLoader.getInstance().getMappingResolver();
        if (null == spigot2obf) {
            spigot2obf = new HashMap<>();
            try {
                if (new File("builddata.txt").isFile()) {
                    for (String s : Files.readAllLines(new File("builddata.txt").toPath())) {
                        if (s.indexOf('#') != -1) continue;
                        
                        String[] spl = s.split(" ");
                        spigot2obf.put(spl[1], spl[0]);
                     //   System.out.println("MAP: " + spl[1] + " | " + spl[0]);
                    }
                    System.out.println("Loaded 1.17 Obf Class Map: " + spigot2obf.size());
                    for (String s : Files.readAllLines(new File("bd-m.txt").toPath())) {
                        if (s.indexOf('#') != -1) continue;
        
                        String[] spl = s.split(" ");
                        spigot2obf.put(spl[0] + "#" + spl[3], spl[1]);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void visitFieldInsn(int opcode, String owner, String name, String desc) {
        if (owner.equalsIgnoreCase("org/bukkit/Material")) {
            if (CraftMagicNumbers.MODDED_MATERIALS.containsKey(name)) { 
                super.visitFieldInsn( opcode, owner, "STONE", desc );
                return;
            }
        }

        super.visitFieldInsn( opcode, owner, name, desc );
    }

    public static Field Material_getField(String name) throws NoSuchFieldException, SecurityException {
        try {
            return Material.class.getField(name);
        } catch (NoSuchFieldException | SecurityException e) {
            return Material.class.getField("STONE");
        }
    }

    public static int fixed = 0; // max so far: 484
    public static int lastF = 484;

    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (name.equals("getCraftServer")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", name, desc, false );
            return;
        }
        
        if (owner.startsWith("net/minecraft") && spigot2obf.size() > 1) {
            String own = spigot2obf.getOrDefault(owner, owner);
            String cl = mr.mapClassName("official", own.replace('/','.'));
            String d = desc;
            String d2 = desc;
            for (String s : spigot2obf.keySet()) {
                d = d.replace("L" + s + ";", "L" + spigot2obf.getOrDefault(s, s) + ";");
                d2 = d2.replace("L" + s + ";", "L" + mr.mapClassName("official", spigot2obf.getOrDefault(s, s).replace('/','.')) + ";");
            }

            if (!own.contains("v1_1")) {
                String name2 = mr.mapMethodName("official", own, 
                        spigot2obf.getOrDefault(owner + "#" + name, name), d);

                if (!own.contains("net.minecraft.server.v1_1") && !name2.equals(name)) {
                    fixed++;
                    if (fixed > lastF) System.out.println(fixed);
                    //System.out.println(cl + "/=/" + name + d + " /=/ " + name2);
                } /*else if (!own.contains("net.minecraft.server.v1_1")) {
                    System.out.println(cl + "/=/" + name + d + " /=/ " + name2);
                }*/
                super.visitMethodInsn( opcode, cl.replace('.', '/'), name2, d2, false );
                return;
            }
        }
        if (owner.contains("NbtCompound") || owner.contains("class_2487")) {
            if (name.startsWith("setString")) {
                String cl = mr.unmapClassName("intermediary", owner.replace('/','.'));
                String name2 = mr.mapMethodName("intermediary", cl.replace('/', '.'), "method_10582", desc);
                super.visitMethodInsn( opcode, owner, name2, desc, false );
                return;
            }
        }

        if (owner.equalsIgnoreCase("org/bukkit/Material")) {
            if (name.equalsIgnoreCase("getField")) {
                System.out.println("\nGET MATERIAL FIELD!!!!!\n");
                super.visitFieldInsn( opcode, "com/javazilla/bukkitfabric/nms/ReflectionMethodVisitor", "Material_getField", desc );
                return;
            }
        }

        if (owner.equalsIgnoreCase("com/comphenix/protocol/utility/MinecraftReflection")) {
            // System.out.println("PROTOCOLLIB REFLECTION: " + name);
            if (name.equals("getCraftBukkitClass") || name.equals("getMinecraftClass")) {
                super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ProtocolLibMapper", name, desc, false );
                return;
            }
        }

        if (owner.equalsIgnoreCase("com/comphenix/protocol/injector/netty/ChannelInjector")) {
            if (name.equals("guessCompression")) {
                super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ProtocolLibMapper", name, desc, false );
                return;
            }
        }
        
        if (owner.equalsIgnoreCase("com/sk89q/worldguard/bukkit/util/Materials")) {
            if (name.equals("isSpawnEgg") || name.equals("getEntitySpawnEgg") || name.equals("isArmor") ||
                    name.equals("isToolApplicable") || name.equals("isWaxedCopper")) {
                super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/WorldGuardMaterialHelper", name, desc, false );
                return;
            }
        }

        for (String str : SKIP) {
            if (this.pln.equalsIgnoreCase(str) || owner.startsWith("org/bukkit")) {
                // Skip Vault cause weird things happen
                super.visitMethodInsn( opcode, owner, name, desc, itf );
                return;
            }
        }

        //if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("forName") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/Class;"))
        //    super.visitMethodInsn(Opcodes.INVOKESTATIC, "com/comphenix/protocol/reflect/FuzzyReflection", "getMethod", "(Ljava/lang/String;)Ljava/lang/String;", false);


        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("forName") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/Class;"))
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "mapClassName", "(Ljava/lang/String;)Ljava/lang/String;", false);

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getMethods")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getMethods", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", false );
            return;
        }

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

        //this.getClass().getCanonicalName();
        if (owner.equalsIgnoreCase("java/lang/Package") && name.equalsIgnoreCase("getName") && desc.equalsIgnoreCase("()Ljava/lang/String;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getPackageName", "(Ljava/lang/Package;)Ljava/lang/String;", false);
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getName") && desc.equalsIgnoreCase("()Ljava/lang/String;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getClassName", "(Ljava/lang/Class;)Ljava/lang/String;", false);
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getCanonicalName") && desc.equalsIgnoreCase("()Ljava/lang/String;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getCanonicalName", "(Ljava/lang/Class;)Ljava/lang/String;", false);
            return;
        }
 
        /*if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getMethod") && desc.equalsIgnoreCase("(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getMethodByName", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false );
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getDeclaredMethod") && desc.equalsIgnoreCase("(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getDeclaredMethodByName", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false );
            return;
        }*/

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
