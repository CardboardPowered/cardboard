package org.cardboardpowered.mixin.item;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.CrossbowItem;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

@MixinInfo(events = {"EntityShootBowEvent"})
@Mixin(value = CrossbowItem.class, priority = 900)
public class MixinCrossbowItem {

    private static transient boolean bukkitCapturedBoolean;

    @Inject(method = "shoot", cancellable = true, locals = LocalCapture.CAPTURE_FAILHARD, at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;damage(ILnet/minecraft/entity/LivingEntity;Ljava/util/function/Consumer;)V"))
    private static void bukkitShoot(World world, LivingEntity shooter, Hand hand, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean creative, float speed, float divergence, float simulated, CallbackInfo ci, boolean bl, ProjectileEntity projectileEntity) {
        EntityShootBowEvent event = BukkitEventFactory.callEntityShootBowEvent(shooter, crossbow, projectile, projectileEntity, shooter.getActiveHand(), soundPitch, true);
        if (event.isCancelled()) {
            event.getProjectile().remove();
            ci.cancel();
        }
        bukkitCapturedBoolean = event.getProjectile() == ((IMixinEntity) projectileEntity).getBukkitEntity();
    }

    @Redirect(method = "shoot", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"))
    private static boolean bukkitSpawnEntity(World world, Entity entityIn, World worldIn, LivingEntity shooter) {
        if (bukkitCapturedBoolean) {
            if (!world.spawnEntity(entityIn)) {
                if (shooter instanceof ServerPlayerEntity) {
                    ((IMixinServerEntityPlayer) shooter).getBukkit().updateInventory();
                }
                bukkitCapturedBoolean = true;
            } else {
                bukkitCapturedBoolean = false;
            }
        }
        return true;
    }

    @Inject(method = "shoot", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"))
    private static void bukkitReturnIfFail(World worldIn, LivingEntity shooter, Hand handIn, ItemStack crossbow, ItemStack projectile, float soundPitch, boolean isCreativeMode, float velocity, float inaccuracy, float projectileAngle, CallbackInfo ci) {
        if (bukkitCapturedBoolean) {
            ci.cancel();
        }
        bukkitCapturedBoolean = false;
    }
}
