package org.cardboardpowered.mixin.entity.block;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.block.CraftBlock;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.event.block.BlockCookEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.CampfireBlockEntity;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.RecipeType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ItemScatterer;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

@Mixin(CampfireBlockEntity.class)
public class MixinCampfireBlockEntity {

    @Shadow
    public DefaultedList<ItemStack> itemsBeingCooked;

    /**
     * @author Cardboard
     * @reason BlockCookEvent
     */
    @Overwrite
    public static void litServerTick(World world, BlockPos pos, BlockState state, CampfireBlockEntity be) {
        for (int i = 0; i < be.getItemsBeingCooked().size(); ++i) {
            ItemStack itemstack = (ItemStack) be.getItemsBeingCooked().get(i);

            if (!itemstack.isEmpty()) {
                be.cookingTimes[i]++;

                if (be.cookingTimes[i] >= be.cookingTotalTimes[i]) {
                    SimpleInventory inventorysubcontainer = new SimpleInventory(new ItemStack[]{itemstack});
                    ItemStack itemstack1 = (ItemStack) be.world.getRecipeManager().getFirstMatch(RecipeType.CAMPFIRE_COOKING, inventorysubcontainer, be.world).map((recipecampfire) -> {
                        return recipecampfire.craft(inventorysubcontainer);
                    }).orElse(itemstack);
                    BlockPos blockposition = be.getPos();

                    CraftItemStack source = CraftItemStack.asCraftMirror(itemstack);
                    org.bukkit.inventory.ItemStack result = CraftItemStack.asBukkitCopy(itemstack1);

                    BlockCookEvent blockCookEvent = new BlockCookEvent(CraftBlock.at((ServerWorld) be.world, be.pos), source, result);
                    CraftServer.INSTANCE.getPluginManager().callEvent(blockCookEvent);

                    if (blockCookEvent.isCancelled()) return;

                    result = blockCookEvent.getResult();
                    itemstack1 = CraftItemStack.asNMSCopy(result);

                    ItemScatterer.spawn(be.world, (double) blockposition.getX(), (double) blockposition.getY(), (double) blockposition.getZ(), itemstack1);
                    be.getItemsBeingCooked().set(i, ItemStack.EMPTY);
                    be.updateListeners();
                }
            }
        }

    }

}