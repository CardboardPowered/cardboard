package org.cardboardpowered.mixin;

import org.bukkit.craftbukkit.inventory.CraftMerchantRecipe;
import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.interfaces.IMixinTradeOffer;

import net.minecraft.village.TradeOffer;

@Mixin(TradeOffer.class)
public class MixinTradeOffer implements IMixinTradeOffer {

    private CraftMerchantRecipe bukkitHandle;

    @Override
    public CraftMerchantRecipe asBukkit() {
        return (bukkitHandle == null) ? bukkitHandle = new CraftMerchantRecipe((TradeOffer)(Object)this) : bukkitHandle;
    }

}