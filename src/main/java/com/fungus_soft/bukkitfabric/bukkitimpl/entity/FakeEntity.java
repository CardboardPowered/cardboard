package com.fungus_soft.bukkitfabric.bukkitimpl.entity;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.bukkit.EntityEffect;
import org.bukkit.Location;
import org.bukkit.Server;
import org.bukkit.World;
import org.bukkit.block.BlockFace;
import org.bukkit.block.PistonMoveReaction;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Pose;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerTeleportEvent.TeleportCause;
import org.bukkit.metadata.MetadataValue;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.plugin.Plugin;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Vector;

import com.mojang.brigadier.Message;
import com.fungus_soft.bukkitfabric.interfaces.IMixinCommandOutput;
import com.mojang.brigadier.LiteralMessage;

import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Texts;

public class FakeEntity implements Entity, CommandSender, IMixinCommandOutput {

    public net.minecraft.entity.Entity nms;

    public FakeEntity(net.minecraft.entity.Entity entity) {
        this.nms = entity;
    }

    @Override
    public List<MetadataValue> getMetadata(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasMetadata(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void removeMetadata(String arg0, Plugin arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setMetadata(String arg0, MetadataValue arg1) {
        // TODO Auto-generated method stub
    }

    @Override
    public String getName() {
        return nms.getEntityName();
    }

    @Override
    public void sendMessage(String message) {
        nms.sendMessage(Texts.toText(new LiteralMessage(message)));
    }

    @Override
    public void sendMessage(String[] arg0) {
        for (String str : arg0)
            sendMessage(str);
    }

    @Override
    public PermissionAttachment addAttachment(Plugin arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public  PermissionAttachment addAttachment(Plugin arg0, int arg1) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public  PermissionAttachment addAttachment(Plugin arg0, String arg1, boolean arg2, int arg3) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<PermissionAttachmentInfo> getEffectivePermissions() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasPermission(String arg0) {
        // TODO Auto-generated method stub
        return true; // TODO
    }

    @Override
    public boolean hasPermission(Permission arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPermissionSet(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPermissionSet(Permission arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void recalculatePermissions() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void removeAttachment(PermissionAttachment arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean isOp() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setOp(boolean arg0) {
        // TODO Auto-generated method stub
    }

    @Override
    public  String getCustomName() {
        return nms.getCustomName().asString();
    }

    @Override
    public void setCustomName( String arg0) {
        nms.setCustomName(Texts.toText(new LiteralMessage(arg0)));
    }

    @Override
    public PersistentDataContainer getPersistentDataContainer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean addPassenger(Entity arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean addScoreboardTag(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean eject() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public BoundingBox getBoundingBox() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getEntityId() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public BlockFace getFacing() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public float getFallDistance() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public int getFireTicks() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public double getHeight() {
        return nms.getHeight();
    }

    @Override
    public  EntityDamageEvent getLastDamageCause() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Location getLocation() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public  Location getLocation( Location arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getMaxFireTicks() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public List<Entity> getNearbyEntities(double arg0, double arg1, double arg2) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public  Entity getPassenger() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<Entity> getPassengers() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PistonMoveReaction getPistonMoveReaction() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getPortalCooldown() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Pose getPose() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Set<String> getScoreboardTags() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Server getServer() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int getTicksLived() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public EntityType getType() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UUID getUniqueId() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public  Entity getVehicle() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Vector getVelocity() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public double getWidth() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public World getWorld() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean hasGravity() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isCustomNameVisible() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isDead() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isGlowing() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInsideVehicle() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isOnGround() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isPersistent() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isSilent() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isValid() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean leaveVehicle() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void playEffect(EntityEffect arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void remove() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean removePassenger(Entity arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean removeScoreboardTag(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setCustomNameVisible(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setFallDistance(float arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setFireTicks(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setGlowing(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setGravity(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setInvulnerable(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setLastDamageCause( EntityDamageEvent arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean setPassenger(Entity arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setPersistent(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setPortalCooldown(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setRotation(float arg0, float arg1) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setSilent(boolean arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setTicksLived(int arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setVelocity(Vector arg0) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public boolean teleport(Location arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean teleport(Entity arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean teleport(Location arg0, TeleportCause arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean teleport(Entity arg0, TeleportCause arg1) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public CommandSender getBukkitSender(ServerCommandSource serverCommandSource) {
        return this;
    }

}
