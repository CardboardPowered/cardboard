package org.cardboardpowered.impl.inventory;

import net.minecraft.village.Merchant;
import net.minecraft.village.MerchantInventory;

import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.inventory.MerchantRecipe;

import com.javazilla.bukkitfabric.interfaces.IMixinTradeOffer;
import com.javazilla.bukkitfabric.interfaces.IMixinTrader;

public class CardboardMerchantInventory extends CraftInventory implements org.bukkit.inventory.MerchantInventory {

    private final Merchant merchant;

    public CardboardMerchantInventory(Merchant merchant, MerchantInventory inventory) {
        super(inventory);
        this.merchant = merchant;
    }

    @Override
    public int getSelectedRecipeIndex() {
        return 0; // TODO 1.17ify getInventory().offerIndex;
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