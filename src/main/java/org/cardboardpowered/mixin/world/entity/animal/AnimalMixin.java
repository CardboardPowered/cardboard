/**
 * The Bukkit for Fabric Project
 * Copyright (C) 2020 Javazilla Software and contributors
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.world.entity.animal;

import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityEnterLoveModeEvent;
import org.cardboardpowered.bridge.world.entity.animal.AnimalBridge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.bukkit.craftbukkit.event.CraftEventFactory;

@Mixin(Animal.class)
public class AnimalMixin implements AnimalBridge {

    @Shadow
    public int inLove;

    /** CraftBukkit's {@code Animal#breedItem}: the food last used to enter love mode. */
    @Unique
    private ItemStack cardboard$breedItem;

    /** Experience agreed on by {@link EntityBreedEvent}; -1 when no event is in flight. */
    @Unique
    private int cardboard$breedExperience = -1;

    @Override
    public ItemStack cardboard$getBreedItem() {
        return this.cardboard$breedItem;
    }

    @Override
    public void cardboard$setBreedItem(ItemStack stack) {
        this.cardboard$breedItem = stack;
    }

    @Inject(at = @At("HEAD"), method = "setInLove", cancellable = true)
    public void callEnterLoveModeEvent(Player entityhuman, CallbackInfo ci) {
        EntityEnterLoveModeEvent entityEnterLoveModeEvent = CraftEventFactory.callEntityEnterLoveModeEvent(entityhuman, (Animal)(Object)this, 600);
        if (entityEnterLoveModeEvent.isCancelled())
            ci.cancel();
        this.inLove = entityEnterLoveModeEvent.getTicksInLove();
    }

    /**
     * Remember the food that was consumed to start breeding, so it can be reported as
     * {@link EntityBreedEvent#getBredWith()} once the child is actually created.
     */
    @Inject(method = "mobInteract(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;)Lnet/minecraft/world/InteractionResult;",
            at = @At(value = "INVOKE", ordinal = 0,
                     target = "Lnet/minecraft/world/entity/animal/Animal;usePlayerItem(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/InteractionHand;Lnet/minecraft/world/item/ItemStack;)V"))
    private void cardboard$rememberBreedItem(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir, @Local ItemStack food) {
        this.cardboard$breedItem = food.copy();
    }

    /**
     * Fire {@link EntityBreedEvent} at the moment the child exists but before any state is
     * committed: {@code finalizeSpawnChildFromBreeding} resets both parents' love/age and
     * {@code addFreshEntityWithPassengers} adds the child to the world, so cancelling here
     * leaves the world exactly as it was.
     */
    @Inject(method = "spawnChildFromBreeding",
            at = @At(value = "INVOKE",
                     target = "Lnet/minecraft/world/entity/animal/Animal;finalizeSpawnChildFromBreeding(Lnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/animal/Animal;Lnet/minecraft/world/entity/AgeableMob;)V"),
            cancellable = true)
    private void cardboard$callEntityBreedEvent(ServerLevel level, Animal partner, CallbackInfo ci, @Local AgeableMob child) {
        Animal mother = (Animal)(Object)this;

        ServerPlayer breeder = mother.getLoveCause();
        if (breeder == null) breeder = partner.getLoveCause();

        ItemStack bredWith = this.cardboard$breedItem;
        if (bredWith == null) bredWith = ((AnimalBridge) partner).cardboard$getBreedItem();

        int experience = mother.getRandom().nextInt(7) + 1;

        EntityBreedEvent event = CraftEventFactory.callEntityBreedEvent(child, mother, partner, breeder, bredWith, experience);

        this.cardboard$breedItem = null;
        ((AnimalBridge) partner).cardboard$setBreedItem(null);

        if (event.isCancelled()) {
            this.cardboard$breedExperience = -1;
            ci.cancel();
            return;
        }

        this.cardboard$breedExperience = event.getExperience();
    }

    @Inject(method = "spawnChildFromBreeding", at = @At("RETURN"))
    private void cardboard$clearBreedExperience(ServerLevel level, Animal partner, CallbackInfo ci) {
        this.cardboard$breedExperience = -1;
    }

    /**
     * Vanilla drops {@code random.nextInt(7) + 1} experience; honour whatever the event agreed on,
     * and drop nothing at all when a plugin set the experience to 0.
     */
    @WrapOperation(method = "finalizeSpawnChildFromBreeding",
                   at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean cardboard$applyBreedExperience(ServerLevel level, Entity orb, Operation<Boolean> original) {
        int experience = this.cardboard$breedExperience;
        this.cardboard$breedExperience = -1;

        if (experience < 0) return original.call(level, orb); // no event fired (Frog / Sniffer path)
        if (experience == 0) return false;

        return original.call(level, new ExperienceOrb(level, orb.getX(), orb.getY(), orb.getZ(), experience));
    }

}
