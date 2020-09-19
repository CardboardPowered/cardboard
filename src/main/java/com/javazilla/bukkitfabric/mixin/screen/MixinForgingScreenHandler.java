package com.javazilla.bukkitfabric.mixin.screen;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinForgingScreenHandler;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.CraftingResultInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.screen.ForgingScreenHandler;
import net.minecraft.screen.ScreenHandlerContext;

@Mixin(ForgingScreenHandler.class)
public abstract class MixinForgingScreenHandler extends MixinScreenHandler implements IMixinForgingScreenHandler {

    @Shadow
    public CraftingResultInventory output = new CraftingResultInventory();

    @Shadow
    public Inventory input;

    @Shadow
    public ScreenHandlerContext context;

    @Shadow
    public PlayerEntity player;


}