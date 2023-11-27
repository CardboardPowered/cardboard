package org.cardboardpowered.mixin.recipe;

import net.minecraft.recipe.Ingredient;
import org.bukkit.inventory.Recipe;
import org.spongepowered.asm.mixin.Mixin;

//@Mixin(LegacySmithingRecipe.class)
@Mixin(Ingredient.class)
public class MixinSmithingRecipe /*implements IMixinRecipe*/ {

	// @Override
	public Recipe toBukkitRecipe() {
		// TODO Auto-generated method stub
		return null;
	}

    /*@Shadow private Ingredient base;
    @Shadow private Ingredient addition;
    @Shadow private ItemStack result;
    @Shadow public Identifier id;

    @Override
    public Recipe toBukkitRecipe() {
        CraftItemStack result = CraftItemStack.asCraftMirror(this.result);
        CardboardSmithingRecipe recipe = new CardboardSmithingRecipe(CraftNamespacedKey.fromMinecraft(this.id), result, RecipeInterface.toBukkit(this.base), RecipeInterface.toBukkit(this.addition));

        return recipe;
    }*/

}
