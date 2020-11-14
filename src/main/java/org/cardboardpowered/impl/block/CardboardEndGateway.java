package org.cardboardpowered.impl.block;

import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.EndGateway;

import net.minecraft.block.entity.EndGatewayBlockEntity;
import net.minecraft.util.math.BlockPos;

public class CardboardEndGateway extends CardboardBlockEntityState<EndGatewayBlockEntity> implements EndGateway {

    public CardboardEndGateway(Block block) {
        super(block, EndGatewayBlockEntity.class);
    }

    public CardboardEndGateway(final Material material, EndGatewayBlockEntity te) {
        super(material, te);
    }

    @Override
    public Location getExitLocation() {
        BlockPos pos = this.getSnapshot().exitPortalPos;
        return pos == null ? null : new Location(this.isPlaced() ? this.getWorld() : null, pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void setExitLocation(Location location) {
        if (location == null) {
            this.getSnapshot().exitPortalPos = null;
        } else if (!Objects.equals(location.getWorld(), this.isPlaced() ? this.getWorld() : null)) {
            throw new IllegalArgumentException("Cannot set exit location to different world");
        } else this.getSnapshot().exitPortalPos = new BlockPos(location.getBlockX(), location.getBlockY(), location.getBlockZ());
    }

    @Override
    public boolean isExactTeleport() {
        return this.getSnapshot().exactTeleport;
    }

    @Override
    public void setExactTeleport(boolean exact) {
        this.getSnapshot().exactTeleport = exact;
    }

    @Override
    public long getAge() {
        return this.getSnapshot().age;
    }

    @Override
    public void setAge(long age) {
        this.getSnapshot().age = age;
    }

    @Override
    public void applyTo(EndGatewayBlockEntity endGateway) {
        super.applyTo(endGateway);
        if (this.getSnapshot().exitPortalPos == null) endGateway.exitPortalPos = null;
    }

}