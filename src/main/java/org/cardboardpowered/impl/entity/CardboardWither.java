package org.cardboardpowered.impl.entity;

import org.bukkit.boss.BossBar;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Wither;

import net.minecraft.entity.boss.WitherEntity;

public class CardboardWither extends MonsterImpl implements Wither {

    private BossBar bossBar;

    public CardboardWither(CraftServer server, WitherEntity entity) {
        super(server, entity);
        // TODO if (entity.bossBar != null) this.bossBar = new CardboardBossBar(entity.bossBar);
    }

    @Override
    public WitherEntity getHandle() {
        return (WitherEntity) nms;
    }

    @Override
    public String toString() {
        return "Wither";
    }

    @Override
    public EntityType getType() {
        return EntityType.WITHER;
    }

    @Override
    public BossBar getBossBar() {
        return bossBar;
    }

    @Override
    public void rangedAttack(LivingEntity arg0, float arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setChargingAttack(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

}