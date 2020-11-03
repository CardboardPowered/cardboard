package com.javazilla.bukkitfabric.mixin.recipe;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.impl.inventory.recipe.CraftSmithingRecipe;
import com.javazilla.bukkitfabric.impl.inventory.recipe.RecipeInterface;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.SmithingRecipe;
import net.minecraft.util.Identifier;

@Mixin(SmithingRecipe.class)
public class MixinSmithingRecipe implements IMixinRecipe {

    @Shadow private Ingredient base;
    @Shadow private Ingredient addition;
    @Shadow private ItemStack result;
    @Shadow public Identifier id;

    @Override
    public Recipe toBukkitRecipe() {
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);
        CraftSmithingRecipe recipe = new CraftSmithingRecipe(CraftNamespacedKey.fromMinecraft(this.id), result, RecipeInterface.toBukkit(this.base), RecipeInterface.toBukkit(this.addition));

        return recipe;
    }

}