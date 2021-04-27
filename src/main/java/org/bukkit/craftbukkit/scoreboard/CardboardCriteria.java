package org.bukkit.craftbukkit.scoreboard;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;

public class CardboardCriteria {

    private static final Map<String, CardboardCriteria> DEFAULTS;
    private static final CardboardCriteria DUMMY;

    static {
        ImmutableMap.Builder<String, CardboardCriteria> defaults = ImmutableMap.builder();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) ScoreboardCriterion.OBJECTIVES).entrySet())
            defaults.put(entry.getKey().toString(), new CardboardCriteria((ScoreboardCriterion) entry.getValue()));
        DEFAULTS = defaults.build();
        DUMMY = DEFAULTS.get("dummy");
    }

    final ScoreboardCriterion criteria;
    final String bukkitName;

    private CardboardCriteria(String bukkitName) {
        this.bukkitName = bukkitName;
        this.criteria = DUMMY.criteria;
    }

    private CardboardCriteria(ScoreboardCriterion criteria) {
        this.criteria = criteria;
        this.bukkitName = criteria.getName();
    }

    static CardboardCriteria getFromNMS(ScoreboardObjective objective) {
        return DEFAULTS.get(objective.getCriterion().getName());
    }

    public static CardboardCriteria getFromBukkit(String name) {
        final CardboardCriteria criteria = DEFAULTS.get(name);
        return (criteria != null) ? criteria : new CardboardCriteria(name);
    }

    @Override
    public boolean equals(Object that) {
        return (!(that instanceof CardboardCriteria)) ? false : ((CardboardCriteria) that).bukkitName.equals(this.bukkitName);
    }

    @Override
    public int hashCode() {
        return this.bukkitName.hashCode() ^ CardboardCriteria.class.hashCode();
    }

}