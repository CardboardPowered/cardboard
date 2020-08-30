package com.javazilla.bukkitfabric.mixin.recipe;

import java.util.Map;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonObject;
import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipeManager;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@Mixin(RecipeManager.class)
public class MixinRecipeManager implements IMixinRecipeManager {

    @Shadow public boolean errored;
    @Shadow public static Recipe<?> deserialize(Identifier minecraftkey, JsonObject jsonobject) {return null;}
    @Shadow public Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes = ImmutableMap.of();
    @Shadow public <C extends Inventory, T extends Recipe<C>> Map<Identifier, Recipe<C>> getAllOfType(RecipeType<T> recipes) {return null;}

    @Override
    public void addRecipe(Recipe<?> irecipe) {
        Map<Identifier, Recipe<?>> map = this.recipes.get(irecipe.getType()); // CraftBukkit

        if (map.containsKey(irecipe.getId())) {
            throw new IllegalStateException("Duplicate recipe ignored with ID " + irecipe.getId());
        } else {
            map.put(irecipe.getId(), irecipe);
        }
    }

    @Overwrite
    public <C extends Inventory, T extends Recipe<C>> Optional<T> getFirstMatch(RecipeType<T> recipes, C c0, World world) {
        Optional<T> recipe = this.getAllOfType(recipes).values().stream().flatMap((irecipe) -> {
            return Util.stream(recipes.get(irecipe, world, c0));
        }).findFirst();
        ((IMixinInventory)c0).setCurrentRecipe(recipe.orElse(null)); // CraftBukkit - Clear recipe when no recipe is found
        return recipe;
    }

    @Override
    public void clearRecipes() {
        this.recipes = Maps.newHashMap();
        for (RecipeType<?> recipeType : Registry.RECIPE_TYPE)
            this.recipes.put(recipeType, new Object2ObjectLinkedOpenHashMap<>());
    }

    @Override
    public Map<RecipeType<?>, Map<Identifier, Recipe<?>>> getRecipes() {
        return recipes;
    }

}