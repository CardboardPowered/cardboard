package com.javazilla.bukkitfabric.interfaces;

import java.util.Map;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;

public interface IMixinRecipeManager {

    public void addRecipe(Recipe<?> irecipe);

    public Map<RecipeType<?>, Object2ObjectLinkedOpenHashMap<Identifier, Recipe<?>>> getRecipes();

    public void clearRecipes();

}