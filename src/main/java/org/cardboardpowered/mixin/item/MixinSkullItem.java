package org.cardboardpowered.mixin.item;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.item.SkullItem;
import net.minecraft.item.WallStandingBlockItem;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtHelper;

@Mixin(SkullItem.class)
public class MixinSkullItem extends WallStandingBlockItem {

    public MixinSkullItem(Block standingBlock, Block wallBlock, Settings settings) {
        super(standingBlock, wallBlock, settings);
    }

    /**
     * @reason Bukkit
     * @author BukkitFabricMod
     */
    @Overwrite
    public boolean postProcessTag(CompoundTag tag) {
        super.postProcessTag(tag);
        if (tag.contains("SkullOwner", 8) && !StringUtils.isBlank(tag.getString("SkullOwner"))) {
            GameProfile gameprofile = new GameProfile((UUID) null, tag.getString("SkullOwner"));
            tag.put("SkullOwner", NbtHelper.fromGameProfile(new CompoundTag(), gameprofile));
            return true;
        } else {
            ListTag textures = tag.getCompound("SkullOwner").getCompound("Properties").getList("textures", 10);
            for (int i = 0; i < textures.size(); i++) {
                if (textures.get(i) instanceof CompoundTag && !((CompoundTag) textures.get(i)).contains("Signature", 8) && ((CompoundTag) textures.get(i)).getString("Value").trim().isEmpty()) {
                    tag.remove("SkullOwner");
                    break;
                }
            }
            return false;
        }
    }

}