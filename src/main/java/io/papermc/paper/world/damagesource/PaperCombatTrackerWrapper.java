/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.common.collect.BiMap
 *  com.google.common.collect.HashBiMap
 *  io.papermc.paper.world.damagesource.CombatEntry
 *  io.papermc.paper.world.damagesource.CombatTracker
 *  io.papermc.paper.world.damagesource.FallLocationType
 *  net.kyori.adventure.text.Component
 *  org.bukkit.entity.LivingEntity
 *  org.jspecify.annotations.NullMarked
 *  org.jspecify.annotations.Nullable
 */
package io.papermc.paper.world.damagesource;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import io.papermc.paper.adventure.PaperAdventure;

import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.minecraft.Optionull;
import net.minecraft.util.Util;
import net.minecraft.world.damagesource.FallLocation;
import org.bukkit.entity.LivingEntity;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record PaperCombatTrackerWrapper(net.minecraft.world.damagesource.CombatTracker handle) implements CombatTracker {

    private static final BiMap<FallLocation, FallLocationType> FALL_LOCATION_MAPPING = Util.make(() -> {
        HashBiMap<FallLocation, FallLocationType> map = HashBiMap.create(8);
        map.put(FallLocation.GENERIC, FallLocationType.GENERIC);
        map.put(FallLocation.LADDER, FallLocationType.LADDER);
        map.put(FallLocation.VINES, FallLocationType.VINES);
        map.put(FallLocation.WEEPING_VINES, FallLocationType.WEEPING_VINES);
        map.put(FallLocation.TWISTING_VINES, FallLocationType.TWISTING_VINES);
        map.put(FallLocation.SCAFFOLDING, FallLocationType.SCAFFOLDING);
        map.put(FallLocation.OTHER_CLIMBABLE, FallLocationType.OTHER_CLIMBABLE);
        map.put(FallLocation.WATER, FallLocationType.WATER);
        return map;
    });

    public LivingEntity getEntity() {
        return (LivingEntity) ( (EntityBridge) this.handle.mob) .getBukkitEntity();
    }

    public List<CombatEntry> getCombatEntries() {
        ArrayList<CombatEntry> combatEntries = new ArrayList<CombatEntry>(this.handle.entries.size());
        this.handle.entries.forEach(combatEntry -> combatEntries.add(new PaperCombatEntryWrapper((net.minecraft.world.damagesource.CombatEntry)combatEntry)));
        return combatEntries;
    }

    public void setCombatEntries(List<CombatEntry> combatEntries) {
        this.handle.entries.clear();
        combatEntries.forEach(combatEntry -> this.handle.entries.add(((PaperCombatEntryWrapper)combatEntry).handle()));
    }

    public @Nullable CombatEntry computeMostSignificantFall() {
        net.minecraft.world.damagesource.CombatEntry combatEntry = this.handle.getMostSignificantFall();
        return combatEntry == null ? null : new PaperCombatEntryWrapper(combatEntry);
    }

    public boolean isInCombat() {
        return this.handle.inCombat;
    }

    public boolean isTakingDamage() {
        return this.handle.takingDamage;
    }

    public int getCombatDuration() {
        return this.handle.getCombatDuration();
    }

    public void addCombatEntry(CombatEntry combatEntry) {
        net.minecraft.world.damagesource.CombatEntry entry = ((PaperCombatEntryWrapper)combatEntry).handle();
        // TODO
        // this.handle.recordDamageAndCheckCombatState(entry);
    }

    public Component getDeathMessage() {
        return PaperAdventure.asAdventure(this.handle.getDeathMessage());
    }

    public void resetCombatState() {
        // TODO
    	// this.handle.resetCombatState();
    }

    public FallLocationType calculateFallLocationType() {
        FallLocation fallLocation = FallLocation.getCurrentFallLocation(this.handle().mob);
        return Optionull.map(fallLocation, PaperCombatTrackerWrapper::minecraftToPaper);
    }

    public static FallLocation paperToMinecraft(FallLocationType fallLocationType) {
        FallLocation fallLocation = (FallLocation)FALL_LOCATION_MAPPING.inverse().get((Object)fallLocationType);
        if (fallLocation == null) {
            throw new IllegalArgumentException("Unknown fall location type: " + fallLocationType.id());
        }
        return fallLocation;
    }

    public static FallLocationType minecraftToPaper(FallLocation fallLocation) {
        FallLocationType fallLocationType = (FallLocationType)FALL_LOCATION_MAPPING.get((Object)fallLocation);
        if (fallLocationType == null) {
            throw new IllegalArgumentException("Unknown fall location: " + fallLocation.id());
        }
        return fallLocationType;
    }


    // 26.2: new on CombatTracker
    @Override
    public int getLastDamageTime() {
        return this.handle.lastDamageTime;
    }

}