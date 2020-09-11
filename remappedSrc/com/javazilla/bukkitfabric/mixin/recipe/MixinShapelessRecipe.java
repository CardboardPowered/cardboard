package com.javazilla.bukkitfabric.mixin.recipe;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.CraftShapelessRecipe;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.util.collection.DefaultedList;

@Mixin(ShapelessRecipe.class)
public class MixinShapelessRecipe implements IMixinRecipe {

    @Shadow public String group;
    @Shadow public ItemStack output;
    @Shadow public DefaultedList<Ingredient> input;

    @Override
    public org.bukkit.inventory.ShapelessRecipe toBukkitRecipe() {
        CraftItemStack result = CraftItemStack.asCraftMirror(this.output);
        CraftShapelessRecipe recipe = new CraftShapelessRecipe(result, (ShapelessRecipe)(Object)this);
        recipe.setGroup(this.group);

        for (Ingredient list : this.input)
            recipe.addIngredient(CraftRecipe.toBukkit(list));
        return recipe;
    }

}