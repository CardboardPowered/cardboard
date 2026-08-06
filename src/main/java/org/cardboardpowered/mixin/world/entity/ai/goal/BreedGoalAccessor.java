package org.cardboardpowered.mixin.world.entity.ai.goal;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.animal.Animal;

/**
 * {@code BreedGoal#animal} / {@code BreedGoal#partner} are protected; subclass mixins such as
 * {@code FoxBreedGoalMixin} cannot @Shadow inherited fields, so read them through an accessor.
 */
@Mixin(BreedGoal.class)
public interface BreedGoalAccessor {

    @Accessor("animal")
    Animal cardboard$getAnimal();

    @Accessor("partner")
    Animal cardboard$getPartner();

}
