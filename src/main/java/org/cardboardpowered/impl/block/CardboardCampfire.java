package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.CampfireBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Campfire;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class CardboardCampfire extends CardboardBlockEntityState<CampfireBlockEntity> implements Campfire {

    public CardboardCampfire(Block block) {
        super(block, CampfireBlockEntity.class);
    }

    public CardboardCampfire(Material material, CampfireBlockEntity te) {
        super(material, te);
    }

    @Override
    public int getSize() {
        return getSnapshot().getItemsBeingCooked().size();
    }

    @Override
    public ItemStack getItem(int index) {
        net.minecraft.item.ItemStack item = getSnapshot().getItemsBeingCooked().get(index);
        return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
    }

    @Override
    public void setItem(int index, ItemStack item) {
        getSnapshot().getItemsBeingCooked().set(index, CraftItemStack.asNMSCopy(item));
    }

    @Override
    public int getCookTime(int index) {
        return getSnapshot().cookingTimes[index];
    }

    @Override
    public void setCookTime(int index, int cookTime) {
        getSnapshot().cookingTimes[index] = cookTime;
    }

    @Override
    public int getCookTimeTotal(int index) {
        return getSnapshot().cookingTotalTimes[index];
    }

    @Override
    public void setCookTimeTotal(int index, int cookTimeTotal) {
        getSnapshot().cookingTotalTimes[index] = cookTimeTotal;
    }

	@Override
	public boolean isCookingDisabled(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void startCooking() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean startCooking(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stopCooking() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean stopCooking(int arg0) {
		// TODO Auto-generated method stub
		return false;
	}

}