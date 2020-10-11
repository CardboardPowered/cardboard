package com.javazilla.bukkitfabric.mixin.entity;

import org.bukkit.craftbukkit.inventory.CraftMerchant;
import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.entity.passive.AbstractTraderEntity;

@Mixin(AbstractTraderEntity.class)
public class MixinAbstractTraderEntity implements IMixinTrader {

    private CraftMerchant craftMerchant;

    @Override
    public CraftMerchant getCraftMerchant() {
        return (craftMerchant == null) ? craftMerchant = new CraftMerchant((AbstractTraderEntity)(Object) this) : craftMerchant;
    }

}
