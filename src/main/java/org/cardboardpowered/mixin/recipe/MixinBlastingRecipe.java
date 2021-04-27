package org.cardboardpowered.mixin.recipe;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.Recipe;
import org.cardboardpowered.impl.inventory.recipe.CardboardBlastingRecipe;
import org.spongepowered.asm.mixin.Mixin;

import org.cardboardpowered.impl.inventory.recipe.RecipeInterface;
import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;

import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.BlastingRecipe;

@Mixin(BlastingRecipe.class)
public class MixinBlastingRecipe implements IMixinRecipe {

    @Override
    public Recipe toBukkitRecipe() {
        AbstractCookingRecipe nms = (AbstractCookingRecipe)(Object)this;
        CraftItemStack result = CraftItemStack.asCraftMirror(nms.output);

        CardboardBlastingRecipe recipe = new CardboardBlastingRecipe(CraftNamespacedKey.fromMinecraft(nms.id), result, RecipeInterface.toBukkit(nms.input), nms.experience, nms.cookTime);
        recipe.setGroup(nms.group);

        return recipe;
    }

}