package com.javazilla.bukkitfabric.mixin;

import org.bukkit.Material;
import org.cardboardpowered.impl.CardboardModdedMaterial;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.javazilla.bukkitfabric.interfaces.IMixinMaterial;

@Mixin(value = Material.class, remap = false)
@Deprecated
public class MixinMaterial implements IMixinMaterial {

    private CardboardModdedMaterial moddedData;

    @Override
    public boolean isModded() {
        return null != moddedData;
    }

    @Override
    public CardboardModdedMaterial getModdedData() {
        return moddedData;
    }

    @Override
    public void setModdedData(CardboardModdedMaterial data) {
        this.moddedData = data;
    }

    @Inject(at = @At("HEAD"), method = "isBlock", cancellable = true)
    public void isBlock_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(moddedData.isBlock());
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isItem", cancellable = true)
    public void isItem_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(moddedData.isItem());
            return;
        } else {
            switch (((Material)(Object)this).name()) {
                //<editor-fold defaultstate="collapsed" desc="isItem">
                case "ACACIA_WALL_SIGN":
                case "ATTACHED_MELON_STEM":
                case "ATTACHED_PUMPKIN_STEM":
                case "BAMBOO_SAPLING":
                case "BEETROOTS":
                case "BIRCH_WALL_SIGN":
                case "BLACK_WALL_BANNER":
                case "BLUE_WALL_BANNER":
                case "BRAIN_CORAL_WALL_FAN":
                case "BROWN_WALL_BANNER":
                case "BUBBLE_COLUMN":
                case "BUBBLE_CORAL_WALL_FAN":
                case "CARROTS":
                case "CAVE_AIR":
                case "COCOA":
                case "CREEPER_WALL_HEAD":
                case "CRIMSON_WALL_SIGN":
                case "CYAN_WALL_BANNER":
                case "DARK_OAK_WALL_SIGN":
                case "DEAD_BRAIN_CORAL_WALL_FAN":
                case "DEAD_BUBBLE_CORAL_WALL_FAN":
                case "DEAD_FIRE_CORAL_WALL_FAN":
                case "DEAD_HORN_CORAL_WALL_FAN":
                case "DEAD_TUBE_CORAL_WALL_FAN":
                case "DRAGON_WALL_HEAD":
                case "END_GATEWAY":
                case "END_PORTAL":
                case "FIRE":
                case "FIRE_CORAL_WALL_FAN":
                case "FROSTED_ICE":
                case "GRAY_WALL_BANNER":
                case "GREEN_WALL_BANNER":
                case "HORN_CORAL_WALL_FAN":
                case "JUNGLE_WALL_SIGN":
                case "KELP_PLANT":
                case "LAVA":
                case "LIGHT_BLUE_WALL_BANNER":
                case "LIGHT_GRAY_WALL_BANNER":
                case "LIME_WALL_BANNER":
                case "MAGENTA_WALL_BANNER":
                case "MELON_STEM":
                case "MOVING_PISTON":
                case "NETHER_PORTAL":
                case "OAK_WALL_SIGN":
                case "ORANGE_WALL_BANNER":
                case "PINK_WALL_BANNER":
                case "PISTON_HEAD":
                case "PLAYER_WALL_HEAD":
                case "POTATOES":
                case "POTTED_ACACIA_SAPLING":
                case "POTTED_ALLIUM":
                case "POTTED_AZURE_BLUET":
                case "POTTED_BAMBOO":
                case "POTTED_BIRCH_SAPLING":
                case "POTTED_BLUE_ORCHID":
                case "POTTED_BROWN_MUSHROOM":
                case "POTTED_CACTUS":
                case "POTTED_CORNFLOWER":
                case "POTTED_CRIMSON_FUNGUS":
                case "POTTED_CRIMSON_ROOTS":
                case "POTTED_DANDELION":
                case "POTTED_DARK_OAK_SAPLING":
                case "POTTED_DEAD_BUSH":
                case "POTTED_FERN":
                case "POTTED_JUNGLE_SAPLING":
                case "POTTED_LILY_OF_THE_VALLEY":
                case "POTTED_OAK_SAPLING":
                case "POTTED_ORANGE_TULIP":
                case "POTTED_OXEYE_DAISY":
                case "POTTED_PINK_TULIP":
                case "POTTED_POPPY":
                case "POTTED_RED_MUSHROOM":
                case "POTTED_RED_TULIP":
                case "POTTED_SPRUCE_SAPLING":
                case "POTTED_WARPED_FUNGUS":
                case "POTTED_WARPED_ROOTS":
                case "POTTED_WHITE_TULIP":
                case "POTTED_WITHER_ROSE":
                case "PUMPKIN_STEM":
                case "PURPLE_WALL_BANNER":
                case "REDSTONE_WALL_TORCH":
                case "REDSTONE_WIRE":
                case "RED_WALL_BANNER":
                case "SKELETON_WALL_SKULL":
                case "SOUL_FIRE":
                case "SOUL_WALL_TORCH":
                case "SPRUCE_WALL_SIGN":
                case "SWEET_BERRY_BUSH":
                case "TALL_SEAGRASS":
                case "TRIPWIRE":
                case "TUBE_CORAL_WALL_FAN":
                case "TWISTING_VINES_PLANT":
                case "VOID_AIR":
                case "WALL_TORCH":
                case "WARPED_WALL_SIGN":
                case "WATER":
                case "WEEPING_VINES_PLANT":
                case "WHITE_WALL_BANNER":
                case "WITHER_SKELETON_WALL_SKULL":
                case "YELLOW_WALL_BANNER":
                case "ZOMBIE_WALL_HEAD":
                // ----- Legacy Separator -----
                case "LEGACY_ACACIA_DOOR":
                case "LEGACY_BED_BLOCK":
                case "LEGACY_BEETROOT_BLOCK":
                case "LEGACY_BIRCH_DOOR":
                case "LEGACY_BREWING_STAND":
                case "LEGACY_BURNING_FURNACE":
                case "LEGACY_CAKE_BLOCK":
                case "LEGACY_CARROT":
                case "LEGACY_CAULDRON":
                case "LEGACY_COCOA":
                case "LEGACY_CROPS":
                case "LEGACY_DARK_OAK_DOOR":
                case "LEGACY_DAYLIGHT_DETECTOR_INVERTED":
                case "LEGACY_DIODE_BLOCK_OFF":
                case "LEGACY_DIODE_BLOCK_ON":
                case "LEGACY_DOUBLE_STEP":
                case "LEGACY_DOUBLE_STONE_SLAB2":
                case "LEGACY_ENDER_PORTAL":
                case "LEGACY_END_GATEWAY":
                case "LEGACY_FIRE":
                case "LEGACY_FLOWER_POT":
                case "LEGACY_FROSTED_ICE":
                case "LEGACY_GLOWING_REDSTONE_ORE":
                case "LEGACY_IRON_DOOR_BLOCK":
                case "LEGACY_JUNGLE_DOOR":
                case "LEGACY_LAVA":
                case "LEGACY_MELON_STEM":
                case "LEGACY_NETHER_WARTS":
                case "LEGACY_PISTON_EXTENSION":
                case "LEGACY_PISTON_MOVING_PIECE":
                case "LEGACY_PORTAL":
                case "LEGACY_POTATO":
                case "LEGACY_PUMPKIN_STEM":
                case "LEGACY_PURPUR_DOUBLE_SLAB":
                case "LEGACY_REDSTONE_COMPARATOR_OFF":
                case "LEGACY_REDSTONE_COMPARATOR_ON":
                case "LEGACY_REDSTONE_LAMP_ON":
                case "LEGACY_REDSTONE_TORCH_OFF":
                case "LEGACY_REDSTONE_WIRE":
                case "LEGACY_SIGN_POST":
                case "LEGACY_SKULL":
                case "LEGACY_SPRUCE_DOOR":
                case "LEGACY_STANDING_BANNER":
                case "LEGACY_STATIONARY_LAVA":
                case "LEGACY_STATIONARY_WATER":
                case "LEGACY_SUGAR_CANE_BLOCK":
                case "LEGACY_TRIPWIRE":
                case "LEGACY_WALL_BANNER":
                case "LEGACY_WALL_SIGN":
                case "LEGACY_WATER":
                case "LEGACY_WOODEN_DOOR":
                case "LEGACY_WOOD_DOUBLE_STEP":
                //</editor-fold>
                    ci.setReturnValue(false);
                    return;
                default:
                    ci.setReturnValue(true);
            }
        }
    }

    @Inject(at = @At("HEAD"), method = "isEdible", cancellable = true)
    public void isEdible_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(moddedData.isEdible());
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isRecord", cancellable = true)
    public void isRecord_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(false);
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isSolid", cancellable = true)
    public void isSolid_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(moddedData.isBlock());
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isAir", cancellable = true)
    public void isAir_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(false);
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isTransparent", cancellable = true)
    public void isTransparent_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(false);
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isBurnable", cancellable = true)
    public void isBurnable_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(false);
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isOccluding", cancellable = true)
    public void isOccluding_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(moddedData.isBlock());
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "hasGravity", cancellable = true)
    public void hasGravity_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(false);
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "isInteractable", cancellable = true)
    public void isInteractable_BF(CallbackInfoReturnable<Boolean> ci) {
        if (isModded()) {
            ci.setReturnValue(false);
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "getHardness", cancellable = true)
    public void getHardness_BF(CallbackInfoReturnable<Float> ci) {
        if (isModded()) {
            ci.setReturnValue(1f);
            return;
        }
    }

    @Inject(at = @At("HEAD"), method = "getCraftingRemainingItem", cancellable = true)
    public void getCraftingRemainingItem_BF(CallbackInfoReturnable<Material> ci) {
        if (isModded()) {
            ci.setReturnValue(null);
            return;
        }
    }


}