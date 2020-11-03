package com.javazilla.bukkitfabric.mixin.recipe;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.impl.inventory.recipe.CraftShapedRecipe;
import com.javazilla.bukkitfabric.impl.inventory.recipe.RecipeInterface;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.util.collection.DefaultedList;

@Mixin(ShapedRecipe.class)
public class MixinShapedRecipe implements IMixinRecipe {

    @Shadow public String group;
    @Shadow public ItemStack output;
    @Shadow public DefaultedList<Ingredient> inputs;
    @Shadow public int width;
    @Shadow public int height;

    public org.bukkit.inventory.ShapedRecipe toBukkitRecipe() {
        CraftItemStack result = CraftItemStack.asCraftMirror(this.output);
        CraftShapedRecipe recipe = new CraftShapedRecipe(result, (ShapedRecipe)(Object)this);
        recipe.setGroup(this.group);

        switch (this.height) {
        case 1:
            switch (this.width) {
            case 1:
                recipe.shape("a");
                break;
            case 2:
                recipe.shape("ab");
                break;
            case 3:
                recipe.shape("abc");
                break;
            }
            break;
        case 2:
            switch (this.width) {
            case 1:
                recipe.shape("a","b");
                break;
            case 2:
                recipe.shape("ab","cd");
                break;
            case 3:
                recipe.shape("abc","def");
                break;
            }
            break;
        case 3:
            switch (this.width) {
            case 1:
                recipe.shape("a","b","c");
                break;
            case 2:
                recipe.shape("ab","cd","ef");
                break;
            case 3:
                recipe.shape("abc","def","ghi");
                break;
            }
            break;
        }
        char c = 'a';
        for (Ingredient list : this.inputs) {
            RecipeChoice choice = RecipeInterface.toBukkit(list);
            if (choice != null) recipe.setIngredient(c, choice);
            c++;
        }
        return recipe;
    }

}