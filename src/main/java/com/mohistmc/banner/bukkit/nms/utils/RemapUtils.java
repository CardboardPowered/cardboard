package com.mohistmc.banner.bukkit.nms.utils;

import com.mohistmc.banner.bukkit.nms.model.ClassMapping;
import com.mohistmc.banner.bukkit.nms.remappers.BannerInheritanceProvider;
import com.mohistmc.banner.bukkit.nms.remappers.BannerJarMapping;
import com.mohistmc.banner.bukkit.nms.remappers.BannerJarRemapper;
import com.mohistmc.banner.bukkit.nms.remappers.BannerSuperClassRemapper;
import com.mohistmc.banner.bukkit.nms.remappers.ClassRemapperSupplier;
import com.mohistmc.banner.bukkit.nms.remappers.ReflectMethodRemapper;
import com.mohistmc.banner.bukkit.nms.remappers.ReflectRemapper;
import net.md_5.specialsource.InheritanceMap;
import net.md_5.specialsource.provider.JointProvider;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Type;
import org.objectweb.asm.commons.ClassRemapper;
import org.objectweb.asm.commons.Remapper;
import org.objectweb.asm.tree.ClassNode;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.invoke.MethodType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author pyz
 * @date 2019/6/30 11:50 PM
 */
public class RemapUtils {

    public static BannerJarMapping jarMapping;
    public static BannerJarRemapper jarRemapper;
    private static final List<Remapper> remappers = new ArrayList<>();

    public static void init() {
    	System.out.println("REMAP UTIL DEBUG");
        jarMapping = new BannerJarMapping();
        // v1_20_R1
        jarMapping.packages.put("org/bukkit/craftbukkit/v1_20_R3/", "org/bukkit/craftbukkit/");
        //jarMapping.packages.put("org/bukkit/craftbukkit/v1_19_R3/", "org/bukkit/craftbukkit/");
        jarMapping.packages.put("org/bukkit/craftbukkit/libs/it/unimi/dsi/fastutil/", "it/unimi/dsi/fastutil/");
        jarMapping.packages.put("org/bukkit/craftbukkit/libs/jline/", "jline/");
        jarMapping.packages.put("org/bukkit/craftbukkit/libs/org/apache/commons/", "org/apache/commons/");
        jarMapping.packages.put("org/bukkit/craftbukkit/libs/org/objectweb/asm/", "org/objectweb/asm/");
        // jarMapping.setInheritanceMap(new BannerInheritanceMap());
        // jarMapping.setFallbackInheritanceProvider(new BannerInheritanceProvider());

        try {
            jarMapping.loadMappings(
                    new BufferedReader(new InputStreamReader(RemapUtils.class.getClassLoader().getResourceAsStream("mappings/spigot2srg-1.20.2.srg"))),
                    null,
                    null, false);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        JointProvider provider = new JointProvider();
        provider.add(new BannerInheritanceProvider());
        jarMapping.setInheritanceMap(new InheritanceMap());
        jarMapping.setFallbackInheritanceProvider(provider);
        jarRemapper = new BannerJarRemapper(jarMapping);
        remappers.add(jarRemapper);
        remappers.add(new ReflectRemapper());
        jarMapping.initFastMethodMapping(jarRemapper);
        ReflectMethodRemapper.init();

        try {
            Class.forName("com.mohistmc.banner.bukkit.nms.proxy.ProxyMethodHandlesLookup");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }


    public static byte[] remapFindClass(byte[] bs) {
        ClassReader reader = new ClassReader(bs); // Turn from bytes into visitor
        ClassNode classNode = new ClassNode();
        reader.accept(classNode, ClassReader.EXPAND_FRAMES);
        for (Remapper remapper : remappers) {

            ClassNode container = new ClassNode();
            ClassRemapper classRemapper;
            if (remapper instanceof ClassRemapperSupplier) {
                classRemapper = ((ClassRemapperSupplier) remapper).getClassRemapper(container);
            } else {
                classRemapper = new ClassRemapper(container, remapper);
            }
            classNode.accept(classRemapper);
            classNode = container;
        }
        BannerSuperClassRemapper.init(classNode);
        ClassWriter writer = new ClassWriter(0);
        classNode.accept(writer);
        return writer.toByteArray();

    }

    public static String map(String typeName) {
        typeName = mapPackage(typeName);
        return jarMapping.classes.getOrDefault(typeName, typeName);
    }

    public static String reverseMap(String typeName) {
        ClassMapping mapping = jarMapping.byNMSInternalName.get(typeName);
        return mapping == null ? typeName : mapping.getNmsSrcName();
    }

    public static String reverseMap(Class<?> clazz) {
        ClassMapping mapping = jarMapping.byMCPName.get(clazz.getName());
        return mapping == null ? ASMUtils.toInternalName(clazz) : mapping.getNmsSrcName();
    }

    public static String mapPackage(String typeName) {
        for (Map.Entry<String, String> entry : jarMapping.packages.entrySet()) {
            String prefix = entry.getKey();
            if (typeName.startsWith(prefix)) {
                return entry.getValue() + typeName.substring(prefix.length());
            }
        }
        return typeName;
    }

    public static String remapMethodDesc(String methodDescriptor) {
        Type rt = Type.getReturnType(methodDescriptor);
        Type[] ts = Type.getArgumentTypes(methodDescriptor);
        rt = Type.getType(ASMUtils.toDescriptorV2(map(ASMUtils.getInternalName(rt))));
        for (int i = 0; i < ts.length; i++) {
            ts[i] = Type.getType(ASMUtils.toDescriptorV2(map(ASMUtils.getInternalName(ts[i]))));
        }
        return Type.getMethodType(rt, ts).getDescriptor();
    }

    public static String mapMethodName(Class<?> clazz, String name, MethodType methodType) {
        return mapMethodName(clazz, name, methodType.parameterArray());
    }

    public static String mapMethodName(Class<?> type, String name, Class<?>... parameterTypes) {
        return jarMapping.fastMapMethodName(type, name, parameterTypes);
    }

    public static String inverseMapMethodName(Class<?> type, String name, Class<?>... parameterTypes) {
        return jarMapping.fastReverseMapMethodName(type, name, parameterTypes);
    }

    public static String mapFieldName(Class<?> type, String fieldName) {
        String key = reverseMap(type) + "/" + fieldName;
        String mapped = jarMapping.fields.get(key);
        if (mapped == null) {
            Class<?> superClass = type.getSuperclass();
            if (superClass != null) {
                mapped = mapFieldName(superClass, fieldName);
            }
        }
        return mapped != null ? mapped : fieldName;
    }

    public static String inverseMapFieldName(Class<?> type, String fieldName) {
        return jarMapping.fastReverseMapFieldName(type, fieldName);
    }

    public static String inverseMapName(Class<?> clazz) {
        ClassMapping mapping = jarMapping.byMCPName.get(clazz.getName());
        return mapping == null ? clazz.getName() : mapping.getNmsName();
    }

    public static String inverseMapSimpleName(Class<?> clazz) {
        ClassMapping mapping = jarMapping.byMCPName.get(clazz.getName());
        return mapping == null ? clazz.getSimpleName() : mapping.getNmsSimpleName();
    }

    public static boolean isNMSClass(String className) {
        return className.startsWith("net.minecraft.");
    }

    public static boolean needRemap(String className){
        return className.startsWith("net.minecraft.");
    }
}
