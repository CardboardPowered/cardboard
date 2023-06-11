package org.cardboardpowered.mixin.item;

import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.mojang.authlib.GameProfile;

import net.minecraft.block.Block;
import net.minecraft.block.entity.SkullBlockEntity;
import net.minecraft.item.Item;
import net.minecraft.item.SkullItem;
//import net.minecraft.item.WallStandingBlockItem;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.nbt.NbtList;

@Mixin(value = SkullItem.class, priority = 900)
public class MixinSkullItem extends Item {

    public MixinSkullItem(Settings properties) {
        super(properties);
    }

    /**
     * @reason Bukkit
     * @author .
     */
    @Overwrite
    public void postProcessNbt(NbtCompound compoundTag) {
        super.postProcessNbt(compoundTag);
        if (compoundTag.contains("SkullOwner", 8) && !StringUtils.isBlank(compoundTag.getString("SkullOwner"))) {
            GameProfile gameProfile = new GameProfile((UUID)null, compoundTag.getString("SkullOwner"));
            SkullBlockEntity.loadProperties(gameProfile, (gameProfilex) -> {
                compoundTag.put("SkullOwner", NbtHelper.writeGameProfile(new NbtCompound(), gameProfilex));
            });
            // CraftBukkit start
        } else {
            net.minecraft.nbt.NbtList textures = compoundTag.getCompound("SkullOwner").getCompound("Properties").getList("textures", 10); // Safe due to method contracts
            for (net.minecraft.nbt.NbtElement texture : textures) {
                if (texture instanceof NbtCompound && !((NbtCompound) texture).contains("Signature", 8) && ((NbtCompound) texture).getString("Value").trim().isEmpty()) {
                    compoundTag.remove("SkullOwner");
                    break;
                }
            }
            // CraftBukkit end
        }

    }

}