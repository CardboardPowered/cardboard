package org.cardboardpowered.api.event;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Vehicle;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.event.vehicle.VehicleExitEvent;

public class CardboardEventManager {

    public static CardboardEventManager INSTANCE = new CardboardEventManager();

    public void callCardboardEvents() {
        this.callCardboardFireworkExplodeEvent();
        this.callCardboardEntityMountEvent();
    }

    private void callCardboardFireworkExplodeEvent() {
        CardboardFireworkExplodeEvent.EVENT.register((firework) -> {
            if (BukkitEventFactory.callFireworkExplodeEvent(firework).isCancelled()) {
                return ActionResult.FAIL;
            }else {
                return ActionResult.PASS;
            }
        });
    }

    private void callCardboardEntityMountEvent() {
        CardboardEntityMountEvent.EVENT.register((vehicle, entity) -> {
            if (vehicle.getPassengerList().isEmpty()) {
                CraftEntity craft = (CraftEntity) ((IMixinEntity) vehicle).getBukkitEntity().getVehicle();
                Entity orig = craft == null ? null : craft.getHandle();
                if (((IMixinEntity) vehicle).getBukkitEntity() instanceof Vehicle && ((IMixinEntity) vehicle).getBukkitEntity() instanceof org.bukkit.entity.LivingEntity) {
                    VehicleExitEvent CBevent = new VehicleExitEvent(
                            (Vehicle) ((IMixinEntity) vehicle).getBukkitEntity(),
                            (LivingEntity) ((IMixinEntity) entity).getBukkitEntity()
                    );
                    if (((IMixinEntity) entity).isValidBF()) {
                        Bukkit.getPluginManager().callEvent(CBevent);
                    }
                    CraftEntity craftn = (CraftEntity) ((IMixinEntity) vehicle).getBukkitEntity().getVehicle();
                    Entity n = craftn == null ? null : craftn.getHandle();
                    if (CBevent.isCancelled() || n != orig) {
                        return ActionResult.FAIL;
                    }
                }

                org.spigotmc.event.entity.EntityDismountEvent SPevent = new org.spigotmc.event.entity.EntityDismountEvent(((IMixinEntity) vehicle).getBukkitEntity(), ((IMixinEntity) entity).getBukkitEntity());
                if (((IMixinEntity) vehicle).isValidBF()) {
                    Bukkit.getPluginManager().callEvent(SPevent);
                }
                if (SPevent.isCancelled()) {
                    return ActionResult.FAIL;
                }
            }
            if (!vehicle.getPassengerList().isEmpty()) {
                com.google.common.base.Preconditions.checkState(!vehicle.getPassengerList().contains(vehicle), "Circular entity riding! %s %s", this, entity);

                CraftEntity craft = (CraftEntity) ((IMixinEntity) vehicle).getBukkitEntity().getVehicle();
                Entity orig = craft == null ? null : craft.getHandle();
                if (((IMixinEntity) entity).getBukkitEntity() instanceof Vehicle && ((IMixinEntity) vehicle).getBukkitEntity() instanceof org.bukkit.entity.LivingEntity) {
                    VehicleEnterEvent CBevent = new VehicleEnterEvent(
                            (Vehicle) ((IMixinEntity) entity).getBukkitEntity(),
                            ((IMixinEntity) vehicle).getBukkitEntity()
                            );
                    if (((IMixinEntity) entity).isValidBF()) {
                        Bukkit.getPluginManager().callEvent(CBevent);
                    }
                    CraftEntity craftn = (CraftEntity) ((IMixinEntity) vehicle).getBukkitEntity().getVehicle();
                    Entity n = craftn == null ? null : craftn.getHandle();
                    if (CBevent.isCancelled() || n != orig) {
                        return ActionResult.FAIL;
                    }
                }

                org.spigotmc.event.entity.EntityMountEvent SPevent = new org.spigotmc.event.entity.EntityMountEvent(((IMixinEntity) vehicle).getBukkitEntity(), ((IMixinEntity) entity).getBukkitEntity());
                if (((IMixinEntity) entity).isValidBF()) {
                    Bukkit.getPluginManager().callEvent(SPevent);
                    if (SPevent.isCancelled()) {
                        return ActionResult.FAIL;
                    }
                }
            }
            return ActionResult.PASS;
        });
    }
}
