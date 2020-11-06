package com.javazilla.bukkitfabric.mixin.item;

import java.util.Random;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ArmorStandItem;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUsageContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import net.minecraft.world.World;

@Mixin(ArmorStandItem.class)
public class MixinArmorStandItem {

    /**
     * @reason .
     * @author .
     */
    @Overwrite
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
            Box axisalignedbb = EntityType.ARMOR_STAND.getDimensions().method_30231(vec3d.getX(), vec3d.getY(), vec3d.getZ());

            if (world.getOtherEntities((Entity) null, axisalignedbb).isEmpty()) {
                if (world instanceof ServerWorld) {
                    ServerWorld worldserver = (ServerWorld) world;
                    ArmorStandEntity entityarmorstand = (ArmorStandEntity) EntityType.ARMOR_STAND.create(worldserver, itemstack.getTag(), (Text) null, itemactioncontext.getPlayer(), blockposition, SpawnReason.NATURAL, true, true);

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
    }

}