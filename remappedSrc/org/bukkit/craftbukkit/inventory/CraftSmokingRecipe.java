package org.bukkit.craftbukkit.inventory;

import net.minecraft.server.MinecraftServer;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmokingRecipe;

import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipeManager;

public class CraftSmokingRecipe extends SmokingRecipe implements CraftRecipe {
    public CraftSmokingRecipe(NamespacedKey key, ItemStack result, RecipeChoice source, float experience, int cookingTime) {
        super(key, result, source, experience, cookingTime);
    }

    public static CraftSmokingRecipe fromBukkitRecipe(SmokingRecipe recipe) {
        if (recipe instanceof CraftSmokingRecipe)
            return (CraftSmokingRecipe) recipe;
        CraftSmokingRecipe ret = new CraftSmokingRecipe(recipe.getKey(), recipe.getResult(), recipe.getInputChoice(), recipe.getExperience(), recipe.getCookingTime());
        ret.setGroup(recipe.getGroup());
        return ret;
    }

    @Override
    public void addToCraftingManager() {
        ItemStack result = this.getResult();
        ((IMixinRecipeManager)IMixinMinecraftServer.getServer().getRecipeManager()).addRecipe(new net.minecraft.recipe.SmokingRecipe(CraftNamespacedKey.toMinecraft(this.getKey()), this.getGroup(), toNMS(this.getInputChoice(), true), CraftItemStack.asNMSCopy(result), getExperience(), getCookingTime()));
    }
}
