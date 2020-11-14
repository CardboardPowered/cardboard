package org.cardboardpowered.impl.inventory.recipe;

import net.minecraft.server.MinecraftServer;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.StonecuttingRecipe;

import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipeManager;

public class CardboardStonecuttingRecipe extends StonecuttingRecipe implements RecipeInterface {

    public CardboardStonecuttingRecipe(NamespacedKey key, ItemStack result, RecipeChoice source) {
        super(key, result, source);
    }

    public static CardboardStonecuttingRecipe fromBukkitRecipe(StonecuttingRecipe recipe) {
        if (recipe instanceof CardboardStonecuttingRecipe)
            return (CardboardStonecuttingRecipe) recipe;
        CardboardStonecuttingRecipe ret = new CardboardStonecuttingRecipe(recipe.getKey(), recipe.getResult(), recipe.getInputChoice());
        ret.setGroup(recipe.getGroup());
        return ret;
    }

    @Override
    public void addToCraftingManager() {
        ItemStack result = this.getResult();
        ((IMixinRecipeManager)IMixinMinecraftServer.getServer().getRecipeManager()).addRecipe(new net.minecraft.recipe.StonecuttingRecipe(CraftNamespacedKey.toMinecraft(this.getKey()), this.getGroup(), toNMS(this.getInputChoice(), true), CraftItemStack.asNMSCopy(result)));
    }

}