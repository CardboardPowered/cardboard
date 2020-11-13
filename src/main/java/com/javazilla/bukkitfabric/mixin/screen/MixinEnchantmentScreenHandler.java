package com.javazilla.bukkitfabric.mixin.screen;

import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.cardboardpowered.impl.inventory.CardboardEnchantingInventory;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.craftbukkit.util.CraftNamespacedKey;
import org.bukkit.enchantments.EnchantmentOffer;
import org.bukkit.entity.Player;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.enchantment.PrepareItemEnchantEvent;
import org.bukkit.inventory.InventoryView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandlerContext;

import net.minecraft.advancement.criterion.Criteria;
import net.minecraft.block.Blocks;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.EnchantmentLevelEntry;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.screen.Property;
import net.minecraft.screen.ScreenHandlerContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.stat.Stats;
import net.minecraft.util.registry.Registry;

@Mixin(EnchantmentScreenHandler.class)
public class MixinEnchantmentScreenHandler extends MixinScreenHandler {

    @Shadow public Inventory inventory;
    @Shadow public ScreenHandlerContext context;
    @Shadow public Random random;
    @Shadow public Property seed;

    @Shadow public int[] enchantmentPower;
    @Shadow public int[] enchantmentId;
    @Shadow public int[] enchantmentLevel;

    private CardboardInventoryView bukkitEntity = null;
    private Player player;

    @Inject(method = "<init>*", at = @At("TAIL"))
    public void setPlayerInv(int i, PlayerInventory playerinventory, ScreenHandlerContext containeraccesss, CallbackInfo ci) {
        this.player = (Player)((IMixinEntity)playerinventory.player).getBukkitEntity();
    }

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public void onContentChanged(Inventory iinventory) {
        if (iinventory == this.inventory) {
            ItemStack itemstack = iinventory.getStack(0);

            if (!itemstack.isEmpty()) {
                this.context.run((world, blockposition) -> {
                    int i = 0;
                    int j;

                    for (j = -1; j <= 1; ++j) {
                        for (int k = -1; k <= 1; ++k) {
                            if ((j != 0 || k != 0) && world.isAir(blockposition.add(k, 0, j)) && world.isAir(blockposition.add(k, 1, j))) {
                                if (world.getBlockState(blockposition.add(k * 2, 0, j * 2)).isOf(Blocks.BOOKSHELF)) ++i;
                                if (world.getBlockState(blockposition.add(k * 2, 1, j * 2)).isOf(Blocks.BOOKSHELF)) ++i;
                                if (k != 0 && j != 0) {
                                    if (world.getBlockState(blockposition.add(k * 2, 0, j)).isOf(Blocks.BOOKSHELF)) ++i;
                                    if (world.getBlockState(blockposition.add(k * 2, 1, j)).isOf(Blocks.BOOKSHELF)) ++i;
                                    if (world.getBlockState(blockposition.add(k, 0, j * 2)).isOf(Blocks.BOOKSHELF)) ++i;
                                    if (world.getBlockState(blockposition.add(k, 1, j * 2)).isOf(Blocks.BOOKSHELF)) ++i;
                                }
                            }
                        }
                    }
                    this.random.setSeed((long) this.seed.get());
                    for (j = 0; j < 3; ++j) {
                        this.enchantmentPower[j] = EnchantmentHelper.calculateRequiredExperienceLevel(this.random, j, i, itemstack);
                        this.enchantmentId[j] = -1;
                        this.enchantmentLevel[j] = -1;
                        if (this.enchantmentPower[j] < j + 1) this.enchantmentPower[j] = 0;
                    }

                    for (j = 0; j < 3; ++j) {
                        if (this.enchantmentPower[j] > 0) {
                            List<EnchantmentLevelEntry> list = this.generateEnchantments(itemstack, j, this.enchantmentPower[j]);
                            if (list != null && !list.isEmpty()) {
                                EnchantmentLevelEntry weightedrandomenchant = (EnchantmentLevelEntry) list.get(this.random.nextInt(list.size()));
                                this.enchantmentId[j] = Registry.ENCHANTMENT.getRawId(weightedrandomenchant.enchantment); // CraftBukkit - decompile error
                                this.enchantmentLevel[j] = weightedrandomenchant.level;
                            }
                        }
                    }

                    CraftItemStack item = CraftItemStack.asCraftMirror(itemstack);
                    org.bukkit.enchantments.EnchantmentOffer[] offers = new EnchantmentOffer[3];
                    for (j = 0; j < 3; ++j) {
                        org.bukkit.enchantments.Enchantment enchantment = (this.enchantmentId[j] >= 0) ? org.bukkit.enchantments.Enchantment.getByKey(CraftNamespacedKey.fromMinecraft(Registry.ENCHANTMENT.getId(Registry.ENCHANTMENT.get(this.enchantmentId[j])))) : null;
                        offers[j] = (enchantment != null) ? new EnchantmentOffer(enchantment, this.enchantmentLevel[j], this.enchantmentPower[j]) : null;
                    }

                    PrepareItemEnchantEvent event = new PrepareItemEnchantEvent(player, this.getBukkitView(), ((IMixinScreenHandlerContext)context).getLocation().getBlock(), item, offers, i);
                    event.setCancelled(!itemstack.isEnchantable());
                    Bukkit.getPluginManager().callEvent(event);

                    if (event.isCancelled()) {
                        for (j = 0; j < 3; ++j) {
                            this.enchantmentPower[j] = 0;
                            this.enchantmentId[j] = -1;
                            this.enchantmentLevel[j] = -1;
                        }
                        return;
                    }

                    for (j = 0; j < 3; j++) {
                        EnchantmentOffer offer = event.getOffers()[j];
                        if (offer != null) {
                            this.enchantmentPower[j] = offer.getCost();
                            this.enchantmentId[j] = Registry.ENCHANTMENT.getRawId(Registry.ENCHANTMENT.get(CraftNamespacedKey.toMinecraft(offer.getEnchantment().getKey())));
                            this.enchantmentLevel[j] = offer.getEnchantmentLevel();
                        } else {
                            this.enchantmentPower[j] = 0;
                            this.enchantmentId[j] = -1;
                            this.enchantmentLevel[j] = -1;
                        }
                    }
                    ((EnchantmentScreenHandler)(Object)this).sendContentUpdates();
                });
            } else {
                for (int i = 0; i < 3; ++i) {
                    this.enchantmentPower[i] = 0;
                    this.enchantmentId[i] = -1;
                    this.enchantmentLevel[i] = -1;
                }
            }
        }

    }

