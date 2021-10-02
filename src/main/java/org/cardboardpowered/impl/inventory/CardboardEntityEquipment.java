package org.cardboardpowered.impl.inventory;

import com.google.common.base.Preconditions;
import net.minecraft.entity.mob.MobEntity;

import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.cardboardpowered.impl.entity.LivingEntityImpl;
import org.jetbrains.annotations.NotNull;

import com.javazilla.bukkitfabric.Utils;

public class CardboardEntityEquipment implements EntityEquipment {

    private final LivingEntityImpl entity;

    public CardboardEntityEquipment(LivingEntityImpl entity) {
        this.entity = entity;
    }

    @Override
    public void setItem(EquipmentSlot slot, ItemStack item) {
        this.setItem(slot, item, false);
    }

    @Override
    public void setItem(EquipmentSlot slot, ItemStack item, boolean silent) {
        Preconditions.checkArgument(slot != null, "slot must not be null");
        net.minecraft.entity.EquipmentSlot nmsSlot = Utils.getNMS(slot);
        setEquipment(nmsSlot, item, silent);
    }

    @Override
    public ItemStack getItem(EquipmentSlot slot) {
        Preconditions.checkArgument(slot != null, "slot must not be null");
        net.minecraft.entity.EquipmentSlot nmsSlot = Utils.getNMS(slot);
        return getEquipment(nmsSlot);
    }

    @Override
    public ItemStack getItemInMainHand() {
        return getEquipment(net.minecraft.entity.EquipmentSlot.MAINHAND);
    }

    @Override
    public void setItemInMainHand(ItemStack item) {
        this.setItemInMainHand(item, false);
    }

    @Override
    public void setItemInMainHand(ItemStack item, boolean silent) {
        setEquipment(net.minecraft.entity.EquipmentSlot.MAINHAND, item, silent);
    }

    @Override
    public ItemStack getItemInOffHand() {
        return getEquipment(net.minecraft.entity.EquipmentSlot.OFFHAND);
    }

    @Override
    public void setItemInOffHand(ItemStack item) {
        this.setItemInOffHand(item, false);
    }

