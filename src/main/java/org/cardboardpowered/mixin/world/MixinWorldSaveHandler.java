package org.cardboardpowered.mixin.world;

import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorldSaveHandler;
import com.mojang.datafixers.DataFixer;
import net.minecraft.datafixer.DataFixTypes;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.WorldSaveHandler;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

@Mixin(value = WorldSaveHandler.class, priority = 999)
public class MixinWorldSaveHandler implements IMixinWorldSaveHandler {

    @Shadow
    @Final
    private File playerDataDir;

    @Shadow
    @Final
    protected DataFixer dataFixer;

    /**
     * @reason Spigot Offline UUID
     * @author Cardboard
     */
    @Overwrite
    @Nullable
    public NbtCompound loadPlayerData(PlayerEntity player) {
        NbtCompound lv = null;
        try {
            File file = new File(this.playerDataDir, player.getUuidAsString() + ".dat");
            if (file.exists() && file.isFile()) {
                lv = NbtIo.readCompressed(file);
            }
        } catch (Exception exception) {
        	BukkitFabricMod.LOGGER.warning("Failed to load player data for " + player.getName().getString());
        }
        if (lv != null) {
        	// Cardboard Start
        	if (player instanceof ServerPlayerEntity) {
                PlayerImpl craftPlayer = (PlayerImpl) ((IMixinServerEntityPlayer)player).getBukkitEntity();
                // Only update first played if it is older than the one we have
                long modified = new File(this.playerDataDir, player.getUuid() + ".dat").lastModified();
                if (modified < craftPlayer.getFirstPlayed()) {
                    craftPlayer.setFirstPlayed(modified);
                }
            }
        	// Cardboard End
            int i = NbtHelper.getDataVersion(lv, -1);
            player.readNbt(DataFixTypes.PLAYER.update(this.dataFixer, lv, i));
        }
        return lv;
    }
    
    /**
     * @reason Spigot Offline UUID
     * @author BukkitFabric
     *
    // @Overwrite
    public NbtCompound loadPlayerData_(PlayerEntity entityhuman) {
        NbtCompound nbttagcompound = null;

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
                PlayerImpl player = (PlayerImpl) ((IMixinServerEntityPlayer)entityhuman).getBukkitEntity();
                // Only update first played if it is older than the one we have
                long modified = new File(this.playerDataDir, entityhuman.getUuid().toString() + ".dat").lastModified();
                if (modified < player.getFirstPlayed()) {
                    player.setFirstPlayed(modified);
                }
            }
            // CraftBukkit end
            int i = nbttagcompound.contains("DataVersion", 3) ? nbttagcompound.getInt("DataVersion") : -1;

            entityhuman.readNbt(NbtHelper.update(this.dataFixer, DataFixTypes.PLAYER, nbttagcompound, i));
        }

        return nbttagcompound;
    }*/

    @SuppressWarnings("resource")
    @Override
    public NbtCompound getPlayerData(String s) {
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
