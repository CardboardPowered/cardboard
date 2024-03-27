package org.cardboardpowered.impl.inventory.recipe;

import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipeManager;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.CampfireRecipe;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.recipe.CookingBookCategory;

public class CardboardCampfireRecipe extends CampfireRecipe implements RecipeInterface {

    public CardboardCampfireRecipe(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookingTime) {
        super(key, result, source, experience, cookingTime);
    }

    public static CardboardCampfireRecipe fromBukkitRecipe(CampfireRecipe recipe) {
        if (recipe instanceof CardboardCampfireRecipe)
            return (CardboardCampfireRecipe) recipe;
        CardboardCampfireRecipe ret = new CardboardCampfireRecipe(recipe.getKey(), recipe.getResult(), recipe.getInputChoice(), recipe.getExperience(), recipe.getCookingTime());
        ret.setGroup(recipe.getGroup());
        return ret;
    }

    @Override
    public void addToCraftingManager() {
        ItemStack result = this.getResult();
        ((IMixinRecipeManager)IMixinMinecraftServer.getServer().getRecipeManager()).addRecipe(getKey(), new net.minecraft.recipe.CampfireCookingRecipe(this.getGroup(), RecipeInterface.getCategory(this.getCategory()), toNMS(this.getInputChoice(), true), CraftItemStack.asNMSCopy(result), getExperience(), getCookingTime()));
    }

    // TODO: Update API to 1.19.4
    public CookingBookCategory getCategory() {
        return CookingBookCategory.MISC;
    }
    
}
