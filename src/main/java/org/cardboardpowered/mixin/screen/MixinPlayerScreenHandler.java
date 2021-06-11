package org.cardboardpowered.mixin.screen;

import org.bukkit.craftbukkit.inventory.CraftInventoryCrafting;
import org.bukkit.entity.Player;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.screen.NamedScreenHandlerFactory;
import net.minecraft.screen.PlayerScreenHandler;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

@Mixin(PlayerScreenHandler.class)
public class MixinPlayerScreenHandler extends MixinScreenHandler implements NamedScreenHandlerFactory {

    @Shadow private CraftingInventory craftingInput;
    @Shadow private CraftingResultInventory craftingResult;
    private CardboardInventoryView bukkitEntity = null;
    private PlayerInventory player;

    @Inject(method = "<init>", at = @At("TAIL"))
    public void setPlayerInv(PlayerInventory playerinventory, boolean flag, PlayerEntity entityhuman, CallbackInfo ci) {
        this.craftingResult = new CraftingResultInventory();
        this.craftingInput = new CraftingInventory((PlayerScreenHandler)(Object)this, 2, 2);
        this.player = playerinventory;
        setTitle(new TranslatableText("container.crafting"));
    }

    @Override
    public CardboardInventoryView getBukkitView() {
        if (bukkitEntity != null)
            return bukkitEntity;

        CraftInventoryCrafting inventory = new CraftInventoryCrafting(this.craftingInput, this.craftingResult);
        bukkitEntity = new CardboardInventoryView((Player)((IMixinServerEntityPlayer)this.player.player).getBukkitEntity(), inventory, (PlayerScreenHandler)(Object)this);
        return bukkitEntity;
    }

    @Override
    public ScreenHandler createMenu(int arg0, PlayerInventory arg1, PlayerEntity arg2) {
        return new PlayerScreenHandler(arg1, true, arg2);
    }

    @Override
    public Text getDisplayName() {
        return this.getTitle();
    }

}