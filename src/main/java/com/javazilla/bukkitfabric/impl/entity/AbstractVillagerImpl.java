package com.javazilla.bukkitfabric.impl.entity;

import java.util.List;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftMerchant;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.MerchantRecipe;

import net.minecraft.entity.passive.AbstractTraderEntity;
import net.minecraft.entity.passive.VillagerEntity;

public class AbstractVillagerImpl extends AgeableImpl implements AbstractVillager, InventoryHolder {

    public AbstractVillagerImpl(CraftServer server, AbstractTraderEntity entity) {
        super(server, entity);
    }

    @Override
    public AbstractTraderEntity getHandle() {
        return (VillagerEntity) nms;
    }

    @Override
    public String toString() {
        return "CraftAbstractVillager";
    }

    @Override
    public Inventory getInventory() {
        return new CraftInventory(getHandle().getInventory());
    }

    private CraftMerchant getMerchant() {
        return null; // TODO
        // TODO return getHandle().getCraftMerchant();
    }

    @Override
    public List<MerchantRecipe> getRecipes() {
        return getMerchant().getRecipes();
    }

    @Override
    public void setRecipes(List<MerchantRecipe> recipes) {
        this.getMerchant().setRecipes(recipes);
    }

    @Override
    public MerchantRecipe getRecipe(int i) {
        return getMerchant().getRecipe(i);
    }

    @Override
    public void setRecipe(int i, MerchantRecipe merchantRecipe) {
        getMerchant().setRecipe(i, merchantRecipe);
    }

    @Override
    public int getRecipeCount() {
        return getMerchant().getRecipeCount();
    }

    @Override
    public boolean isTrading() {
        return getTrader() != null;
    }

    @Override
    public HumanEntity getTrader() {
        return getMerchant().getTrader();
    }

}