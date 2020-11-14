package org.cardboardpowered.impl.inventory.recipe;

import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.ComplexRecipe;
import org.bukkit.inventory.ItemStack;

import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipeManager;

import net.minecraft.recipe.SpecialCraftingRecipe;

public class CardboardComplexRecipe implements RecipeInterface, ComplexRecipe {

    private final SpecialCraftingRecipe recipe;

    public CardboardComplexRecipe(SpecialCraftingRecipe recipe) {
        this.recipe = recipe;
    }

    @Override
    public ItemStack getResult() {
        return CraftItemStack.asCraftMirror(recipe.getOutput());
    }

    @Override
    public NamespacedKey getKey() {
        return CraftNamespacedKey.fromMinecraft(recipe.getId());
    }

    @Override
    public void addToCraftingManager() {
        ((IMixinRecipeManager)IMixinMinecraftServer.getServer().getRecipeManager()).addRecipe(recipe);
    }

}