package org.cardboardpowered.mixin.loot;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import com.javazilla.bukkitfabric.interfaces.IMixinLootManager;

import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Mixin(LootManager.class)
public class MixinLootManager implements IMixinLootManager {

    @Shadow
    public Map<Identifier, LootTable> tables;

    public Map<LootTable, Identifier> lootTableToKey = ImmutableMap.of(); // CraftBukkit

    @Override
    public Map<LootTable, Identifier> getLootTableToKeyMapBF() {
        return lootTableToKey;
    }

    @Inject(at = @At("TAIL"), method = "apply")
    public void fillLootTableToKeyMap(Map<Identifier, JsonElement> map, ResourceManager iresourcemanager, Profiler gameprofilerfiller, CallbackInfo ci) {
        ImmutableMap.Builder<LootTable, Identifier> lootTableToKeyBuilder = ImmutableMap.builder();
        this.tables.forEach((lootTable, key) -> lootTableToKeyBuilder.put(key, lootTable));
        this.lootTableToKey = lootTableToKeyBuilder.build();
    }

}