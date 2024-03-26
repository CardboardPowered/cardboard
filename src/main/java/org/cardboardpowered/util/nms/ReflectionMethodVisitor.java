/**
 * Cardboard - Bukkit/Spigot/Paper API for Fabric
 * Copyright (C) 2023, CardboardPowered.org
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
package org.cardboardpowered.util.nms;
// package com.javazilla.bukkitfabric.nms;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.MappingResolver;
import net.techcable.srglib.JavaType;
import net.techcable.srglib.MethodSignature;

import org.bukkit.Material;
import org.bukkit.craftbukkit.util.Commodore;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import com.javazilla.bukkitfabric.BukkitFabricMod;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

// <<<<<<< HEAD:src/main/java/com/javazilla/bukkitfabric/nms/ReflectionMethodVisitor.java
// =======
//import org.bukkit.Material;
//import org.bukkit.craftbukkit.util.Commodore;
//import org.bukkit.craftbukkit.util.CraftMagicNumbers;
//import org.objectweb.asm.MethodVisitor;
//import org.objectweb.asm.Opcodes;

//import net.fabricmc.loader.api.FabricLoader;
//mport net.fabricmc.loader.api.MappingResolver;

// >>>>>>> upstream/ver/1.20:src/main/java/org/cardboardpowered/util/nms/ReflectionMethodVisitor.java
public class ReflectionMethodVisitor extends MethodVisitor {

    public static ArrayList<String> SKIP = new ArrayList<>();
    static {
        SKIP.add("vault");
        SKIP.add("worldguard");
        //SKIP.add("worldedit");
    }
    private String pln;
    private MappingResolver mr;
    // private MappingResolver mr2;
    
    public static HashMap<String,String> spigot2obf;
    public static HashMap<String,String> cbm;

    public ReflectionMethodVisitor(int api, MethodVisitor visitMethod, String pln) {
        super(api, visitMethod);
        this.pln = pln;
      //  net.fabricmc.loader.impl.FabricLoaderImpl l;
        this.mr = FabricLoader.getInstance().getMappingResolver();
        // this.mr2 = new Testing("official");
        if (null == spigot2obf) {
            spigot2obf = new HashMap<>();
            cbm = new HashMap<>();
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
                    for (String s : Files.readAllLines(new File("cbm.txt").toPath())) {
                        if (s.indexOf('!') != -1) continue;

                        String[] spl = s.split("=");
                        System.out.println("SPLIT: " + spl[0] + "," + spl[1]);
                        cbm.put(spl[0].trim(), spl[1].trim());
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
        
        if (owner.startsWith("net/minecraft") && name.length() <= 2) {
        	MappingResolver mr = FabricLoader.getInstance().getMappingResolver(); 

        	String owner_official = mr.unmapClassName("official", owner.replace('/', '.'));

        	String sigg = "";

        	JavaType jt  = JavaType.fromDescriptor(desc);
        	
        	if (jt.getDescriptor().startsWith("L") || jt.getDescriptor().contains("[[L")) {
    			String in = jt.getInternalName().replace('/', '.');
    			String ll = mr.unmapClassName("official", in);
    			sigg += jt.getDescriptor().replace(jt.getInternalName(), ll).replace('.', '/');
    		} else {
    			sigg += jt.getDescriptor();
    		}

        	String mapped = mr.mapFieldName("official", owner_official, name, sigg);
        	
        	if (!mapped.startsWith("field_")) {
        		System.out.println("TESTINGF: " + owner + " " + name + " " + desc + " (" + sigg + ") " + " === " + mapped);
        	}

        	 super.visitFieldInsn( opcode, owner, mapped, desc );
             return;
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

    
    public String do_map(String owner, String name, String desc) {
    	MappingResolver mr = FabricLoader.getInstance().getMappingResolver(); 

    	String owner_official = mr.unmapClassName("official", owner.replace('/', '.'));
    	
    	MethodSignature sig = MethodSignature.fromDescriptor(desc);
    	
    	List<JavaType> jts = sig.getParameterTypes();
    	
    	String sigg = "(";
    	
    	for (JavaType jt : jts) {
    		if (jt.getDescriptor().startsWith("L") || jt.getDescriptor().contains("[[L")) {
    			String in = jt.getInternalName().replace('/', '.');
    			String ll = mr.unmapClassName("official", in);
    			sigg += jt.getDescriptor().replace(jt.getInternalName(), ll).replace('.', '/');
    		} else {
    			sigg += jt.getDescriptor();
    		}
    	}

    	sigg += ")";
    	
    	JavaType jt  = sig.getReturnType();
    	
    	if (jt.getDescriptor().startsWith("L") || jt.getDescriptor().contains("[[L")) {
			String in = jt.getInternalName().replace('/', '.');
			String ll = mr.unmapClassName("official", in);
			sigg += jt.getDescriptor().replace(jt.getInternalName(), ll).replace('.', '/');
		} else {
			sigg += jt.getDescriptor();
		}

    	String mapped = mr.mapMethodName("official", owner_official, name, sigg);
    	
    	
    	if (!mapped.startsWith("method_")) {
    		
    		String res = mapped;

    		// Check super class:
    		try {
    			Class<?> up = Class.forName(owner.replace('/', '.'));
    			
    			if (null != up.getSuperclass()) {
        			String supn = up.getSuperclass().getName();
        			res = do_map(supn, name, desc);
    			}
    		} catch (ClassNotFoundException e) {
    			BukkitFabricMod.LOGGER.finest("MISSING CLASS MAPPING FOR: " + owner);
    			System.out.println(e.getMessage());
    		} catch (Exception e) {
    			// Oh no!
    			e.printStackTrace();
    		}
    		
    		if (!res.startsWith("method_")) {
    		//	System.out.println("TESTING: " + owner + " " + name + " " + desc + " (" + sigg + ") " + " === " + mapped + " (" + res + ")");
    		}
    		mapped = res;
    		
    	}
    	
    	//if (!mapped.startsWith("method_")) {
    	//	System.out.println("TESTING: " + owner + " " + name + " " + desc + " (" + sigg + ") " + " === " + mapped);
    	//}
    	return mapped;
    }
   
    
    @Override
    public void visitMethodInsn(int opcode, String owner, String name, String desc, boolean itf) {
        if (name.equals("getCraftServer")) {
        	System.out.println(owner + " " + name + " " + desc);
            // super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", name, desc, false );
            // return;
        }
        
        if (owner.startsWith("org/bukkit/craftbukkit") && owner.contains(ReflectionRemapper.NMS_VERSION)) {
        	System.out.println("Stripping version package (" + ReflectionRemapper.NMS_VERSION + ") from org/bukkit/craftbukkit reference.");
        	owner = owner.replace("org/bukkit/craftbukkit/" + ReflectionRemapper.NMS_VERSION + "/", "org/bukkit/craftbukkit/");
        }
        
        if (owner.startsWith("net/minecraft") && name.equals("getMinecraftServer")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getNmsServer", desc, false );
            System.out.println(owner + " " + name + " " + desc);
            return;
        }

        if (owner.startsWith("net/minecraft") && name.equals("getServer")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "com/javazilla/bukkitfabric/nms/ReflectionRemapper", "getNmsServer", desc, false );
            System.out.println(owner + " " + name + " " + desc);
            return;
        }
        
        if (name.contains("getWorld")) {
        	//System.out.println(owner + " " + name + " " + desc);
        }

        
        //  net/minecraft/class_3218 getWorld ()Lorg/cardboardpowered/impl/world/WorldImpl;
        
        /*if (owner.contains("class_3218") && name.equals("getWorld") && desc.contains("WorldImpl")) {
        	System.out.println("Casting ServerWorld to IServerWorld.");
        	String iw = "org/cardboardpowered/interfaces/IServerWorld";
        	
        	super.visitVarInsn(Opcodes.ILOAD, 1); // Load the 'level' parameter
            super.visitTypeInsn(Opcodes.CHECKCAST, iw); // Cast the 'level' parameter to 'ILevel'
            super.visitMethodInsn(opcode, iw, "getWorld", desc, itf);
            return;
        }
        
        
        if (name.equals("getWorld") && (desc.contains("org/bukkit/craftbukkit") || desc.contains("WorldImpl"))) {
            name = "getWorldImpl";
            desc = desc.replace("/v1_17_R1", "");
        }*/
        
        if (owner.startsWith("net/minecraft") && name.length() <= 2) {
        	MappingResolver mr = FabricLoader.getInstance().getMappingResolver(); 

        	String owner_official = mr.unmapClassName("official", owner.replace('/', '.'));
        	
        	MethodSignature sig = MethodSignature.fromDescriptor(desc);
        	
        	List<JavaType> jts = sig.getParameterTypes();
        	
        	String sigg = "(";
        	
        	for (JavaType jt : jts) {
        		if (jt.getDescriptor().startsWith("L") || jt.getDescriptor().contains("[[L")) {
        			String in = jt.getInternalName().replace('/', '.');
        			String ll = mr.unmapClassName("official", in);
        			sigg += jt.getDescriptor().replace(jt.getInternalName(), ll).replace('.', '/');
        		} else {
        			sigg += jt.getDescriptor();
        		}
        	}

        	sigg += ")";
        	
        	JavaType jt  = sig.getReturnType();
        	
        	if (jt.getDescriptor().startsWith("L") || jt.getDescriptor().contains("[[L")) {
    			String in = jt.getInternalName().replace('/', '.');
    			String ll = mr.unmapClassName("official", in);
    			sigg += jt.getDescriptor().replace(jt.getInternalName(), ll).replace('.', '/');
    		} else {
    			sigg += jt.getDescriptor();
    		}

        	String mapped = mr.mapMethodName("official", owner_official, name, sigg);
        	
        	if (!mapped.startsWith("method_")) {
        		
        		String res = mapped;

        		// Check super class:
        		try {
        			Class<?> up = Class.forName(owner.replace('/', '.'));
        			
        			if (null != up.getSuperclass()) {
	        			String supn = up.getSuperclass().getName();
	        			res = do_map(supn, name, desc);
        			}
        		} catch (ClassNotFoundException e) {
        			BukkitFabricMod.LOGGER.finest("MISSING CLASS MAPPING FOR: " + owner);
        			System.out.println(e.getMessage());
        		} catch (Exception e) {
        			// Oh no!
        			e.printStackTrace();
        		}
        		
        		if (!res.startsWith("method_")) {
        			System.out.println("TESTING: " + owner + " " + name + " " + desc + " (" + sigg + ") " + " === " + mapped + " (" + res + ")");
        		}
        		mapped = res;
        		
        	}
        	
        	
        	
        	 super.visitMethodInsn( opcode, owner, mapped, desc, false );
             return;
        }

        if (owner.startsWith("net/minecraft") && spigot2obf.size() > 1) {
            String own = spigot2obf.getOrDefault(owner, owner);
            String cl = mr.mapClassName("official", own.replace('/','.'));
            String d = desc;
            String d2 = desc;
            for (String s : spigot2obf.keySet()) {
                d = d.replace("L" + s + ";", "L" + spigot2obf.getOrDefault(s, s) + ";");
                d2 = d2.replace("L" + s + ";", "L" + mr.mapClassName("official", spigot2obf.getOrDefault(s, s).replace('/','.')) + ";").replace('.', '/');
            }

            if (!own.contains("v1_1")) {
                String name2 = mr.mapMethodName("official", own.replace('/', '.'), 
                        spigot2obf.getOrDefault(owner + "#" + name, name).replace('/', '.'), d);

                if (!own.contains("net.minecraft.server.v1_1") && !name2.equals(name)) {
                    fixed++;
                   // if (fixed > lastF) System.out.println(fixed);
                    //System.out.println(cl + "/=/" + name + d + " /=/ " + name2);
                } else if (!own.contains("net.minecraft.server.v1_1") && !name.contains("<init>")) {
                    String key = cl.substring(cl.lastIndexOf('.')+1) + "#" + name + d2;
                    name2 = cbm.getOrDefault(key, name2);
                    if (!cbm.containsKey(key) && name.length() < 3) {
                        //System.out.println(cl.substring(cl.lastIndexOf('.')+1) + "#" + name + d2 + " |  " + name2);
                       
                        //System.out.println(own + " / " + mr.unmapClassName("official", own.replace('/','.')) + " / " + mr.unmapClassName("official", own.replace('/','.')));
                        //System.out.println(mr.mapMethodName("official", own.replace('/', '.'), name2, desc));
                        //System.out.println(mr.mapMethodName("official", own.replace('/', '.'), name2, d2));

                        try {
                            Class<?> cz = Class.forName(cl);
                            for (Method m : cz.getDeclaredMethods()) {

                                //if (cl.contains("NbtList")) {
                                    String tt = "";
                                    for (Class<?> zz : m.getParameterTypes()) {
                                        tt += (fixName(zz.getName()) + ";");
                                    }
                                    tt = tt.replace("int;","I");
                                    tt = "(" + tt + ")" + fixName(m.getReturnType().getName() + ";");
                                    if (tt.equalsIgnoreCase(d2)) {
                                        //System.out.println( "\tPossible Match?: " + m.getName() + tt + "");
                                        
//<<<<<<< HEAD:src/main/java/com/javazilla/bukkitfabric/nms/ReflectionMethodVisitor.java
                                        // String obfn = mr2.mapMethodName("named", mr2.mapClassName("named", cz.getName()), m.getName(), tt);
//=======
                                       //  String obfn = mr2.mapMethodName("named", mr2.mapClassName("named", cz.getName()), m.getName(), tt);
//>>>>>>> upstream/ver/1.20:src/main/java/org/cardboardpowered/util/nms/ReflectionMethodVisitor.java
                                        //System.out.println("OBFN: " + obfn);
                                    }
                                    
                                //}
                            }
                        } catch (Exception e) {
                            // TODO Auto-generated catch block
                            e.printStackTrace();
                        }
                    }
                }

                System.out.println("NAM2!: " + name2);
                super.visitMethodInsn( opcode, cl.replace('.', '/'), name2, d2.replace('.', '/'), false );
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
                super.visitFieldInsn( opcode, "org/cardboardpowered/util/nms/ReflectionMethodVisitor", "Material_getField", desc );
                return;
            }
        }
        
        if (owner.equals("protocolsupport/utils/reflection/ReflectionUtils")) {
            owner = "org/cardboardpowered/util/nms/Ref";
        }
        
        if (owner.equals("protocolsupport/utils/reflection/FieldWriter")) {
            owner = "org/cardboardpowered/util/nms/FieldWriter";
        }

        if (owner.equalsIgnoreCase("com/comphenix/protocol/utility/MinecraftReflection")) {
            // System.out.println("PROTOCOLLIB REFLECTION: " + name);
            if (name.equals("getCraftBukkitClass") || name.equals("getMinecraftClass")) {
                super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ProtocolLibMapper", name, desc, false );
                return;
            }
        }

        if (owner.equalsIgnoreCase("com/comphenix/protocol/injector/netty/ChannelInjector")) {
            if (name.equals("guessCompression")) {
                super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ProtocolLibMapper", name, desc, false );
                return;
            }
        }
        
        if (owner.equalsIgnoreCase("com/sk89q/worldguard/bukkit/util/Materials")) {
            if (name.equals("isSpawnEgg") || name.equals("getEntitySpawnEgg") || name.equals("isArmor") ||
                    name.equals("isToolApplicable") || name.equals("isWaxedCopper")) {
                super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/WorldGuardMaterialHelper", name, desc, false );
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
            super.visitMethodInsn(Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "mapClassName", "(Ljava/lang/String;)Ljava/lang/String;", false);
        
        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getMethods")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getMethods", "(Ljava/lang/Class;)[Ljava/lang/reflect/Method;", false );
            return;
        }
        /*
        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getField") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/reflect/Field;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getFieldByName", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false );
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getDeclaredField") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/reflect/Field;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getDeclaredFieldByName", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Field;", false );
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getMethod") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/reflect/Method;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getMethodByName", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Method;", false );
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getDeclaredMethod") && desc.equalsIgnoreCase("(Ljava/lang/String;)Ljava/lang/reflect/Method;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getDeclaredMethodByName", "(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/reflect/Method;", false );
            return;
        }

        //this.getClass().getCanonicalName();
        if (owner.equalsIgnoreCase("java/lang/Package") && name.equalsIgnoreCase("getName") && desc.equalsIgnoreCase("()Ljava/lang/String;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getPackageName", "(Ljava/lang/Package;)Ljava/lang/String;", false);
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getName") && desc.equalsIgnoreCase("()Ljava/lang/String;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getClassName", "(Ljava/lang/Class;)Ljava/lang/String;", false);
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getCanonicalName") && desc.equalsIgnoreCase("()Ljava/lang/String;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getCanonicalName", "(Ljava/lang/Class;)Ljava/lang/String;", false);
            return;
        }
 
        /*if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getMethod") && desc.equalsIgnoreCase("(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getMethodByName", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false );
            return;
        }

        if (owner.equalsIgnoreCase("java/lang/Class") && name.equalsIgnoreCase("getDeclaredMethod") && desc.equalsIgnoreCase("(Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;")) {
            super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getDeclaredMethodByName", "(Ljava/lang/Class;Ljava/lang/String;[Ljava/lang/Class;)Ljava/lang/reflect/Method;", false );
            return;
        }*/

        if (owner.startsWith("net/minecraft/class_")) {
            // if (!name.startsWith("method_"))
            //    name = MappingsReader.METHODS2.getOrDefault(name + desc, MappingsReader.getIntermedMethod(owner.replace('/', '.'), name));

            if (owner.equalsIgnoreCase("net/minecraft/class_3176") && name.equalsIgnoreCase("getVersion")) {
                // Add MinecraftServer#getVersion
                super.visitMethodInsn( Opcodes.INVOKESTATIC, "org/cardboardpowered/util/nms/ReflectionRemapper", "getMinecraftServerVersion", "()Ljava/lang/String;", false);
                return;
            }
        }
        
        owner = Commodore.getOriginalOrRewrite(owner);

        super.visitMethodInsn( opcode, owner, name, desc, itf );
    }

    private String fixName(String name) {
        String r = name.replace("boolean;", "Z").replace("byte;", "B").replace("double;", "D").replace("float;", "F").replace("int;", "I")
                .replace("long;", "J").replace("short;", "S").replace('.','/').replace("Lvoid","");

        if (r.length() > 3)
            r = "L" + r;
        return r;
    }

}
