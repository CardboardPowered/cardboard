package org.cardboardpowered.mixin.recipe;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
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

@Mixin(RecipeManager.class)
public class MixinRecipeManager implements IMixinRecipeManager {

    @Shadow public boolean errored;
    @Shadow public static Recipe<?> deserialize(Identifier minecraftkey, JsonObject jsonobject) {return null;}
    @Shadow public Map<RecipeType<?>, Map<Identifier, Recipe<?>>> recipes = ImmutableMap.of();
    @Shadow public <C extends Inventory, T extends Recipe<C>> Map<Identifier, Recipe<C>> getAllOfType(RecipeType<T> recipes) {return null;}

    private static final Logger LOGGER_BF = LogManager.getLogger("Bukkit|RecipeManager");

    /**
     * @author BukkitFabric
     * @reason Properly fill recipe map
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    @Inject(at = @At("TAIL"), method = "apply")
    public void apply(Map<Identifier, JsonElement> map, ResourceManager iresourcemanager, Profiler gameprofilerfiller, CallbackInfo ci) {
        this.errored = false;
        Map<RecipeType<?>, Map<Identifier, Recipe<?>>> map1 = Maps.newHashMap();
        for (RecipeType<?> recipeType : Registry.RECIPE_TYPE)
            map1.put(recipeType, new HashMap<>());
        Iterator<Entry<Identifier, JsonElement>> iterator = map.entrySet().iterator();

        while (iterator.hasNext()) {
            Entry<Identifier, JsonElement> entry = (Entry) iterator.next();
            Identifier minecraftkey = (Identifier) entry.getKey();

            try {
                Recipe<?> irecipe = deserialize(minecraftkey, JsonHelper.asObject((JsonElement) entry.getValue(), "top element"));
                (map1.computeIfAbsent(irecipe.getType(), (recipes) -> new Object2ObjectLinkedOpenHashMap<>())).put(minecraftkey, irecipe);
            } catch (IllegalArgumentException | JsonParseException jsonparseexception) {
                LOGGER_BF.error("Parsing error loading recipe {}", minecraftkey, jsonparseexception);
            }
        }

        this.recipes = (Map) map1.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, (entry1) -> entry1.getValue()));
        LOGGER_BF.info("Loaded " + map1.size() + " recipes");
    }

    @Override
    public void addRecipe(Recipe<?> irecipe) {
        Map<Identifier, Recipe<?>> map = this.recipes.get(irecipe.getType());
        if (map.containsKey(irecipe.getId()))
            throw new IllegalStateException("Duplicate recipe ignored with ID " + irecipe.getId());
        else map.put(irecipe.getId(), irecipe);
    }

    /**
     * @author BukkitFabric
     * @reason Clear when no recipe is found
     */
    // FIXME: 1.18.2
    /*@Overwrite
    public <C extends Inventory, T extends Recipe<C>> Optional<T> getFirstMatch(RecipeType<T> recipes, C c0, World world) {
        Optional<T> recipe = this.getAllOfType(recipes).values().stream().flatMap((irecipe) -> Util.stream(recipes.match(irecipe, world, c0))).findFirst();
        ((IMixinInventory)c0).setCurrentRecipe(recipe.orElse(null)); // Clear recipe when no recipe is found
        return recipe;
    }*/

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