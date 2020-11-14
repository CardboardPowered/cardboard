package org.bukkit.craftbukkit.inventory;

import java.util.Arrays;
import java.util.List;
import net.minecraft.inventory.Inventory;
import org.bukkit.inventory.CraftingInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;

import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;

public class CraftInventoryCrafting extends CraftInventory implements CraftingInventory {
    private final Inventory resultInventory;

    public CraftInventoryCrafting(Inventory inventory, Inventory resultInventory) {
        super(inventory);
        this.resultInventory = resultInventory;
    }

    public Inventory getResultInventory() {
        return resultInventory;
    }

    public Inventory getMatrixInventory() {
        return inventory;
    }

    @Override
    public int getSize() {
        return getResultInventory().size() + getMatrixInventory().size();
    }

    @Override
    public void setContents(ItemStack[] items) {
        if (getSize() > items.length)
            throw new IllegalArgumentException("Invalid inventory size; expected " + getSize() + " or less");
        setContents(items[0], Arrays.copyOfRange(items, 1, items.length));
    }

    @Override
    public ItemStack[] getContents() {
        ItemStack[] items = new ItemStack[getSize()];
        List<net.minecraft.item.ItemStack> mcResultItems = ((IMixinInventory)getResultInventory()).getContents();

        int i = 0;
        for (i = 0; i < mcResultItems.size(); i++)
            items[i] = CraftItemStack.asCraftMirror(mcResultItems.get(i));

        List<net.minecraft.item.ItemStack> mcItems = ((IMixinInventory)getMatrixInventory()).getContents();
        for (int j = 0; j < mcItems.size(); j++)
            items[i + j] = CraftItemStack.asCraftMirror(mcItems.get(j));

        return items;
    }

    public void setContents(ItemStack result, ItemStack[] contents) {
        setResult(result);
        setMatrix(contents);
    }

    @Override
    public CraftItemStack getItem(int index) {
        if (index < getResultInventory().size()) {
            net.minecraft.item.ItemStack item = getResultInventory().getStack(index);
            return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
        } else {
            net.minecraft.item.ItemStack item = getMatrixInventory().getStack(index - getResultInventory().size());
            return item.isEmpty() ? null : CraftItemStack.asCraftMirror(item);
        }
    }

    @Override
    public void setItem(int index, ItemStack item) {
        if (index < getResultInventory().size())
            getResultInventory().setStack(index, CraftItemStack.asNMSCopy(item));
        else getMatrixInventory().setStack((index - getResultInventory().size()), CraftItemStack.asNMSCopy(item));
    }

    @Override
    public ItemStack[] getMatrix() {
        return asCraftMirror(((IMixinInventory)getMatrixInventory()).getContents());
    }

    @Override
    public ItemStack getResult() {
        net.minecraft.item.ItemStack item = getResultInventory().getStack(0);
        if (!item.isEmpty()) return CraftItemStack.asCraftMirror(item);
        return null;
    }

    @Override
    public void setMatrix(ItemStack[] contents) {
        if (getMatrixInventory().size() > contents.length)
            throw new IllegalArgumentException("Invalid inventory size; expected " + getMatrixInventory().size() + " or less");

        for (int i = 0; i < getMatrixInventory().size(); i++)
            getMatrixInventory().setStack(i, (i < contents.length) ? CraftItemStack.asNMSCopy(contents[i]) : net.minecraft.item.ItemStack.EMPTY);
    }

    @Override
    public void setResult(ItemStack item) {
        List<net.minecraft.item.ItemStack> contents = ((IMixinInventory)getResultInventory()).getContents();
        contents.set(0, CraftItemStack.asNMSCopy(item));
    }

    @SuppressWarnings("rawtypes")
    @Override
    public Recipe getRecipe() {
        net.minecraft.recipe.Recipe recipe = ((IMixinInventory)getInventory()).getCurrentRecipe();
        return recipe == null ? null : ((IMixinRecipe)recipe).toBukkitRecipe();
    }
}
