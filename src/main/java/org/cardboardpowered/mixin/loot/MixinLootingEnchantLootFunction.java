package org.cardboardpowered.mixin.loot;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinLootContextParameters;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootContext;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.function.LootingEnchantLootFunction;

@Mixin(LootingEnchantLootFunction.class)
public class MixinLootingEnchantLootFunction {

    @Shadow
    public int limit;

   // @Shadow
    //public UniformLootTableRange countRange;

    //@Overwrite
    public ItemStack process_TODO(ItemStack itemstack, LootContext loottableinfo) {
        /*Entity entity = (Entity) loottableinfo.get(LootContextParameters.KILLER_ENTITY);
        if (entity instanceof LivingEntity) {
            int i = EnchantmentHelper.getLooting((LivingEntity) entity);
            if (loottableinfo.hasParameter(IMixinLootContextParameters.LOOTING_MOD))
                i = loottableinfo.get(IMixinLootContextParameters.LOOTING_MOD);
            if (i <= 0) return itemstack; // CraftBukkit - account for possible negative looting values
            float f = (float) i * this.countRange.nextFloat(loottableinfo.getRandom());
            itemstack.increment(Math.round(f));
            if ((this.limit > 0) && itemstack.getCount() > this.limit) itemstack.setCount(this.limit);
        }
        return itemstack;*/return null; // TODO 1.17ify
    }

}