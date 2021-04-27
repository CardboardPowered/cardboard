package org.cardboardpowered.mixin.entity.block;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockCookEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;

@Mixin(CampfireBlockEntity.class)
public class MixinCampfireBlockEntity {

    @Shadow
    public DefaultedList<ItemStack> itemsBeingCooked;

    /**
     * @author BukkitFabric
     * @reason BlockCookEvent
     */
    @Overwrite
    public void updateItemsBeingCooked() {
        CampfireBlockEntity nms = (CampfireBlockEntity)(Object)this;
        for (int i = 0; i < this.itemsBeingCooked.size(); ++i) {
            ItemStack itemstack = (ItemStack) this.itemsBeingCooked.get(i);

            if (!itemstack.isEmpty()) {
                nms.cookingTimes[i]++;

                if (nms.cookingTimes[i] >= nms.cookingTotalTimes[i]) {
                    SimpleInventory inventorysubcontainer = new SimpleInventory(new ItemStack[]{itemstack});
                    ItemStack itemstack1 = (ItemStack) nms.world.getRecipeManager().getFirstMatch(RecipeType.CAMPFIRE_COOKING, inventorysubcontainer, nms.world).map((recipecampfire) -> {
                        return recipecampfire.craft(inventorysubcontainer);
                    }).orElse(itemstack);
                    BlockPos blockposition = nms.getPos();

                    CraftItemStack source = CraftItemStack.asCraftMirror(itemstack);
                    org.bukkit.inventory.ItemStack result = CraftItemStack.asBukkitCopy(itemstack1);

                    BlockCookEvent blockCookEvent = new BlockCookEvent(CraftBlock.at((ServerWorld) nms.world, nms.pos), source, result);
                    CraftServer.INSTANCE.getPluginManager().callEvent(blockCookEvent);

                    if (blockCookEvent.isCancelled()) return;

                    result = blockCookEvent.getResult();
                    itemstack1 = CraftItemStack.asNMSCopy(result);

                    ItemScatterer.spawn(nms.world, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), itemstack1);
                    this.itemsBeingCooked.set(i, ItemStack.EMPTY);
                    nms.updateListeners();
                }
            }
        }

    }

}