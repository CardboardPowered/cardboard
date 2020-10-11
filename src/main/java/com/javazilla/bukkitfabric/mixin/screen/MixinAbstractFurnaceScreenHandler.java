package com.javazilla.bukkitfabric.mixin.screen;

import org.bukkit.craftbukkit.inventory.CraftInventoryFurnace;
import org.bukkit.craftbukkit.inventory.CraftInventoryView;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import net.minecraft.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.recipe.AbstractCookingRecipe;
import net.minecraft.recipe.RecipeType;
import net.minecraft.recipe.book.RecipeBookCategory;
import net.minecraft.screen.AbstractFurnaceScreenHandler;
import net.minecraft.screen.PropertyDelegate;
import net.minecraft.screen.ScreenHandlerType;

@Mixin(AbstractFurnaceScreenHandler.class)
public class MixinAbstractFurnaceScreenHandler extends MixinScreenHandler {

    @Shadow
    public Inventory inventory;

    private CraftInventoryView bukkitEntity = null;
    private PlayerInventory playerInv;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void setPlayerInv(ScreenHandlerType<?> containers, RecipeType<? extends AbstractCookingRecipe> recipes, RecipeBookCategory recipebooktype, int i, PlayerInventory playerinventory, Inventory iinventory, PropertyDelegate icontainerproperties, CallbackInfo ci) {
        this.playerInv = playerinventory;
    }

    @Override
    public CraftInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        CraftInventoryFurnace inventory = new CraftInventoryFurnace((AbstractFurnaceBlockEntity) this.inventory);
        bukkitEntity = new CraftInventoryView((Player)((IMixinEntity)this.playerInv.player).getBukkitEntity(), inventory, (AbstractFurnaceScreenHandler)(Object)this);
        return bukkitEntity;
    }


}