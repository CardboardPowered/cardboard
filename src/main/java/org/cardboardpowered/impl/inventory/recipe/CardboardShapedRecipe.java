package org.cardboardpowered.impl.inventory.recipe;

import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipeManager;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.RawShapedRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.util.collection.DefaultedList;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.recipe.CraftingBookCategory;

import java.util.Map;
import java.util.Optional;

public class CardboardShapedRecipe extends ShapedRecipe implements RecipeInterface {

    // TODO: Could eventually use this to add a matches() method or some such
    private net.minecraft.recipe.ShapedRecipe recipe;

    public CardboardShapedRecipe(NamespacedKey key, ItemStack result) {
        super(key, result);
    }

    public CardboardShapedRecipe(Identifier id, ItemStack result, net.minecraft.recipe.ShapedRecipe recipe) {
        this(CraftNamespacedKey.fromMinecraft(id), result);
        this.recipe = recipe;
    }

    public static CardboardShapedRecipe fromBukkitRecipe(ShapedRecipe recipe) {
        if (recipe instanceof CardboardShapedRecipe)
            return (CardboardShapedRecipe) recipe;
        CardboardShapedRecipe ret = new CardboardShapedRecipe(recipe.getKey(), recipe.getResult());
        ret.setGroup(recipe.getGroup());
        String[] shape = recipe.getShape();
        ret.shape(shape);
        Map<Character, RecipeChoice> ingredientMap = recipe.getChoiceMap();
        for (char c : ingredientMap.keySet()) {
            RecipeChoice stack = ingredientMap.get(c);
            if (stack != null)
                ret.setIngredient(c, stack);
        }
        return ret;
    }

    @Override
    public void addToCraftingManager() {
        String[] shape = this.getShape();
        Map<Character, org.bukkit.inventory.RecipeChoice> ingred = this.getChoiceMap();
        int width = shape[0].length();
        DefaultedList<Ingredient> data = DefaultedList.ofSize(shape.length * width, Ingredient.EMPTY);

        for (int i = 0; i < shape.length; i++) {
            String row = shape[i];
            for (int j = 0; j < row.length(); j++)
                data.set(i * width + j, toNMS(ingred.get(row.charAt(j)), false));
        }

        ((IMixinRecipeManager)IMixinMinecraftServer.getServer().getRecipeManager()).addRecipe(getKey(), new net.minecraft.recipe.ShapedRecipe(this.getGroup(), RecipeInterface.getCategory(this.getCategory()), new RawShapedRecipe(width, shape.length, data, Optional.empty()), CraftItemStack.asNMSCopy(this.getResult())));
    }
    
    // TODO: Update API to 1.19.4
    public CraftingBookCategory getCategory() {
        return CraftingBookCategory.MISC;
    }

}
