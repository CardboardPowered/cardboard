package org.cardboardpowered.mixin.item;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.vehicle.BoatEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;
import net.minecraft.world.World;
import net.minecraft.world.event.GameEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.item.BoatItem;
import net.minecraft.item.Item;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;

@MixinInfo(events = {"PlayerInteractEvent"})
@Mixin(BoatItem.class)
public class MixinBoatItem extends Item {

    // TODO: 1.19
    /*@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
    @Inject(method = "use", at = @At(value = "NEW", target = "(Lnet/minecraft/world/World;DDD)Lnet/minecraft/entity/vehicle/BoatEntity;"), cancellable = true)
    public void bukkitize(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult> ci) {
        ItemStack itemstack = player.getStackInHand(hand);
        BlockHitResult movingobjectpositionblock = raycast(world, player, RaycastContext.FluidHandling.ANY);
        org.bukkit.event.player.PlayerInteractEvent event = BukkitEventFactory.callPlayerInteractEvent((ServerPlayerEntity) player, org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getSide(), itemstack, hand);

        if (event.isCancelled()) {
            ci.setReturnValue(new TypedActionResult(ActionResult.PASS, itemstack));
        }
    }*/

    // @formatter:off
    @Shadow @Final private BoatEntity.Type type;
    @Shadow @Final private static Predicate<Entity> RIDERS;
    // @formatter:on

    public MixinBoatItem(Settings settings) {
        super(settings);
    }

    /**
     * @author
     * @reason
     */
    @Overwrite
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        ItemStack itemStack = user.getStackInHand(hand);
        HitResult hitResult = raycast(world, user, RaycastContext.FluidHandling.ANY);
        if (hitResult.getType() == HitResult.Type.MISS) {
            return TypedActionResult.pass(itemStack);
        } else {
            Vec3d vec3d = user.getRotationVec(1.0F);
            double d = 5.0;
            List<Entity> list = world.getOtherEntities(user, user.getBoundingBox().stretch(vec3d.multiply(5.0)).expand(1.0), RIDERS);
            if (!list.isEmpty()) {
                Vec3d vec3d2 = user.getEyePos();
                Iterator var11 = list.iterator();

                while(var11.hasNext()) {
                    Entity entity = (Entity)var11.next();
                    Box box = entity.getBoundingBox().expand((double)entity.getTargetingMargin());
                    if (box.contains(vec3d2)) {
                        return TypedActionResult.pass(itemStack);
                    }
                }
            }

            if (hitResult.getType() == HitResult.Type.BLOCK) {
                BlockHitResult movingobjectpositionblock = raycast(world, user, RaycastContext.FluidHandling.ANY);
                org.bukkit.event.player.PlayerInteractEvent event = BukkitEventFactory.callPlayerInteractEvent((ServerPlayerEntity) user, org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getSide(), itemStack, hand);
                if (event.isCancelled()) {
                    return new TypedActionResult(ActionResult.PASS, itemStack);
                }

                BoatEntity boatEntity = new BoatEntity(world, hitResult.getPos().x, hitResult.getPos().y, hitResult.getPos().z);
                boatEntity.setBoatType(this.type);
                boatEntity.setYaw(user.getYaw());
                if (!world.isSpaceEmpty(boatEntity, boatEntity.getBoundingBox())) {
                    return TypedActionResult.fail(itemStack);
                } else {
                    if (!world.isClient) {
                        if (BukkitEventFactory.callEntityPlaceEvent(world, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getSide(), user, boatEntity).isCancelled()) {
                            return TypedActionResult.fail(itemStack);
                        }

                        if (!world.spawnEntity(boatEntity)) {
                            return TypedActionResult.pass(itemStack);
                        }

                        world.emitGameEvent(user, GameEvent.ENTITY_PLACE, new BlockPos(hitResult.getPos()));
                        if (!user.getAbilities().creativeMode) {
                            itemStack.decrement(1);
                        }
                    }

                    user.incrementStat(Stats.USED.getOrCreateStat(this));
                    return TypedActionResult.success(itemStack, world.isClient());
                }
            } else {
                return TypedActionResult.pass(itemStack);
            }
        }
    }
}