package org.cardboardpowered.mixin.world.item.crafting;

import net.minecraft.world.item.crafting.*;
import org.bukkit.craftbukkit.inventory.*;
import org.cardboardpowered.bridge.world.item.crafting.RecipeHolderBridge;
import java.util.Optional;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffer;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.inventory.RecipeChoice;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(RecipeHolder.class)
public class RecipeHolderMixin implements RecipeHolderBridge {
	// Campfire
    public org.bukkit.inventory.Recipe toBukkitRecipe(CampfireCookingRecipe thiz, NamespacedKey id) {
        CraftItemStack result = CraftItemStack.asCraftMirror(thiz.result().create());
        CraftCampfireRecipe recipe = new CraftCampfireRecipe(id, result, CraftRecipe.toBukkit(thiz.input), thiz.experience, thiz.cookingTime());
        recipe.setGroup(thiz.group());
        recipe.setCategory(CraftRecipe.getCategory(thiz.category()));
        return recipe;
    }

    // Blasting
    public org.bukkit.inventory.Recipe toBukkitRecipe(BlastingRecipe thiz, NamespacedKey id) {
        CraftItemStack result = CraftItemStack.asCraftMirror(thiz.result().create());
        CraftBlastingRecipe recipe = new CraftBlastingRecipe(id, result, CraftRecipe.toBukkit(thiz.input), thiz.experience, thiz.cookingTime());
        recipe.setGroup(thiz.group());
        recipe.setCategory(CraftRecipe.getCategory(thiz.category()));
        return recipe;
    }

    // Smoking
    public org.bukkit.inventory.Recipe toBukkitRecipe(SmokingRecipe thiz, NamespacedKey id) {
        CraftItemStack result = CraftItemStack.asCraftMirror(thiz.result().create());
        CraftSmokingRecipe recipe = new CraftSmokingRecipe(id, result, CraftRecipe.toBukkit(thiz.input), thiz.experience, thiz.cookingTime());
        recipe.setGroup(thiz.group());
        recipe.setCategory(CraftRecipe.getCategory(thiz.category()));
        return recipe;
    }

    // Stonecutting
    public org.bukkit.inventory.Recipe toBukkitRecipe(StonecutterRecipe thiz, NamespacedKey id) {
        CraftItemStack result = CraftItemStack.asCraftMirror(thiz.result().create());
        CraftStonecuttingRecipe recipe = new CraftStonecuttingRecipe(id, result, CraftRecipe.toBukkit(thiz.input()));
        recipe.setGroup(thiz.group());
        return recipe;
    }

	// SpecialCraftingRecipe
	public org.bukkit.inventory.Recipe toBukkitRecipe(net.minecraft.world.item.crafting.CraftingRecipe thiz, NamespacedKey id) {
        CraftItemStack result = CraftItemStack.asCraftMirror(ItemStack.EMPTY);
        CraftComplexRecipe recipe = new CraftComplexRecipe(id, result, thiz);
        recipe.setGroup(thiz.group());
        recipe.setCategory(CraftRecipe.getCategory(thiz.category()));
        return recipe;
    }


	private org.bukkit.inventory.Recipe toBukkitRecipe(TransmuteRecipe thiz, NamespacedKey id) {
		return new org.bukkit.craftbukkit.inventory.CraftTransmuteRecipe(id, org.bukkit.craftbukkit.inventory.CraftItemType.minecraftToBukkit(thiz.result.item().value()), org.bukkit.craftbukkit.inventory.CraftRecipe.toBukkit(thiz.input), org.bukkit.craftbukkit.inventory.CraftRecipe.toBukkit(thiz.material));
	}

	private org.bukkit.inventory.Recipe toBukkitRecipe(SmithingTrimRecipe thiz, NamespacedKey id) {
		return new org.bukkit.craftbukkit.inventory.CraftSmithingTrimRecipe(id, org.bukkit.craftbukkit.inventory.CraftRecipe.toBukkit(thiz.template), org.bukkit.craftbukkit.inventory.CraftRecipe.toBukkit(thiz.base), org.bukkit.craftbukkit.inventory.CraftRecipe.toBukkit(thiz.addition), org.bukkit.craftbukkit.inventory.trim.CraftTrimPattern.minecraftHolderToBukkit(thiz.pattern));
	}

	private org.bukkit.inventory.Recipe toBukkitRecipe(SmithingTransformRecipe thiz, NamespacedKey id) {
		org.bukkit.craftbukkit.inventory.CraftItemStack result = org.bukkit.craftbukkit.inventory.CraftItemStack.asCraftMirror(new ItemStack(thiz.result.item(), thiz.result.count(), thiz.result.components()));

		org.bukkit.craftbukkit.inventory.CraftSmithingTransformRecipe recipe = new org.bukkit.craftbukkit.inventory.CraftSmithingTransformRecipe(id, result, org.bukkit.craftbukkit.inventory.CraftRecipe.toBukkit(thiz.template), org.bukkit.craftbukkit.inventory.CraftRecipe.toBukkit(thiz.base), org.bukkit.craftbukkit.inventory.CraftRecipe.toBukkit(thiz.addition));

		return recipe;
	}

