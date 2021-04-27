package org.cardboardpowered.mixin;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinServerEntityPlayer;

import net.minecraft.network.Packet;
import net.minecraft.network.packet.s2c.play.TeamS2CPacket;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardObjective;
import net.minecraft.scoreboard.ServerScoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerScoreboard.class)
public class MixinServerScoreboard extends Scoreboard {

    @Shadow
    public Set<ScoreboardObjective> objectives;

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public void addScoreboardObjective(ScoreboardObjective scoreboardobjective) {
        List<Packet<?>> list = ((ServerScoreboard)(Object)this).createChangePackets(scoreboardobjective);
        Iterator iterator = CraftServer.INSTANCE.getHandle().getPlayerManager().getPlayerList().iterator();

        while (iterator.hasNext()) {
            ServerPlayerEntity entityplayer = (ServerPlayerEntity) iterator.next();
            if (((PlayerImpl)((IMixinServerEntityPlayer)entityplayer).getBukkitEntity()).getScoreboard().getHandle() != (ServerScoreboard)(Object)this) continue; // Bukkit - Only players on this board
            Iterator iterator1 = list.iterator();

            while (iterator1.hasNext()) {
                Packet<?> packet = (Packet) iterator1.next();
                entityplayer.networkHandler.sendPacket(packet);
            }
        }

        this.objectives.add(scoreboardobjective);
    }

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public void removeScoreboardObjective(ScoreboardObjective scoreboardobjective) {
        List<Packet<?>> list = ((ServerScoreboard)(Object)this).createRemovePackets(scoreboardobjective);
        Iterator iterator = CraftServer.INSTANCE.getHandle().getPlayerManager().getPlayerList().iterator();

        while (iterator.hasNext()) {
            ServerPlayerEntity entityplayer = (ServerPlayerEntity) iterator.next();
            if (((PlayerImpl)((IMixinServerEntityPlayer)entityplayer).getBukkitEntity()).getScoreboard().getHandle() != (ServerScoreboard)(Object)this) continue; // Bukkit - Only players on this board
            Iterator iterator1 = list.iterator();

            while (iterator1.hasNext()) {
                Packet<?> packet = (Packet) iterator1.next();
                entityplayer.networkHandler.sendPacket(packet);
            }
        }

        this.objectives.remove(scoreboardobjective);
    }

    private void sendAll(Packet packet) {
        for (ServerPlayerEntity entityplayer : CraftServer.server.getPlayerManager().players)
            if (((PlayerImpl)((IMixinServerEntityPlayer)entityplayer).getBukkitEntity()).getScoreboard().getHandle() == (ServerScoreboard)(Object)this)
                entityplayer.networkHandler.sendPacket(packet);
    }

}