package com.javazilla.bukkitfabric.mixin.recipe;

import org.bukkit.craftbukkit.inventory.CraftCampfireRecipe;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;

import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.CampfireCookingRecipe;

@Mixin(CampfireCookingRecipe.class)
public class MixinCampfireCookingRecipe implements IMixinRecipe {

    @Override
    public Recipe toBukkitRecipe() {
        AbstractCookingRecipe nms = (AbstractCookingRecipe)(Object)this;
        CraftItemStack result = CraftItemStack.asCraftMirror(nms.output);

        CraftCampfireRecipe recipe = new CraftCampfireRecipe(CraftNamespacedKey.fromMinecraft(nms.id), result, CraftRecipe.toBukkit(nms.input), nms.experience, nms.cookTime);
        recipe.setGroup(nms.group);

        return recipe;
    }

}