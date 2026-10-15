package org.bukkit.craftbukkit.inventory;

import net.minecraft.world.item.crafting.RecipeHolder;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.CraftingRecipe;
import org.bukkit.inventory.ItemStack;
import org.cardboardpowered.bridge.world.item.crafting.RecipeManagerBridge;

public class CraftComplexRecipe extends CraftingRecipe implements CraftRecipe, ComplexRecipe {

    // Any crafting recipe Bukkit cannot describe: CustomRecipe, and 26.1's DyeRecipe/ImbueRecipe.
    private final net.minecraft.world.item.crafting.CraftingRecipe recipe;

    public CraftComplexRecipe(NamespacedKey key, ItemStack result, net.minecraft.world.item.crafting.CraftingRecipe recipe) {
        super(key, result);
        this.recipe = recipe;
    }

    @Override
    public void addToCraftingManager() {
        ((RecipeManagerBridge)CraftServer.INSTANCE.getServer().getRecipeManager()).cardboard$addRecipe(new RecipeHolder<>(CraftRecipe.toMinecraft(this.getKey()), this.recipe));
    }
}
