package org.cardboardpowered.mixin.recipe;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipeManager;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.RecipeManager;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.Registries;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

@Mixin(RecipeManager.class)
public class MixinRecipeManager implements IMixinRecipeManager {

    @Invoker("deserialize")
    protected static RecipeEntry<?> method_17720(Identifier minecraftkey, JsonObject jsonobject) {
        return null;
    }

    @Shadow public boolean errored;
    @Shadow public Map<RecipeType<?>, Map<Identifier, RecipeEntry<?>>> recipes = ImmutableMap.of();
    @Shadow public <C extends Inventory, T extends Recipe<C>> Map<Identifier, Recipe<C>> getAllOfType(RecipeType<T> recipes) {return null;}

    @Unique private static final Logger LOGGER_BF = LogManager.getLogger("Bukkit|RecipeManager");

    /**
     * @author BukkitFabric
     * @reason Properly fill recipe map
     */
    @Inject(at = @At("TAIL"), method = "apply(Ljava/util/Map;Lnet/minecraft/resource/ResourceManager;Lnet/minecraft/util/profiler/Profiler;)V")
    public void apply(Map<Identifier, JsonElement> map, ResourceManager iresourcemanager, Profiler gameprofilerfiller, CallbackInfo ci) {
        this.errored = false;
        Map<RecipeType<?>, Map<Identifier, RecipeEntry<?>>> map1 = Maps.newHashMap();
        for (RecipeType<?> recipeType : Registries.RECIPE_TYPE)
            map1.put(recipeType, new HashMap<>());

	    for(Entry<Identifier, JsonElement> entry : map.entrySet()) {
		    Identifier minecraftkey = entry.getKey();

		    try {
                RecipeEntry<?> irecipe = method_17720(minecraftkey, JsonHelper.asObject(entry.getValue(), "top element"));
			    (map1.computeIfAbsent(irecipe.value().getType(), (recipes) -> new Object2ObjectLinkedOpenHashMap<>())).put(minecraftkey, irecipe);
		    } catch(IllegalArgumentException | JsonParseException jsonparseexception) {
			    LOGGER_BF.error("Parsing error loading recipe {}", minecraftkey, jsonparseexception);
		    }
	    }

        this.recipes = map1.entrySet().stream().collect(ImmutableMap.toImmutableMap(Entry::getKey, Entry::getValue));
        LOGGER_BF.info("Loaded " + map1.size() + " recipes");
    }

    @Override
    public void addRecipe(RecipeEntry<?> entry) {
        Map<Identifier, RecipeEntry<?>> map = this.recipes.get(entry.value().getType());
        if (map.containsKey(entry.id()))
            throw new IllegalStateException("Duplicate recipe ignored with ID " + entry.toString());
        else
            map.put(entry.id(), entry);
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
        for (RecipeType<?> recipeType : Registries.RECIPE_TYPE)
            this.recipes.put(recipeType, new Object2ObjectLinkedOpenHashMap<>());
    }

    @Override
    public Map<RecipeType<?>, Map<Identifier, RecipeEntry<?>>> getRecipes() {
        return recipes;
    }

}
