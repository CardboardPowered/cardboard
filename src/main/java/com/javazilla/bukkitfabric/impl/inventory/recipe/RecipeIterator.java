package com.javazilla.bukkitfabric.impl.inventory.recipe;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.bukkit.inventory.Recipe;

import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipeManager;

import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;

public class RecipeIterator implements Iterator<Recipe> {

    private final Iterator<Entry<RecipeType<?>, Map<Identifier, net.minecraft.recipe.Recipe<?>>>> recipes;
    private Iterator<net.minecraft.recipe.Recipe<?>> current;

    public RecipeIterator() {
        this.recipes = ((IMixinRecipeManager)IMixinMinecraftServer.getServer().getRecipeManager()).getRecipes().entrySet().iterator();
    }

    @Override
    public boolean hasNext() {
        return (current != null && current.hasNext()) || recipes.hasNext();
    }

    @Override
    public Recipe next() {
        if (current == null || !current.hasNext()) current = recipes.next().getValue().values().iterator();
        return ((IMixinRecipe)current.next()).toBukkitRecipe();
    }

    @Override
    public void remove() {
        if (current == null) throw new IllegalStateException("next() not yet called");
        current.remove();
    }

}