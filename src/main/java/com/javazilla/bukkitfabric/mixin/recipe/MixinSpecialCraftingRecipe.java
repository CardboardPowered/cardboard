package com.javazilla.bukkitfabric.mixin.recipe;

import org.spongepowered.asm.mixin.Mixin;

import com.javazilla.bukkitfabric.impl.inventory.recipe.CraftComplexRecipe;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;

import net.minecraft.recipe.SpecialCraftingRecipe;

@Mixin(SpecialCraftingRecipe.class)
public class MixinSpecialCraftingRecipe implements IMixinRecipe {

    @Override
    public org.bukkit.inventory.Recipe toBukkitRecipe() {
        return new CraftComplexRecipe((SpecialCraftingRecipe)(Object)this);
    }

}