package org.cardboardpowered.impl.entity;

import java.util.List;

import org.bukkit.Chunk;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftMerchant;
import org.bukkit.entity.AbstractVillager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantRecipe;

import com.destroystokyo.paper.block.TargetBlockInfo;
import com.destroystokyo.paper.block.TargetBlockInfo.FluidMode;
import com.destroystokyo.paper.entity.Pathfinder;
import com.destroystokyo.paper.entity.TargetEntityInfo;

import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.VillagerEntity;

public class AbstractVillagerImpl extends AgeableImpl implements AbstractVillager, InventoryHolder {

    public AbstractVillagerImpl(CraftServer server, MerchantEntity entity) {
        super(server, entity);
    }

    @Override
    public MerchantEntity getHandle() {
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

    @Override
    public Pathfinder getPathfinder() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void clearActiveItem() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public ItemStack getActiveItem() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getArrowsStuck() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getHandRaisedTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getHurtDirection() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getItemUseRemainingTime() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getShieldBlockingDelay() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Block getTargetBlock(int arg0, FluidMode arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public BlockFace getTargetBlockFace(int arg0, FluidMode arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TargetBlockInfo getTargetBlockInfo(int arg0, FluidMode arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Entity getTargetEntity(int arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TargetEntityInfo getTargetEntityInfo(int arg0, boolean arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isHandRaised() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void playPickupItemAnimation(Item arg0, int arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setArrowsStuck(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setHurtDirection(float arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setKiller(Player arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setShieldBlockingDelay(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean fromMobSpawner() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Chunk getChunk() {
        // TODO Auto-generated method stub
        return this.getWorld().getChunkAt(nms.chunkX, nms.chunkZ);
    }

    @Override
    public SpawnReason getEntitySpawnReason() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isInBubbleColumn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInWaterOrBubbleColumn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInWaterOrRain() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInWaterOrRainOrBubbleColumn() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void resetOffers() {
        // TODO Auto-generated method stub
        
    }

}