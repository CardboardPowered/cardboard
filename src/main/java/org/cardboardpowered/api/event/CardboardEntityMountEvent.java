package org.cardboardpowered.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.Entity;
import net.minecraft.util.ActionResult;

public interface CardboardEntityMountEvent {

    Event<CardboardEntityMountEvent> EVENT = EventFactory.createArrayBacked(CardboardEntityMountEvent.class,
            (listeners) -> (vehicle, entity) -> {
                for (CardboardEntityMountEvent listener : listeners) {
                    ActionResult result = listener.interact(vehicle, entity);

                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult interact(Entity vehicle, Entity entity);

}
