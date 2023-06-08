package org.cardboardpowered.impl.block;

import java.util.Optional;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.SkullType;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.Skull;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.Rotatable;
import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.jetbrains.annotations.Nullable;

import com.destroystokyo.paper.profile.CraftPlayerProfile;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.google.common.base.Preconditions;
import com.mojang.authlib.GameProfile;

import net.minecraft.block.entity.SkullBlockEntity;

@SuppressWarnings("deprecation")
public class CardboardSkull extends CardboardBlockEntityState<SkullBlockEntity> implements Skull {

    private static final int MAX_OWNER_LENGTH = 16;
    private GameProfile profile;

    public CardboardSkull(Block block) {
        super(block, SkullBlockEntity.class);
    }

    public CardboardSkull(Material material, SkullBlockEntity te) {
        super(material, te);
    }

    @Override
    public void load(SkullBlockEntity skull) {
        super.load(skull);

        profile = skull.getOwner();
    }

    static int getSkullType(SkullType type) {
        switch (type) {
            default:
            case SKELETON:
                return 0;
            case WITHER:
                return 1;
            case ZOMBIE:
                return 2;
            case PLAYER:
                return 3;
            case CREEPER:
                return 4;
            case DRAGON:
                return 5;
        }
    }

    @Override
    public boolean hasOwner() {
        return profile != null;
    }

    @Override
    public String getOwner() {
        return hasOwner() ? profile.getName() : null;
    }

    @Override
    public boolean setOwner(String name) {
        if (name == null || name.length() > MAX_OWNER_LENGTH) return false;

        Optional<GameProfile> profile = CraftServer.getUC().card_findByName(name);
        if (profile.isEmpty()) return false;

        this.profile = profile.get();
        return true;
    }

    @Override
    public OfflinePlayer getOwningPlayer() {
        if (profile != null) {
            if (profile.getId() != null) return Bukkit.getOfflinePlayer(profile.getId());
            if (profile.getName() != null) return Bukkit.getOfflinePlayer(profile.getName());
        }
        return null;
    }

    @Override
    public void setOwningPlayer(OfflinePlayer player) {
        Preconditions.checkNotNull(player, "player");
        this.profile = (player instanceof PlayerImpl) ? ((PlayerImpl) player).nms.getGameProfile() : new GameProfile(player.getUniqueId(), player.getName());
    }

    @Override
    public BlockFace getRotation() {
        BlockData blockData = getBlockData();
        return (blockData instanceof Rotatable) ? ((Rotatable) blockData).getRotation() : ((Directional) blockData).getFacing();
    }

    @Override
    public void setRotation(BlockFace rotation) {
        BlockData blockData = getBlockData();
        if (blockData instanceof Rotatable) ((Rotatable) blockData).setRotation(rotation);
        else ((Directional) blockData).setFacing(rotation);

        setBlockData(blockData);
    }

    @Override
    public SkullType getSkullType() {
        switch (getType()) {
            case SKELETON_SKULL:
            case SKELETON_WALL_SKULL:
                return SkullType.SKELETON;
            case WITHER_SKELETON_SKULL:
            case WITHER_SKELETON_WALL_SKULL:
                return SkullType.WITHER;
            case ZOMBIE_HEAD:
            case ZOMBIE_WALL_HEAD:
                return SkullType.ZOMBIE;
            case PLAYER_HEAD:
            case PLAYER_WALL_HEAD:
                return SkullType.PLAYER;
            case CREEPER_HEAD:
            case CREEPER_WALL_HEAD:
                return SkullType.CREEPER;
            case DRAGON_HEAD:
            case DRAGON_WALL_HEAD:
                return SkullType.DRAGON;
            default:
                throw new IllegalArgumentException("Unknown SkullType for " + getType());
        }
    }

    @Override
    public void setSkullType(SkullType skullType) {
        throw new UnsupportedOperationException("Must change block type");
    }

    @Override
    public void applyTo(SkullBlockEntity skull) {
        super.applyTo(skull);
        if (getSkullType() == SkullType.PLAYER)
            skull.setOwner(profile);
    }

    @Override
    public PlayerProfile getPlayerProfile() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setPlayerProfile(PlayerProfile arg0) {
        this.profile = new GameProfile(arg0.getId(), arg0.getName());
    }

    @Override
    public PlayerProfile getOwnerProfile() {
        if (!hasOwner()) {
            return null;
        }

        return new CraftPlayerProfile(profile);
    }

	
	@Override
    public void setOwnerProfile(org.bukkit.profile.@Nullable PlayerProfile profile) {
        if (profile == null) {
            this.profile = null;
        } else {
           //  this.profile = CraftPlayerProfile.validateSkullProfile(((CraftPlayerProfile) profile).getGameProfile());
        }
    }

}