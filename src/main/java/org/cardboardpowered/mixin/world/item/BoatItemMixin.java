package org.cardboardpowered.mixin.world.item;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import java.util.List;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.boat.AbstractBoat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

@MixinInfo(events = {"PlayerInteractEvent"})
@Mixin(BoatItem.class)
public abstract class BoatItemMixin extends Item {

    // TODO: 1.19
    /*@SuppressWarnings({ "deprecation", "rawtypes", "unchecked" })
    @Inject(method = "use", at = @At(value = "NEW", target = "(Lnet/minecraft/world/World;DDD)Lnet/minecraft/entity/vehicle/BoatEntity;"), cancellable = true)
    public void bukkitize(World world, PlayerEntity player, Hand hand, CallbackInfoReturnable<TypedActionResult> ci) {
        ItemStack itemstack = player.getStackInHand(hand);
        BlockHitResult movingobjectpositionblock = raycast(world, player, RaycastContext.FluidHandling.ANY);
        org.bukkit.event.player.PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent((ServerPlayerEntity) player, org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getSide(), itemstack, hand);

        if (event.isCancelled()) {
            ci.setReturnValue(new TypedActionResult(ActionResult.PASS, itemstack));
        }
    }*/

    // @formatter:off
    //@Shadow @Final private BoatEntity.Type type;
    // @Shadow @Final private static Predicate<Entity> RIDERS;
    // @formatter:on
    
    // @formatter:off
    // @Shadow @Final private BoatEntity.Type type;
    // @formatter:on

    @Shadow protected abstract AbstractBoat getBoat(Level world, HitResult hitResult, ItemStack stack, Player player);

    public BoatItemMixin(net.minecraft.world.item.Item.Properties settings) {
        super(settings);
    }
    
    private static boolean isValid(LevelAccessor level) {
        return level != null
                && !level.isClientSide();
    }

    private static boolean isValid(BlockGetter getter) {
        return getter instanceof LevelAccessor level && isValid(level);
    }

