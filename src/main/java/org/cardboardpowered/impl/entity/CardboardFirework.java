package org.cardboardpowered.impl.entity;

import java.util.Random;
import java.util.UUID;

import com.javazilla.bukkitfabric.interfaces.IMixinDataTracker;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.meta.FireworkMeta;
import org.jetbrains.annotations.Nullable;

public class CardboardFirework extends ProjectileImpl implements Firework {

    private final Random random = new Random();
    private final CraftItemStack item;

    public CardboardFirework(CraftServer server, FireworkRocketEntity entity) {
        super(server, entity);

        ItemStack item = getHandle().getDataTracker().get(FireworkRocketEntity.ITEM);

        if (item.isEmpty()) {
            item = new ItemStack(Items.FIREWORK_ROCKET);
            getHandle().getDataTracker().set(FireworkRocketEntity.ITEM, item);
        }

        this.item = CraftItemStack.asCraftMirror(item);

        // Ensure the item is a firework...
        if (this.item.getType() != Material.FIREWORK_ROCKET) {
            this.item.setType(Material.FIREWORK_ROCKET);
        }
    }

    @Override
    public FireworkRocketEntity getHandle() {
        return (FireworkRocketEntity) nms;
    }

    @Override
    public String toString() {
        return "CraftFirework";
    }

    @Override
    public EntityType getType() {
        return EntityType.FIREWORK;
    }

    @Override
    public FireworkMeta getFireworkMeta() {
        return (FireworkMeta) item.getItemMeta();
    }

    @Override
    public void setFireworkMeta(FireworkMeta meta) {
        item.setItemMeta(meta);

        // Copied from FireworkRocketEntity constructor, update firework lifetime/power
        getHandle().lifeTime = 10 * (1 + meta.getPower()) + random.nextInt(6) + random.nextInt(7);

        ((IMixinDataTracker) getHandle().getDataTracker()).markDirty(FireworkRocketEntity.ITEM);
    }

    @Override
    public void detonate() {
        getHandle().lifeTime = 0;
    }

    @Override
    public boolean isShotAtAngle() {
        return getHandle().wasShotAtAngle();
    }

    @Override
    public void setShotAtAngle(boolean shotAtAngle) {
        getHandle().getDataTracker().set(FireworkRocketEntity.SHOT_AT_ANGLE, shotAtAngle);
    }

    @Override
    public @Nullable UUID getSpawningEntity() {
        return null;//TODO
    }

    @Override
    public @Nullable LivingEntity getBoostedEntity() {
        return null;//TODO
    }
}
