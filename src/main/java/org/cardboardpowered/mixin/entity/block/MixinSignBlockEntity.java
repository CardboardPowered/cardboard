package org.cardboardpowered.mixin.entity.block;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinSignBlockEntity;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.Text;

@Mixin(SignBlockEntity.class)
public class MixinSignBlockEntity implements IMixinSignBlockEntity {

    @Shadow
    public Text[] texts;

    @Override
    public Text[] getTextBF() {
        return texts;
    }

}