    @Override
    public void setItemInOffHand(ItemStack item, boolean silent) {
        setEquipment(net.minecraft.entity.EquipmentSlot.OFFHAND, item, silent);
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
    public ItemStack getHelmet() {
        return getEquipment(net.minecraft.entity.EquipmentSlot.HEAD);
    }

    @Override
    public void setHelmet(ItemStack helmet) {
        this.setHelmet(helmet, false);
    }

    @Override
    public void setHelmet(ItemStack helmet, boolean silent) {
        setEquipment(net.minecraft.entity.EquipmentSlot.HEAD, helmet, silent);
    }

    @Override
    public ItemStack getChestplate() {
        return getEquipment(net.minecraft.entity.EquipmentSlot.CHEST);
    }

    @Override
    public void setChestplate(ItemStack chestplate) {
        this.setChestplate(chestplate, false);
    }

    @Override
    public void setChestplate(ItemStack chestplate, boolean silent) {
        setEquipment(net.minecraft.entity.EquipmentSlot.CHEST, chestplate, silent);
    }

    @Override
    public ItemStack getLeggings() {
        return getEquipment(net.minecraft.entity.EquipmentSlot.LEGS);
    }

    @Override
    public void setLeggings(ItemStack leggings) {
        this.setLeggings(leggings, false);
    }

    @Override
    public void setLeggings(ItemStack leggings, boolean silent) {
        setEquipment(net.minecraft.entity.EquipmentSlot.LEGS, leggings, silent);
    }

    @Override
    public ItemStack getBoots() {
        return getEquipment(net.minecraft.entity.EquipmentSlot.FEET);
    }

    @Override
    public void setBoots(ItemStack boots) {
        this.setBoots(boots, false);
    }

    @Override
    public void setBoots(ItemStack boots, boolean silent) {
        setEquipment(net.minecraft.entity.EquipmentSlot.FEET, boots, silent);
    }

    @Override
    public ItemStack[] getArmorContents() {
        ItemStack[] armor = new ItemStack[]{
                getEquipment(net.minecraft.entity.EquipmentSlot.FEET),
                getEquipment(net.minecraft.entity.EquipmentSlot.LEGS),
                getEquipment(net.minecraft.entity.EquipmentSlot.CHEST),
                getEquipment(net.minecraft.entity.EquipmentSlot.HEAD),
        };
        return armor;
    }

    @Override
    public void setArmorContents(ItemStack[] items) {
        setEquipment(net.minecraft.entity.EquipmentSlot.FEET, items.length >= 1 ? items[0] : null, false);
        setEquipment(net.minecraft.entity.EquipmentSlot.LEGS, items.length >= 2 ? items[1] : null, false);
        setEquipment(net.minecraft.entity.EquipmentSlot.CHEST, items.length >= 3 ? items[2] : null, false);
        setEquipment(net.minecraft.entity.EquipmentSlot.HEAD, items.length >= 4 ? items[3] : null, false);
    }

    private ItemStack getEquipment(net.minecraft.entity.EquipmentSlot slot) {
        return CraftItemStack.asBukkitCopy(entity.getHandle().getEquippedStack(slot));
    }

    private void setEquipment(net.minecraft.entity.EquipmentSlot slot, ItemStack stack, boolean silent) {
        entity.getHandle().equipStack(slot, CraftItemStack.asNMSCopy(stack));
    }

    @Override
    public void clear() {
        for (net.minecraft.entity.EquipmentSlot slot : net.minecraft.entity.EquipmentSlot.values()) {
            setEquipment(slot, null, false);
        }
    }

    @Override
    public Entity getHolder() {
        return entity;
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
       return getDropChance(net.minecraft.entity.EquipmentSlot.MAINHAND);
    }

    @Override
    public void setItemInMainHandDropChance(float chance) {
        setDropChance(net.minecraft.entity.EquipmentSlot.MAINHAND, chance);
    }

    @Override
    public float getItemInOffHandDropChance() {
        return getDropChance(net.minecraft.entity.EquipmentSlot.OFFHAND);
    }

    @Override
    public void setItemInOffHandDropChance(float chance) {
        setDropChance(net.minecraft.entity.EquipmentSlot.OFFHAND, chance);
    }

    @Override
    public float getHelmetDropChance() {
        return getDropChance(net.minecraft.entity.EquipmentSlot.HEAD);
    }

    @Override
    public void setHelmetDropChance(float chance) {
        setDropChance(net.minecraft.entity.EquipmentSlot.HEAD, chance);
    }

    @Override
    public float getChestplateDropChance() {
        return getDropChance(net.minecraft.entity.EquipmentSlot.CHEST);
    }

    @Override
    public void setChestplateDropChance(float chance) {
        setDropChance(net.minecraft.entity.EquipmentSlot.CHEST, chance);
    }

    @Override
    public float getLeggingsDropChance() {
        return getDropChance(net.minecraft.entity.EquipmentSlot.LEGS);
    }

    @Override
    public void setLeggingsDropChance(float chance) {
        setDropChance(net.minecraft.entity.EquipmentSlot.LEGS, chance);
    }

    @Override
    public float getBootsDropChance() {
        return getDropChance(net.minecraft.entity.EquipmentSlot.FEET);
    }

    @Override
    public void setBootsDropChance(float chance) {
        setDropChance(net.minecraft.entity.EquipmentSlot.FEET, chance);
    }

    private void setDropChance(net.minecraft.entity.EquipmentSlot slot, float chance) {
        if (slot == net.minecraft.entity.EquipmentSlot.MAINHAND || slot == net.minecraft.entity.EquipmentSlot.OFFHAND) {
            ((MobEntity) entity.getHandle()).handDropChances[slot.getEntitySlotId()] = chance;
        } else {
            ((MobEntity) entity.getHandle()).armorDropChances[slot.getEntitySlotId()] = chance;
        }
    }

    private float getDropChance(net.minecraft.entity.EquipmentSlot slot) {
        if (!(entity.getHandle() instanceof MobEntity)) return 1;

        if (slot == net.minecraft.entity.EquipmentSlot.MAINHAND || slot == net.minecraft.entity.EquipmentSlot.OFFHAND) {
            return ((MobEntity) entity.getHandle()).handDropChances[slot.getEntitySlotId()];
        } else {
            return ((MobEntity) entity.getHandle()).armorDropChances[slot.getEntitySlotId()];
        }
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