    /**
     * @author cardboard
     * @reason PlayerInteractEvent
     */
    @Overwrite
    public InteractionResult use(Level world, Player user, InteractionHand hand) {
        ItemStack itemstack = user.getItemInHand(hand);
        BlockHitResult movingobjectpositionblock = BoatItem.getPlayerPOVHitResult(world, user, ClipContext.Fluid.ANY);
        if (movingobjectpositionblock.getType() == HitResult.Type.MISS) {
            return InteractionResult.PASS;
        }
        Vec3 vec3d = user.getViewVector(1.0f);
        double d0 = 5.0;
        List<Entity> list = world.getEntities(user, user.getBoundingBox().expandTowards(vec3d.scale(5.0)).inflate(1.0), EntitySelector.CAN_BE_PICKED);
        if (!list.isEmpty()) {
            Vec3 vec3d1 = user.getEyePosition();
            for (Entity entity : list) {
                AABB axisalignedbb = entity.getBoundingBox().inflate(entity.getPickRadius());
                if (!axisalignedbb.contains(vec3d1)) continue;
                return InteractionResult.PASS;
            }
        }
        if (movingobjectpositionblock.getType() == HitResult.Type.BLOCK) {
            // Singleplayer: the client predicts with a LocalPlayer; only the server fires the event.
            if (!world.isClientSide()) {
                PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent((ServerPlayer)user, Action.RIGHT_CLICK_BLOCK, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getDirection(), itemstack, false, hand/*, movingobjectpositionblock.getPos()*/);
                if (event.isCancelled()) {
                    return InteractionResult.PASS;
                }
            }
            AbstractBoat abstractboat = this.getBoat(world, movingobjectpositionblock, itemstack, user);
            if (abstractboat == null) {
                return InteractionResult.FAIL;
            }
            abstractboat.setYRot(user.getYRot());
            if (!world.noCollision(abstractboat, abstractboat.getBoundingBox())) {
                return InteractionResult.FAIL;
            }
            if (!world.isClientSide()) {
                if (CraftEventFactory.callEntityPlaceEvent(world, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getDirection(), user, abstractboat, hand).isCancelled()) {
                    return InteractionResult.FAIL;
                }
                if (!world.addFreshEntity(abstractboat)) {
                    return InteractionResult.PASS;
                }
                world.gameEvent((Entity)user, GameEvent.ENTITY_PLACE, movingobjectpositionblock.getLocation());
                itemstack.consume(1, user);
            }
            user.awardStat(Stats.ITEM_USED.get(this));
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    /*
    public TypedActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {
        ItemStack itemstack = playerIn.getStackInHand(handIn);
        BlockHitResult result = raycast(worldIn, playerIn, RaycastContext.FluidHandling.ANY);
        if (result.getType() == HitResult.Type.MISS) {
            return new TypedActionResult<>(ActionResult.PASS, itemstack);
        } else {
            Vec3d vec3d = playerIn.getRotationVec(1.0F);
            double d0 = 5.0D;
            List<Entity> list = worldIn.getOtherEntities(playerIn, playerIn.getBoundingBox().stretch(vec3d.multiply(5.0D)).expand(1.0D), RIDERS);
            if (!list.isEmpty()) {
                Vec3d vec3d1 = playerIn.getCameraPosVec(1.0F);

                for (Entity entity : list) {
                    Box axisalignedbb = entity.getBoundingBox().expand(entity.getTargetingMargin());
                    if (axisalignedbb.contains(vec3d1)) {
                        return new TypedActionResult<>(ActionResult.PASS, itemstack);
                    }
                }
            }

            if (result.getType() == HitResult.Type.BLOCK) {
                if (isValid(worldIn)) {
                    PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent((ServerPlayerEntity)playerIn, Action.RIGHT_CLICK_BLOCK, result.getBlockPos(), result.getSide(), itemstack, handIn);

                    if (event.isCancelled()) {
                        return new TypedActionResult<>(ActionResult.PASS, itemstack);
                    }
                }

                BoatEntity boatentity = this.createEntity(worldIn, result, itemstack, playerIn);
                boatentity.setVariant(this.type);
                boatentity.setYaw(playerIn.getYaw());
                if (!worldIn.isSpaceEmpty(boatentity, boatentity.getBoundingBox().expand(-0.1D))) {
                    return new TypedActionResult<>(ActionResult.FAIL, itemstack);
                } else {
                    if (!worldIn.isClient) {
                        if (isValid(worldIn) && CraftEventFactory.callEntityPlaceEvent(worldIn, result.getBlockPos(), result.getSide(), playerIn, boatentity, handIn).isCancelled()) {
                            return new TypedActionResult<>(ActionResult.FAIL, itemstack);
                        }
                        if (!worldIn.spawnEntity(boatentity)) {
                            return new TypedActionResult<>(ActionResult.PASS, itemstack);
                        }

                        if (!playerIn.getAbilities().creativeMode) {
                            itemstack.decrement(1);
                        }
                    }

                    playerIn.incrementStat(Stats.USED.getOrCreateStat(this));
                    return TypedActionResult.success(itemstack, worldIn.isClient());
                }
            } else {
                return new TypedActionResult<>(ActionResult.PASS, itemstack);
            }
        }
    }
    */
    
    
    /**
     * @author
     * @reason
     */
    /*@Overwrite
    public TypedActionResult<ItemStack> use_1(World world, PlayerEntity user, Hand hand) {
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
                org.bukkit.event.player.PlayerInteractEvent event = CraftEventFactory.callPlayerInteractEvent((ServerPlayerEntity) user, org.bukkit.event.block.Action.RIGHT_CLICK_BLOCK, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getSide(), itemStack, hand);
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
                        if (CraftEventFactory.callEntityPlaceEvent(world, movingobjectpositionblock.getBlockPos(), movingobjectpositionblock.getSide(), user, boatEntity).isCancelled()) {
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
    }*/
}