	@Override
	public org.bukkit.inventory.Recipe toBukkitRecipe() {
		RecipeHolder<?> recipeEntry = (RecipeHolder<?>) (Object) this;
		Recipe<?> nmsRecipe = recipeEntry.value();
		ResourceKey<Recipe<?>> id = recipeEntry.id();

		if(nmsRecipe instanceof BlastingRecipe nms) {
			/*
			CraftItemStack result = CraftItemStack.asCraftMirror(nms.getResult(null));

			CardboardBlastingRecipe recipe = new CardboardBlastingRecipe(CraftNamespacedKey.fromMinecraft(id),
					result,
					RecipeInterface.toBukkit(nms.getIngredients().get(0)),
					nms.experience, nms.getCookingTime());
			recipe.setGroup(nms.getGroup());

			return recipe;
			*/
			return toBukkitRecipe(nms, CraftNamespacedKey.fromMinecraft(id.identifier()));
		} else if(nmsRecipe instanceof CampfireCookingRecipe nms) {

			return toBukkitRecipe(nms, CraftNamespacedKey.fromMinecraft(id.identifier()));

			/*
			CraftItemStack result = CraftItemStack.asCraftMirror(nms.getResult(null));

			CardboardCampfireRecipe recipe = new CardboardCampfireRecipe(CraftNamespacedKey.fromMinecraft(id),
					result,
					RecipeInterface.toBukkit(nms.getIngredients().get(0)),
					nms.experience, nms.getCookingTime());
			recipe.setGroup(nms.getGroup());

			return recipe;
			*/
		} else if(nmsRecipe instanceof ShapedRecipe nms) {
			CraftItemStack result = CraftItemStack.asCraftMirror(nms.result.create());
			CraftShapedRecipe recipe = new CraftShapedRecipe(CraftNamespacedKey.fromMinecraft(id.identifier()), result, nms);
			recipe.setGroup(nms.group());

			switch(nms.getHeight()) {
				case 1:
					switch(nms.getWidth()) {
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
					switch(nms.getWidth()) {
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
					switch(nms.getWidth()) {
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
			for(Optional<Ingredient> list : nms.getIngredients()) {
				RecipeChoice choice = CraftRecipe.toBukkit(list);

				if (choice != RecipeChoice.empty()) {
					 recipe.setIngredient(c, choice);
				}

				// if(choice != null) recipe.setIngredient(c, choice);
				c++;
			}
			return recipe;
		} else if(nmsRecipe instanceof ShapelessRecipe nms) {
			CraftItemStack result = CraftItemStack.asCraftMirror(nms.result.create());
			CraftShapelessRecipe recipe = new CraftShapelessRecipe(CraftNamespacedKey.fromMinecraft(id.identifier()), result, nms);
			recipe.setGroup(nms.group());
			for(Ingredient list : nms.ingredients)
				recipe.addIngredient(CraftRecipe.toBukkit(list));
			return recipe;
		} else if(nmsRecipe instanceof SmeltingRecipe nms) {
			CraftItemStack result = CraftItemStack.asCraftMirror(nms.result.create());

			CraftFurnaceRecipe recipe = new CraftFurnaceRecipe(CraftNamespacedKey.fromMinecraft(id.identifier()),
					result,
					CraftRecipe.toBukkit(nms.input()),
					nms.experience, nms.cookingTime());
			recipe.setGroup(nms.group());

			return recipe;
		} else if(nmsRecipe instanceof SmokingRecipe nms) {
			/*
			CraftItemStack result = CraftItemStack.asCraftMirror(nms.getResult(null));

			CardboardSmokingRecipe recipe = new CardboardSmokingRecipe(CraftNamespacedKey.fromMinecraft(id),
					result,
					RecipeInterface.toBukkit(nms.getIngredients().get(0)),
					nms.experience, nms.getCookingTime());
			recipe.setGroup(nms.group);

			return recipe;
			*/
			return toBukkitRecipe(nms, CraftNamespacedKey.fromMinecraft(id.identifier()));
		} else if(nmsRecipe instanceof StonecutterRecipe nms) {
			/*
			CraftItemStack result = CraftItemStack.asCraftMirror(nms.getResult(null));

			CardboardStonecuttingRecipe recipe = new CardboardStonecuttingRecipe(
					CraftNamespacedKey.fromMinecraft(id),
					result,
					RecipeInterface.toBukkit(nms.getIngredients().get(0)));
			recipe.setGroup(nms.getGroup());

			return recipe;
			*/
			return toBukkitRecipe(nms, CraftNamespacedKey.fromMinecraft(id.identifier()));
		} else if(nmsRecipe instanceof MerchantOffer nms) {
			return new CraftMerchantRecipe(nms);
		} else if(nmsRecipe instanceof CustomRecipe nms) {

			return toBukkitRecipe(nms, CraftNamespacedKey.fromMinecraft(id.identifier()));
			// return new CardboardComplexRecipe((RecipeEntry<SpecialCraftingRecipe>) recipeEntry);
		} else if(nmsRecipe instanceof SmithingTransformRecipe nms) {
			return toBukkitRecipe(nms, CraftNamespacedKey.fromMinecraft(id.identifier()));
		} else if(nmsRecipe instanceof SmithingTrimRecipe nms) {
			return toBukkitRecipe(nms, CraftNamespacedKey.fromMinecraft(id.identifier()));
		} else if(nmsRecipe instanceof TransmuteRecipe nms) {
			return toBukkitRecipe(nms, CraftNamespacedKey.fromMinecraft(id.identifier()));
		} else if(nmsRecipe instanceof net.minecraft.world.item.crafting.CraftingRecipe nms) {
			// Crafting recipes with no Bukkit shape (DyeRecipe, ImbueRecipe) are complex recipes.
			return toBukkitRecipe(nms, CraftNamespacedKey.fromMinecraft(id.identifier()));
		} else {
			throw new IllegalArgumentException("Invalid recipe type: " + nmsRecipe.getClass());
		}

	}
}
