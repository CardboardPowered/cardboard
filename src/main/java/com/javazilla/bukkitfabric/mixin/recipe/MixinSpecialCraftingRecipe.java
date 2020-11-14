package com.javazilla.bukkitfabric.mixin.recipe;

import org.spongepowered.asm.mixin.Mixin;

import org.cardboardpowered.impl.inventory.recipe.CardboardComplexRecipe;

import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;

import net.minecraft.recipe.SpecialCraftingRecipe;

@Mixin(SpecialCraftingRecipe.class)
public class MixinSpecialCraftingRecipe implements IMixinRecipe {

    @Override
    public org.bukkit.inventory.Recipe toBukkitRecipe() {
        return new CardboardComplexRecipe((SpecialCraftingRecipe)(Object)this);
    }

}