package org.cardboardpowered.mixin.commands;

import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.cardboardpowered.bridge.commands.CommandSourceStackBridge;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.level.LevelBridge;
import org.cardboardpowered.impl.world.CraftWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.cardboardpowered.bridge.commands.CommandSourceBridge;
import org.spongepowered.asm.mixin.Shadow;

/**
 * Also makes the vanilla source stack a Paper {@link io.papermc.paper.command.brigadier.CommandSourceStack},
 * which is what lets plugin built Brigadier nodes run against the vanilla dispatcher.
 */
@Mixin(CommandSourceStack.class)
public abstract class CommandSourceStackMixin implements CommandSourceStackBridge, io.papermc.paper.command.brigadier.CommandSourceStack {
    @Shadow
    public CommandSource source;

    @Shadow public abstract Vec3 getPosition();
    @Shadow public abstract Vec2 getRotation();
    @Shadow public abstract ServerLevel getLevel();
    @Shadow public abstract net.minecraft.world.entity.Entity getEntity();
    @Shadow public abstract CommandSourceStack withLevel(ServerLevel level);
    @Shadow public abstract CommandSourceStack withPosition(Vec3 position);
    @Shadow public abstract CommandSourceStack withRotation(Vec2 rotation);
    @Shadow public abstract CommandSourceStack withEntity(net.minecraft.world.entity.Entity entity);

    // CraftBukkit start
    public org.bukkit.command.CommandSender getBukkitSender() {
        return ((CommandSourceBridge)this.source).getBukkitSender((CommandSourceStack)(Object)this);
    }
    // CraftBukkit end

    @Override
    public Location getLocation() {
        Vec3 position = this.getPosition();
        Vec2 rotation = this.getRotation();
        return new Location(((LevelBridge)(Object) this.getLevel()).cardboard$getWorld(),
                position.x, position.y, position.z, rotation.y, rotation.x);
    }

    @Override
    public org.bukkit.command.CommandSender getSender() {
        return this.getBukkitSender();
    }

    @Override
    public org.bukkit.entity.Entity getExecutor() {
        net.minecraft.world.entity.Entity entity = this.getEntity();
        return (entity == null) ? null : ((EntityBridge)(Object) entity).getBukkitEntity();
    }

    @Override
    public io.papermc.paper.command.brigadier.CommandSourceStack withLocation(Location location) {
        CommandSourceStack moved = this.withPosition(new Vec3(location.getX(), location.getY(), location.getZ()))
                .withRotation(new Vec2(location.getPitch(), location.getYaw()));

        if (location.getWorld() != null)
            moved = moved.withLevel(((CraftWorld) location.getWorld()).getHandle());

        return (io.papermc.paper.command.brigadier.CommandSourceStack)(Object) moved;
    }

    @Override
    public io.papermc.paper.command.brigadier.CommandSourceStack withExecutor(org.bukkit.entity.Entity executor) {
        return (io.papermc.paper.command.brigadier.CommandSourceStack)(Object) this.withEntity(((CraftEntity) executor).getHandle());
    }
}
