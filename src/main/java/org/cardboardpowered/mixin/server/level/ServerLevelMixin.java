package org.cardboardpowered.mixin.server.level;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executor;

import net.minecraft.world.level.storage.LevelData;
import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.CardboardConfig;
import org.cardboardpowered.bridge.world.level.storage.LevelData_RespawnDataBridge;
import org.cardboardpowered.bridge.world.level.storage.PrimaryLevelDataBridge;
import org.cardboardpowered.impl.world.CraftWorld;
import org.cardboardpowered.bridge.server.level.ServerLevelBridge;
import org.cardboardpowered.mixin.world.level.LevelMixin;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.cardboardpowered.bridge.world.level.saveddata.maps.MapItemSavedDataBridge;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import org.cardboardpowered.CardboardMod;
import com.javazilla.bukkitfabric.Utils;
import org.cardboardpowered.bridge.world.level.LevelBridge;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.progress.LevelLoadListener;
import net.minecraft.server.level.progress.LoggingLevelLoadListener;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.RandomSequences;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.CustomSpawner;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.entity.LevelEntityGetter;
import net.minecraft.world.level.entity.PersistentEntitySectionManager;
import net.minecraft.world.level.storage.LevelStorageSource;
import net.minecraft.world.level.storage.ServerLevelData;

@Mixin(ServerLevel.class)
public class ServerLevelMixin extends LevelMixin implements ServerLevelBridge {

   // @Shadow
   // public boolean inEntityTick;

    /*@Inject(at = @At("TAIL"), method = "<init>")
    public void addToBukkit( ... ,  CallbackInfo ci){
        // ((CraftServer)Bukkit.getServer()).addWorldToMap(getCraftWorld());
    }*/

	private LevelStorageSource.LevelStorageAccess cardboard$session;
	private UUID cardboard$uuid;
	private LevelLoadListener cardboard$levelLoadListener;

	@Inject(method = "<init>", at = @At(value = "RETURN"))
    private void cardboard$initWorldServer(
    		MinecraftServer minecraftserver,
    		Executor executor,
    		LevelStorageSource.LevelStorageAccess convertable_conversionsession,
    		ServerLevelData iworlddataserver, ResourceKey<Level> resourcekey,
    		LevelStem worlddimension, // WorldGenerationProgressListener worldloadlistener,
    		boolean flag, long i2, List<CustomSpawner> list, boolean flag1,
    		CallbackInfo ci
    	) {
		
		if (CardboardConfig.DEBUG_OTHER) {
			CardboardMod.LOGGER.info("Debug: getting world uuid");
		}

        this.cardboard$session = convertable_conversionsession;
        this.cardboard$uuid = Utils.getWorldUUID(cardboard$session.getDimensionPath(((ServerLevel)(Object)this).dimension()).toFile());
        
        // TODO: add ServerWorld argument to LoggingChunkLoadProgress constructor
        this.cardboard$levelLoadListener = new LoggingLevelLoadListener(false);
    }

    /**
     * Vanilla stores the map id in the saved-data key, not on the data object, but the
     * Bukkit MapView API exposes it. Stamp it on wherever map data enters or leaves the world.
     */
    @Inject(at = @At("RETURN"), method = "getMapData")
    public void cardboard$stampMapId(MapId id, CallbackInfoReturnable<MapItemSavedData> cir) {
        MapItemSavedData data = cir.getReturnValue();
        if (data != null) {
            ((MapItemSavedDataBridge) data).setMapIdBF(id.id());
        }
    }

    @Inject(at = @At("HEAD"), method = "setMapData")
    public void cardboard$stampMapIdOnStore(MapId id, MapItemSavedData data, CallbackInfo ci) {
        ((MapItemSavedDataBridge) data).setMapIdBF(id.id());
    }

    @Inject(at = @At("HEAD"), method = "save")
    public void doWorldSaveEvent(ProgressListener aa, boolean bb, boolean cc, CallbackInfo ci) {
        if (!cc) {
            org.bukkit.Bukkit.getPluginManager().callEvent(new org.bukkit.event.world.WorldSaveEvent(this.cardboard$getWorld())); // WorldSaveEvent
        }
    }
    
    @Shadow 
    public ServerLevelData serverLevelData;

    @Override
    public ServerLevelData cardboard_worldProperties() {
        return serverLevelData;
    }

	@Override
	public CraftServer getCraftServer() {
		// TODO Auto-generated method stub
		return CraftServer.INSTANCE;
	}
	
	@Shadow
	public PersistentEntitySectionManager<Entity> entityManager;

	@Shadow
	public LevelEntityGetter<Entity> getEntities() {
		return this.entityManager.getEntityGetter();
	}

	@Shadow
	@Final
	private MinecraftServer server;

	@Override
	public void cardboard$set_uuid(UUID id) {
		this.cardboard$uuid = id;
	}

	@Override
	public UUID cardboard$get_uuid() {
		return this.cardboard$uuid;
	}
	
	@Override
	public LevelLoadListener cardboard$levelLoadListener() {
		return cardboard$levelLoadListener;
	}

    /**
     * @reason MapInitalizeEvent
     * @author BukkitFabricMod
     */
    //@Overwrite
   // public MapState getMapState(String s) {
        // TODO 1.17ify
       // return null; return (MapState) CraftServer.INSTANCE.getServer().getOverworld().getPersistentStateManager().get(() -> {
           /*MapState newMap = new MapState(s);
            MapInitializeEvent event = new MapInitializeEvent(((IMixinMapState)newMap).getMapViewBF());
            Bukkit.getServer().getPluginManager().callEvent(event);
            return newMap;
        }, s);*/
  //  }

    // TODO 1.17ify
   /* @Inject(at = @At("TAIL"), method = "unloadEntity")
    public void unvalidateEntityBF(Entity entity, CallbackInfo ci) {
        ((IMixinEntity)entity).setValid(false);
    } 

    @Inject(at = @At("TAIL"), method = "loadEntityUnchecked")
    public void validateEntityBF(Entity entity, CallbackInfo ci) {
        //if (!this.inEntityTick) {
            IMixinEntity bf = (IMixinEntity) entity;
            bf.setValid(true);
            if (null == bf.getOriginBF() && null != bf.getBukkitEntity()) {
                // Paper's Entity Origin API
                bf.setOriginBF(bf.getBukkitEntity().getLocation());
            }
       // }
    }*/

	@Inject(method = "setRespawnData", at = @At("HEAD"), cancellable = true)
	public void setRespawnDataPaper(LevelData.RespawnData respawnData, CallbackInfo ci) {
		// Paper start
		if (!((LevelData_RespawnDataBridge)(Object)this.serverLevelData.getRespawnData()).cardboard$positionEquals(respawnData)) {
			org.bukkit.Location previousLocation = this.cardboard$getWorld().getSpawnLocation();
			this.serverLevelData.setSpawn(respawnData);
			this.server.getPlayerList().broadcastAll(new net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket(respawnData), this.dimension());
			this.server.updateEffectiveRespawnData();
			new org.bukkit.event.world.SpawnChangeEvent(this.cardboard$getWorld(), previousLocation).callEvent();
		}
		if (((PrimaryLevelDataBridge)this.server.overworld().serverLevelData).cardboard$getRespawnDimension() != this.dimension()) {
			((PrimaryLevelDataBridge)this.server.overworld().serverLevelData).cardboard$setRespawnDimension(this.dimension());
			this.server.updateEffectiveRespawnData();
		}
		ci.cancel();
		// Paper end
	}
}