package org.bukkit.craftbukkit.scoreboard;

import net.minecraft.scoreboard.ReadableScoreboardScore;
import net.minecraft.scoreboard.ScoreHolder;
import net.minecraft.scoreboard.Scoreboard;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Objective;
import org.bukkit.scoreboard.Score;

public final class CardboardScore implements Score {

    private final ScoreHolder entry;
    private final CardboardObjective objective;

    public CardboardScore(CardboardObjective objective, ScoreHolder entry) {
        this.objective = objective;
        this.entry = entry;
    }

    @Override
    public OfflinePlayer getPlayer() {
        return Bukkit.getOfflinePlayer(entry.getNameForScoreboard());
    }

    @Override
    public String getEntry() {
        return entry.getNameForScoreboard();
    }

    @Override
    public Objective getObjective() {
        return objective;
    }

    @Override
    public int getScore() throws IllegalStateException {
        Scoreboard board = objective.checkState().board;

        if(board.getKnownScoreHolders().contains(entry)) {
            ReadableScoreboardScore score = board.getScore(entry, objective.getHandle());
            if(score != null) {
                return score.getScore();
            }
        }

        return 0;
    }

    @Override
    public void setScore(int score) throws IllegalStateException {
        objective.checkState().board.getOrCreateScore(entry, objective.getHandle()).setScore(score);
    }

    @Override
    public boolean isScoreSet() throws IllegalStateException {
        Scoreboard board = objective.checkState().board;
        return board.getKnownScoreHolders().contains(entry) &&
                board.getScore(entry, objective.getHandle()) != null;
    }

    @Override
    public CardboardScoreboard getScoreboard() {
        return objective.getScoreboard();
    }

    @Override
    public void resetScore() throws IllegalStateException {
        // TODO Auto-generated method stub
    }

}
