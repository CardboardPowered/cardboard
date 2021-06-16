package org.bukkit.craftbukkit.scoreboard;

import com.google.common.collect.ImmutableSet;
import java.util.Set;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minecraft.scoreboard.AbstractTeam;
import net.minecraft.scoreboard.AbstractTeam.VisibilityRule;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Team;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CardboardTeam extends CardboardScoreboardComponent implements Team {
    private final net.minecraft.scoreboard.Team team;

    CardboardTeam(CardboardScoreboard scoreboard, net.minecraft.scoreboard.Team team) {
        super(scoreboard);
        this.team = team;
    }

    @Override
    public String getName() throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();

        return team.getName();
    }

    @Override
    public String getDisplayName() throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();

        return CraftChatMessage.fromComponent(team.getDisplayName());
    }

    @Override
    public void setDisplayName(String displayName) throws IllegalStateException {
        Validate.notNull(displayName, "Display name cannot be null");
        Validate.isTrue(ChatColor.stripColor(displayName).length() <= 128, "Display name '" + displayName + "' is longer than the limit of 128 characters");
        CardboardScoreboard scoreboard = checkState();

        team.setDisplayName(CraftChatMessage.fromString(displayName)[0]); // SPIGOT-4112: not nullable
    }

    @Override
    public String getPrefix() throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();

        return CraftChatMessage.fromComponent(team.getPrefix());
    }

    @Override
    public void setPrefix(String prefix) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(prefix, "Prefix cannot be null");
        Validate.isTrue(ChatColor.stripColor(prefix).length() <= 64, "Prefix '" + prefix + "' is longer than the limit of 64 characters");
        CardboardScoreboard scoreboard = checkState();

        team.setPrefix(CraftChatMessage.fromStringOrNull(prefix));
    }

    @Override
    public String getSuffix() throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();

        return CraftChatMessage.fromComponent(team.getSuffix());
    }

    @Override
    public void setSuffix(String suffix) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(suffix, "Suffix cannot be null");
        Validate.isTrue(ChatColor.stripColor(suffix).length() <= 64, "Suffix '" + suffix + "' is longer than the limit of 64 characters");
        CardboardScoreboard scoreboard = checkState();

        team.setSuffix(CraftChatMessage.fromStringOrNull(suffix));
    }

    @Override
    public ChatColor getColor() throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();
        return CraftChatMessage.getColor(team.getColor());
    }

    @Override
    public void setColor(ChatColor color) {
        Validate.notNull(color, "Color cannot be null");
        CardboardScoreboard scoreboard = checkState();

        team.setColor(CraftChatMessage.getColor(color));
    }

    @Override
    public boolean allowFriendlyFire() throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();

        return team.isFriendlyFireAllowed();
    }

    @Override
    public void setAllowFriendlyFire(boolean enabled) throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();

        team.setFriendlyFireAllowed(enabled);
    }

    @Override
    public boolean canSeeFriendlyInvisibles() throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();

        return team.shouldShowFriendlyInvisibles();
    }

    @Override
    public void setCanSeeFriendlyInvisibles(boolean enabled) throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();

        team.setShowFriendlyInvisibles(enabled);
    }

    @Override
    public NameTagVisibility getNameTagVisibility() throws IllegalArgumentException {
        CardboardScoreboard scoreboard = checkState();

        return notchToBukkit(team.getNameTagVisibilityRule());
    }

    @Override
    public void setNameTagVisibility(NameTagVisibility visibility) throws IllegalArgumentException {
        CardboardScoreboard scoreboard = checkState();

        team.setNameTagVisibilityRule(bukkitToNotch(visibility));
    }

    @Override
    public Set<OfflinePlayer> getPlayers() throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();

        ImmutableSet.Builder<OfflinePlayer> players = ImmutableSet.builder();
        for (String playerName : team.getPlayerList()) {
            players.add(Bukkit.getOfflinePlayer(playerName));
        }
        return players.build();
    }

    @Override
    public Set<String> getEntries() throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();

        ImmutableSet.Builder<String> entries = ImmutableSet.builder();
        for (String playerName : team.getPlayerList()) {
            entries.add(playerName);
        }
        return entries.build();
    }

    @Override
    public int getSize() throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();

        return team.getPlayerList().size();
    }

    @Override
    public void addPlayer(OfflinePlayer player) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(player, "OfflinePlayer cannot be null");
        addEntry(player.getName());
    }

    @Override
    public void addEntry(String entry) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(entry, "Entry cannot be null");
        CardboardScoreboard scoreboard = checkState();

        scoreboard.board.addPlayerToTeam(entry, team);
    }

    @Override
    public boolean removePlayer(OfflinePlayer player) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(player, "OfflinePlayer cannot be null");
        return removeEntry(player.getName());
    }

    @Override
    public boolean removeEntry(String entry) throws IllegalStateException, IllegalArgumentException {
        Validate.notNull(entry, "Entry cannot be null");
        CardboardScoreboard scoreboard = checkState();

        if (!team.getPlayerList().contains(entry)) {
            return false;
        }

        scoreboard.board.removePlayerFromTeam(entry, team);
        return true;
    }

    @Override
    public boolean hasPlayer(OfflinePlayer player) throws IllegalArgumentException, IllegalStateException {
        Validate.notNull(player, "OfflinePlayer cannot be null");
        return hasEntry(player.getName());
    }

    @Override
    public boolean hasEntry(String entry) throws IllegalArgumentException, IllegalStateException {
        Validate.notNull("Entry cannot be null");

        CardboardScoreboard scoreboard = checkState();

        return team.getPlayerList().contains(entry);
    }

    @Override
    public void unregister() throws IllegalStateException {
        CardboardScoreboard scoreboard = checkState();

        scoreboard.board.removeTeam(team);
    }

    @Override
    public OptionStatus getOption(Option option) throws IllegalStateException {
        checkState();

        switch (option) {
            case NAME_TAG_VISIBILITY:
                return OptionStatus.values()[team.getNameTagVisibilityRule().ordinal()];
            case DEATH_MESSAGE_VISIBILITY:
                return OptionStatus.values()[team.getDeathMessageVisibilityRule().ordinal()];
            case COLLISION_RULE:
                return OptionStatus.values()[team.getCollisionRule().ordinal()];
            default:
                throw new IllegalArgumentException("Unrecognised option " + option);
        }
    }

    @Override
    public void setOption(Option option, OptionStatus status) throws IllegalStateException {
        checkState();

        switch (option) {
            case NAME_TAG_VISIBILITY:
                team.setNameTagVisibilityRule(VisibilityRule.values()[status.ordinal()]);
                break;
            case DEATH_MESSAGE_VISIBILITY:
                team.setDeathMessageVisibilityRule(VisibilityRule.values()[status.ordinal()]);
                break;
            case COLLISION_RULE:
                team.setCollisionRule(AbstractTeam.CollisionRule.values()[status.ordinal()]);
                break;
            default:
                throw new IllegalArgumentException("Unrecognised option " + option);
        }
    }

    public static VisibilityRule bukkitToNotch(NameTagVisibility visibility) {
        switch (visibility) {
            case ALWAYS:
                return VisibilityRule.ALWAYS;
            case NEVER:
                return VisibilityRule.NEVER;
            case HIDE_FOR_OTHER_TEAMS:
                return VisibilityRule.HIDE_FOR_OTHER_TEAMS;
            case HIDE_FOR_OWN_TEAM:
                return VisibilityRule.HIDE_FOR_OWN_TEAM;
            default:
                throw new IllegalArgumentException("Unknown visibility level " + visibility);
        }
    }

    public static NameTagVisibility notchToBukkit(VisibilityRule visibility) {
        switch (visibility) {
            case ALWAYS:
                return NameTagVisibility.ALWAYS;
            case NEVER:
                return NameTagVisibility.NEVER;
            case HIDE_FOR_OTHER_TEAMS:
                return NameTagVisibility.HIDE_FOR_OTHER_TEAMS;
            case HIDE_FOR_OWN_TEAM:
                return NameTagVisibility.HIDE_FOR_OWN_TEAM;
            default:
                throw new IllegalArgumentException("Unknown visibility level " + visibility);
        }
    }

    @Override
    CardboardScoreboard checkState() throws IllegalStateException {
        if (getScoreboard().board.getTeam(team.getName()) == null)
            throw new IllegalStateException("Unregistered scoreboard component");
        return getScoreboard();
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 43 * hash + (this.team != null ? this.team.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        final CardboardTeam other = (CardboardTeam) obj;
        return !(this.team != other.team && (this.team == null || !this.team.equals(other.team)));
    }

    @Override
    public @NotNull TextColor color() throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void color(@Nullable NamedTextColor arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public @NotNull Component displayName() throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void displayName(@Nullable Component arg0) throws IllegalStateException, IllegalArgumentException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public @NotNull Component prefix() throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void prefix(@Nullable Component arg0) throws IllegalStateException, IllegalArgumentException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public @NotNull Component suffix() throws IllegalStateException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void suffix(@Nullable Component arg0) throws IllegalStateException, IllegalArgumentException {
        // TODO Auto-generated method stub
        
    }

}
