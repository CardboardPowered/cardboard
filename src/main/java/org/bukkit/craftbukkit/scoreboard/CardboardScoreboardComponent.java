package org.bukkit.craftbukkit.scoreboard;

public abstract class CardboardScoreboardComponent {

    private CardboardScoreboard scoreboard;

    public CardboardScoreboardComponent(CardboardScoreboard scoreboard) {
        this.scoreboard = scoreboard;
    }

    abstract CardboardScoreboard checkState() throws IllegalStateException;

    public CardboardScoreboard getScoreboard() {
        return scoreboard;
    }

    abstract void unregister() throws IllegalStateException;

}
