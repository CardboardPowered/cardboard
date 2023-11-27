package org.cardboardpowered.impl.inventory;

import com.google.common.base.Preconditions;
import com.javazilla.bukkitfabric.interfaces.IMixinInventory;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.packet.s2c.play.ScreenHandlerSlotUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.UpdateSelectedSlotS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import org.apache.commons.lang.Validate;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.cardboardpowered.impl.entity.PlayerImpl;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class CardboardPlayerInventory extends CraftInventory implements org.bukkit.inventory.PlayerInventory, EntityEquipment {

    public CardboardPlayerInventory(net.minecraft.entity.player.PlayerInventory inventory) {
        super(inventory);
    }

    @Override
    public PlayerInventory getInventory() {
        return (PlayerInventory) inventory;
    }

    @Override
    public ItemStack[] getStorageContents() {
        return asCraftMirror(getInventory().main);
    }

    @Override
    public ItemStack getItemInMainHand() {
        return CraftItemStack.asCraftMirror(getInventory().getMainHandStack());
    }

    @Override
    public void setItemInMainHand(ItemStack item) {
        setItem(getHeldItemSlot(), item);
    }

    @Override
    public ItemStack getItemInOffHand() {
        return CraftItemStack.asCraftMirror(getInventory().offHand.get(0));
    }

    @Override
    public void setItemInOffHand(ItemStack item) {
        ItemStack[] extra = getExtraContents();
        extra[0] = item;
        setExtraContents(extra);
    }

    @Override
    public ItemStack getItemInHand() {
        return getItemInMainHand();
    }

    @Override
    public void setItemInHand(ItemStack stack) {
        setItemInMainHand(stack);
    }

    @Override
    public void setItem(int index, ItemStack item) {
        super.setItem(index, item);
        if (this.getHolder() == null) return;
        ServerPlayerEntity player = ((PlayerImpl) this.getHolder()).getHandle();
        if (player.networkHandler == null) return;

        if (index < PlayerInventory.getHotbarSize())
            index += 36;
        else if (index > 39)
            index += 5; // Off hand
        else if (index > 35)
            index = 8 - (index - 36);

        player.networkHandler.sendPacket(new ScreenHandlerSlotUpdateS2CPacket(player.playerScreenHandler.syncId,  player.playerScreenHandler.nextRevision(), index, CraftItemStack.asNMSCopy(item)));
    }

    @Override
    public void setItem(EquipmentSlot slot, ItemStack item) {
        Preconditions.checkArgument(slot != null, "slot must not be null");

        switch (slot) {
            case HAND:
                this.setItemInMainHand(item);
                break;
            case OFF_HAND:
                this.setItemInOffHand(item);
                break;
            case FEET:
                this.setBoots(item);
                break;
            case LEGS:
                this.setLeggings(item);
                break;
            case CHEST:
                this.setChestplate(item);
                break;
            case HEAD:
                this.setHelmet(item);
                break;
            default:
                throw new IllegalArgumentException("Not implemented. This is a bug");
        }
    }

    @Override
    public ItemStack getItem(EquipmentSlot slot) {
        Preconditions.checkArgument(slot != null, "slot must not be null");

        switch (slot) {
            case HAND:
                return getItemInMainHand();
            case OFF_HAND:
                return getItemInOffHand();
            case FEET:
                return getBoots();
            case LEGS:
                return getLeggings();
            case CHEST:
                return getChestplate();
            case HEAD:
                return getHelmet();
            default:
                throw new IllegalArgumentException("Not implemented");
        }
    }

    @Override
    public int getHeldItemSlot() {
        return getInventory().selectedSlot;
    }

    @Override
    public void setHeldItemSlot(int slot) {
        Validate.isTrue(slot >= 0 && slot < PlayerInventory.getHotbarSize(), "Slot is not between 0 and 8 inclusive");
        this.getInventory().selectedSlot = slot;
        ((PlayerImpl) this.getHolder()).getHandle().networkHandler.sendPacket(new UpdateSelectedSlotS2CPacket(slot));
    }

    @Override
    public ItemStack getHelmet() {
        return getItem(getSize() - 2);
    }

    @Override
    public ItemStack getChestplate() {
        return getItem(getSize() - 3);
    }

    @Override
    public ItemStack getLeggings() {
        return getItem(getSize() - 4);
    }

    @Override
    public ItemStack getBoots() {
        return getItem(getSize() - 5);
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        setItem(getSize() - 2, helmet);
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        setItem(getSize() - 3, chestplate);
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        setItem(getSize() - 4, leggings);
    }

    @Override
    public void setBoots(ItemStack boots) {
        setItem(getSize() - 5, boots);
    }

    @Override
    public ItemStack[] getArmorContents() {
        return asCraftMirror(getInventory().armor);
    }

    private void setSlots(ItemStack[] items, int baseSlot, int length) {
        if (items == null)
            items = new ItemStack[length];

        Preconditions.checkArgument(items.length <= length, "items.length must be < " + length);

        for (int i = 0; i < length; i++)
            setItem(baseSlot + i, i >= items.length ? null : items[i]);
    }

    @Override
    public void setStorageContents(ItemStack[] items) throws IllegalArgumentException {
        setSlots(items, 0, getInventory().main.size());
    }

    @Override
    public void setArmorContents(ItemStack[] items) {
        setSlots(items, getInventory().main.size(), getInventory().armor.size());
    }

    @Override
    public ItemStack[] getExtraContents() {
        return asCraftMirror(getInventory().offHand);
    }

    @Override
    public void setExtraContents(ItemStack[] items) {
        setSlots(items, getInventory().main.size() + getInventory().armor.size(), getInventory().offHand.size());
    }

    @Override
    public HumanEntity getHolder() {
        return (HumanEntity) ((IMixinInventory)inventory).getOwner();
    }

    @Override
    public float getItemInHandDropChance() {
        return getItemInMainHandDropChance();
    }

    @Override
    public void setItemInHandDropChance(float chance) {
        setItemInMainHandDropChance(chance);
    }

    @Override
    public float getItemInMainHandDropChance() {
        return 1;
    }

    @Override
    public void setItemInMainHandDropChance(float chance) {
        throw new UnsupportedOperationException("Cannot set drop chance for PlayerInventory");
    }

    @Override
    public float getItemInOffHandDropChance() {
        return 1;
    }

    @Override
    public void setItemInOffHandDropChance(float chance) {
        throw new UnsupportedOperationException("Cannot set drop chance for PlayerInventory");
    }

    @Override
    public float getHelmetDropChance() {
        return 1;
    }

    @Override
    public void setHelmetDropChance(float chance) {
        throw new UnsupportedOperationException("Cannot set drop chance for PlayerInventory");
    }

    @Override
    public float getChestplateDropChance() {
        return 1;
    }

    @Override
    public void setChestplateDropChance(float chance) {
        throw new UnsupportedOperationException("Cannot set drop chance for PlayerInventory");
    }

    @Override
    public float getLeggingsDropChance() {
        return 1;
    }

    @Override
    public void setLeggingsDropChance(float chance) {
        throw new UnsupportedOperationException("Cannot set drop chance for PlayerInventory");
    }

    @Override
    public float getBootsDropChance() {
        return 1;
    }

    @Override
    public void setBootsDropChance(float chance) {
        throw new UnsupportedOperationException("Cannot set drop chance for PlayerInventory");
    }

    @Override
    public InventoryHolder getHolder(boolean arg0) {
        return this.getHolder();
    }

    @Override
    public HashMap<Integer, ItemStack> removeItemAnySlot(ItemStack... arg0) throws IllegalArgumentException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setBoots(ItemStack arg0, boolean arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setChestplate(ItemStack arg0, boolean arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setHelmet(ItemStack arg0, boolean arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setItem(EquipmentSlot arg0, ItemStack arg1, boolean arg2) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setItemInMainHand(ItemStack arg0, boolean arg1) {
        this.setItemInMainHand(arg0);
    }

    @Override
    public void setItemInOffHand(ItemStack arg0, boolean arg1) {
        // TODO Auto-generated method stub
        this.setItemInOffHand(arg0);
    }

    @Override
    public void setLeggings(ItemStack arg0, boolean arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public int close() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public float getDropChance(@NotNull EquipmentSlot arg0) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setDropChance(@NotNull EquipmentSlot arg0, float arg1) {
        // TODO Auto-generated method stub
        
    }

}
