package com.javazilla.bukkitfabric.mixin.item;

import java.util.List;

import org.bukkit.Bukkit;
import org.cardboardpowered.impl.entity.LivingEntityImpl;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockDispenseArmorEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.javazilla.bukkitfabric.impl.block.DispenserBlockHelper;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.block.DispenserBlock;
import net.minecraft.block.dispenser.DispenserBehavior;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.predicate.entity.EntityPredicates;
import net.minecraft.util.math.BlockPointer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(ArmorItem.class)
public class MixinArmorItem {

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public static boolean dispenseArmor(BlockPointer isourceblock, ItemStack itemstack) {
        BlockPos blockposition = isourceblock.getBlockPos().offset((Direction) isourceblock.getBlockState().get(DispenserBlock.FACING));
        List<LivingEntity> list = isourceblock.getWorld().getEntitiesByClass(LivingEntity.class, new Box(blockposition), EntityPredicates.EXCEPT_SPECTATOR.and(new EntityPredicates.Equipable(itemstack)));

        if (list.isEmpty()) {
            return false;
        } else {
            LivingEntity entityliving = (LivingEntity) list.get(0);
            EquipmentSlot enumitemslot = MobEntity.getPreferredEquipmentSlot(itemstack);
            ItemStack itemstack1 = itemstack.split(1);

            World world = isourceblock.getWorld();
            org.bukkit.block.Block block = ((IMixinWorld)world).getWorldImpl().getBlockAt(isourceblock.getBlockPos().getX(), isourceblock.getBlockPos().getY(), isourceblock.getBlockPos().getZ());
            CraftItemStack craftItem = CraftItemStack.asCraftMirror(itemstack1);

            BlockDispenseArmorEvent event = new BlockDispenseArmorEvent(block, craftItem.clone(), (LivingEntityImpl) ((IMixinEntity)entityliving).getBukkitEntity());
            if (!DispenserBlockHelper.eventFired)
                Bukkit.getPluginManager().callEvent(event);

            if (event.isCancelled()) {
                itemstack.increment(1);
                return false;
            }

            if (!event.getItem().equals(craftItem)) {
                itemstack.increment(1);
                // Chain to handler for new item
                ItemStack eventStack = CraftItemStack.asNMSCopy(event.getItem());
                DispenserBehavior idispensebehavior = (DispenserBehavior) DispenserBlock.BEHAVIORS.get(eventStack.getItem());
                if (idispensebehavior != DispenserBehavior.NOOP && idispensebehavior != ArmorItem.DISPENSER_BEHAVIOR) {
                    idispensebehavior.dispense(isourceblock, eventStack);
                    return true;
                }
            }

            entityliving.equipStack(enumitemslot, itemstack1);
            if (entityliving instanceof MobEntity) {
                ((MobEntity) entityliving).setEquipmentDropChance(enumitemslot, 2.0F);
                ((MobEntity) entityliving).setPersistent();
            }

            return true;
        }
    }

}