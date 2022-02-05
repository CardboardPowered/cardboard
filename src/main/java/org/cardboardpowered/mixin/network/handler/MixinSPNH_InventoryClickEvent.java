package org.cardboardpowered.mixin.network.handler;

import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.util.CraftMagicNumbers;
import org.bukkit.entity.HumanEntity;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.CraftItemEvent;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.cardboardpowered.impl.inventory.CardboardInventoryView;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinScreenHandler;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.ClickSlotC2SPacket;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@MixinInfo(events = {"InventoryClickEvent", "CraftItemEvent"})
@Mixin(value = ServerPlayNetworkHandler.class, priority = 800)
public class MixinSPNH_InventoryClickEvent {

    @Shadow 
    public ServerPlayerEntity player;

    private boolean doCl = false;
    
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;onSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V"), method = "onClickSlot")
    public void doBukkitEvent_InventoryClickedEvent_skipOriginalProcess(ScreenHandler handler, int i, int j, SlotActionType actionType, PlayerEntity playerEntity) {
        //
        if (doCl) handler.onSlotClick(i, j, actionType, playerEntity);
    }

    @SuppressWarnings("deprecation")
    @Inject(at = @At(value = "INVOKE", target = "Lnet/minecraft/screen/ScreenHandler;onSlotClick(IILnet/minecraft/screen/slot/SlotActionType;Lnet/minecraft/entity/player/PlayerEntity;)V", 
            shift = At.Shift.BEFORE), method = "onClickSlot", cancellable = true)
    public void doBukkitEvent_InventoryClickedEvent(ClickSlotC2SPacket packet, CallbackInfo ci) {
        if(packet.getSlot() < -1 && packet.getSlot() != -999)
            return;

        this.doCl = false;
        CardboardInventoryView inventory = ((IMixinScreenHandler) player.currentScreenHandler).getBukkitView();
        inventory.setPlayerIfNotSet((HumanEntity) ((com.javazilla.bukkitfabric.interfaces.IMixinEntity)this.player).getBukkitEntity());

        InventoryType.SlotType type = inventory.getSlotType(packet.getSlot());

        InventoryClickEvent event;
        ClickType click = ClickType.UNKNOWN;
        InventoryAction action = InventoryAction.UNKNOWN;

        switch (packet.getActionType()) {
            case PICKUP:
                click = packet.getButton() == 0 ? ClickType.LEFT : (packet.getButton() == 1 ? ClickType.RIGHT : ClickType.UNKNOWN);

                if(packet.getButton() == 0 || packet.getButton() == 1) {
                    action = InventoryAction.NOTHING; // Don't want to repeat ourselves
                    if(packet.getSlot() == -999) {
                        if(!player.currentScreenHandler.getCursorStack().isEmpty())
                            action = packet.getButton() == 0 ? InventoryAction.DROP_ALL_CURSOR : InventoryAction.DROP_ONE_CURSOR;
                    } else if(packet.getSlot() < 0) {
                        action = InventoryAction.NOTHING;
                    } else {
                        Slot slot = this.player.currentScreenHandler.getSlot(packet.getSlot());
                        if(slot != null) {
                            ItemStack clickedItem = slot.getStack();
                            ItemStack cursor = player.currentScreenHandler.getCursorStack();
                            if(clickedItem.isEmpty()) {
                                if(!cursor.isEmpty()) {
                                    action = packet.getButton() == 0 ? InventoryAction.PLACE_ALL : InventoryAction.PLACE_ONE;
                                }
                            } else if(slot.canTakeItems(player)) {
                                if(cursor.isEmpty()) {
                                    action = packet.getButton() == 0 ? InventoryAction.PICKUP_ALL : InventoryAction.PICKUP_HALF;
                                } else if(slot.canInsert(cursor)) {
                                    if(clickedItem.isItemEqualIgnoreDamage(cursor) && ItemStack.areNbtEqual(clickedItem, cursor)) {
                                        int toPlace = packet.getButton() == 0 ? cursor.getCount() : 1;
                                        toPlace = Math.min(toPlace, clickedItem.getMaxCount() - clickedItem.getCount());
                                        toPlace = Math.min(toPlace, slot.inventory.getMaxCountPerStack() - clickedItem.getCount());
                                        if(toPlace == 1) {
                                            action = InventoryAction.PLACE_ONE;
                                        } else if(toPlace == cursor.getCount()) {
                                            action = InventoryAction.PLACE_ALL;
                                        } else if(toPlace < 0) {
                                            action = toPlace != -1 ? InventoryAction.PICKUP_SOME : InventoryAction.PICKUP_ONE;
                                        } else if(toPlace != 0)
                                            action = InventoryAction.PLACE_SOME;
                                    } else if(cursor.getCount() < slot.getMaxItemCount()) {
                                        action = InventoryAction.SWAP_WITH_CURSOR;
                                    } else if(cursor.getItem() == clickedItem.getItem() && ItemStack.areNbtEqual(cursor ,clickedItem)) {
                                        if(clickedItem.getCount() >= 0)
                                            if(clickedItem.getCount() + cursor.getCount() <= cursor.getMaxCount())
                                                action = InventoryAction.PICKUP_ALL;
                                    }
                                }
                            }
                        }
                    }
                }
                break;
            case QUICK_MOVE:
                if(packet.getButton() == 0) {
                    click = ClickType.LEFT;
                } else if(packet.getButton() == 1) {
                    click = ClickType.RIGHT;
                }
                if(packet.getButton() == 0 || packet.getButton() == 1) {
                    if(packet.getSlot() < 0) {
                        action = InventoryAction.NOTHING;
                    } else {
                        Slot slot = this.player.currentScreenHandler.getSlot(packet.getSlot());
                        if(slot != null && slot.canTakeItems(this.player) && slot.hasStack()) {
                            action = InventoryAction.MOVE_TO_OTHER_INVENTORY;
                        } else {
                            action = InventoryAction.NOTHING;
                        }
                    }
                }
                break;
            case SWAP:
                if((packet.getButton() >= 0 && packet.getButton() <= 9) || packet.getButton() == 40) {
                    click = (packet.getButton() == 40) ? ClickType.SWAP_OFFHAND : ClickType.NUMBER_KEY;
                    Slot clickedSlot = this.player.currentScreenHandler.getSlot(packet.getSlot());
                    if(clickedSlot != null && clickedSlot.canTakeItems(player)) {
                        ItemStack hotbar = this.player.inventory.getStack(packet.getButton());
                        boolean canCleanSwap = hotbar.isEmpty() || (clickedSlot.inventory == player.inventory); // the slot will accept the hotbar item
                        if(clickedSlot.hasStack()) {
                            if(canCleanSwap) {
                                action = InventoryAction.HOTBAR_SWAP;
                            } else {
                                action = InventoryAction.HOTBAR_MOVE_AND_READD;
                            }
                        } else if(!clickedSlot.hasStack() && !hotbar.isEmpty() && clickedSlot.canInsert(hotbar)) {
                            action = InventoryAction.HOTBAR_SWAP;
                        } else {
                            action = InventoryAction.NOTHING;
                        }
                    } else {
                        action = InventoryAction.NOTHING;
                    }
                }
                break;
            case CLONE:
                if(packet.getButton() == 2) {
                    click = ClickType.MIDDLE;
                    if(packet.getSlot() < 0) {
                        action = InventoryAction.NOTHING;
                    } else {
                        Slot slot = this.player.currentScreenHandler.getSlot(packet.getSlot());
                        if(slot != null && slot.hasStack() && player.abilities.creativeMode && player.currentScreenHandler.getCursorStack().isEmpty()) {
                            action = InventoryAction.CLONE_STACK;
                        } else {
                            action = InventoryAction.NOTHING;
                        }
                    }
                } else {
                    click = ClickType.UNKNOWN;
                    action = InventoryAction.UNKNOWN;
                }
                break;
            case THROW:
                if(packet.getSlot() >= 0) {
                    if(packet.getButton() == 0) {
                        click = ClickType.DROP;
                        Slot slot = this.player.currentScreenHandler.getSlot(packet.getSlot());
                        if(slot != null && slot.hasStack() && slot.canTakeItems(player) && !slot.getStack().isEmpty() && slot.getStack().getItem() != Item.fromBlock(Blocks.AIR)) {
                            action = InventoryAction.DROP_ONE_SLOT;
                        } else {
                            action = InventoryAction.NOTHING;
                        }
                    } else if(packet.getButton() == 0) {
                        click = ClickType.DROP;
                        Slot slot = this.player.currentScreenHandler.getSlot(packet.getSlot());
                        if(slot != null && slot.hasStack() && slot.canTakeItems(player) && !slot.getStack().isEmpty() && slot.getStack().getItem() != Item.fromBlock(Blocks.AIR)) {
                            action = InventoryAction.DROP_ALL_SLOT;
                        } else {
                            action = InventoryAction.NOTHING;
                        }
                    }
                } else {
                    click = ClickType.LEFT;
                    if(packet.getButton() == 1) {
                        click = ClickType.RIGHT;
                    }
                    action = InventoryAction.NOTHING;
                }
                break;
            case QUICK_CRAFT:
                this.player.currentScreenHandler.onSlotClick(packet.getSlot(), packet.getButton(), packet.getActionType(), this.player);
                break;
            case PICKUP_ALL:
                click = ClickType.DOUBLE_CLICK;
                action = InventoryAction.NOTHING;
                if(packet.getSlot() >= 0 && !this.player.currentScreenHandler.getCursorStack().isEmpty()) {
                    ItemStack cursor = this.player.currentScreenHandler.getCursorStack();
                    action = InventoryAction.NOTHING;
                    // Quick check for if we have any of the item
                    if(inventory.getTopInventory().contains(CraftMagicNumbers.getMaterial(cursor.getItem())) || inventory.getBottomInventory().contains(CraftMagicNumbers.getMaterial(cursor.getItem()))) {
                        action = InventoryAction.COLLECT_TO_CURSOR;
                    }
                }
                break;
            default:
                break;
        }
        if(packet.getActionType() != SlotActionType.QUICK_CRAFT) {
            if(click == ClickType.NUMBER_KEY) {
                event = new InventoryClickEvent(inventory, type, packet.getSlot(), click, action, packet.getButton());
            } else {
                event = new InventoryClickEvent(inventory, type, packet.getSlot(), click, action);
            }

            Inventory top = inventory.getTopInventory();
            if(packet.getSlot() == 0 && top instanceof org.bukkit.inventory.CraftingInventory) {
                org.bukkit.inventory.Recipe recipe = ((org.bukkit.inventory.CraftingInventory) top).getRecipe();
                if(recipe != null) {
                    if(click == ClickType.NUMBER_KEY) {
                        event = new CraftItemEvent(recipe, inventory, type, packet.getSlot(), click, action, packet.getButton());
                    } else {
                        event = new CraftItemEvent(recipe, inventory, type, packet.getSlot(), click, action);
                    }
                }
            }

            event.setCancelled(player.isSpectator());
            ScreenHandler oldContainer = player.currentScreenHandler;
            Bukkit.getServer().getPluginManager().callEvent(event);
            if(player.currentScreenHandler != oldContainer) {
                ci.cancel();
                return;
            }

            switch (event.getResult()) {
                case ALLOW:
                case DEFAULT:
                    this.doCl = true;
                    break;
                case DENY:
                    // [DELETED COMMENTS FROM BUKKIT]
                    switch (action) {
                        // Modified other slots
                        case PICKUP_ALL:
                        case MOVE_TO_OTHER_INVENTORY:
                        case HOTBAR_MOVE_AND_READD:
                        case HOTBAR_SWAP:
                        case COLLECT_TO_CURSOR:
                        case UNKNOWN:
                            player.currentScreenHandler.syncState();
                            break;
                        // Modified cursor and clicked
                        case PICKUP_SOME:
                        case PICKUP_HALF:
                        case PICKUP_ONE:
                        case PLACE_ALL:
                        case PLACE_SOME:
                        case PLACE_ONE:
                            this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, -1, this.player.playerScreenHandler.nextRevision(), this.player.currentScreenHandler.getCursorStack()));
                            this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.player.currentScreenHandler.syncId, packet.getSlot(), this.player.playerScreenHandler.nextRevision(), this.player.currentScreenHandler.getSlot(packet.getSlot()).getStack()));
                            break;
                        // Modified clicked only
                        case DROP_ALL_SLOT:
                        case DROP_ONE_SLOT:
                            this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(this.player.currentScreenHandler.syncId, packet.getSlot(), this.player.playerScreenHandler.nextRevision(), this.player.currentScreenHandler.getSlot(packet.getSlot()).getStack()));
                            break;
                        // Modified cursor only
                        case DROP_ALL_CURSOR:
                        case DROP_ONE_CURSOR:
                        case CLONE_STACK:
                            this.player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(-1, -1, this.player.playerScreenHandler.nextRevision(), this.player.currentScreenHandler.getCursorStack()));
                            break;
                        case NOTHING:
                        default:
                            break;
                    }
                    ci.cancel();
                    return;
            }
            if(event instanceof CraftItemEvent) player.currentScreenHandler.syncState();
        }
    }

}