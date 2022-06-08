package org.cardboardpowered.mixin.entity;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.bukkit.Bukkit;
import org.bukkit.event.entity.EntityTransformEvent;
import org.bukkit.event.entity.SlimeSplitEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import org.cardboardpowered.interfaces.ISlimeEntity;


import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.world.World;

@Mixin(SlimeEntity.class)
public class MixinSlimeEntity extends MixinEntity implements ISlimeEntity {

    @Shadow public int getSize() {return 0;}
    @Shadow public void setSize(int i, boolean flag) {}

    @Override
    public void setSizeBF(int i, boolean flag) {
        setSize(i, flag);
    }

    private boolean cancelRemove_B;
    private List<net.minecraft.entity.LivingEntity> slimes_B = new ArrayList<>();

    private final Random randoms = new Random();

    @Redirect(at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"), method = "remove")
    public int doBukkitEvent_SlimeSplitEvent(Random r, int a) {
        slimes_B.clear();
        int k = 2 + this.randoms.nextInt(3);

        SlimeSplitEvent event = new SlimeSplitEvent((org.bukkit.entity.Slime) this.getBukkitEntity(), k);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled() && event.getCount() > 0) {
            return event.getCount() - 2;
        } else cancelRemove_B = true;
        return k - 2;
    }

    @Inject(at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"), method = "remove", cancellable = true)
    public void doBukkitEvent_SlimeSplitEvent_2(CallbackInfo ci) {
        if (cancelRemove_B) {
            super.removeBF();
            ci.cancel();
            return;
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), method = "remove")
    public boolean doBukkitEvent_RedirectSpawnEntity(World w, Entity e) {
        this.slimes_B.add((SlimeEntity)e);
        return false;
    }

    /**
     * @reason EntityTransformEvent
     */
    @Inject(at = @At(value = "TAIL"), method = "remove", cancellable = true)
    public void doBukkitEvent_RedirectSpawnEntity_2(CallbackInfo ci) {
        EntityTransformEvent ev = BukkitEventFactory.callEntityTransformEvent((SlimeEntity)(Object)this, slimes_B, EntityTransformEvent.TransformReason.SPLIT);
        if (ev != null && ev.isCancelled()) {
            ci.cancel();
            return;
        }
        for (net.minecraft.entity.LivingEntity living : slimes_B)
            this.world.spawnEntity(living); // TODO SpawnReason.SLIME_SPLIT
    }

}