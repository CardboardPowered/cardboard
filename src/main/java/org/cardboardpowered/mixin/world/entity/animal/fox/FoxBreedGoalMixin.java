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
package org.cardboardpowered.mixin.world.entity.animal.fox;

import org.bukkit.craftbukkit.event.CraftEventFactory;
import org.bukkit.event.entity.EntityBreedEvent;
import org.cardboardpowered.bridge.world.entity.animal.AnimalBridge;
import org.cardboardpowered.mixin.world.entity.ai.goal.BreedGoalAccessor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.item.ItemStack;

/**
 * Fox breeding does not go through {@code Animal#spawnChildFromBreeding}: {@code FoxBreedGoal}
 * overrides {@code BreedGoal#breed()} and creates the kit itself so it can copy the parents'
 * trusted players. It therefore needs its own {@link EntityBreedEvent} call.
 */
@Mixin(targets = "net.minecraft.world.entity.animal.fox.Fox$FoxBreedGoal")
public class FoxBreedGoalMixin {

    @Unique
    private int cardboard$breedExperience = -1;

    @Inject(method = "breed",
            at = @At(value = "INVOKE", ordinal = 0, target = "Lnet/minecraft/world/entity/animal/Animal;setAge(I)V"),
            cancellable = true)
    private void cardboard$callEntityBreedEvent(CallbackInfo ci, @Local Fox offspring) {
        Animal mother = ((BreedGoalAccessor)(Object)this).cardboard$getAnimal();
        Animal father = ((BreedGoalAccessor)(Object)this).cardboard$getPartner();

        ServerPlayer breeder = mother.getLoveCause();
        if (breeder == null) breeder = father.getLoveCause();

        ItemStack bredWith = ((AnimalBridge) mother).cardboard$getBreedItem();
        if (bredWith == null) bredWith = ((AnimalBridge) father).cardboard$getBreedItem();

        int experience = mother.getRandom().nextInt(7) + 1;

        EntityBreedEvent event = CraftEventFactory.callEntityBreedEvent(offspring, mother, father, breeder, bredWith, experience);

        ((AnimalBridge) mother).cardboard$setBreedItem(null);
        ((AnimalBridge) father).cardboard$setBreedItem(null);

        if (event.isCancelled()) {
            this.cardboard$breedExperience = -1;
            ci.cancel();
            return;
        }

        this.cardboard$breedExperience = event.getExperience();
    }

    @WrapOperation(method = "breed",
                   at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"))
    private boolean cardboard$applyBreedExperience(ServerLevel level, Entity orb, Operation<Boolean> original) {
        int experience = this.cardboard$breedExperience;
        this.cardboard$breedExperience = -1;

        if (experience < 0) return original.call(level, orb);
        if (experience == 0) return false;

        return original.call(level, new ExperienceOrb(level, orb.getX(), orb.getY(), orb.getZ(), experience));
    }

}
