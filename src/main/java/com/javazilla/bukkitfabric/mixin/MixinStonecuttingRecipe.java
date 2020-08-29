package com.javazilla.bukkitfabric.mixin;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftRecipe;
import org.bukkit.craftbukkit.inventory.CraftStonecuttingRecipe;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;

import net.minecraft.recipe.StonecuttingRecipe;

@Mixin(StonecuttingRecipe.class)
public class MixinStonecuttingRecipe implements IMixinRecipe {

    @Override
    public Recipe toBukkitRecipe() {
        CraftItemStack result = CraftItemStack.asCraftMirror(((StonecuttingRecipe)(Object)this).output);

        CraftStonecuttingRecipe recipe = new CraftStonecuttingRecipe(CraftNamespacedKey.fromMinecraft(((StonecuttingRecipe)(Object)this).id),
                result, CraftRecipe.toBukkit(((StonecuttingRecipe)(Object)this).input));
        recipe.setGroup(((StonecuttingRecipe)(Object)this).group);

        return recipe;
    }

}