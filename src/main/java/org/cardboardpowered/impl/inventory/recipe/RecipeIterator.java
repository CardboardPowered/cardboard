package org.cardboardpowered.impl.inventory.recipe;

import com.javazilla.bukkitfabric.interfaces.IMixinMinecraftServer;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipeManager;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeType;
import net.minecraft.util.Identifier;
import org.bukkit.inventory.Recipe;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

public class RecipeIterator implements Iterator<Recipe> {

    private final Iterator<Entry<RecipeType<?>, Map<Identifier, RecipeEntry<?>>>> recipes;
    private Iterator<RecipeEntry<?>> current;

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
        return ((IMixinRecipe)(Object) current.next()).toBukkitRecipe();
    }

    @Override
    public void remove() {
        if (current == null) throw new IllegalStateException("next() not yet called");
        current.remove();
    }

}
