package org.cardboardpowered.mixin;

import org.bukkit.Material;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = Material.class, remap = false)
public class MixinMaterial {

	/**
	 * @reason We need API update
	 * @see https://hub.spigotmc.org/stash/projects/SPIGOT/repos/bukkit/diff/src/main/java/org/bukkit/Material.java?until=ad2fd61c8784c7bac6542e39fca7e506c7966865
	 */
	@Inject(at = @At("HEAD"), method = "isBlock", cancellable = true, remap = false)
	public void fix_material_block(CallbackInfoReturnable<Boolean> ci) {
		if ( ((Material)(Object)this) == Material.GRASS ) {
			ci.setReturnValue(false);
		}
	}
	
}
