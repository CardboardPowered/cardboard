package org.cardboardpowered.impl.inventory.recipe;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.recipe.Ingredient;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.recipe.CookingBookCategory;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import org.cardboardpowered.interfaces.IIngredient;

public interface RecipeInterface extends Recipe {

    void addToCraftingManager();

    default Ingredient toNMS(RecipeChoice bukkit, boolean requireNotEmpty) {
        Ingredient stack;

        if (bukkit == null) {
            stack = Ingredient.EMPTY;
        } else if (bukkit instanceof RecipeChoice.MaterialChoice) {
            stack = new Ingredient(((RecipeChoice.MaterialChoice) bukkit).getChoices().stream().map((mat) -> new net.minecraft.recipe.Ingredient.StackEntry(CraftItemStack.asNMSCopy(new ItemStack(mat)))));
        } else if (bukkit instanceof RecipeChoice.ExactChoice) {
            stack = new Ingredient(((RecipeChoice.ExactChoice) bukkit).getChoices().stream().map((mat) -> new net.minecraft.recipe.Ingredient.StackEntry(CraftItemStack.asNMSCopy(mat))));
            ((IIngredient)stack).setExact_BF(true);
        } else throw new IllegalArgumentException("Unknown recipe stack instance " + bukkit);

        stack.getMatchingStacks();
        if (requireNotEmpty && stack.matchingStacks.length == 0) throw new IllegalArgumentException("Recipe requires at least one non-air choice!");
        return stack;
    }

    public static RecipeChoice toBukkit(Ingredient list) {
        list.getMatchingStacks();
        if (list.matchingStacks.length == 0) return null;
        if (((IIngredient)list).getExact_BF()) {
            List<org.bukkit.inventory.ItemStack> choices = new ArrayList<>(list.matchingStacks.length);
            for (net.minecraft.item.ItemStack i : list.matchingStacks) choices.add(CraftItemStack.asBukkitCopy(i));
            return new RecipeChoice.ExactChoice(choices);
        } else {
            List<org.bukkit.Material> choices = new ArrayList<>(list.matchingStacks.length);
            for (net.minecraft.item.ItemStack i : list.matchingStacks) choices.add(CraftMagicNumbers.getMaterial(i.getItem()));
            return new RecipeChoice.MaterialChoice(choices);
        }
    }
    
    
    public static net.minecraft.recipe.book.CraftingRecipeCategory getCategory(CraftingBookCategory bukkit) {
        return net.minecraft.recipe.book.CraftingRecipeCategory.valueOf(bukkit.name());
    }

    public static CraftingBookCategory getCategory(net.minecraft.recipe.book.CraftingRecipeCategory nms) {
        return CraftingBookCategory.valueOf(nms.name());
    }

    public static net.minecraft.recipe.book.CookingRecipeCategory getCategory(CookingBookCategory bukkit) {
        return net.minecraft.recipe.book.CookingRecipeCategory.valueOf(bukkit.name());
    }

    public static CookingBookCategory getCategory(net.minecraft.recipe.book.CookingRecipeCategory nms) {
        return CookingBookCategory.valueOf(nms.name());
    }

}