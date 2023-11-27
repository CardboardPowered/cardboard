package org.cardboardpowered.impl.inventory.recipe;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingRecipe;

public class CardboardSmithingRecipe extends SmithingRecipe implements RecipeInterface {

    public CardboardSmithingRecipe(NamespacedKey key, ItemStack result, RecipeChoice base, RecipeChoice addition) {
        super(key, result, base, addition);
    }

    public static CardboardSmithingRecipe fromBukkitRecipe(SmithingRecipe recipe) {
        if (recipe instanceof CardboardSmithingRecipe) return (CardboardSmithingRecipe) recipe;
        CardboardSmithingRecipe ret = new CardboardSmithingRecipe(recipe.getKey(), recipe.getResult(), recipe.getBase(), recipe.getAddition());
        return ret;
    }

    @Override
    public void addToCraftingManager() {
        ItemStack result = this.getResult();
        // TODO: 1.20
        // ((IMixinRecipeManager)IMixinMinecraftServer.getServer().getRecipeManager()).addRecipe(new net.minecraft.recipe.LegacySmithingRecipe(CraftNamespacedKey.toMinecraft(this.getKey()), toNMS(this.getBase(), true), toNMS(this.getAddition(), true), CraftItemStack.asNMSCopy(result)));
    }

}
