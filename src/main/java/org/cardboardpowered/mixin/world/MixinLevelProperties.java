package org.cardboardpowered.mixin.world;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;
import org.cardboardpowered.interfaces.ILevelProperties;
import org.cardboardpowered.util.MixinInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.world.level.LevelInfo;
import net.minecraft.world.level.LevelProperties;

@MixinInfo(events = {"ThunderChangeEvent","WeatherChangeEvent"})
@Mixin(LevelProperties.class)
public class MixinLevelProperties implements ILevelProperties {

    @Shadow
    private LevelInfo levelInfo;

    @Inject(at = @At("HEAD"), method = "setThundering")
    public void thunder(boolean flag, CallbackInfo info) {
        LevelProperties p = (LevelProperties)(Object) this;
        if (p.isThundering() == flag)
            return;
        World world = Bukkit.getWorld(p.getLevelName());
        if (world != null) {
            ThunderChangeEvent thunder = new ThunderChangeEvent(world, flag);
            Bukkit.getServer().getPluginManager().callEvent(thunder);
            if (thunder.isCancelled())
                return;
        }
    }

    @Inject(at = @At("HEAD"), method = "setRaining")
    public void rain(boolean flag, CallbackInfo info) {
        LevelProperties p = (LevelProperties)(Object) this;
        if (p.isRaining() == flag)
            return;
        World world = Bukkit.getWorld(p.getLevelName());
        if (world != null) {
            WeatherChangeEvent event = new WeatherChangeEvent(world, flag);
            Bukkit.getServer().getPluginManager().callEvent(event);
            if (event.isCancelled())
                return;
        }
    }

    @Override
    public void checkName(String name) {
    }

}