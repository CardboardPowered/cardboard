package org.bukkit.craftbukkit.scoreboard;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import net.kyori.adventure.text.Component;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.entity.Entity;
import org.bukkit.scoreboard.Criteria;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.RenderType;
import org.bukkit.scoreboard.Score;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public final class CardboardScoreboard implements org.bukkit.scoreboard.Scoreboard {

    final Scoreboard board;

    CardboardScoreboard(Scoreboard board) {
        this.board = board;
    }

    @Override
    public CardboardObjective registerNewObjective(String name, String criteria) throws IllegalArgumentException {
        return registerNewObjective(name, criteria, name);
    }

    @Override
    public CardboardObjective registerNewObjective(String name, String criteria, String displayName) throws IllegalArgumentException {
        return registerNewObjective(name, criteria, displayName, RenderType.INTEGER);
    }

    @Override
    public CardboardObjective registerNewObjective(String name, String criteria, String displayName, RenderType renderType) throws IllegalArgumentException {
        Validate.notNull(name, "Objective name cannot be null");
        Validate.notNull(criteria, "Criteria cannot be null");
        Validate.notNull(displayName, "Display name cannot be null");
        Validate.notNull(renderType, "RenderType cannot be null");
        Validate.isTrue(name.length() <= 16, "The name '" + name + "' is longer than the limit of 16 characters");
        Validate.isTrue(displayName.length() <= 128, "The display name '" + displayName + "' is longer than the limit of 128 characters");
        Validate.isTrue(board.getNullableObjective(name) == null, "An objective of name '" + name + "' already exists");

        CardboardCriteria craftCriteria = CardboardCriteria.getFromBukkit(criteria);
        ScoreboardObjective objective = board.addObjective(name, craftCriteria.criteria, CraftChatMessage.fromStringOrNull(displayName), CardboardScoreboardTranslations.fromBukkitRender(renderType), true, null);
        return new CardboardObjective(this, objective);
    }

    @Override
    public Objective getObjective(String name) throws IllegalArgumentException {
        Validate.notNull(name, "Name cannot be null");
        ScoreboardObjective nms = board.getNullableObjective(name);
        return nms == null ? null : new CardboardObjective(this, nms);
    }

    @Override
    public ImmutableSet<Objective> getObjectivesByCriteria(String criteria) throws IllegalArgumentException {
        Validate.notNull(criteria, "Criteria cannot be null");

        ImmutableSet.Builder<Objective> objectives = ImmutableSet.builder();
        for (ScoreboardObjective netObjective : (Collection<ScoreboardObjective>) this.board.getObjectives()) {
            CardboardObjective objective = new CardboardObjective(this, netObjective);
            if (objective.getCriteria().equals(criteria)) objectives.add(objective);
        }
        return objectives.build();
    }

    @Override
    public ImmutableSet<Objective> getObjectives() {
        return ImmutableSet.copyOf(Iterables.transform((Collection<ScoreboardObjective>) this.board.getObjectives(), new Function<ScoreboardObjective, Objective>() {
            @Override
            public Objective apply(ScoreboardObjective input) {
                return new CardboardObjective(CardboardScoreboard.this, input);
            }
        }));
    }

    @Override
    public Objective getObjective(DisplaySlot slot) throws IllegalArgumentException {
        Validate.notNull(slot, "Display slot cannot be null");
        ScoreboardObjective objective = board.getObjectiveForSlot(CardboardScoreboardTranslations.fromBukkitSlot(slot));
        return (objective == null) ? null : new CardboardObjective(this, objective);
    }

    @Override
    public ImmutableSet<Score> getScores(OfflinePlayer player) throws IllegalArgumentException {
        Validate.notNull(player, "OfflinePlayer cannot be null");
        return getScores(player.getName());
    }

    @Override
    public ImmutableSet<Score> getScores(String entry) throws IllegalArgumentException {
        Validate.notNull(entry, "Entry cannot be null");
        return getScores(() -> entry);
    }

    private ImmutableSet<Score> getScores(ScoreHolder entry) throws IllegalArgumentException {
        ImmutableSet.Builder<Score> scores = ImmutableSet.builder();
        for (ScoreboardObjective objective : this.board.getObjectives())
            scores.add(new CardboardScore(new CardboardObjective(this, objective), entry));
        return scores.build();
    }

    @Override
    public void resetScores(OfflinePlayer player) throws IllegalArgumentException {
        Validate.notNull(player, "OfflinePlayer cannot be null");
        resetScores(player.getName());
    }

    @Override
    public void resetScores(String entry) throws IllegalArgumentException {
        Validate.notNull(entry, "Entry cannot be null");
        for (ScoreboardObjective objective : this.board.getObjectives())
            board.removeScore(() -> entry, objective);
    }

    @Override
    public Team getPlayerTeam(OfflinePlayer player) throws IllegalArgumentException {
        Validate.notNull(player, "OfflinePlayer cannot be null");

        net.minecraft.scoreboard.Team team = board.getTeam(player.getName());
        return team == null ? null : new CardboardTeam(this, team);
    }

    @Override
    public Team getEntryTeam(String entry) throws IllegalArgumentException {
        Validate.notNull(entry, "Entry cannot be null");
        net.minecraft.scoreboard.Team team = board.getTeam(entry);
        return team == null ? null : new CardboardTeam(this, team);
    }

    @Override
    public Team getTeam(String teamName) throws IllegalArgumentException {
        Validate.notNull(teamName, "Team name cannot be null");
        net.minecraft.scoreboard.Team team = board.getTeam(teamName);
        return team == null ? null : new CardboardTeam(this, team);
    }

    @Override
    public ImmutableSet<Team> getTeams() {
        return ImmutableSet.copyOf(Iterables.transform((Collection<net.minecraft.scoreboard.Team>) this.board.getTeams(), new Function<net.minecraft.scoreboard.Team, Team>() {
            @Override
            public Team apply(net.minecraft.scoreboard.Team input) {
                return new CardboardTeam(CardboardScoreboard.this, input);
            }
        }));
    }

    @Override
    public Team registerNewTeam(String name) throws IllegalArgumentException {
        Validate.notNull(name, "Team name cannot be null");
        Validate.isTrue(name.length() <= 16, "Team name '" + name + "' is longer than the limit of 16 characters");
        Validate.isTrue(board.getTeam(name) == null, "Team name '" + name + "' is already in use");
        return new CardboardTeam(this, board.addTeam(name));
    }

    @Override
    public ImmutableSet<OfflinePlayer> getPlayers() {
        ImmutableSet.Builder<OfflinePlayer> players = ImmutableSet.builder();
        for (ScoreHolder holder : board.getKnownScoreHolders())
            players.add(Bukkit.getOfflinePlayer(holder.getNameForScoreboard()));
        return players.build();
    }

    @Override
    public ImmutableSet<String> getEntries() {
        ImmutableSet.Builder<String> entries = ImmutableSet.builder();
        for (ScoreHolder entry : board.getKnownScoreHolders()) entries.add(entry.getNameForScoreboard());
        return entries.build();
    }

    @Override
    public void clearSlot(DisplaySlot slot) throws IllegalArgumentException {
        Validate.notNull(slot, "Slot cannot be null");
        board.setObjectiveSlot(CardboardScoreboardTranslations.fromBukkitSlot(slot), null);
    }

    public Scoreboard getHandle() {
        return board;
    }

    @Override
    public @NotNull Objective registerNewObjective(@NotNull String arg0, @NotNull String arg1, @Nullable Component arg2)
            throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public @NotNull Objective registerNewObjective(@NotNull String arg0, @NotNull String arg1, @Nullable Component arg2,
            @NotNull RenderType arg3) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }
    
    // 1.18.2 api:

	@Override
	public @Nullable Team getEntityTeam(@NotNull Entity arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull Set<Score> getScoresFor(@NotNull Entity arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetScoresFor(@NotNull Entity arg0) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		
	}

    @Override
    public ImmutableSet<Objective> getObjectivesByCriteria(Criteria criteria) {
        ImmutableSet.Builder<Objective> objectives = ImmutableSet.builder();
        for (net.minecraft.scoreboard.ScoreboardObjective netObjective : board.getObjectives()) {
            CardboardObjective objective = new CardboardObjective(this, netObjective);
            if (objective.getTrackedCriteria().equals(criteria)) {
                objectives.add(objective);
            }
        }

        return objectives.build();
    }
    
    @Override
    public Objective registerNewObjective(String name, Criteria criteria, String displayName) {
        return registerNewObjective(name, criteria, displayName, RenderType.INTEGER);
    }

	@Override
	public @NotNull Objective registerNewObjective(@NotNull String arg0, @NotNull Criteria arg1,
			@Nullable Component arg2) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull Objective registerNewObjective(@NotNull String arg0, @NotNull Criteria arg1,
			@Nullable Component arg2, @NotNull RenderType arg3) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull Objective registerNewObjective(@NotNull String arg0, @NotNull Criteria arg1, @NotNull String arg2,
			@NotNull RenderType arg3) throws IllegalArgumentException {
		// TODO Auto-generated method stub
		return null;
	}

}