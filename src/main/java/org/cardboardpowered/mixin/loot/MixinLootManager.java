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

import net.minecraft.loot.LootDataKey;
import net.minecraft.loot.LootDataType;
import net.minecraft.loot.LootManager;
import net.minecraft.loot.LootTable;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;

@Mixin(LootManager.class)
public class MixinLootManager implements IMixinLootManager {

   // @Shadow
   // public Map<Identifier, LootTable> tables;

	// LootManager.keyToValue
	
    @Shadow private Map<LootDataKey<?>, ?> keyToValue;

	
    public Map<?, Identifier> lootTableToKey = ImmutableMap.of(); // CraftBukkit

    @Override
    public Map<?, Identifier> getLootTableToKeyMapBF() {
        return lootTableToKey;
    }

    @Inject(at = @At("TAIL"), method = "validate")
    private void cardboard$buildRev(Map<LootDataType<?>, Map<Identifier, ?>> map, CallbackInfo ci) {
  //  public void fillLootTableToKeyMap(Map<Identifier, JsonElement> map, ResourceManager iresourcemanager, Profiler gameprofilerfiller, CallbackInfo ci) {
        ImmutableMap.Builder<Object, Identifier> lootTableToKeyBuilder = ImmutableMap.builder();
       // this.keyToValue.forEach((lootTable, key) -> lootTableToKeyBuilder.put(key, lootTable));
        this.keyToValue.forEach((key, lootTable) -> lootTableToKeyBuilder.put((Object) lootTable, key.id()));

        this.lootTableToKey = lootTableToKeyBuilder.build();
    }

}