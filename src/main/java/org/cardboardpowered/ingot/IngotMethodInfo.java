package org.cardboardpowered.ingot;

import java.util.function.UnaryOperator;

import org.cardboardpowered.ingot.srglib.ArrayType;
import org.cardboardpowered.ingot.srglib.JavaType;
import org.cardboardpowered.ingot.srglib.MethodSignature;
import org.cardboardpowered.ingot.srglib.ReferenceType;

import net.fabricmc.loader.api.FabricLoader;

/**
 * Class containing info about a method.
 * <p>
 *   - Method name in official, spigot, and fabric<br>
 *   - Method descriptor in official, spigot and fabric<br>
 *   - Providing class name in spigot and fabric.<br>
 * </p>
 */
public class IngotMethodInfo {

    public String obfMethodName;
    public String obfDescriptor;

    public String spigotClassName;
    public String spigotDescriptor;
    public String spigotMethodName;

    public String fabricClassName;
    public String fabricMethodName;
    public String fabricDescriptor;

    public static String asFabricDescriptor(String spigotDescriptor) {
        MethodSignature si2 = MethodSignature.fromDescriptor(spigotDescriptor);
        si2 = si2.mapTypes(new UnaryOperator<JavaType>() {
            @Override
            public JavaType apply(JavaType t) {
                if (t instanceof ReferenceType)
                    return new ReferenceType(IngotReader.classes_S2F.getOrDefault("net.minecraft.server." + t.getName(), t.getName()));
                if (t instanceof ArrayType) {
                    ArrayType at = (ArrayType)t;
                    if (at.getElementType() instanceof ReferenceType)
                        return new ArrayType(new ReferenceType(IngotReader.classes_S2F.get("net.minecraft.server." + t.getElementType().getName())));
                }
                return t;
            }});
        return si2.getDescriptor();
    }

    public IngotMethodInfo(String line) {
        String[] spl = line.split(" ");

        this.obfMethodName = spl[1];

        this.spigotClassName = spl[0].startsWith("net/minecraft") ? spl[0].replace('/', '.') : "net.minecraft.server." + spl[0];
        this.spigotDescriptor = spl[2];
        this.spigotMethodName = spl[3];

        this.fabricClassName = IngotReader.classes_S2F.getOrDefault(this.spigotClassName, this.spigotClassName);
        MethodSignature si = MethodSignature.fromDescriptor(this.spigotDescriptor);
        si = si.mapTypes(new UnaryOperator<JavaType>() {
            @Override
            public JavaType apply(JavaType t) {
                if (t instanceof ReferenceType)
                    return new ReferenceType(IngotReader.classes_S2O.getOrDefault("net.minecraft.server." + t.getName(), t.getName()));
                if (t instanceof ArrayType) {
                    ArrayType at = (ArrayType)t;
                    if (at.getElementType() instanceof ReferenceType)
                        return new ArrayType(new ReferenceType(IngotReader.classes_S2O.get("net.minecraft.server." + t.getElementType().getName())));
                }
                return t;
            }});
        this.obfDescriptor = si.getDescriptor();

        MethodSignature si2 = MethodSignature.fromDescriptor(this.spigotDescriptor);
        si2 = si2.mapTypes(new UnaryOperator<JavaType>() {
            @Override
            public JavaType apply(JavaType t) {
                if (t instanceof ReferenceType)
                    return new ReferenceType(IngotReader.classes_S2F.getOrDefault("net.minecraft.server." + t.getName(), t.getName()));
                if (t instanceof ArrayType) {
                    ArrayType at = (ArrayType)t;
                    if (at.getElementType() instanceof ReferenceType)
                        return new ArrayType(new ReferenceType(IngotReader.classes_S2F.get("net.minecraft.server." + t.getElementType().getName())));
                }
                return t;
            }});
        this.fabricDescriptor = si2.getDescriptor();

        this.fabricMethodName = FabricLoader.getInstance().getMappingResolver().mapMethodName("official",
                this.fabricClassName,
                this.spigotMethodName,
                this.obfDescriptor);
    }

}