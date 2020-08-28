package com.javazilla.bukkitfabric.mixin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayNetworkHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayerManager;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.s2c.play.ChunkLoadDistanceS2CPacket;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.tag.BlockTags;
import net.minecraft.tag.Tag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;
import net.minecraft.world.WorldProperties;
import net.minecraft.world.biome.source.BiomeAccess;
import net.minecraft.world.dimension.DimensionType;

@Mixin(PlayerManager.class)
public abstract class MixinPlayerManager implements IMixinPlayerManager {

    @Shadow
    public List<ServerPlayerEntity> players;

    @Shadow
    public abstract void sendCommandTree(ServerPlayerEntity player);

    @Shadow
    public abstract void sendWorldInfo(ServerPlayerEntity player, ServerWorld world);

    @Shadow
    public abstract void savePlayerData(ServerPlayerEntity player);

    @Shadow
    public abstract void sendPlayerStatus(ServerPlayerEntity player);

    @Shadow
    public Map<UUID, ServerPlayerEntity> playerMap;

    @Override
    public ServerPlayerEntity moveToWorld(ServerPlayerEntity entityplayer, ServerWorld worldserver, boolean flag, Location location, boolean avoidSuffocation) {
        entityplayer.stopRiding(); // CraftBukkit
        this.players.remove(entityplayer);
        entityplayer.getServerWorld().removePlayer(entityplayer);

        BlockPos blockposition = entityplayer.getSpawnPointPosition();
        float f = entityplayer.getSpawnAngle();
        boolean flag1 = entityplayer.isSpawnPointSet();

        org.bukkit.World fromWorld = ((IMixinServerEntityPlayer)entityplayer).getBukkitEntity().getWorld();
        entityplayer.notInAnyWorld = false;
        // CraftBukkit end

        Iterator iterator = entityplayer.getScoreboardTags().iterator();

        while (iterator.hasNext()) {
            String s = (String) iterator.next();
            entityplayer.addScoreboardTag(s);
        }

        boolean flag2 = false;

        // CraftBukkit start - fire PlayerRespawnEvent
        if (location == null) {
            boolean isBedSpawn = false;
            ServerWorld worldserver1 = CraftServer.server.getWorld(entityplayer.getSpawnPointDimension());
            if (worldserver1 != null) {
                Optional optional;

                if (blockposition != null)
                    optional = PlayerEntity.findRespawnPosition(worldserver1, blockposition, f, flag1, flag);
                else optional = Optional.empty();

                if (optional.isPresent()) {
                    BlockState iblockdata = worldserver1.getBlockState(blockposition);
                    boolean flag3 = iblockdata.isOf(Blocks.RESPAWN_ANCHOR);
                    Vec3d vec3d = (Vec3d) optional.get();
                    float f1;

                    if (!iblockdata.isIn((Tag<Block>) BlockTags.BEDS) && !flag3) {
                        f1 = f;
                    } else {
                        Vec3d vec3d1 = Vec3d.ofBottomCenter((Vec3i) blockposition).subtract(vec3d).normalize();
                        f1 = (float) MathHelper.wrapDegrees(MathHelper.atan2(vec3d1.z, vec3d1.x) * 57.2957763671875D - 90.0D);
                    }

                    entityplayer.refreshPositionAndAngles(vec3d.x, vec3d.y, vec3d.z, f1, 0.0F);
                    entityplayer.setSpawnPoint(worldserver1.getRegistryKey(), blockposition, f, flag1, false);
                    flag2 = !flag && flag3;
                    isBedSpawn = true;
                    location = new Location(((IMixinWorld)worldserver1).getCraftWorld(), vec3d.x, vec3d.y, vec3d.z);
                } else if (blockposition != null)
                    entityplayer.networkHandler.sendPacket(new GameStateChangeS2CPacket(GameStateChangeS2CPacket.NO_RESPAWN_BLOCK, 0.0F));
            }

            if (location == null) {
                worldserver1 = CraftServer.server.getWorld(World.OVERWORLD);
                blockposition = entityplayer.getSpawnPointPosition();
                location = new Location(((IMixinWorld)worldserver1).getCraftWorld(), (double) ((float) blockposition.getX() + 0.5F), (double) ((float) blockposition.getY() + 0.1F), (double) ((float) blockposition.getZ() + 0.5F));
            }

            Player respawnPlayer = CraftServer.INSTANCE.getPlayer(entityplayer);
            PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(respawnPlayer, location, isBedSpawn && !flag2, flag2);
            CraftServer.INSTANCE.getPluginManager().callEvent(respawnEvent);

            if (entityplayer.isDisconnected())
                return entityplayer;

            location = respawnEvent.getRespawnLocation();
        } else location.setWorld(((IMixinWorld)worldserver).getCraftWorld());

        while (avoidSuffocation && !worldserver.doesNotCollide(entityplayer) && entityplayer.getY() < 256.0D)
            entityplayer.updatePosition(entityplayer.getX(), entityplayer.getY() + 1.0D, entityplayer.getZ());

        WorldProperties worlddata = worldserver.getLevelProperties();
        entityplayer.networkHandler.sendPacket(new PlayerRespawnS2CPacket(worldserver.getDimension(), worldserver.getRegistryKey(), BiomeAccess.hashSeed(worldserver.getSeed()), entityplayer.interactionManager.getGameMode(), entityplayer.interactionManager.method_30119(), worldserver.isDebugWorld(), worldserver.isFlat(), flag));
        entityplayer.setWorld(worldserver);
        entityplayer.removed = false;
        entityplayer.teleport(worldserver, location.getX(), location.getY(), location.getZ(), entityplayer.yaw, entityplayer.pitch);
        entityplayer.setSneaking(false);

        entityplayer.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(worldserver.getSpawnPos(), worldserver.getSpawnAngle()));
        entityplayer.networkHandler.sendPacket(new DifficultyS2CPacket(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
        entityplayer.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(entityplayer.experienceProgress, entityplayer.totalExperience, entityplayer.experienceLevel));
        this.sendWorldInfo(entityplayer, worldserver);
        this.sendCommandTree(entityplayer);
        if (!entityplayer.isDisconnected()) {
            worldserver.onPlayerRespawned(entityplayer);
            this.players.add(entityplayer);
        }

        entityplayer.setHealth(entityplayer.getHealth());
        if (flag2)
            entityplayer.networkHandler.sendPacket(new PlaySoundS2CPacket(SoundEvents.BLOCK_RESPAWN_ANCHOR_DEPLETE, SoundCategory.BLOCKS, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), 1.0F, 1.0F));

        sendPlayerStatus(entityplayer);
        entityplayer.sendAbilitiesUpdate();
        for (Object o1 : entityplayer.getStatusEffects()) {
            StatusEffectInstance mobEffect = (StatusEffectInstance) o1;
            entityplayer.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(entityplayer.getEntityId(), mobEffect));
        }

        if (fromWorld != location.getWorld()) {
            PlayerChangedWorldEvent event = new PlayerChangedWorldEvent((Player) ((IMixinServerEntityPlayer)entityplayer).getBukkitEntity(), fromWorld);
            CraftServer.INSTANCE.getPluginManager().callEvent(event);
        }

        if (entityplayer.isDisconnected())
            this.savePlayerData(entityplayer);

        return entityplayer;
    }

}