    /**
     * @reason .
     * @author .
     */
    @Overwrite
    public boolean onButtonClick(PlayerEntity entityhuman, int i) {
        ItemStack itemstack = this.inventory.getStack(0);
        ItemStack itemstack1 = this.inventory.getStack(1);
        int j = i + 1;

        if ((itemstack1.isEmpty() || itemstack1.getCount() < j) && !entityhuman.abilities.creativeMode) {
            return false;
        } else if (this.enchantmentPower[i] > 0 && !itemstack.isEmpty() && (entityhuman.experienceLevel >= j && entityhuman.experienceLevel >= this.enchantmentPower[i] || entityhuman.abilities.creativeMode)) {
            this.context.run((world, blockposition) -> {
                ItemStack itemstack2 = itemstack;
                List<EnchantmentLevelEntry> list = this.generateEnchantments(itemstack, i, this.enchantmentPower[i]);

                boolean flag = itemstack.getItem() == Items.BOOK;
                Map<org.bukkit.enchantments.Enchantment, Integer> enchants = new java.util.HashMap<org.bukkit.enchantments.Enchantment, Integer>();
                for (Object obj : list) {
                    EnchantmentLevelEntry instance = (EnchantmentLevelEntry) obj;
                    enchants.put(org.bukkit.enchantments.Enchantment.getByKey(CraftNamespacedKey.fromMinecraft(Registry.ENCHANTMENT.getId(instance.enchantment))), instance.level);
                }
                CraftItemStack item = CraftItemStack.asCraftMirror(itemstack2);

                EnchantItemEvent event = new EnchantItemEvent((Player) ((IMixinEntity)entityhuman).getBukkitEntity(), this.getBukkitView(), ((IMixinScreenHandlerContext)context).getLocation().getBlock(), item, this.enchantmentPower[i], enchants, i);
                Bukkit.getPluginManager().callEvent(event);

                int level = event.getExpLevelCost();
                if (event.isCancelled() || (level > entityhuman.experienceLevel && !entityhuman.abilities.creativeMode) || event.getEnchantsToAdd().isEmpty()) return;

                if (flag) {
                    itemstack2 = new ItemStack(Items.ENCHANTED_BOOK);
                    CompoundTag nbttagcompound = itemstack.getTag();
                    if (nbttagcompound != null) itemstack2.setTag(nbttagcompound.copy());

                    this.inventory.setStack(0, itemstack2);
                }

                for (Map.Entry<org.bukkit.enchantments.Enchantment, Integer> entry : event.getEnchantsToAdd().entrySet()) {
                    try {
                        if (flag) {
                            NamespacedKey enchantId = entry.getKey().getKey();
                            Enchantment nms = Registry.ENCHANTMENT.get(CraftNamespacedKey.toMinecraft(enchantId));
                            if (nms == null) continue;

                            EnchantmentLevelEntry weightedrandomenchant = new EnchantmentLevelEntry(nms, entry.getValue());
                            EnchantedBookItem.addEnchantment(itemstack2, weightedrandomenchant);
                        } else item.addUnsafeEnchantment(entry.getKey(), entry.getValue());
                    } catch (IllegalArgumentException e) {/* Just swallow invalid enchantments */}
                }
                entityhuman.applyEnchantmentCosts(itemstack, j);

                if (!entityhuman.abilities.creativeMode) {
                    itemstack1.decrement(j);
                    if (itemstack1.isEmpty()) this.inventory.setStack(1, ItemStack.EMPTY);
                }

                entityhuman.incrementStat(Stats.ENCHANT_ITEM);
                if (entityhuman instanceof ServerPlayerEntity)
                    Criteria.ENCHANTED_ITEM.trigger((ServerPlayerEntity) entityhuman, itemstack2, j);

                this.inventory.markDirty();
                this.seed.set(entityhuman.getEnchantmentTableSeed());
                this.onContentChanged(this.inventory);
                world.playSound((PlayerEntity) null, blockposition, SoundEvents.BLOCK_ENCHANTMENT_TABLE_USE, SoundCategory.BLOCKS, 1.0F, world.random.nextFloat() * 0.1F + 0.9F);
            });
            return true;
        } else return false;
    }

    @Shadow
    public List<EnchantmentLevelEntry> generateEnchantments(ItemStack itemstack, int i, int j) {return null;}

    @Override
    public InventoryView getBukkitView() {
        if (bukkitEntity != null) return bukkitEntity;

        CardboardEnchantingInventory inventory = new CardboardEnchantingInventory(this.inventory);
        bukkitEntity = new CardboardInventoryView(this.player, inventory, (EnchantmentScreenHandler)(Object)this);
        return bukkitEntity;
    }


}