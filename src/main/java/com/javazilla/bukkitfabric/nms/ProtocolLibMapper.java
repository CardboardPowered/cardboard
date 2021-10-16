package com.javazilla.bukkitfabric.nms;

import com.google.common.base.Joiner;
import io.netty.channel.ChannelHandler;

/**
 * Some ProtocolLib methods are redirected to this
 * class to avoid our currently trash mapping system.
 *
 * @see https://github.com/dmulloy2/ProtocolLib/issues/1146
 * @see https://github.com/CardboardPowered/cardboard/issues/82
 * @see https://github.com/CardboardPowered/cardboard/issues/120
 * @see https://github.com/CardboardPowered/cardboard/issues/173
 */
public class ProtocolLibMapper {

    /**
     * ProtocolLib relies solely on the channel's class name containing
     * "Compressor" or "Decompressor". This causes DecoderExceptions on
     * Fabric due to the intermediary name not being the same as Spigot.
     *
     * spigot=PacketDecompressor, intermediary=class_2532, yarn=PacketInflater
     * spigot=PacketCompressor,   intermediary=class_2534, yarn=PacketDeflater
     *
     * Original Javadoc: Determine if the given object is a compressor or decompressor.
     *
     * @see https://mapping.javazilla.com/
     * @see https://github.com/Mohist-Community/Mohist/commit/efe7cbfd7b8d36c4f55bed9f16bedea2797dfa9f
     * @see com.comphenix.protocol.injector.netty.ChannelInjector (Line 423; ProtocolLib v4.6.0)
     */
	public static boolean guessCompression(ChannelHandler handler) {
		String className = handler != null ? handler.getClass().getCanonicalName() : "";
        String[] names = {
            "Inflater", "Deflater",         // Yarn          (Fabric in dev-environment)
            "class_2532", "class_2534",     // Intermediary  (Fabric in production)
            "Compressor", "Decompressor"    // Spigot        (for backwards compatibility)
        };
		for (String name : names) {
            if (className.contains(name)) {
                return true;
            }
        }
        return false;
	}

    /**
     * ProtocolLib Reflection
     * Original Javadoc: 
     *  Retrieve the class object of a specific CraftBukkit class.
     */
    public static Class<?> getCraftBukkitClass(String className) {
        try {
            return Class.forName(ReflectionRemapper.mapClassName("org.bukkit.craftbukkit." + className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find CraftBukkit class!!!!: " + className);
        }
    }

    /**
     * ProtocolLib Reflection
     * Original Javadoc:
     *   Retrieve the class object of a specific Minecraft class.
     */
    public static Class<?> getMinecraftClass(String className) {
        try {
            if (className.equals("ServerConnection")) {
                return Class.forName(ReflectionRemapper.mapClassName("net.minecraft.server.ServerNetworkIo"));
            }
            return Class.forName(ReflectionRemapper.mapClassName("net.minecraft.server.v1_17_R1." + className));
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Failed to find Minecraft class!!!!: " + className);
        }
    }

    /**
     * ProtocolLib Reflection
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
                }
            }

            if (success != null) {
                return success;
            } else {
                throw new RuntimeException(String.format("Unable to find " + className + " (%s)", Joiner.on(", ").join(aliases)));
            }
        }
    }

}