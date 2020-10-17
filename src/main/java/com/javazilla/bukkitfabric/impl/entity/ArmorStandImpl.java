package com.javazilla.bukkitfabric.impl.entity;

import net.minecraft.entity.decoration.ArmorStandEntity;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;

import com.javazilla.bukkitfabric.interfaces.IMixinArmorStandEntity;

public class ArmorStandImpl extends CraftLivingEntity implements ArmorStand {

    public ArmorStandImpl(CraftServer server, ArmorStandEntity entity) {
        super(server, entity);
    }

    @Override
    public String toString() {
        return "CraftArmorStand";
    }

    @Override
    public EntityType getType() {
        return EntityType.ARMOR_STAND;
    }

    @Override
    public ArmorStandEntity getHandle() {
        return (ArmorStandEntity) super.getHandle();
    }

    @Override
    public ItemStack getItemInHand() {
        return getEquipment().getItemInHand();
    }

    @Override
    public void setItemInHand(ItemStack item) {
        getEquipment().setItemInHand(item);
    }

    @Override
    public ItemStack getBoots() {
        return getEquipment().getBoots();
    }

    @Override
    public void setBoots(ItemStack item) {
        getEquipment().setBoots(item);
    }

    @Override
    public ItemStack getLeggings() {
        return getEquipment().getLeggings();
    }

    @Override
    public void setLeggings(ItemStack item) {
        getEquipment().setLeggings(item);
    }

    @Override
    public ItemStack getChestplate() {
        return getEquipment().getChestplate();
    }

    @Override
    public void setChestplate(ItemStack item) {
        getEquipment().setChestplate(item);
    }

    @Override
    public ItemStack getHelmet() {
        return getEquipment().getHelmet();
    }

    @Override
    public void setHelmet(ItemStack item) {
        getEquipment().setHelmet(item);
    }

    @Override
    public EulerAngle getBodyPose() {
        return fromNMS(getHandle().getBodyRotation());
    }

    @Override
    public void setBodyPose(EulerAngle pose) {
        getHandle().setBodyRotation(toNMS(pose));
    }

    @Override
    public EulerAngle getLeftArmPose() {
        return fromNMS(getHandle().getLeftArmRotation());
    }

    @Override
    public void setLeftArmPose(EulerAngle pose) {
        getHandle().setLeftArmRotation(toNMS(pose));
    }

    @Override
    public EulerAngle getRightArmPose() {
        return fromNMS(getHandle().getRightArmRotation());
    }

    @Override
    public void setRightArmPose(EulerAngle pose) {
        getHandle().setRightArmRotation(toNMS(pose));
    }

    @Override
    public EulerAngle getLeftLegPose() {
        return fromNMS(getHandle().getLeftLegRotation());
    }

    @Override
    public void setLeftLegPose(EulerAngle pose) {
        getHandle().setLeftLegRotation(toNMS(pose));
    }

    @Override
    public EulerAngle getRightLegPose() {
        return fromNMS(getHandle().getRightLegRotation());
    }

    @Override
    public void setRightLegPose(EulerAngle pose) {
        getHandle().setRightLegRotation(toNMS(pose));
    }

    @Override
    public EulerAngle getHeadPose() {
        return fromNMS(getHandle().getHeadRotation());
    }

    @Override
    public void setHeadPose(EulerAngle pose) {
        getHandle().setHeadRotation(toNMS(pose));
    }

    @Override
    public boolean hasBasePlate() {
        return !getHandle().shouldHideBasePlate();
    }

    @Override
    public void setBasePlate(boolean basePlate) {
        ((IMixinArmorStandEntity)getHandle()).setHideBasePlateBF(!basePlate);
    }

    @Override
    public void setGravity(boolean gravity) {
        super.setGravity(gravity);
        // Armor stands are special
        getHandle().noClip = !gravity;
    }

    @Override
    public boolean isVisible() {
        return !getHandle().isInvisible();
    }

    @Override
    public void setVisible(boolean visible) {
        getHandle().setInvisible(!visible);
    }

    @Override
    public boolean hasArms() {
        return getHandle().shouldShowArms();
    }

    @Override
    public void setArms(boolean arms) {
        ((IMixinArmorStandEntity)getHandle()).setShowArmsBF(arms);
    }

    @Override
    public boolean isSmall() {
        return getHandle().isSmall();
    }

    @Override
    public void setSmall(boolean small) {
        ((IMixinArmorStandEntity)getHandle()).setSmallBF(small);
    }

    private static EulerAngle fromNMS(net.minecraft.util.math.EulerAngle old) {
        return new EulerAngle(
            Math.toRadians(old.getPitch()),
            Math.toRadians(old.getYaw()),
            Math.toRadians(old.getRoll())
        );
    }

    private static net.minecraft.util.math.EulerAngle toNMS(EulerAngle old) {
        return new net.minecraft.util.math.EulerAngle(
            (float) Math.toDegrees(old.getX()),
            (float) Math.toDegrees(old.getY()),
            (float) Math.toDegrees(old.getZ())
        );
    }

    @Override
    public boolean isMarker() {
        return getHandle().isMarker();
    }

    @Override
    public void setMarker(boolean marker) {
        ((IMixinArmorStandEntity)getHandle()).setMarkerBF(marker);
    }

    @Override
    public void addEquipmentLock(EquipmentSlot slot, LockType lockType) {
        // TODO Auto-generated method stub
    }

    @Override
    public void removeEquipmentLock(EquipmentSlot slot, LockType lockType) {
        // TODO Auto-generated method stub
    }

    @Override
    public boolean hasEquipmentLock(EquipmentSlot slot, LockType lockType) {
        // TODO Auto-generated method stub
        return false;
    }

}