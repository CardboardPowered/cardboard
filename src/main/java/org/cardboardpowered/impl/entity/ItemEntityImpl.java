package org.cardboardpowered.impl.entity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.util.TriState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class ItemEntityImpl extends CraftEntity implements Item {

    private final ItemEntity item;

    public ItemEntityImpl(CraftServer server, Entity entity, ItemEntity item) {
        super(entity);
        this.item = item;
    }

    public ItemEntityImpl(CraftServer server, ItemEntity entity) {
        this(server, entity, entity);
    }

    @Override
    public ItemStack getItemStack() {
        return CraftItemStack.asCraftMirror(item.getStack());
    }

    @Override
    public void setItemStack(ItemStack stack) {
        item.setStack(CraftItemStack.asNMSCopy(stack));
    }

    @Override
    public int getPickupDelay() {
        return item.pickupDelay;
    }

    @Override
    public void setPickupDelay(int delay) {
        item.pickupDelay = Math.min(delay, Short.MAX_VALUE);
    }

    @Override
    public void setTicksLived(int value) {
        super.setTicksLived(value);
     // TODO 1.17ify item.itemAge = value;
    }

    @Override
    public String toString() {
        return "CraftItem";
    }

    @Override
    public EntityType getType() {
        return EntityType.DROPPED_ITEM;
    }

    public void setOwner(UUID uuid) {
        item.setOwner(uuid);
    }


    // Spigot #758
    public UUID getOwner() {
        return item.getOwner().getUuid();
    }

    // Spigot #758
    public void setThrower(UUID uuid) {
        item.throwerUuid = uuid;
    }

    // Spigot #758
    public UUID getThrower() {
    	return item.throwerUuid;
    }

    @Override
    public boolean canMobPickup() {
        return !item.cannotPickup();
    }

    @Override
    public boolean canPlayerPickup() {
        return !item.cannotPickup();
    }

    @Override
    public void setCanMobPickup(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setCanPlayerPickup(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setWillAge(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean willAge() {
        // TODO Auto-generated method stub
        return false;
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
	public int getHealth() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isUnlimitedLifetime() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setHealth(int arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setUnlimitedLifetime(boolean arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public @NotNull TriState getFrictionState() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setFrictionState(@NotNull TriState arg0) {
		// TODO Auto-generated method stub
		
	}

}