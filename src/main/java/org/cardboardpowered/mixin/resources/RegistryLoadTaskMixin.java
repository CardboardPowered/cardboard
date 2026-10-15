package org.cardboardpowered.mixin.resources;

import java.util.Map;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.serialization.Lifecycle;

import io.papermc.paper.registry.PaperRegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.resources.NetworkRegistryLoadTask;
import net.minecraft.resources.RegistryDataLoader;
import net.minecraft.resources.RegistryLoadTask;

@Mixin(RegistryLoadTask.class)
public class RegistryLoadTaskMixin {

	@Shadow
	public WritableRegistry registry;
	
	@Inject(
	        method = "<init>(Lnet/minecraft/resources/RegistryDataLoader$RegistryData;Lcom/mojang/serialization/Lifecycle;Ljava/util/Map;)V",
	        at = @At("TAIL")
	    )
	    private void onConstructorTail(RegistryDataLoader.RegistryData<?> data, Lifecycle lifecycle, Map<?, ?> loadingErrors, CallbackInfo ci) {
			// The client's copy of registries received from a server; Bukkit only wraps the server's own.
			if ((Object) this instanceof NetworkRegistryLoadTask) return;
			PaperRegistryAccess.instance().registerRegistry(this.registry);
	    }
	
}
