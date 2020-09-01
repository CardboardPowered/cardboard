package com.javazilla.bukkitfabric.mixin.recipe;

import org.bukkit.craftbukkit.inventory.CraftMerchantRecipe;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;

import net.minecraft.village.TradeOffer;

@Mixin(TradeOffer.class)
public class MixinTradeOffer implements IMixinRecipe {

    private CraftMerchantRecipe bukkitHandle;

    @Override
    public Recipe toBukkitRecipe() {
        return (bukkitHandle == null) ? bukkitHandle = new CraftMerchantRecipe((TradeOffer)(Object)this) : bukkitHandle;
    }


}