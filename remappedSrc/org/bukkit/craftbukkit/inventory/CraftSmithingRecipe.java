package org.bukkit.craftbukkit.inventory;

import net.minecraft.server.MinecraftServer;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.SmithingRecipe;

import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipeManager;

public class CraftSmithingRecipe extends SmithingRecipe implements CraftRecipe {
    public CraftSmithingRecipe(NamespacedKey key, ItemStack result, RecipeChoice base, RecipeChoice addition) {
        super(key, result, base, addition);
    }

    public static CraftSmithingRecipe fromBukkitRecipe(SmithingRecipe recipe) {
        if (recipe instanceof CraftSmithingRecipe) {
            return (CraftSmithingRecipe) recipe;
        }
        CraftSmithingRecipe ret = new CraftSmithingRecipe(recipe.getKey(), recipe.getResult(), recipe.getBase(), recipe.getAddition());
        return ret;
    }

    @Override
    public void addToCraftingManager() {
        ItemStack result = this.getResult();

        ((IMixinRecipeManager)IMixinMinecraftServer.getServer().getRecipeManager()).addRecipe(new net.minecraft.recipe.SmithingRecipe(CraftNamespacedKey.toMinecraft(this.getKey()), toNMS(this.getBase(), true), toNMS(this.getAddition(), true), CraftItemStack.asNMSCopy(result)));
    }
}
