package com.javazilla.bukkitfabric.mixin.recipe;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

import org.cardboardpowered.impl.inventory.recipe.CardboardFurnaceRecipe;
import org.cardboardpowered.impl.inventory.recipe.RecipeInterface;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;

import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.SmeltingRecipe;

@Mixin(SmeltingRecipe.class)
public class MixinSmeltingRecipe implements IMixinRecipe {

    @Override
    public Recipe toBukkitRecipe() {
        AbstractCookingRecipe nms = (AbstractCookingRecipe)(Object)this;
        CraftItemStack result = CraftItemStack.asCraftMirror(nms.output);

        CardboardFurnaceRecipe recipe = new CardboardFurnaceRecipe(CraftNamespacedKey.fromMinecraft(nms.id), result, RecipeInterface.toBukkit(nms.input), nms.experience, nms.cookTime);
        recipe.setGroup(nms.group);

        return recipe;
    }

}