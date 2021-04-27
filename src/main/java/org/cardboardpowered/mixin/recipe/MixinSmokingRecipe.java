package org.cardboardpowered.mixin.recipe;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

import org.cardboardpowered.impl.inventory.recipe.CardboardSmokingRecipe;
import org.cardboardpowered.impl.inventory.recipe.RecipeInterface;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;

import net.minecraft.recipe.SmokingRecipe;

@Mixin(SmokingRecipe.class)
public class MixinSmokingRecipe implements IMixinRecipe {

    @Override
    public Recipe toBukkitRecipe() {
        SmokingRecipe rrr = (SmokingRecipe)(Object)this;
        CraftItemStack result = CraftItemStack.asCraftMirror(rrr.output);

        CardboardSmokingRecipe recipe = new CardboardSmokingRecipe(CraftNamespacedKey.fromMinecraft(rrr.id), result, RecipeInterface.toBukkit(rrr.input), rrr.experience, rrr.cookTime);
        recipe.setGroup(rrr.group);

        return recipe;
    }

}