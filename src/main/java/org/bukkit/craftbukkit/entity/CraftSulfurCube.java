package org.bukkit.craftbukkit.entity;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.SulfurCube;

// 26.2: SulfurCube is a new cube mob. Bukkit models it as
// AbstractCubeMob + Shearable + Bucketable + Ageable (notably not a Slime).
public class CraftSulfurCube extends CraftAgeable implements SulfurCube {

    public CraftSulfurCube(CraftServer server, net.minecraft.world.entity.monster.cubemob.SulfurCube entity) {
        super(server, entity);
    }

    @Override
    public net.minecraft.world.entity.monster.cubemob.SulfurCube getHandle() {
        return (net.minecraft.world.entity.monster.cubemob.SulfurCube) this.entity;
    }

    // --- AbstractCubeMob ---

    @Override
    public int getSize() {
        return this.getHandle().getSize();
    }

    @Override
    public void setSize(int size) {
        this.getHandle().setSize(size, this.getHandle().isAlive());
    }

    @Override
    public boolean canWander() {
        // Cardboard does not expose the wander flag yet; matches CraftSlime.
        return true;
    }

    @Override
    public void setWander(boolean canWander) {
        // Cardboard does not expose the wander flag yet; matches CraftSlime.
    }

    // --- SulfurCube ---

    @Override
    public int getFuseTicks() {
        return this.getHandle().getFuse();
    }

    @Override
    public void setFuseTicks(int ticks) {
        this.getHandle().setFuse(ticks);
    }

    @Override
    public boolean canExplode() {
        return this.getHandle().canExplode();
    }

    @Override
    public boolean ignite(boolean force) {
        return this.getHandle().primeTime(force);
    }

    // --- Shearable ---

    @Override
    public void shear(net.kyori.adventure.sound.Sound.Source source) {
        if (!(this.getHandle().level() instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return;
        }
        this.getHandle().shear(serverLevel,
                net.minecraft.sounds.SoundSource.valueOf(source.name()),
                net.minecraft.world.item.ItemStack.EMPTY);
    }

    @Override
    public boolean readyToBeSheared() {
        return this.getHandle().readyForShearing();
    }

    // --- Bucketable ---

    @Override
    public boolean isFromBucket() {
        return this.getHandle().fromBucket();
    }

    @Override
    public void setFromBucket(boolean fromBucket) {
        this.getHandle().setFromBucket(fromBucket);
    }

    @Override
    public org.bukkit.inventory.ItemStack getBaseBucketItem() {
        return CraftItemStack.asBukkitCopy(this.getHandle().getBucketItemStack());
    }

    @Override
    public org.bukkit.Sound getPickupSound() {
        return org.bukkit.craftbukkit.CraftSound.minecraftToBukkit(this.getHandle().getPickupSound());
    }
}
