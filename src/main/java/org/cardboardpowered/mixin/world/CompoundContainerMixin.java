package org.cardboardpowered.mixin.world;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.world.CompoundContainer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.InventoryHolder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import org.cardboardpowered.bridge.world.ContainerBridge;

@Mixin(CompoundContainer.class)
public abstract class CompoundContainerMixin implements Container, ContainerBridge {

    @Shadow public Container container1;
    @Shadow public Container container2;

    public List<HumanEntity> transaction = new java.util.ArrayList<HumanEntity>();

    @Override
    public List<ItemStack> getContents() {
        List<ItemStack> result = new ArrayList<ItemStack>(this.container1.getContainerSize() + this.container2.getContainerSize());
        for (int i = 0; i < (this.container1.getContainerSize() + this.container2.getContainerSize()); i++)
            result.add(this.getItem(i));
        return result;
    }

    @Shadow
    public ItemStack getItem(int i) {
        return null;
    }

    @Override
    public void onOpen(CraftHumanEntity who) {
        ((ContainerBridge) this.container1).onOpen(who);
        ((ContainerBridge) this.container2).onOpen(who);
        transaction.add(who);
    }

    @Override
    public void onClose(CraftHumanEntity who) {
        ((ContainerBridge) this.container1).onClose(who);
        ((ContainerBridge) this.container2).onClose(who);
        transaction.remove(who);
    }

    @Override
    public List<HumanEntity> getViewers() {
        return transaction;
    }

    @Override
    public InventoryHolder getOwner() {
        return null; // Bukkit DoubleChest does not refer to this method.
    }

    @Override
    public void cardboard$setMaxStackSize(int size) {
        ((ContainerBridge)this.container1).cardboard$setMaxStackSize(size);
        ((ContainerBridge)this.container2).cardboard$setMaxStackSize(size);
    }

    @Override
    public Location getLocation() {
        return ((ContainerBridge)this.container1).getLocation();
    }

    @Override
    public int getMaxStackSize() {
        return MAX_STACK;
    }

}