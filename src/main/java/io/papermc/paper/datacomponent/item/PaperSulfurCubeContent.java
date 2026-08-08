package io.papermc.paper.datacomponent.item;

import net.minecraft.world.item.component.SulfurCubeContent;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.Handleable;

// 26.2: SULFUR_CUBE_CONTENT is a new data component type.
public record PaperSulfurCubeContent(SulfurCubeContent impl)
        implements io.papermc.paper.datacomponent.item.SulfurCubeContent, Handleable<SulfurCubeContent> {

    @Override
    public SulfurCubeContent getHandle() {
        return this.impl;
    }

    @Override
    public org.bukkit.inventory.ItemStack absorbedItem() {
        // ItemStackTemplate -> ItemStack; apply an empty patch to materialise the stack
        return CraftItemStack.asCraftMirror(
                this.impl.absorbedBlockItemStack().apply(net.minecraft.core.component.DataComponentPatch.EMPTY));
    }
}
