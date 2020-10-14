package com.javazilla.bukkitfabric.mixin.world;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorldSaveHandler;
import com.mojang.datafixers.DataFixer;

import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.WorldSaveHandler;

@Mixin(WorldSaveHandler.class)
public class MixinWorldSaveHandler implements IMixinWorldSaveHandler {

    @Shadow
    @Final
    private File playerDataDir;

    @Shadow
    @Final
    protected DataFixer dataFixer;

    /**
     * @reason Spigot Offline UUID
     * @author BukkitFabric
     */
    @Overwrite
    public CompoundTag loadPlayerData(PlayerEntity entityhuman) {
        CompoundTag nbttagcompound = null;

        try {
            File file = new File(this.playerDataDir, entityhuman.getUuidAsString() + ".dat");
            // Spigot Start
            boolean usingWrongFile = false;
            if (!file.exists()) {
                file = new File( this.playerDataDir, java.util.UUID.nameUUIDFromBytes(("OfflinePlayer:" + entityhuman.getEntityName()).getBytes(StandardCharsets.UTF_8)).toString() + ".dat");
                if (file.exists()) {
                    usingWrongFile = true;
                    org.bukkit.Bukkit.getServer().getLogger().warning("Using offline mode UUID file for player " + entityhuman.getEntityName() + " as it is the only copy we can find");
                }
            } // Spigot End

            if (file.exists() && file.isFile())
                nbttagcompound = NbtIo.readCompressed(file);

            if (usingWrongFile) // Spigot
                file.renameTo(new File(file.getPath() + ".offline-read")); // Spigot
        } catch (Exception exception) {
            BukkitFabricMod.LOGGER.warning("Failed to load player data for " + entityhuman.getName().getString());
        }

        if (nbttagcompound != null) {
            // CraftBukkit start
            if (entityhuman instanceof ServerPlayerEntity) {
                CraftPlayer player = (CraftPlayer) ((IMixinServerEntityPlayer)entityhuman).getBukkitEntity();
                // Only update first played if it is older than the one we have
                long modified = new File(this.playerDataDir, entityhuman.getUuid().toString() + ".dat").lastModified();
                if (modified < player.getFirstPlayed()) {
                    player.setFirstPlayed(modified);
                }
            }
            // CraftBukkit end
            int i = nbttagcompound.contains("DataVersion", 3) ? nbttagcompound.getInt("DataVersion") : -1;

            entityhuman.fromTag(NbtHelper.update(this.dataFixer, DataFixTypes.PLAYER, nbttagcompound, i));
        }

        return nbttagcompound;
    }

    @SuppressWarnings("resource")
    @Override
    public CompoundTag getPlayerData(String s) {
        try {
            File file1 = new File(this.playerDataDir, s + ".dat");
            if (file1.exists())
                return NbtIo.readCompressed((InputStream) (new FileInputStream(file1)));
        } catch (Exception exception) {
            BukkitFabricMod.LOGGER.warning("Failed to load player data for " + s);
        }

        return null;
    }

}
