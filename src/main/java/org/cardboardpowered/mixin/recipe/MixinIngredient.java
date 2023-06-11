package org.cardboardpowered.mixin.recipe;

import org.cardboardpowered.interfaces.IIngredient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;
import net.minecraft.recipe.Ingredient.Entry;

@Mixin(Ingredient.class)
public class MixinIngredient implements IIngredient {

	@Shadow public Entry[] entries;
    @Shadow public ItemStack[] matchingStacks;
    // @Shadow public void cacheMatchingStacks() {}

    public boolean exact_BF;

    @Override
    public boolean getExact_BF() {
        return exact_BF;
    }

    @Override
    public void setExact_BF(boolean value) {
        exact_BF = value;
    }
    
    @Shadow
    public ItemStack[] getMatchingStacks() {
    	return null;
    }

    public boolean test(ItemStack itemstack) {
        if (itemstack == null) {
            return false;
        } else {
            if (this.entries.length == 0) {
            	return itemstack.isEmpty();
            }
            /*if (this.matchingStacks.length == 0) {
                return itemstack.isEmpty();
            }*/
                ItemStack[] aitemstack = getMatchingStacks();
                int i = aitemstack.length;

                for (int j = 0; j < i; ++j) {
                    ItemStack itemstack1 = aitemstack[j];

                    // Bukkit start
                    if (exact_BF) {
                        if (itemstack1.getItem() == itemstack.getItem() && ItemStack.areNbtEqual(itemstack, itemstack1))
                            return true;
                        continue;
                    }
                    // Bukkit end
                    if (itemstack1.getItem() == itemstack.getItem())
                        return true;
                }

                return false;
            
        }
    }

}