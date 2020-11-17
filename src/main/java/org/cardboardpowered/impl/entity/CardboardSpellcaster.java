package org.cardboardpowered.impl.entity;

import com.google.common.base.Preconditions;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.Spellcaster;

public class CardboardSpellcaster extends CardboardIllager implements Spellcaster {

    public CardboardSpellcaster(CraftServer server, SpellcastingIllagerEntity entity) {
        super(server, entity);
    }

    @Override
    public SpellcastingIllagerEntity getHandle() {
        return (SpellcastingIllagerEntity) super.getHandle();
    }

    @Override
    public String toString() {
        return "Spellcaster";
    }

    @Override
    public Spell getSpell() {
        return toBukkitSpell(getHandle().getSpell());
    }

    @Override
    public void setSpell(Spell spell) {
        Preconditions.checkArgument(spell != null, "Use Spell.NONE");
        getHandle().setSpell(toNMSSpell(spell));
    }

    public static Spell toBukkitSpell(SpellcastingIllagerEntity.Spell spell) {
        return Spell.valueOf(spell.name());
    }

    public static SpellcastingIllagerEntity.Spell toNMSSpell(Spell spell) {
        return SpellcastingIllagerEntity.Spell.byId(spell.ordinal());
    }

}
