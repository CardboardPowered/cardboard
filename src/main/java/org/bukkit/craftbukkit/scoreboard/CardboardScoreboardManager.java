package org.bukkit.craftbukkit.scoreboard;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.network.packet.s2c.play.ScoreboardObjectiveUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang.Validate;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.cardboardpowered.impl.util.WeakCollection;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.ScoreboardManager;

import com.javazilla.bukkitfabric.interfaces.IMixinPlayerManager;

public final class CardboardScoreboardManager implements ScoreboardManager {

    private final CardboardScoreboard mainScoreboard;
    private final MinecraftServer server;
    private final Collection<CardboardScoreboard> scoreboards = new WeakCollection<CardboardScoreboard>();
    private final Map<PlayerImpl, CardboardScoreboard> playerBoards = new HashMap<PlayerImpl, CardboardScoreboard>();

    public CardboardScoreboardManager(MinecraftServer minecraftserver, net.minecraft.scoreboard.Scoreboard scoreboardServer) {
        mainScoreboard = new CardboardScoreboard(scoreboardServer);
        server = minecraftserver;
        scoreboards.add(mainScoreboard);
    }

    @Override
    public CardboardScoreboard getMainScoreboard() {
        return mainScoreboard;
    }

    @Override
    public CardboardScoreboard getNewScoreboard() {
        CardboardScoreboard scoreboard = new CardboardScoreboard(new ServerScoreboard(server));
        scoreboards.add(scoreboard);
        return scoreboard;
    }

    // CardboardBukkit method
    public CardboardScoreboard getPlayerBoard(PlayerImpl player) {
        CardboardScoreboard board = playerBoards.get(player);
        return (CardboardScoreboard) (board == null ? getMainScoreboard() : board);
    }

    // CardboardBukkit method
    public void setPlayerBoard(PlayerImpl player, org.bukkit.scoreboard.Scoreboard bukkitScoreboard) throws IllegalArgumentException {
        Validate.isTrue(bukkitScoreboard instanceof CardboardScoreboard, "Cannot set player scoreboard to an unregistered Scoreboard");

        CardboardScoreboard scoreboard = (CardboardScoreboard) bukkitScoreboard;
        net.minecraft.scoreboard.Scoreboard oldboard = getPlayerBoard(player).getHandle();
        net.minecraft.scoreboard.Scoreboard newboard = scoreboard.getHandle();
        ServerPlayerEntity entityplayer = player.getHandle();

        if (oldboard == newboard) return;

        if (scoreboard == mainScoreboard) {
            playerBoards.remove(player);
        } else playerBoards.put(player, (CardboardScoreboard) scoreboard);

        // Old objective tracking
        HashSet<ScoreboardObjective> removed = new HashSet<ScoreboardObjective>();
        for (int i = 0; i < 3; ++i) {
            ScoreboardObjective scoreboardobjective = oldboard.getObjectiveForSlot(i);
            if (scoreboardobjective != null && !removed.contains(scoreboardobjective)) {
                entityplayer.networkHandler.sendPacket(new ScoreboardObjectiveUpdateS2CPacket(scoreboardobjective, 1));
                removed.add(scoreboardobjective);
            }
        }

        // Old team tracking
        Iterator<?> iterator = oldboard.getTeams().iterator();
        //while (iterator.hasNext())
        // TODO: 1.17ify    entityplayer.networkHandler.sendPacket(new TeamS2CPacket((Team) iterator.next(), 1));

        // The above is the reverse of the below method. 
        ((IMixinPlayerManager)server.getPlayerManager()).sendScoreboardBF((ServerScoreboard) newboard, player.getHandle());
    }

    // CardboardBukkit method
    public void removePlayer(Player player) {
        playerBoards.remove(player);
    }

    // CardboardBukkit method
    public void getScoreboardScores(ScoreboardCriterion criteria, String name, Consumer<ScoreboardPlayerScore> consumer) {
        for (CardboardScoreboard scoreboard : scoreboards) {
            Scoreboard board = scoreboard.board;
            board.forEachScore(criteria, name, (score) -> consumer.accept(score));
        }
    }

}