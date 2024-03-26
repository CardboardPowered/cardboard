package org.cardboardpowered.mixin.entity;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import com.mojang.authlib.GameProfile;

import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageTracker;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardCriterion;
// import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;

@Mixin(ServerPlayerEntity.class)
public abstract class MixinServerPlayerEntity_DeathEvent extends PlayerEntity {

	public MixinServerPlayerEntity_DeathEvent(World w, BlockPos p, GameProfile gp) {
		super(w, p, 0, gp);
	}

	// Bukkit start
	public boolean keepLevel = false;
	// Bukkit end
	
	private AtomicReference<String> cardboard$deathString = new AtomicReference<>("null");
    private AtomicReference<String> cardboard$deathMsg = new AtomicReference<>("null");

    private AtomicReference<PlayerDeathEvent> cardboard$deathEvent = new AtomicReference<>();

    private ServerPlayerEntity cb$this() {
    	return (ServerPlayerEntity)(Object)this;
    }

    @Inject(method = "onDeath", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/GameRules;getBoolean(Lnet/minecraft/world/GameRules$Key;)Z",
            ordinal = 0),
            cancellable = true)
    private void cardboard$do_PlayerDeathEvent(DamageSource damageSource, CallbackInfo ci) {
        // CraftBukkit start - fire PlayerDeathEvent
        if (cb$this().isRemoved()) {
            ci.cancel();
        }
        java.util.List<org.bukkit.inventory.ItemStack> loot = new java.util.ArrayList<>(cb$this().getInventory().size());

        boolean keepInventory = cb$this().getWorld().getGameRules().getBoolean(GameRules.KEEP_INVENTORY) || cb$this().isSpectator();
        
        if (!keepInventory) {
            for (net.minecraft.item.ItemStack item : ((IMixinInventory) ((ServerPlayerEntity) (Object) this).getInventory()).getContents()) {
                if (!item.isEmpty() && !EnchantmentHelper.hasVanishingCurse(item)) {
                    loot.add(CraftItemStack.asCraftMirror(item));
                }
            }
        }
        // SPIGOT-5071: manually add player loot tables (SPIGOT-5195 - ignores keepInventory rule)
        this.dropLoot(damageSource, cb$this().playerHitTimer > 0);

        ((IMixinEntity)(Object)this).cardboard_getDrops();

        for (org.bukkit.inventory.ItemStack item : ((IMixinEntity)(Object)this).cardboard_getDrops()) {
            loot.add(item);
        }
        // SPIGOT-5188: make sure to clear
        ((IMixinEntity)(Object)this).cardboard_getDrops().clear();

        Text defaultMessage = cb$this().getDamageTracker().getDeathMessage();
        String deathmessage = defaultMessage.getString();
        cardboard$deathMsg.set(deathmessage);
        keepLevel = keepInventory; // SPIGOT-2222: pre-set keepLevel
        org.bukkit.event.entity.PlayerDeathEvent event = BukkitEventFactory.callPlayerDeathEvent(((ServerPlayerEntity) (Object) this), loot, deathmessage, keepInventory);
        cardboard$deathEvent.set(event);

        // SPIGOT-943 - only call if they have an inventory open
        if (cb$this().currentScreenHandler != cb$this().playerScreenHandler) {
            this.closeHandledScreen();
        }

        String deathMessage = event.getDeathMessage();
        cardboard$deathString.set(deathMessage);
    }

    @Inject(method = "onDeath", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/damage/DamageTracker;getDeathMessage()Lnet/minecraft/text/Text;"),
            cancellable = true)
    private void cardboard$check_if_dead(DamageSource damageSource, CallbackInfo ci) {
        boolean cardboard$flag = cardboard$deathString.get() != null && !cardboard$deathString.get().isEmpty();
        if (!cardboard$flag) { // TODO: allow plugins to override?
            ci.cancel();
        }
    }

    @Redirect(method = "onDeath", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/entity/damage/DamageTracker;getDeathMessage()Lnet/minecraft/text/Text;"))
    private Text cardboard$redirect_death_message(DamageTracker instance) {
        Text cardboard$component;
        if (cardboard$deathString.get().equals(cardboard$deathMsg.get())) {
            cardboard$component = instance.getDeathMessage();
        } else {
            cardboard$component = CraftChatMessage.fromStringOrNull(cardboard$deathString.get());
        }
        return cardboard$component;
    }

    @Inject(method = "onDeath", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;isSpectator()Z"))
    private void cardboard$check_event_drops(DamageSource damageSource, CallbackInfo ci) {
        // SPIGOT-5478 must be called manually now
    	cb$this().dropXp();
        // we clean the player's inventory after the EntityDeathEvent is called so plugins can get the exact state of the inventory.
        if (!cardboard$deathEvent.get().getKeepInventory()) {
        	cb$this().getInventory().clear();
        }
    }

    @Redirect(method = "onDeath",
            at = @At(value = "INVOKE",
            target = "Lnet/minecraft/server/network/ServerPlayerEntity;drop(Lnet/minecraft/entity/damage/DamageSource;)V"))
    private void cardboard$cancel_vanilla_drop(ServerPlayerEntity instance, DamageSource damageSource) {
    }
    
    // Lnet/minecraft/world/entity/LivingEntity;dropAllDeathLoot(Lnet/minecraft/world/damagesource/DamageSource;)V
    // Lnet/minecraft/entity/LivingEntity;drop(Lnet/minecraft/entity/damage/DamageSource;)V

    
    // TODO: 1.20.4
    /*@Redirect(method = "onDeath", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/scoreboard/Scoreboard;forEachScore(Lnet/minecraft/scoreboard/ScoreboardCriterion;Ljava/lang/String;Ljava/util/function/Consumer;)V "))
    private void cardboard$use_bukkit_scoreboard(Scoreboard instance, ScoreboardCriterion criteria, String scoreboardName, Consumer<ScoreboardPlayerScore> action) {
    	cb$this().setCameraEntity(((ServerPlayerEntity) (Object) this));
        CraftServer.INSTANCE.getScoreboardManager().getScoreboardScores(ScoreboardCriterion.DEATH_COUNT, cb$this().getEntityName(), ScoreboardPlayerScore::incrementScore);
    }*/
    
    

	
}
