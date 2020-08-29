package com.javazilla.bukkitfabric.mixin;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.CraftSmokingRecipe;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;

import net.minecraft.recipe.SmokingRecipe;

@Mixin(SmokingRecipe.class)
public class MixinSmokingRecipe implements IMixinRecipe {

    @Override
    public Recipe toBukkitRecipe() {
        SmokingRecipe rrr = (SmokingRecipe)(Object)this;
        CraftItemStack result = CraftItemStack.asCraftMirror(rrr.output);

        CraftSmokingRecipe recipe = new CraftSmokingRecipe(CraftNamespacedKey.fromMinecraft(rrr.id), result, CraftRecipe.toBukkit(rrr.input), rrr.experience, rrr.cookTime);
        recipe.setGroup(rrr.group);

        return recipe;
    }

}