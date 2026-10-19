package org.cardboardpowered.mixin.world.entity.animal.equine;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.cardboardpowered.bridge.world.entity.animal.equine.AbstractHorseBridge;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.world.SimpleContainer;
import org.cardboardpowered.bridge.world.entity.EntityBridge;
import org.cardboardpowered.bridge.world.ContainerBridge;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(net.minecraft.world.entity.animal.equine.AbstractHorse.class)
public abstract class AbstractHorseMixin extends Animal implements AbstractHorseBridge, EntityBridge {

    @Unique
    public int maxDomestication = 100; // CraftBukkit - store max domestication value
    @Shadow
    protected SimpleContainer inventory;

    @Shadow
    protected abstract void setFlag(int i, boolean bl);

    @Shadow
    protected abstract boolean getFlag(int i);

    @Shadow
    @Final
    private static int FLAG_OPEN_MOUTH;

    protected AbstractHorseMixin(EntityType<? extends Animal> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public boolean cardboard$isCollidable(boolean ignoreClimbing) { // Paper - Climbing should not bypass cramming gamerule
        return !this.isVehicle();
    }


    @Inject(method = "getMaxTemper", at = @At("HEAD"), cancellable = true)
    public void getMaxTemperCraftBukkit(CallbackInfoReturnable<Integer> cir) {
        cir.setReturnValue(this.maxDomestication); // CraftBukkit - return stored max domestication instead
    }

    // Paper start - Horse API
    @Override
    public void cardboard$setMouthOpen(boolean open) {
        this.setFlag(FLAG_OPEN_MOUTH, open);
    }

    @Override
    public boolean cardboard$isMouthOpen() {
        return this.getFlag(FLAG_OPEN_MOUTH);
    }
    // Paper end - Horse API

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    public void addAdditionalSaveDataPaper(ValueOutput valueOutput, CallbackInfo ci) {
        valueOutput.putInt("Bukkit.MaxDomestication", this.maxDomestication); // Paper - max domestication
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    public void readAdditionalSaveDataPaper(ValueInput valueInput, CallbackInfo ci) {
        this.maxDomestication = valueInput.getIntOr("Bukkit.MaxDomestication", (net.minecraft.world.entity.animal.equine.AbstractHorse)(Object)this instanceof Llama ? 30 : 100); // Paper - max domestication
    }

    @Inject(method = "handleStartJump", at = @At("HEAD"), cancellable = true)
    public void callJumpEvent(int jumpPower, CallbackInfo ci) {
        // CraftBukkit start
        float power;
        if (jumpPower >= 90) {
            power = 1.0F;
        } else {
            power = 0.4F + 0.4F * (float) jumpPower / 90.0F;
        }
        if (!org.bukkit.craftbukkit.event.CraftEventFactory.callHorseJumpEvent((net.minecraft.world.entity.animal.equine.AbstractHorse)(Object)this, power)) {
            ci.cancel();
            return;
        }
        // CraftBukkit end
    }
    
    @Inject(method = "createInventory", at = @At("TAIL"))
    public void cardboard$setInvOwner(CallbackInfo ci) {
        // Client entities have no Bukkit side; on a remote server CraftServer.console is null
        if (((net.minecraft.world.entity.Entity) (Object) this).level().isClientSide()) return;
        ((ContainerBridge)inventory).cardboard$setOwner((org.bukkit.entity.AbstractHorse)this.getBukkitEntity());
    }

    @Override
    public int cardboard$getMaxDomestication() {
        return maxDomestication;
    }

    @Override
    public void cardboard$setMaxDomestication(int maxDomestication) {
        this.maxDomestication = maxDomestication;
    }
}