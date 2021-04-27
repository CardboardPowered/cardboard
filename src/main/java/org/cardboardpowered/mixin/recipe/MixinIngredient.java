package org.cardboardpowered.mixin.recipe;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinIngredient;

import net.minecraft.item.ItemStack;
import net.minecraft.recipe.Ingredient;

@Mixin(Ingredient.class)
public class MixinIngredient implements IMixinIngredient {

    @Shadow public ItemStack[] matchingStacks;
    @Shadow public void cacheMatchingStacks() {}

    public boolean exact_BF;

    @Override
    public boolean getExact_BF() {
        return exact_BF;
    }

    @Override
    public void setExact_BF(boolean value) {
        exact_BF = value;
    }

    public boolean test(ItemStack itemstack) {
        if (itemstack == null) {
            return false;
        } else {
            this.cacheMatchingStacks();
            if (this.matchingStacks.length == 0) {
                return itemstack.isEmpty();
            } else {
                ItemStack[] aitemstack = this.matchingStacks;
                int i = aitemstack.length;

                for (int j = 0; j < i; ++j) {
                    ItemStack itemstack1 = aitemstack[j];

                    // Bukkit start
                    if (exact_BF) {
                        if (itemstack1.getItem() == itemstack.getItem() && ItemStack.areTagsEqual(itemstack, itemstack1))
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

}