package org.cardboardpowered.mixin.screen;

import java.util.Optional;

import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.RecipeInputInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.recipe.CraftingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.screen.CraftingScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

@Mixin(CraftingScreenHandler.class)
public class MixinCraftingScreenHandler extends MixinScreenHandler {

	// Lnet/minecraft/screen/CraftingScreenHandler;input:Lnet/minecraft/inventory/RecipeInputInventory;
	
    @Shadow public RecipeInputInventory input;
    @Shadow public CraftingResultInventory result;
    @Shadow public ScreenHandlerContext context;
    @Shadow public PlayerEntity player;

    private CardboardInventoryView bukkitEntity = null;
    private PlayerInventory playerInv;

    @Inject(method = "<init>(ILnet/minecraft/entity/player/PlayerInventory;Lnet/minecraft/screen/ScreenHandlerContext;)V", at = @At("TAIL"))
    public void setPlayerInv(int i, PlayerInventory playerinventory, ScreenHandlerContext containeraccess, CallbackInfo ci) {
    	this.playerInv = playerinventory;
    }

    @Override
    public CardboardInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        CraftInventoryCrafting inventory = new CraftInventoryCrafting(this.input, this.result);
        bukkitEntity = new CardboardInventoryView((Player)((IMixinEntity)this.playerInv.player).getBukkitEntity(), inventory, (CraftingScreenHandler)(Object)this);
        return bukkitEntity;
    }

    private static void aBF(int i, World world, PlayerEntity entityhuman, RecipeInputInventory inventorycrafting, CraftingResultInventory inventorycraftresult, ScreenHandler container) {
        if (!world.isClient) {
            ServerPlayerEntity entityplayer = (ServerPlayerEntity) entityhuman;
            ItemStack itemstack = ItemStack.EMPTY;
            Optional<CraftingRecipe> optional = world.getServer().getRecipeManager().getFirstMatch(RecipeType.CRAFTING, inventorycrafting, world);

            if (optional.isPresent()) {
                CraftingRecipe recipecrafting = (CraftingRecipe) optional.get();
                if (inventorycraftresult.shouldCraftRecipe(world, entityplayer, recipecrafting))
                    itemstack = recipecrafting.craft(inventorycrafting, DynamicRegistryManager.EMPTY);
            }
            itemstack = BukkitEventFactory.callPreCraftEvent(inventorycrafting, inventorycraftresult, itemstack, ((IMixinScreenHandler)container).getBukkitView(), false);
            inventorycraftresult.setStack(0, itemstack);
            entityplayer.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(i, container.nextRevision(), 0, itemstack));
        }
    }

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public void onContentChanged(Inventory iinventory) {
        this.context.run((world, blockposition) -> {
            aBF(((CraftingScreenHandler)(Object)this).syncId, world, this.player, this.input, this.result, (CraftingScreenHandler)(Object)this);
        });
    }

}