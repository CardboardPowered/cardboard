package org.cardboardpowered.mixin.world.entity.projectile.throwableitemprojectile;

import java.util.Random;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrownEgg;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Ageable;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.event.player.PlayerEggThrowEvent;
import org.cardboardpowered.impl.world.CraftWorld;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.destroystokyo.paper.event.entity.ThrownEggHatchEvent;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;

@MixinInfo(events = {"ThrownEggHatchEvent", "PlayerEggThrowEvent"})
@Mixin(value = ThrownEgg.class, priority = 999)
public abstract class ThrownEggMixin {

    private final Random random = new Random();

    // Injected after the super call, not at HEAD: super.onHit is what dispatches onHitEntity and
    // onHitBlock, so cancelling before it stops the egg hitting anything at all. Everything after
    // the super call is what this handler replaces.
    @Inject(method = "onHit", cancellable = true,
            at = @At(value = "INVOKE", shift = Shift.AFTER,
                    target = "Lnet/minecraft/world/entity/projectile/throwableitemprojectile/ThrowableItemProjectile;onHit(Lnet/minecraft/world/phys/HitResult;)V"))
    public void cardboard_doEggThrowEvent(HitResult res, CallbackInfo ci) {
        ThrownEgg egg = (ThrownEgg)(Object)this;
        Level world = egg.level();

        if (!world.isClientSide()) {
            boolean hatching = this.random.nextInt(8) == 0; // Spigot
            byte b0 = 1;
            if (this.random.nextInt(32) == 0) b0 = 4;

            // Spigot start
            if (!hatching) b0 = 0;
            EntityType hatchingType = EntityType.CHICKEN;

            Entity shooter = egg.getOwner();
            if (shooter instanceof ServerPlayer) {
                PlayerEggThrowEvent event = new PlayerEggThrowEvent((Player) ((EntityBridge)shooter).getBukkitEntity(), (org.bukkit.entity.Egg) ((EntityBridge)egg).getBukkitEntity(), hatching, b0, hatchingType);
                CraftServer.INSTANCE.getPluginManager().callEvent(event);

                b0 = event.getNumHatches();
                hatching = event.isHatching();
                hatchingType = event.getHatchingType();
            }

            // Paper start
            ThrownEggHatchEvent event = new ThrownEggHatchEvent((org.bukkit.entity.Egg) ((EntityBridge)egg).getBukkitEntity(), hatching, b0, hatchingType);
            event.callEvent();

            b0 = event.getNumHatches();
            hatching = event.isHatching();
            hatchingType = event.getHatchingType();
            // Paper end
            if (hatching) {
                for (int i = 0; i < b0; ++i) {
                    CraftWorld cw = ((LevelBridge)world).cardboard$getWorld();
                    Entity entity = cw.createEntity_Old(new org.bukkit.Location(cw, egg.getX(), egg.getY(), egg.getZ(), egg.getYRot(), 0.0F), hatchingType.getEntityClass());
                    if (((EntityBridge)entity).getBukkitEntity() instanceof Ageable)
                        ((Ageable) ((EntityBridge)entity).getBukkitEntity()).setBaby();
                    cw.addEntity(entity, SpawnReason.EGG);
                }
            }
            // Spigot end

            world.broadcastEntityEvent(egg, (byte) 3);
            egg.discard();
        }
        ci.cancel();
        return;
    }

}