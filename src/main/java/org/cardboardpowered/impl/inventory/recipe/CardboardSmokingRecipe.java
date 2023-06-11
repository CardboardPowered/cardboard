package org.cardboardpowered.impl.inventory.recipe;

import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmokingRecipe;
import org.bukkit.inventory.recipe.CookingBookCategory;

import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipeManager;

public class CardboardSmokingRecipe extends SmokingRecipe implements RecipeInterface {

    public CardboardSmokingRecipe(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookingTime) {
        super(key, result, source, experience, cookingTime);
    }

    public static CardboardSmokingRecipe fromBukkitRecipe(SmokingRecipe recipe) {
        if (recipe instanceof CardboardSmokingRecipe) return (CardboardSmokingRecipe) recipe;
        CardboardSmokingRecipe ret = new CardboardSmokingRecipe(recipe.getKey(), recipe.getResult(), recipe.getInputChoice(), recipe.getExperience(), recipe.getCookingTime());
        ret.setGroup(recipe.getGroup());
        return ret;
    }

    @Override
    public void addToCraftingManager() {
        ItemStack result = this.getResult();
        ((IMixinRecipeManager)IMixinMinecraftServer.getServer().getRecipeManager()).addRecipe(new net.minecraft.recipe.SmokingRecipe(CraftNamespacedKey.toMinecraft(this.getKey()), this.getGroup(), RecipeInterface.getCategory(this.getCategory()), toNMS(this.getInputChoice(), true), CraftItemStack.asNMSCopy(result), getExperience(), getCookingTime()));
    }

    
    // TODO: Update API to 1.19.4
    public CookingBookCategory getCategory() {
        return CookingBookCategory.MISC;
    }

}