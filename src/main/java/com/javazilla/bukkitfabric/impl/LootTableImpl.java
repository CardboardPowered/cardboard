package com.javazilla.bukkitfabric.impl;

import com.google.common.base.Preconditions;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import net.minecraft.entity.Entity;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameter;
import net.minecraft.loot.context.LootContextParameterSet;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootContextType;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.inventory.CraftInventory;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.loot.LootContext;
import org.cardboardpowered.impl.world.WorldImpl;

public class LootTableImpl implements org.bukkit.loot.LootTable {

    public static final LootContextParameter<Integer> LOOTING_MOD = new LootContextParameter<>(new Identifier("bukkit:looting_mod")); // CraftBukkit
	
    private final LootTable handle;
    private final NamespacedKey key;

    public LootTableImpl(NamespacedKey key, LootTable handle) {
        this.handle = handle;
        this.key = key;
    }

    public LootTable getHandle() {
        return handle;
    }

    @Override
    public Collection<ItemStack> populateLoot(Random random, LootContext context) {
        Preconditions.checkArgument(context != null, "LootContext cannot be null");
        LootContextParameterSet nmsContext = convertContext(context, random);
        List<net.minecraft.item.ItemStack> nmsItems = handle.generateLoot(nmsContext);
        Collection<ItemStack> bukkit = new ArrayList<>(nmsItems.size());

        for (net.minecraft.item.ItemStack item : nmsItems) {
            if (item.isEmpty()) {
                continue;
            }
            bukkit.add(CraftItemStack.asBukkitCopy(item));
        }

        return bukkit;
    }

    @Override
    public void fillInventory(Inventory inventory, Random random, LootContext context) {
        Preconditions.checkArgument(inventory != null, "Inventory cannot be null");
        Preconditions.checkArgument(context != null, "LootContext cannot be null");
        net.minecraft.loot.context.LootContextParameterSet nmsContext = convertContext(context, random);
        CraftInventory craftInventory = (CraftInventory) inventory;
        net.minecraft.inventory.Inventory handle = craftInventory.getInventory();

        // TODO: When events are added, call event here w/ custom reason?
        getHandle().supplyInventory(handle, nmsContext, random.nextLong());
    }

    @Override
    public NamespacedKey getKey() {
        return key;
    }
    
    public static Vec3d toVec3D(Location location) {
        return new Vec3d(location.getX(), location.getY(), location.getZ());
    }

    private net.minecraft.loot.context.LootContextParameterSet convertContext(LootContext context, Random random) {
        Preconditions.checkArgument(context != null, "LootContext cannot be null");
        Location loc = context.getLocation();
        Preconditions.checkArgument(loc.getWorld() != null, "LootContext.getLocation#getWorld cannot be null");
        ServerWorld handle = ((WorldImpl) loc.getWorld()).getHandle();
        net.minecraft.loot.context.LootContextParameterSet.Builder builder = new net.minecraft.loot.context.LootContextParameterSet.Builder(handle);
        if (random != null) {
            // builder = builder.withRandom(new RandomSourceWrapper(random)); // Banner TODO
        }

        setMaybe(builder, LootContextParameters.ORIGIN, toVec3D(loc));
        if (getHandle() != LootTable.EMPTY) {
            // builder.luck(context.getLuck());

            if (context.getLootedEntity() != null) {
                Entity nmsLootedEntity = ((CraftEntity) context.getLootedEntity()).getHandle();
                setMaybe(builder, LootContextParameters.THIS_ENTITY, nmsLootedEntity);
                setMaybe(builder, LootContextParameters.DAMAGE_SOURCE, handle.getDamageSources().generic());
                setMaybe(builder, LootContextParameters.ORIGIN, nmsLootedEntity.getPos());
            }

            if (context.getKiller() != null) {
                net.minecraft.entity.player.PlayerEntity nmsKiller = ((CraftHumanEntity) context.getKiller()).getHandle();
                setMaybe(builder, LootContextParameters.KILLER_ENTITY, nmsKiller);
                // If there is a player killer, damage source should reflect that in case loot tables use that information
                setMaybe(builder, LootContextParameters.DAMAGE_SOURCE, handle.getDamageSources().playerAttack(nmsKiller));
                setMaybe(builder, LootContextParameters.LAST_DAMAGE_PLAYER, nmsKiller); // SPIGOT-5603 - Set minecraft:killed_by_player
                setMaybe(builder, LootContextParameters.TOOL, nmsKiller.getActiveItem()); // SPIGOT-6925 - Set minecraft:match_tool
            }

            // SPIGOT-5603 - Use LootContext#lootingModifier
            if (context.getLootingModifier() != LootContext.DEFAULT_LOOT_MODIFIER) {
                setMaybe(builder, LOOTING_MOD, context.getLootingModifier());
            }
        }

        // SPIGOT-5603 - Avoid IllegalArgumentException in LootContext#build()
        LootContextType.Builder nmsBuilder = new LootContextType.Builder();
        for (LootContextParameter<?> param : getHandle().getType().getRequired()) {
            nmsBuilder.require(param);
        }
        for (LootContextParameter<?> param : getHandle().getType().getAllowed()) {
            if (!getHandle().getType().getRequired().contains(param)) {
                nmsBuilder.allow(param);
            }
        }
        nmsBuilder.allow(LOOTING_MOD);

        return builder.build(getHandle().getType());
    }

    private <T> void setMaybe(net.minecraft.loot.context.LootContextParameterSet.Builder builder, LootContextParameter<T> param, T value) {
        if (getHandle().getType().getRequired().contains(param) || getHandle().getType().getAllowed().contains(param)) {
            //builder.withParameter(param, value); Banner - TODO
        }
    }

    public static LootContext convertContext(net.minecraft.loot.context.LootContext info) {
        Vec3d position = info.get(LootContextParameters.ORIGIN);
        if (position == null) {
            position = info.get(LootContextParameters.THIS_ENTITY).getPos(); // Every vanilla context has origin or this_entity, see LootContextParameterSets
        }
        
        Location location = new Location(((IMixinWorld)info.getWorld()).getWorldImpl(), position.getX(), position.getY(), position.getZ());;
        LootContext.Builder contextBuilder = new LootContext.Builder(location);

        if (info.hasParameter(LootContextParameters.KILLER_ENTITY)) {
            CraftEntity killer = ((IMixinEntity) info.get(LootContextParameters.KILLER_ENTITY)).getBukkitEntity();
            if (killer instanceof CraftHumanEntity) {
                contextBuilder.killer((CraftHumanEntity) killer);
            }
        }

        if (info.hasParameter(LootContextParameters.THIS_ENTITY)) {
            contextBuilder.lootedEntity(((IMixinEntity) info.get(LootContextParameters.THIS_ENTITY)).getBukkitEntity());
        }

        if (info.hasParameter(LOOTING_MOD)) {
            contextBuilder.lootingModifier(info.get(LOOTING_MOD));
        }

        contextBuilder.luck(info.getLuck());
        return contextBuilder.build();
    }

    @Override
    public String toString() {
        return getKey().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof org.bukkit.loot.LootTable)) {
            return false;
        }

        org.bukkit.loot.LootTable table = (org.bukkit.loot.LootTable) obj;
        return table.getKey().equals(this.getKey());
    }
}
