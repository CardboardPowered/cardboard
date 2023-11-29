package org.bukkit.craftbukkit.scoreboard;

import com.google.common.collect.ImmutableMap;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.RenderType;

import java.util.Map;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;

public final class CraftCriteria implements Criteria {
    static final Map<String, CraftCriteria> DEFAULTS;
    static final CraftCriteria DUMMY;

    static {
        ImmutableMap.Builder<String, CraftCriteria> defaults = ImmutableMap.builder();

        for (Map.Entry<String, ScoreboardCriterion> entry : ScoreboardCriterion.CRITERIA.entrySet()) {
            String name = entry.getKey();
            ScoreboardCriterion criteria = entry.getValue();

            defaults.put(name, new CraftCriteria(criteria));
        }

        DEFAULTS = defaults.build();
        DUMMY = DEFAULTS.get("dummy");
    }

    final ScoreboardCriterion criteria;
    final String bukkitName;

    private CraftCriteria(String bukkitName) {
        this.bukkitName = bukkitName;
        this.criteria = DUMMY.criteria;
    }

    private CraftCriteria(ScoreboardCriterion criteria) {
        this.criteria = criteria;
        this.bukkitName = criteria.getName();
    }

    @Override
    public String getName() {
        return bukkitName;
    }

    @Override
    public boolean isReadOnly() {
        return criteria.isReadOnly();
    }

    @Override
    public RenderType getDefaultRenderType() {
        return RenderType.values()[criteria.getDefaultRenderType().ordinal()];
    }

    static CraftCriteria getFromNMS(ScoreboardObjective objective) {
        return DEFAULTS.get(objective.getCriterion().getName());
    }

    public static CraftCriteria getFromBukkit(String name) {
        CraftCriteria criteria = DEFAULTS.get(name);
        if (criteria != null) {
            return criteria;
        }
        return ScoreboardCriterion.getOrCreateStatCriterion(name).map(CraftCriteria::new).orElseGet(() -> new CraftCriteria(name));
    }

    @Override
    public boolean equals(Object that) {
        if (!(that instanceof CraftCriteria)) {
            return false;
        }
        return ((CraftCriteria) that).bukkitName.equals(this.bukkitName);
    }

    @Override
    public int hashCode() {
        return this.bukkitName.hashCode() ^ CraftCriteria.class.hashCode();
    }
}
