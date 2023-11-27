package org.bukkit.craftbukkit.scoreboard;

import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.RenderType;

final class CardboardScoreboardTranslations {
    private CardboardScoreboardTranslations() {}

    static DisplaySlot toBukkitSlot(ScoreboardDisplaySlot slot) {
        return DisplaySlot.values()[slot.ordinal()];
    }

    static ScoreboardDisplaySlot fromBukkitSlot(DisplaySlot slot) {
        return ScoreboardDisplaySlot.values()[slot.ordinal()];
    }

    static RenderType toBukkitRender(ScoreboardCriterion.RenderType display) {
        return RenderType.valueOf(display.name());
    }

    static ScoreboardCriterion.RenderType fromBukkitRender(RenderType render) {
        return ScoreboardCriterion.RenderType.valueOf(render.name());
    }
}
