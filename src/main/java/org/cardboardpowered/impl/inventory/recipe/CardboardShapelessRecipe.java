package org.cardboardpowered.impl.inventory.recipe;

import java.util.List;
import net.minecraft.recipe.Ingredient;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.collection.DefaultedList;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;

import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipeManager;

public class CardboardShapelessRecipe extends ShapelessRecipe implements RecipeInterface {

    private net.minecraft.recipe.ShapelessRecipe recipe;

    public CardboardShapelessRecipe(NamespacedKey key, ItemStack result) {
        super(key, result);
    }

    public CardboardShapelessRecipe(ItemStack result, net.minecraft.recipe.ShapelessRecipe recipe) {
        this(CraftNamespacedKey.fromMinecraft(recipe.getId()), result);
        this.recipe = recipe;
    }

    public static CardboardShapelessRecipe fromBukkitRecipe(ShapelessRecipe recipe) {
        if (recipe instanceof CardboardShapelessRecipe) return (CardboardShapelessRecipe) recipe;

        CardboardShapelessRecipe ret = new CardboardShapelessRecipe(recipe.getKey(), recipe.getResult());
        ret.setGroup(recipe.getGroup());
        for (RecipeChoice ingred : recipe.getChoiceList()) ret.addIngredient(ingred);
        return ret;
    }

    @Override
    public void addToCraftingManager() {
        List<org.bukkit.inventory.RecipeChoice> ingred = this.getChoiceList();
        DefaultedList<Ingredient> data = DefaultedList.ofSize(ingred.size(), Ingredient.EMPTY);
        for (int i = 0; i < ingred.size(); i++) data.set(i, toNMS(ingred.get(i), true));

        ((IMixinRecipeManager)IMixinMinecraftServer.getServer().getRecipeManager()).addRecipe(new net.minecraft.recipe.ShapelessRecipe(CraftNamespacedKey.toMinecraft(this.getKey()), this.getGroup(), CraftItemStack.asNMSCopy(this.getResult()), data));
    }

}