package com.javazilla.bukkitfabric.nms;

import com.google.common.base.Joiner;

public class ProtocolLibMapper {

    /**
     * ProtocolLib
     */
    public static Class<?> getCraftBukkitClass(String className) {
        try {
            System.out.println("CARDBOARD PLCB MAP: " + className + " / " + ReflectionRemapper.mapClassName("net.minecraft.server.v1_16_R3." + className));
            return Class.forName(ReflectionRemapper.mapClassName("org.bukkit.craftbukkit." + className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find CraftBukkit class!!!!: " + className);
        }
    }

    /**
     * ProtocolLib
     */
    public static Class<?> getMinecraftClass(String className) {
        try {
            if (!className.equals("Packet")) {
                System.out.println("CARDBOARD PL MAP: " + className + " / " + ReflectionRemapper.mapClassName("net.minecraft.server.v1_16_R3." + className));
            }
            if (className.equals("ServerConnection")) {
                return Class.forName(ReflectionRemapper.mapClassName("net.minecraft.server.ServerNetworkIo"));
            }
            return Class.forName(ReflectionRemapper.mapClassName("net.minecraft.server.v1_16_R3." + className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find Minecraft class!!!!: " + className);
        }
    }

    /**
     * ProtocolLib
     */
    public static Class<?> getMinecraftClass(String className, String... aliases) {
        try {
            // Try the main class first
            return getMinecraftClass(className);
        } catch (RuntimeException e) {
            Class<?> success = null;

            // Try every alias too
            for (String alias : aliases) {
                try {
                    success = getMinecraftClass(alias);
                    break;
                } catch (RuntimeException e1) {
                    // e1.printStackTrace();
                }
            }

            if (success != null) {
                // Save it for later
                // minecraftPackage.setPackageClass(className, success);
                return success;
            } else {
                // Hack failed
                throw new RuntimeException(String.format("Unable to find %s (%s)", className, Joiner.on(", ").join(aliases)));
            }
        }
    }

}