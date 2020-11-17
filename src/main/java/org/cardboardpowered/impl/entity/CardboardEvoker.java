package org.cardboardpowered.impl.entity;

import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Evoker;

public class CardboardEvoker extends CardboardSpellcaster implements Evoker {

    public CardboardEvoker(CraftServer server, EvokerEntity entity) {
        super(server, entity);
    }

    @Override
    public EvokerEntity getHandle() {
        return (EvokerEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "Evoker";
    }

    @Override
    public EntityType getType() {
        return EntityType.EVOKER;
    }

    @Override
    public Evoker.Spell getCurrentSpell() {
        return Evoker.Spell.values()[getHandle().getSpell().ordinal()];
    }

    @Override
    public void setCurrentSpell(Evoker.Spell spell) {
        getHandle().setSpell(spell == null ? SpellcastingIllagerEntity.Spell.NONE : SpellcastingIllagerEntity.Spell.byId(spell.ordinal()));
    }

}