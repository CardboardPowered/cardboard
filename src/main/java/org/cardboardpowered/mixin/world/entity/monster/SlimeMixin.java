package org.cardboardpowered.mixin.world.entity.monster;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import net.minecraft.world.entity.monster.cubemob.AbstractCubeMob;
import org.cardboardpowered.mixin.world.entity.EntityMixin;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.cardboardpowered.bridge.world.entity.monster.SlimeBridge;

// 26.2: getSize/setSize moved from Slime to AbstractCubeMob (shared with MagmaCube)
@Mixin(AbstractCubeMob.class)
public class SlimeMixin extends EntityMixin implements SlimeBridge {

    @Shadow public int getSize() {return 0;}
    @Shadow public void setSize(int i, boolean flag) {}

    @Override
    public void setSizeBF(int i, boolean flag) {
        setSize(i, flag);
    }

    private boolean cancelRemove_B;
    private List<net.minecraft.world.entity.LivingEntity> slimes_B = new ArrayList<>();

    private final Random randoms = new Random();

    // TODO: 1.19
    /*@Redirect(at = @At(value = "INVOKE", target = "Ljava/util/Random;nextInt(I)I"), method = "remove")
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
    }*/
    
    // TODO: 1.21.4

    /*
    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), method = "remove")
    public boolean doBukkitEvent_RedirectSpawnEntity(World w, Entity e) {
        this.slimes_B.add((SlimeEntity)e);
        return false;
    }

    /**
     * @reason EntityTransformEvent
     *
    @Inject(at = @At(value = "TAIL"), method = "remove", cancellable = true)
    public void doBukkitEvent_RedirectSpawnEntity_2(CallbackInfo ci) {
        EntityTransformEvent ev = CraftEventFactory.callEntityTransformEvent((SlimeEntity)(Object)this, slimes_B, EntityTransformEvent.TransformReason.SPLIT);
        if (ev != null && ev.isCancelled()) {
            ci.cancel();
            return;
        }
        for (net.minecraft.entity.LivingEntity living : slimes_B)
            this.mc_world().spawnEntity(living); // TODO SpawnReason.SLIME_SPLIT
    }
    */

}