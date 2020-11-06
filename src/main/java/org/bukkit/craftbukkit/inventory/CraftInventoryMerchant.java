package org.bukkit.craftbukkit.inventory;

import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;

import org.bukkit.inventory.MerchantRecipe;

import com.javazilla.bukkitfabric.interfaces.IMixinTradeOffer;
import com.javazilla.bukkitfabric.interfaces.IMixinTrader;

public class CraftInventoryMerchant extends CraftInventory implements org.bukkit.inventory.MerchantInventory {

    private final Merchant merchant;

    public CraftInventoryMerchant(Merchant merchant, MerchantInventory inventory) {
        super(inventory);
        this.merchant = merchant;
    }

    @Override
    public int getSelectedRecipeIndex() {
        return getInventory().recipeIndex;
    }

    @Override
    public MerchantRecipe getSelectedRecipe() {
        net.minecraft.village.TradeOffer nmsRecipe = getInventory().getTradeOffer();
        return (nmsRecipe == null) ? null : ((IMixinTradeOffer)nmsRecipe).asBukkit();
    }

    @Override
    public MerchantInventory getInventory() {
        return (MerchantInventory) inventory;
    }

    @Override
    public org.bukkit.inventory.Merchant getMerchant() {
        return ((IMixinTrader)merchant).getCraftMerchant();
    }

}