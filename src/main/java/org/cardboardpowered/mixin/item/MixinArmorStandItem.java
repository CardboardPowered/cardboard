package org.cardboardpowered.mixin.item;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.util.ActionResult;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.item.ArmorStandItem;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@MixinInfo(events = {"EntityPlaceEvent"})
@Mixin(value = ArmorStandItem.class, priority = 900)
public class MixinArmorStandItem {

    private transient ArmorStandEntity bukkitEntity;

    @Redirect(method = "useOnBlock", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/decoration/ArmorStandEntity;refreshPositionAndAngles(DDDFF)V"))
    private void bukkitCaptureEntity(ArmorStandEntity instance, double x, double y, double z, float yaw, float pitch) {
        instance.refreshPositionAndAngles(x, y, z, yaw, pitch);
        bukkitEntity = instance;
    }

    @Inject(method = "useOnBlock", cancellable = true, at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntityAndPassengers(Lnet/minecraft/entity/Entity;)V"))
    public void bukkitEntityPlace(ItemUsageContext context, CallbackInfoReturnable<ActionResult> cir) {
        if (BukkitEventFactory.callEntityPlaceEvent(context, bukkitEntity).isCancelled()) {
            cir.setReturnValue(ActionResult.FAIL);
        }
        bukkitEntity = null;
    }
	
	// TODO: 1.19

    /**
     * @reason .
     * @author .
     */
    /*@Overwrite
    public ActionResult useOnBlock(ItemUsageContext itemactioncontext) {
        Direction enumdirection = itemactioncontext.getSide();

        if (enumdirection == Direction.DOWN) {
            return ActionResult.FAIL;
        } else {
            World world = itemactioncontext.getWorld();
            ItemPlacementContext blockactioncontext = new ItemPlacementContext(itemactioncontext);
            BlockPos blockposition = blockactioncontext.getBlockPos();
            ItemStack itemstack = itemactioncontext.getStack();
            Vec3d vec3d = Vec3d.ofBottomCenter((Vec3i) blockposition);
            Box axisalignedbb = EntityType.ARMOR_STAND.getDimensions().getBoxAt(vec3d.getX(), vec3d.getY(), vec3d.getZ());

            if (world.getOtherEntities((Entity) null, axisalignedbb).isEmpty()) {
                if (world instanceof ServerWorld) {
                    ServerWorld worldserver = (ServerWorld) world;
                    ArmorStandEntity entityarmorstand = (ArmorStandEntity) EntityType.ARMOR_STAND.create(worldserver, itemstack.getNbt(), (Text) null, itemactioncontext.getPlayer(), blockposition, SpawnReason.NATURAL, true, true);

                    if (entityarmorstand == null)  return ActionResult.FAIL;
                    worldserver.spawnEntityAndPassengers(entityarmorstand);
                    float f = (float) MathHelper.floor((MathHelper.wrapDegrees(itemactioncontext.getPlayerYaw() - 180.0F) + 22.5F) / 45.0F) * 45.0F;

                    entityarmorstand.refreshPositionAndAngles(entityarmorstand.getX(), entityarmorstand.getY(), entityarmorstand.getZ(), f, 0.0F);
                    this.setRotations(entityarmorstand, world.random);
                    if (BukkitEventFactory.callEntityPlaceEvent(itemactioncontext, entityarmorstand).isCancelled()) return ActionResult.FAIL; // Bukkit

                    world.spawnEntity(entityarmorstand);
                    world.playSound((PlayerEntity) null, entityarmorstand.getX(), entityarmorstand.getY(), entityarmorstand.getZ(), SoundEvents.ENTITY_ARMOR_STAND_PLACE, SoundCategory.BLOCKS, 0.75F, 0.8F);
                }

                itemstack.decrement(1);
                return ActionResult.success(world.isClient);
            } else return ActionResult.FAIL;
        }
    }

    @Shadow
    public void setRotations(ArmorStandEntity entityarmorstand, Random random) {
    }*/

}