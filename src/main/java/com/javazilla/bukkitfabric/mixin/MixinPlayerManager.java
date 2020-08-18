package com.javazilla.bukkitfabric.mixin;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Location;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.CraftWorld;
import org.bukkit.entity.Player;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayNetworkHandler;
import com.javazilla.bukkitfabric.interfaces.IMixinPlayerManager;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldProperties;
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
    public ServerPlayerEntity moveToWorld(ServerPlayerEntity entityplayer, DimensionType dimensionmanager, boolean flag, Location location, boolean avoidSuffocation) {
        entityplayer.stopRiding(); // CraftBukkit
        this.players.remove(entityplayer);
        entityplayer.getServerWorld().removePlayer(entityplayer);
        BlockPos blockposition = entityplayer.getSpawnPointPosition();
        boolean flag1 = entityplayer.isSpawnPointSet();

        ServerPlayerEntity entityplayer1 = entityplayer;
        org.bukkit.World fromWorld = ((Player)((IMixinServerEntityPlayer)entityplayer).getBukkitEntity()).getWorld();
        entityplayer.notInAnyWorld = false;
        // CraftBukkit end

        entityplayer1.networkHandler = entityplayer.networkHandler;
        entityplayer1.copyFrom(entityplayer, flag);
        entityplayer1.setEntityId(entityplayer.getEntityId());
        entityplayer1.setMainArm(entityplayer.getMainArm());
        Iterator<String> iterator = entityplayer.getScoreboardTags().iterator();

        while (iterator.hasNext())
            entityplayer1.addScoreboardTag(iterator.next());

        // CraftBukkit start - fire PlayerRespawnEvent
        if (location == null) {
            boolean isBedSpawn = false;

            CraftWorld cworld = ((IMixinWorld)(Object)entityplayer.world).getCraftWorld();

            entityplayer1.moveToSpawn((ServerWorld) entityplayer.world);

            Player respawnPlayer = CraftServer.INSTANCE.getPlayer(entityplayer1);
            PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(respawnPlayer, location, isBedSpawn);
            CraftServer.INSTANCE.getPluginManager().callEvent(respawnEvent);

            location = respawnEvent.getRespawnLocation();
            if (!flag) ((IMixinServerEntityPlayer)(Object)entityplayer).reset(); // SPIGOT-4785
        } // TODO else location.setWorld(((IMixinWorld)(Object)CraftServer.server.getWorld(dimensionmanager)).getCraftWorld());

        ServerWorld worldserver = (ServerWorld) ((CraftWorld) location.getWorld()).getHandle();

        while (avoidSuffocation && !worldserver.doesNotCollide(entityplayer1) && entityplayer1.getY() < 256.0D)
            entityplayer1.updatePosition(entityplayer1.getX(), entityplayer1.getY() + 1.0D, entityplayer1.getZ());

        // CraftBukkit start - Force the client to refresh their chunk cache
        if (fromWorld.getEnvironment() == ((IMixinWorld)(Object)worldserver).getCraftWorld().getEnvironment())
            entityplayer1.networkHandler.sendPacket(new PlayerRespawnS2CPacket());

        WorldProperties worlddata = worldserver.getLevelProperties();

        entityplayer1.networkHandler.sendPacket(new PlayerRespawnS2CPacket()); // TODO
        entityplayer1.setWorld(worldserver);
        entityplayer1.removed = false;
        ((IMixinPlayNetworkHandler)(Object)entityplayer1.networkHandler).teleport(new Location(((IMixinWorld)(Object)worldserver).getCraftWorld(), entityplayer1.getX(), entityplayer1.getY(), entityplayer1.getZ(), entityplayer1.yaw, entityplayer1.pitch));
        entityplayer1.setSneaking(false);
        BlockPos blockposition1 = worldserver.getSpawnPos();

        entityplayer1.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(blockposition1, 0));
        entityplayer1.networkHandler.sendPacket(new DifficultyS2CPacket(worlddata.getDifficulty(), worlddata.isDifficultyLocked()));
        entityplayer1.networkHandler.sendPacket(new ExperienceBarUpdateS2CPacket(entityplayer1.experienceProgress, entityplayer1.totalExperience, entityplayer1.experienceLevel));
        this.sendWorldInfo(entityplayer1, worldserver);
        this.sendCommandTree(entityplayer1);
        if (!((IMixinPlayNetworkHandler)(Object)entityplayer.networkHandler).isDisconnected()) {
            worldserver.onPlayerRespawned(entityplayer1);
            this.players.add(entityplayer1);
            this.playerMap.put(entityplayer1.getUuid(), entityplayer1);
        }
        entityplayer1.setHealth(entityplayer1.getHealth());

        sendPlayerStatus(entityplayer);
        entityplayer.sendAbilitiesUpdate();
        for (Object o1 : entityplayer.getStatusEffects()) {
            StatusEffectInstance mobEffect = (StatusEffectInstance) o1;
            entityplayer.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(entityplayer.getEntityId(), mobEffect));
        }

        // TODO BUKKITFABRIC 1.16.2 entityplayer.worldChanged((ServerWorld) ((CraftWorld) fromWorld).getHandle());

        if (fromWorld != location.getWorld())
            CraftServer.INSTANCE.getPluginManager().callEvent(new PlayerChangedWorldEvent((Player) ((IMixinEntity)(Object)entityplayer).getBukkitEntity(), fromWorld));

        if (((IMixinPlayNetworkHandler)(Object)entityplayer.networkHandler).isDisconnected())
            this.savePlayerData(entityplayer);

        return entityplayer1;
    }

}
