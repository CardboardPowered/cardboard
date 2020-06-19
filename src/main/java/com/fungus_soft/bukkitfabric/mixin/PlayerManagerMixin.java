
package com.fungus_soft.bukkitfabric.mixin;

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
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.fungus_soft.bukkitfabric.interfaces.IMixinPlayerManager;
import com.fungus_soft.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.fungus_soft.bukkitfabric.interfaces.IMixinServerWorld;
import com.fungus_soft.bukkitfabric.interfaces.IMixinBukkitGetter;
import com.fungus_soft.bukkitfabric.interfaces.IMixinEntity;
import com.fungus_soft.bukkitfabric.interfaces.IMixinPlayNetworkHandler;

import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.packet.s2c.play.DifficultyS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityStatusEffectS2CPacket;
import net.minecraft.network.packet.s2c.play.ExperienceBarUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.GameStateChangeS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerRespawnS2CPacket;
import net.minecraft.network.packet.s2c.play.PlayerSpawnPositionS2CPacket;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.level.LevelProperties;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin implements IMixinPlayerManager {

    @Shadow
    public List<ServerPlayerEntity> players;

    @Shadow
    public abstract void sendCommandTree(ServerPlayerEntity player);

    @Shadow
    public abstract void sendWorldInfo(ServerPlayerEntity player, ServerWorld world);

    @Shadow
    public abstract void savePlayerData(ServerPlayerEntity player);

    @Shadow
    public abstract void method_14594(ServerPlayerEntity player);

    @Shadow
    public Map<UUID, ServerPlayerEntity> playerMap;

    @Override
    public ServerPlayerEntity moveToWorld(ServerPlayerEntity entityplayer, DimensionType dimensionmanager, boolean flag, Location location, boolean avoidSuffocation) {
        entityplayer.stopRiding(); // CraftBukkit
        this.players.remove(entityplayer);
        entityplayer.getServerWorld().removePlayer(entityplayer);
        BlockPos blockposition = entityplayer.getSpawnPosition();
        boolean flag1 = entityplayer.isSpawnForced();

        ServerPlayerEntity entityplayer1 = entityplayer;
        org.bukkit.World fromWorld = ((Player)((IMixinBukkitGetter)entityplayer).getBukkitObject()).getWorld();
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

            // TODO Bukkit4Fabric: should be spawn world not current world!
            CraftWorld cworld = ((IMixinServerWorld)(Object)entityplayer.world).getCraftWorld();

            if (cworld != null && blockposition != null) {
                Optional<Vec3d> optional = PlayerEntity.findRespawnPosition(cworld.getHandle(), blockposition, flag1);

                if (optional.isPresent()) {
                    Vec3d vec3d = (Vec3d) optional.get();

                    isBedSpawn = true;
                    location = new Location(cworld, vec3d.x, vec3d.y, vec3d.z);
                } else {
                    entityplayer1.setPlayerSpawn(null, true, false);
                    entityplayer1.networkHandler.sendPacket(new GameStateChangeS2CPacket(0, 0.0F));
                }
            }

            if (location == null) {
                cworld = (CraftWorld) CraftServer.INSTANCE.getWorlds().get(0);
                blockposition = ((IMixinServerEntityPlayer)(Object)entityplayer1).getSpawnPoint(cworld.getHandle());
                location = new Location(cworld, (float) blockposition.getX() + 0.5F, (double) ((float) blockposition.getY() + 0.1F), (double) ((float) blockposition.getZ() + 0.5F));
            }

            Player respawnPlayer = CraftServer.INSTANCE.getPlayer(entityplayer1);
            PlayerRespawnEvent respawnEvent = new PlayerRespawnEvent(respawnPlayer, location, isBedSpawn);
            CraftServer.INSTANCE.getPluginManager().callEvent(respawnEvent);

            location = respawnEvent.getRespawnLocation();
            if (!flag) ((IMixinServerEntityPlayer)(Object)entityplayer).reset(); // SPIGOT-4785
        } else location.setWorld(((IMixinServerWorld)(Object)CraftServer.server.getWorld(dimensionmanager)).getCraftWorld());

        ServerWorld worldserver = (ServerWorld) ((CraftWorld) location.getWorld()).getHandle();

        while (avoidSuffocation && !worldserver.doesNotCollide(entityplayer1) && entityplayer1.getY() < 256.0D)
            entityplayer1.updatePosition(entityplayer1.getX(), entityplayer1.getY() + 1.0D, entityplayer1.getZ());

        // CraftBukkit start - Force the client to refresh their chunk cache
        if (fromWorld.getEnvironment() == ((IMixinServerWorld)(Object)worldserver).getCraftWorld().getEnvironment())
            entityplayer1.networkHandler.sendPacket(new PlayerRespawnS2CPacket(worldserver.dimension.getType().getRawId() >= 0 ? DimensionType.THE_NETHER : DimensionType.OVERWORLD, LevelProperties.sha256Hash(worldserver.getLevelProperties().getSeed()), worldserver.getLevelProperties().getGeneratorType(), entityplayer.interactionManager.getGameMode()));

        LevelProperties worlddata = worldserver.getLevelProperties();

        entityplayer1.networkHandler.sendPacket(new PlayerRespawnS2CPacket(worldserver.dimension.getType(), LevelProperties.sha256Hash(worldserver.getLevelProperties().getSeed()), worldserver.getLevelProperties().getGeneratorType(), entityplayer1.interactionManager.getGameMode()));
        entityplayer1.setWorld(worldserver);
        entityplayer1.removed = false;
        ((IMixinPlayNetworkHandler)(Object)entityplayer1.networkHandler).teleport(new Location(((IMixinServerWorld)(Object)worldserver).getCraftWorld(), entityplayer1.getX(), entityplayer1.getY(), entityplayer1.getZ(), entityplayer1.yaw, entityplayer1.pitch));
        entityplayer1.setSneaking(false);
        BlockPos blockposition1 = worldserver.getSpawnPos();

        entityplayer1.networkHandler.sendPacket(new PlayerSpawnPositionS2CPacket(blockposition1));
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

        method_14594(entityplayer);
        entityplayer.sendAbilitiesUpdate();
        for (Object o1 : entityplayer.getStatusEffects()) {
            StatusEffectInstance mobEffect = (StatusEffectInstance) o1;
            entityplayer.networkHandler.sendPacket(new EntityStatusEffectS2CPacket(entityplayer.getEntityId(), mobEffect));
        }

        entityplayer.dimensionChanged((ServerWorld) ((CraftWorld) fromWorld).getHandle());

        if (fromWorld != location.getWorld())
            CraftServer.INSTANCE.getPluginManager().callEvent(new PlayerChangedWorldEvent((Player) ((IMixinEntity)(Object)entityplayer).getBukkitEntity(), fromWorld));

        if (((IMixinPlayNetworkHandler)(Object)entityplayer.networkHandler).isDisconnected())
            this.savePlayerData(entityplayer);

        return entityplayer1;
    }

}
