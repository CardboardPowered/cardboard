package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.EnchantingTableBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.EnchantingTable;
import org.bukkit.craftbukkit.util.CraftChatMessage;

public class CardboardEnchantingTable extends CardboardBlockEntityState<EnchantingTableBlockEntity> implements EnchantingTable {

    public CardboardEnchantingTable(final Block block) {
        super(block, EnchantingTableBlockEntity.class);
    }

    public CardboardEnchantingTable(final Material material, final EnchantingTableBlockEntity te) {
        super(material, te);
    }

    @Override
    public String getCustomName() {
        EnchantingTableBlockEntity enchant = this.getSnapshot();
        return enchant.hasCustomName() ? CraftChatMessage.fromComponent(enchant.getCustomName()) : null;
    }

    @Override
    public void setCustomName(String name) {
        this.getSnapshot().setCustomName(CraftChatMessage.fromStringOrNull(name));
    }

    @Override
    public void applyTo(EnchantingTableBlockEntity enchantingTable) {
        super.applyTo(enchantingTable);
        if (!this.getSnapshot().hasCustomName()) enchantingTable.setCustomName(null);
    }

}