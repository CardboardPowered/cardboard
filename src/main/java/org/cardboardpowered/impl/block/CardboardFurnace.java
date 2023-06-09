package org.cardboardpowered.impl.block;

import net.kyori.adventure.text.Component;
import net.minecraft.block.AbstractFurnaceBlock;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;

import java.util.Map;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.craftbukkit.block.CraftContainer;
import org.bukkit.inventory.CookingRecipe;
import org.bukkit.inventory.FurnaceInventory;
import org.cardboardpowered.impl.inventory.CardboardFurnaceInventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CardboardFurnace<T extends AbstractFurnaceBlockEntity> extends CraftContainer<T> implements Furnace {

    public CardboardFurnace(Block block, Class<T> tileEntityClass) {
        super(block, tileEntityClass);
    }

    public CardboardFurnace(final Material material, final T te) {
        super(material, te);
    }

    @Override
    public FurnaceInventory getSnapshotInventory() {
        return new CardboardFurnaceInventory(this.getSnapshot());
    }

    @Override
    public FurnaceInventory getInventory() {
        return (!this.isPlaced()) ? this.getSnapshotInventory() : new CardboardFurnaceInventory(this.getTileEntity());
    }

    @Override
    public short getBurnTime() {
        return (short) this.getSnapshot().burnTime;
    }

    @Override
    public void setBurnTime(short burnTime) {
        this.getSnapshot().burnTime = burnTime;
        this.data = this.data.with(AbstractFurnaceBlock.LIT, burnTime > 0);
    }

    @Override
    public short getCookTime() {
        return (short) this.getSnapshot().cookTime;
    }

    @Override
    public void setCookTime(short cookTime) {
        this.getSnapshot().cookTime = cookTime;
    }

    @Override
    public int getCookTimeTotal() {
        return this.getSnapshot().cookTimeTotal;
    }

    @Override
    public void setCookTimeTotal(int cookTimeTotal) {
        this.getSnapshot().cookTimeTotal = cookTimeTotal;
    }

    @Override
    public double getCookSpeedMultiplier() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setCookSpeedMultiplier(double arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public @Nullable Component customName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void customName(@Nullable Component arg0) {
        // TODO Auto-generated method stub
        
    }

	@Override
	public int getRecipeUsedCount(@NotNull NamespacedKey arg0) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public @NotNull Map<CookingRecipe<?>, Integer> getRecipesUsed() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean hasRecipeUsedCount(@NotNull NamespacedKey arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setRecipeUsedCount(@NotNull CookingRecipe<?> arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setRecipesUsed(@NotNull Map<CookingRecipe<?>, Integer> arg0) {
		// TODO Auto-generated method stub
		
	}

}