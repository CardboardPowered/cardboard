package org.cardboardpowered.mixin.world;

import org.bukkit.craftbukkit.CraftServer;
import org.cardboardpowered.impl.world.WorldImpl;
import org.cardboardpowered.interfaces.IServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.interfaces.IMixinWorld;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ProgressListener;
import net.minecraft.world.level.ServerWorldProperties;

@Mixin(ServerWorld.class)
public class MixinServerWorld extends MixinWorld implements IServerWorld {

   // @Shadow
   // public boolean inEntityTick;

    /*@Inject(at = @At("TAIL"), method = "<init>")
    public void addToBukkit( ... ,  CallbackInfo ci){
        // ((CraftServer)Bukkit.getServer()).addWorldToMap(getWorldImpl());
    }*/

    @Inject(at = @At("HEAD"), method = "save")
    public void doWorldSaveEvent(ProgressListener aa, boolean bb, boolean cc, CallbackInfo ci) {
        if (!cc) {
            org.bukkit.Bukkit.getPluginManager().callEvent(new org.bukkit.event.world.WorldSaveEvent(getWorldImpl())); // WorldSaveEvent
        }
    }
    
    @Shadow 
    public ServerWorldProperties worldProperties;

    @Override
    public ServerWorldProperties cardboard_worldProperties() {
        return worldProperties;
    }
    
    @Override
    public WorldImpl getWorld() {
        return ((IMixinWorld)(Object)this).getWorldImpl();
    }

	@Override
	public CraftServer getCraftServer() {
		// TODO Auto-generated method stub
		return CraftServer.INSTANCE;
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

}