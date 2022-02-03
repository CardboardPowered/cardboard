package org.cardboardpowered.mixin.entity;

import java.util.Random;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.cardboardpowered.impl.world.WorldImpl;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.destroystokyo.paper.event.entity.ThrownEggHatchEvent;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.hit.HitResult;
import net.minecraft.world.World;

@MixinInfo(events = {"ThrownEggHatchEvent", "PlayerEggThrowEvent"})
@Mixin(value = EggEntity.class, priority = 999)
public abstract class MixinEggEntity {

    private final Random random = new Random();

    @Inject(at = @At(shift = Shift.AFTER, value = "HEAD"), method = "onCollision", cancellable = true)
    public void cardboard_doEggThrowEvent(HitResult res, CallbackInfo ci) {
        EggEntity egg = (EggEntity)(Object)this;
        World world = egg.world;

        if (!world.isClient) {
            boolean hatching = this.random.nextInt(8) == 0; // Spigot
            byte b0 = 1;
            if (this.random.nextInt(32) == 0) b0 = 4;

            // Spigot start
            if (!hatching) b0 = 0;
            EntityType hatchingType = EntityType.CHICKEN;

            Entity shooter = egg.getOwner();
            if (shooter instanceof ServerPlayerEntity) {
                PlayerEggThrowEvent event = new PlayerEggThrowEvent((Player) ((IMixinEntity)shooter).getBukkitEntity(), (org.bukkit.entity.Egg) ((IMixinEntity)egg).getBukkitEntity(), hatching, b0, hatchingType);
                CraftServer.INSTANCE.getPluginManager().callEvent(event);

                b0 = event.getNumHatches();
                hatching = event.isHatching();
                hatchingType = event.getHatchingType();
            }

            // Paper start
            ThrownEggHatchEvent event = new ThrownEggHatchEvent((org.bukkit.entity.Egg) ((IMixinEntity)egg).getBukkitEntity(), hatching, b0, hatchingType);
            event.callEvent();

            b0 = event.getNumHatches();
            hatching = event.isHatching();
            hatchingType = event.getHatchingType();
            // Paper end
            if (hatching) {
                for (int i = 0; i < b0; ++i) {
                    WorldImpl cw = ((IMixinWorld)world).getWorldImpl();
                    Entity entity = cw.createEntity(new org.bukkit.Location(cw, egg.getX(), egg.getY(), egg.getZ(), egg.getYaw(), 0.0F), hatchingType.getEntityClass());
                    if (((IMixinEntity)entity).getBukkitEntity() instanceof Ageable)
                        ((Ageable) ((IMixinEntity)entity).getBukkitEntity()).setBaby();
                    cw.addEntity(entity, SpawnReason.EGG);
                }
            }
            // Spigot end

            world.sendEntityStatus(egg, (byte) 3);
            egg.discard();
        }
        ci.cancel();
        return;
    }

}