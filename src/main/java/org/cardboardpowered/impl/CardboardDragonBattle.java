package org.cardboardpowered.impl;

import net.minecraft.entity.boss.dragon.EnderDragonFight;
import net.minecraft.entity.boss.dragon.EnderDragonSpawnState;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.boss.DragonBattle;
import org.bukkit.entity.EnderDragon;

public class CardboardDragonBattle implements DragonBattle {

    private final EnderDragonFight handle;

    public CardboardDragonBattle(EnderDragonFight handle) {
        this.handle = handle;
    }

    @Override
    public EnderDragon getEnderDragon() {
        return null; // TODO
    }

    @Override
    public BossBar getBossBar() {
        return null; // TODO
    }

    @Override
    public Location getEndPortalLocation() {
        return null; // TODO
    }

    @Override
    public boolean generateEndPortal(boolean withPortals) {
        // TODO
        return true;
    }

    @Override
    public boolean hasBeenPreviouslyKilled() {
        return handle.hasPreviouslyKilled();
    }

    @Override
    public void initiateRespawn() {
        this.handle.respawnDragon();
    }

    @Override
    public RespawnPhase getRespawnPhase() {
        return RespawnPhase.NONE; // TODO
    }

    @Override
    public boolean setRespawnPhase(RespawnPhase phase) {
        // TODO
        return true;
    }

    @Override
    public void resetCrystals() {
        this.handle.resetEndCrystals();
    }

    @Override
    public int hashCode() {
        return handle.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof CardboardDragonBattle && ((CardboardDragonBattle) obj).handle == this.handle;
    }

    private RespawnPhase toBukkitRespawnPhase(EnderDragonSpawnState phase) {
        return (phase != null) ? RespawnPhase.values()[phase.ordinal()] : RespawnPhase.NONE;
    }

    private EnderDragonSpawnState toNMSRespawnPhase(RespawnPhase phase) {
        return (phase != RespawnPhase.NONE) ? EnderDragonSpawnState.values()[phase.ordinal()] : null;
    }
}
