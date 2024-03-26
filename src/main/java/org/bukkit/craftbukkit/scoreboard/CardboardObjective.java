package org.bukkit.craftbukkit.scoreboard;

import net.kyori.adventure.text.Component;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardDisplaySlot;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.apache.commons.lang.Validate;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class CardboardObjective extends CardboardScoreboardComponent implements Objective {

    private final ScoreboardObjective objective;
    private final CardboardCriteria criteria;

    CardboardObjective(CardboardScoreboard scoreboard, ScoreboardObjective objective) {
        super(scoreboard);
        this.objective = objective;
        this.criteria = CardboardCriteria.getFromNMS(objective);
    }

    ScoreboardObjective getHandle() {
        return objective;
    }

    @Override
    public String getName() throws IllegalStateException {
        checkState();
        return objective.getName();
    }

    @Override
    public String getDisplayName() throws IllegalStateException {
        checkState();
        return CraftChatMessage.fromComponent(objective.getDisplayName());
    }

    @Override
    public void setDisplayName(String displayName) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(displayName, "Display name cannot be null");
        Validate.isTrue(displayName.length() <= 128, "Display name '" + displayName + "' is longer than the limit of 128 characters");
        checkState();
        objective.setDisplayName(CraftChatMessage.fromString(displayName)[0]); // SPIGOT-4112: not nullable
    }

    @Override
    public String getCriteria() throws IllegalStateException {
        checkState();
        return criteria.bukkitName;
    }

    @Override
    public boolean isModifiable() throws IllegalStateException {
        checkState();
        return !criteria.criteria.isReadOnly();
    }

    @Override
    public void setDisplaySlot(DisplaySlot slot) throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();
        Scoreboard board = scoreboard.board;
        ScoreboardObjective objective = this.objective;

        for(ScoreboardDisplaySlot nmsSlot : ScoreboardDisplaySlot.values()) {
            if(board.getObjectiveForSlot(nmsSlot) == objective)
                board.setObjectiveSlot(nmsSlot, null);
        }

        if (slot != null) {
            ScoreboardDisplaySlot nmsSlot = CardboardScoreboardTranslations.fromBukkitSlot(slot);
            board.setObjectiveSlot(nmsSlot, objective);
        }
    }

    @Override
    public DisplaySlot getDisplaySlot() throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();
        Scoreboard board = scoreboard.board;
        ScoreboardObjective objective = this.objective;

        for(ScoreboardDisplaySlot slot : ScoreboardDisplaySlot.values()) {
            if(board.getObjectiveForSlot(slot) == objective) {
                return CardboardScoreboardTranslations.toBukkitSlot(slot);
            }
        }

        return null;
    }

    @Override
    public void setRenderType(RenderType renderType) throws IllegalStateException {
        Validate.notNull(renderType, "RenderType cannot be null");
        checkState();
        this.objective.setRenderType(CardboardScoreboardTranslations.fromBukkitRender(renderType));
    }

    @Override
    public RenderType getRenderType() throws IllegalStateException {
        checkState();
        return CardboardScoreboardTranslations.toBukkitRender(this.objective.getRenderType());
    }

    @Override
    public Score getScore(OfflinePlayer player) throws IllegalArgumentException, IllegalStateException {
        Validate.notNull(player, "Player cannot be null");
        checkState();
        return new CardboardScore(this, player::getName);
    }

    @Override
    public Score getScore(String entry) throws IllegalArgumentException, IllegalStateException {
        Validate.notNull(entry, "Entry cannot be null");
        Validate.isTrue(entry.length() <= 40, "Score '" + entry + "' is longer than the limit of 40 characters");
        checkState();
        return new CardboardScore(this, () -> entry);
    }

    @Override
    public void unregister() throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();
        scoreboard.board.removeObjective(objective);
    }

    @Override
    public CardboardScoreboard checkState() throws IllegalStateException {
        if (getScoreboard().board.getNullableObjective(objective.getName()) == null)
            throw new IllegalStateException("Unregistered scoreboard component");
        return getScoreboard();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 31 * hash + (this.objective != null ? this.objective.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || getClass() != obj.getClass())
            return false;
        final CardboardObjective other = (CardboardObjective) obj;
        return Objects.equals(this.objective, other.objective);
    }

    // 1.18.2 api:
    
    @Override
    public @NotNull Component displayName() throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void displayName(@Nullable Component arg0) throws IllegalStateException, IllegalArgumentException {
        // TODO Auto-generated method stub
        this.setDisplayName(arg0.toString());
    }

	@Override
	public @NotNull Score getScoreFor(@NotNull Entity arg0) throws IllegalArgumentException, IllegalStateException {
		// TODO Auto-generated method stub
		return new CardboardScore(this, arg0.getName());
	}

	@Override
	public @NotNull Criteria getTrackedCriteria() throws IllegalStateException {
		checkState();

        return criteria;
	}

}
