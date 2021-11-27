package org.cardboardpowered.impl.entity;

import net.kyori.adventure.text.Component;
import net.minecraft.entity.LightningEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LightningStrike;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

public class LightningStrikeImpl extends CraftEntity implements LightningStrike {

    public LightningStrikeImpl(final CraftServer server, final LightningEntity entity) {
        super(entity);
    }

    @Override
    public boolean isEffect() {
        return false; // TODO getHandle().cosmetic;
    }

    @Override
    public LightningEntity getHandle() {
        return (LightningEntity) nms;
    }

    @Override
    public String toString() {
        return "LightningStrikeImpl";
    }

    @Override
    public EntityType getType() {
        return EntityType.LIGHTNING;
    }

    private final LightningStrike.Spigot spigot = new LightningStrike.Spigot() {
        @Override
        public boolean isSilent() {
            return getHandle().isSilent();
        }
    };

    @Override
    public LightningStrike.Spigot spigot() {
        return spigot;
    }

    @Override
    public int getFlashCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getLifeTicks() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setFlashCount(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setLifeTicks(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public @Nullable Component customName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void customName(@Nullable Component arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public @Nullable Entity getCausingEntity() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setCausingPlayer(@Nullable Player arg0) {
        // TODO Auto-generated method stub
        
    }

}