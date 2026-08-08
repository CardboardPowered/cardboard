package org.bukkit.craftbukkit.inventory;

import com.google.common.base.Preconditions;
import io.papermc.paper.adventure.PaperAdventure;
import io.papermc.paper.event.player.PlayerPurchaseEvent;
import javax.annotation.Nullable;
import net.kyori.adventure.text.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.cardboardpowered.bridge.world.entity.npc.villager.AbstractVillagerBridge;

public class CraftMerchantCustom implements CraftMerchant {
   private CraftMerchantCustom.MinecraftMerchant merchant;

   @Deprecated
   public CraftMerchantCustom(String title) {
      this.merchant = new CraftMerchantCustom.MinecraftMerchant(title);
      this.getMerchant().craftMerchant = this;
   }

   public CraftMerchantCustom(Component title) {
      this.merchant = new CraftMerchantCustom.MinecraftMerchant(title);
      this.getMerchant().craftMerchant = this;
   }

   public CraftMerchantCustom() {
      this.merchant = new CraftMerchantCustom.MinecraftMerchant();
      this.getMerchant().craftMerchant = this;
   }

   public CraftMerchantCustom.MinecraftMerchant getMerchant() {
      return this.merchant;
   }

   public static class MinecraftMerchant implements Merchant, AbstractVillagerBridge {
      private final net.minecraft.network.chat.Component title;
      private final MerchantOffers trades = new MerchantOffers();
      private Player tradingPlayer;
      protected CraftMerchant craftMerchant;

      @Deprecated
      public MinecraftMerchant(String title) {
         Preconditions.checkArgument(title != null, "Title cannot be null");
         this.title = CraftChatMessage.fromString(title)[0];
      }

      public MinecraftMerchant(Component title) {
         Preconditions.checkArgument(title != null, "Title cannot be null");
         this.title = PaperAdventure.asVanilla(title);
      }

      public MinecraftMerchant() {
         this.title = EntityTypes.VILLAGER.getDescription();
      }

      @Override
      public CraftMerchant getCraftMerchant() {
         return this.craftMerchant;
      }

      @Override
      public void setTradingPlayer(Player customer) {
         this.tradingPlayer = customer;
      }

      @Override
      public Player getTradingPlayer() {
         return this.tradingPlayer;
      }

      @Override
      public MerchantOffers getOffers() {
         return this.trades;
      }

      // @Override
      public void processTrade(MerchantOffer offer, @Nullable PlayerPurchaseEvent event) {
         if (this.getTradingPlayer() instanceof ServerPlayer) {
            if (event == null || event.willIncreaseTradeUses()) {
               offer.increaseUses();
            }

            if (event == null || event.isRewardingExp()) {
               this.tradingPlayer
                  .level()
                  .addFreshEntity(
                     new ExperienceOrb(
                        this.tradingPlayer.level(),
                        this.tradingPlayer.getX(),
                        this.tradingPlayer.getY(),
                        this.tradingPlayer.getZ(),
                        offer.getXp()
                        // SpawnReason.VILLAGER_TRADE,
                        // this.tradingPlayer,
                        // null
                     )
                  );
            }
         }

         this.notifyTrade(offer);
      }

      @Override
      public void notifyTrade(MerchantOffer offer) {
      }

      @Override
      public void notifyTradeUpdated(ItemStack stack) {
      }

      public net.minecraft.network.chat.Component getScoreboardDisplayName() {
         return this.title;
      }

      @Override
      public int getVillagerXp() {
         return 0;
      }

      @Override
      public void overrideXp(int experience) {
      }

      @Override
      public boolean showProgressBar() {
         return false;
      }

      @Override
      public SoundEvent getNotifyTradeSound() {
         return SoundEvents.VILLAGER_YES;
      }

      @Override
      public void overrideOffers(MerchantOffers offers) {
      }

      @Override
      public boolean isClientSide() {
         return false;
      }

      @Override
      public boolean stillValid(Player player) {
         return this.tradingPlayer == player;
      }
   }
}
