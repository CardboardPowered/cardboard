package com.fungus_soft.bukkitfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import net.minecraft.block.entity.LecternBlockEntity;
import net.minecraft.inventory.Inventory;

import com.fungus_soft.bukkitfabric.interfaces.IMixinLecternBlockEntity;

@Mixin(LecternBlockEntity.class)
public class LecternBlockEntityMixin implements IMixinLecternBlockEntity {

    @Shadow
    public Inventory inventory;

    @Override
    public Inventory getInventory() {
        return inventory;
    }

}