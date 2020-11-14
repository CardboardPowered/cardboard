package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.DaylightDetectorBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.DaylightDetector;

public class CardboardDaylightDetector extends CardboardBlockEntityState<DaylightDetectorBlockEntity> implements DaylightDetector {

    public CardboardDaylightDetector(final Block block) {
        super(block, DaylightDetectorBlockEntity.class);
    }

    public CardboardDaylightDetector(final Material material, final DaylightDetectorBlockEntity te) {
        super(material, te);
    }

}