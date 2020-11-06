package com.javazilla.bukkitfabric.mixin.entity;

import org.bukkit.craftbukkit.inventory.CraftMerchant;
import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.interfaces.IMixinTrader;

import net.minecraft.entity.passive.MerchantEntity;

@Mixin(MerchantEntity.class)
public class MixinAbstractTraderEntity implements IMixinTrader {

    private CraftMerchant craftMerchant;

    @Override
    public CraftMerchant getCraftMerchant() {
        return (craftMerchant == null) ? craftMerchant = new CraftMerchant((MerchantEntity)(Object) this) : craftMerchant;
    }

}
