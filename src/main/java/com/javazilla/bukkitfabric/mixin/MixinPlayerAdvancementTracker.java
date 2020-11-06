package com.javazilla.bukkitfabric.mixin;

import java.util.Set;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;

import com.javazilla.bukkitfabric.interfaces.IMixinAdvancement;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.advancement.PlayerAdvancementTracker;
import net.minecraft.network.MessageType;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.TranslatableText;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;

@Mixin(PlayerAdvancementTracker.class)
public class MixinPlayerAdvancementTracker {

    @Shadow public ServerPlayerEntity owner;
    @Shadow public Set<Advancement> progressUpdates;
    @Shadow public PlayerManager field_25325;

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public boolean grantCriterion(Advancement advancement, String s) {
        boolean flag = false;
        AdvancementProgress advancementprogress = this.getProgress(advancement);
        boolean flag1 = advancementprogress.isDone();

        if (advancementprogress.obtain(s)) {
            this.endTrackingCompleted(advancement);
            this.progressUpdates.add(advancement);
            flag = true;
            if (!flag1 && advancementprogress.isDone()) {
                Bukkit.getServer().getPluginManager().callEvent(new org.bukkit.event.player.PlayerAdvancementDoneEvent((Player) ((IMixinEntity)this.owner).getBukkitEntity(), ((IMixinAdvancement)advancement).getBukkitAdvancement())); // Bukkit
                advancement.getRewards().apply(this.owner);
                if (advancement.getDisplay() != null && advancement.getDisplay().shouldAnnounceToChat() && this.owner.world.getGameRules().getBoolean(GameRules.ANNOUNCE_ADVANCEMENTS))
                    this.field_25325.broadcastChatMessage(new TranslatableText("chat.type.advancement." + advancement.getDisplay().getFrame().getId(), new Object[]{this.owner.getDisplayName(), advancement.toHoverableText()}), MessageType.SYSTEM, Util.NIL_UUID);
            }
        }

        if (advancementprogress.isDone()) this.updateDisplay(advancement);
        return flag;
    }

    @Shadow
    public void endTrackingCompleted(Advancement advancement) {
    }

    @Shadow
    public void updateDisplay(Advancement advancement) {
    }

    @Shadow
    public AdvancementProgress getProgress(Advancement advancement) {
        return null;
    }

}