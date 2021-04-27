package org.cardboardpowered.mixin.entity;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinThrownItemEntity;

import net.minecraft.entity.projectile.thrown.ThrownItemEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@Mixin(ThrownItemEntity.class)
public abstract class MixinThrownItemEntity implements IMixinThrownItemEntity {

    @Shadow
    public abstract Item getDefaultItem();

    @Override
    public Item getDefaultItemPublic() {
        return getDefaultItem();
    }

    @Shadow
    public ItemStack getItem() {
        return null;
    }

    @Override
    public ItemStack getItemBF() {
        return getItem();
    }

}