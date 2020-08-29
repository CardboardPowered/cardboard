package com.javazilla.bukkitfabric.mixin;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipeManager;

import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.Util;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;

@SuppressWarnings({"unchecked", "rawtypes"})
@Mixin(RecipeManager.class)
public class MixinRecipeManager implements IMixinRecipeManager {

    @Shadow public boolean errored;
    @Shadow public static Recipe<?> deserialize(Identifier minecraftkey, JsonObject jsonobject) {return null;}
    @Shadow public Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes = ImmutableMap.of();

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

    /*@Overwrite
    public void apply(Map<Identifier, JsonElement> map, ResourceManager iresourcemanager, Profiler gameprofilerfiller) {
        this.errored = false;
        // CraftBukkit start - SPIGOT-5667 make sure all types are populated and mutable
        HashMap<RecipeType<?>, Map<Identifier, Recipe<?>>>  map1 = Maps.newHashMap();
        for (Map.Entry<Identifier, JsonElement> entry2 : map.entrySet()) {
            Identifier identifier = entry2.getKey();
            try {
                Recipe<?> recipe = RecipeManager.deserialize(identifier, JsonHelper.asObject(entry2.getValue(), "top element"));
                map1.computeIfAbsent(recipe.getType(), recipeType -> new ImmutableMap.Builder<RecipeType<?>, Map<Identifier, Recipe<?>>>().build()).put(identifier, recipe);
            }
            catch (JsonParseException | IllegalArgumentException runtimeException) {
              //  LOGGER.error("Parsing error loading recipe {}", (Object)identifier, (Object)runtimeException);
            }
        }

        // CraftBukkit end
        Iterator iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Map.Entry<Identifier, JsonElement> entry = (Map.Entry) iterator.next();
            Identifier minecraftkey = (Identifier) entry.getKey();

            try {
                Recipe<?> irecipe = deserialize(minecraftkey, JsonHelper.asObject((JsonElement) entry.getValue(), "top element"));

                // CraftBukkit start - SPIGOT-4638: last recipe gets priority
                (map1.computeIfAbsent(irecipe.getType(), (recipes) -> {
                    return new Object2ObjectLinkedOpenHashMap<>();
                })).put(minecraftkey, irecipe);
                // CraftBukkit end
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                BukkitFabricMod.LOGGER.info("Parsing error loading recipe " + minecraftkey + " " +  jsonparseexception);
            }
        }

        this.recipes = (Map) map1.entrySet().stream().collect(ImmutableMap.toImmutableMap(Map.Entry::getKey, (entry1) -> {
            return (entry1.getValue()); // CraftBukkit
        }));
        BukkitFabricMod.LOGGER.info("Loaded " +  map1.size() + " recipes.");
    }*/

    @Overwrite
    public <C extends Inventory, T extends Recipe<C>> Map<Identifier, Recipe<C>> getAllOfType(RecipeType<T> recipes) {
        return (Map) this.recipes.getOrDefault(recipes, new Object2ObjectLinkedOpenHashMap<>()); // CraftBukkit
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