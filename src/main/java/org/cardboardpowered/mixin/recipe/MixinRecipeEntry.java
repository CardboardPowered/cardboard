package org.cardboardpowered.mixin.recipe;

import com.javazilla.bukkitfabric.interfaces.IMixinRecipe;
import net.minecraft.recipe.BlastingRecipe;
import net.minecraft.recipe.CampfireCookingRecipe;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Recipe;
import net.minecraft.recipe.RecipeEntry;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.ShapelessRecipe;
import net.minecraft.recipe.SmeltingRecipe;
import net.minecraft.recipe.SmokingRecipe;
import net.minecraft.recipe.SpecialCraftingRecipe;
import net.minecraft.recipe.StonecuttingRecipe;
import net.minecraft.util.Identifier;
import net.minecraft.village.TradeOffer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.inventory.CraftMerchantRecipe;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.cardboardpowered.impl.inventory.recipe.CardboardBlastingRecipe;
import org.cardboardpowered.impl.inventory.recipe.CardboardCampfireRecipe;
import org.cardboardpowered.impl.inventory.recipe.CardboardComplexRecipe;
import org.cardboardpowered.impl.inventory.recipe.CardboardFurnaceRecipe;
import org.cardboardpowered.impl.inventory.recipe.CardboardShapedRecipe;
import org.cardboardpowered.impl.inventory.recipe.CardboardShapelessRecipe;
import org.cardboardpowered.impl.inventory.recipe.CardboardSmokingRecipe;
import org.cardboardpowered.impl.inventory.recipe.CardboardStonecuttingRecipe;
import org.cardboardpowered.impl.inventory.recipe.RecipeInterface;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RecipeEntry.class)
public class MixinRecipeEntry implements IMixinRecipe {

	@Override
	public org.bukkit.inventory.Recipe toBukkitRecipe() {
		RecipeEntry<?> recipeEntry = (RecipeEntry<?>) (Object) this;
		Recipe<?> nmsRecipe = recipeEntry.value();
		Identifier id = recipeEntry.id();

		if(nmsRecipe instanceof BlastingRecipe nms) {
			CraftItemStack result = CraftItemStack.asCraftMirror(nms.getResult(null));

			CardboardBlastingRecipe recipe = new CardboardBlastingRecipe(CraftNamespacedKey.fromMinecraft(id),
					result,
					RecipeInterface.toBukkit(nms.getIngredients().get(0)),
					nms.experience, nms.getCookingTime());
			recipe.setGroup(nms.group);

			return recipe;
		} else if(nmsRecipe instanceof CampfireCookingRecipe nms) {
			CraftItemStack result = CraftItemStack.asCraftMirror(nms.getResult(null));

			CardboardCampfireRecipe recipe = new CardboardCampfireRecipe(CraftNamespacedKey.fromMinecraft(id),
					result,
					RecipeInterface.toBukkit(nms.getIngredients().get(0)),
					nms.experience, nms.getCookingTime());
			recipe.setGroup(nms.group);

			return recipe;
		} else if(nmsRecipe instanceof ShapedRecipe nms) {
			CraftItemStack result = CraftItemStack.asCraftMirror(nms.getResult(null));
			CardboardShapedRecipe recipe = new CardboardShapedRecipe(id, result, nms);
			recipe.setGroup(nms.group);

			switch(nms.height) {
				case 1:
					switch(nms.width) {
						case 1:
							recipe.shape("a");
							break;
						case 2:
							recipe.shape("ab");
							break;
						case 3:
							recipe.shape("abc");
							break;
					}
					break;
				case 2:
					switch(nms.width) {
						case 1:
							recipe.shape("a", "b");
							break;
						case 2:
							recipe.shape("ab", "cd");
							break;
						case 3:
							recipe.shape("abc", "def");
							break;
					}
					break;
				case 3:
					switch(nms.width) {
						case 1:
							recipe.shape("a", "b", "c");
							break;
						case 2:
							recipe.shape("ab", "cd", "ef");
							break;
						case 3:
							recipe.shape("abc", "def", "ghi");
							break;
					}
					break;
			}
			char c = 'a';
			for(Ingredient list : nms.getIngredients()) {
				RecipeChoice choice = RecipeInterface.toBukkit(list);
				if(choice != null) recipe.setIngredient(c, choice);
				c++;
			}
			return recipe;
		} else if(nmsRecipe instanceof ShapelessRecipe nms) {
			CraftItemStack result = CraftItemStack.asCraftMirror(nms.getResult(null));
			CardboardShapelessRecipe recipe = new CardboardShapelessRecipe(id, result, nms);
			recipe.setGroup(nms.group);
			for(Ingredient list : nms.getIngredients())
				recipe.addIngredient(RecipeInterface.toBukkit(list));
			return recipe;
		} else if(nmsRecipe instanceof SmeltingRecipe nms) {
			CraftItemStack result = CraftItemStack.asCraftMirror(nms.getResult(null));

			CardboardFurnaceRecipe recipe = new CardboardFurnaceRecipe(CraftNamespacedKey.fromMinecraft(id),
					result,
					RecipeInterface.toBukkit(nms.getIngredients().get(0)),
					nms.experience, nms.getCookingTime());
			recipe.setGroup(nms.group);

			return recipe;
		} else if(nmsRecipe instanceof SmokingRecipe nms) {
			CraftItemStack result = CraftItemStack.asCraftMirror(nms.getResult(null));

			CardboardSmokingRecipe recipe = new CardboardSmokingRecipe(CraftNamespacedKey.fromMinecraft(id),
					result,
					RecipeInterface.toBukkit(nms.getIngredients().get(0)),
					nms.experience, nms.getCookingTime());
			recipe.setGroup(nms.group);

			return recipe;
		} else if(nmsRecipe instanceof StonecuttingRecipe nms) {
			CraftItemStack result = CraftItemStack.asCraftMirror(nms.getResult(null));

			CardboardStonecuttingRecipe recipe = new CardboardStonecuttingRecipe(
					CraftNamespacedKey.fromMinecraft(id),
					result,
					RecipeInterface.toBukkit(nms.getIngredients().get(0)));
			recipe.setGroup(nms.group);

			return recipe;
		} else if(nmsRecipe instanceof TradeOffer nms) {
			return new CraftMerchantRecipe(nms);
		} else if(nmsRecipe instanceof SpecialCraftingRecipe) {
			return new CardboardComplexRecipe((RecipeEntry<SpecialCraftingRecipe>) recipeEntry);
		} else {
			throw new IllegalArgumentException("Invalid recipe type: " + nmsRecipe.getClass());
		}

	}

}
