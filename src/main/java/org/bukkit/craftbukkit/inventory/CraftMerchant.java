package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;
import com.javazilla.bukkitfabric.interfaces.IMixinTradeOffer;

import java.util.Collections;
import java.util.List;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.village.Merchant;
import net.minecraft.village.TradeOfferList;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.MerchantRecipe;

public class CraftMerchant implements org.bukkit.inventory.Merchant {

    protected final Merchant merchant;

    public CraftMerchant(Merchant merchant) {
        this.merchant = merchant;
    }

    public Merchant getMerchant() {
        return merchant;
    }

    @Override
    public List<MerchantRecipe> getRecipes() {
        return Collections.unmodifiableList(Lists.transform(merchant.getOffers(), new Function<net.minecraft.village.TradeOffer, MerchantRecipe>() {
            @Override
            public MerchantRecipe apply(net.minecraft.village.TradeOffer recipe) {
                return ((IMixinTradeOffer)recipe).asBukkit();
            }
        }));
    }

    @Override
    public void setRecipes(List<MerchantRecipe> recipes) {
        TradeOfferList recipesList = merchant.getOffers();
        recipesList.clear();
        for (MerchantRecipe recipe : recipes)
            recipesList.add(CraftMerchantRecipe.fromBukkit(recipe).toMinecraft());
    }

    @Override
    public MerchantRecipe getRecipe(int i) {
        return ((IMixinTradeOffer)merchant.getOffers().get(i)).asBukkit();
    }

    @Override
    public void setRecipe(int i, MerchantRecipe merchantRecipe) {
        merchant.getOffers().set(i, CraftMerchantRecipe.fromBukkit(merchantRecipe).toMinecraft());
    }

    @Override
    public int getRecipeCount() {
        return merchant.getOffers().size();
    }

    @Override
    public boolean isTrading() {
        return getTrader() != null;
    }

    @Override
    public HumanEntity getTrader() {
        PlayerEntity eh = merchant.getCustomer();
        return eh == null ? null : (Player)((IMixinServerEntityPlayer)eh).getBukkitEntity();
    }

    @Override
    public int hashCode() {
        return merchant.hashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof CraftMerchant && ((CraftMerchant) obj).merchant.equals(this.merchant);
    }

}
