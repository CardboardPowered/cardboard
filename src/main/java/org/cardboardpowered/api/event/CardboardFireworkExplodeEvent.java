package org.cardboardpowered.api.event;

import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.util.ActionResult;

public interface CardboardFireworkExplodeEvent {

    Event<CardboardFireworkExplodeEvent> EVENT = EventFactory.createArrayBacked(CardboardFireworkExplodeEvent.class,
            (listeners) -> (firework) -> {
                for (CardboardFireworkExplodeEvent listener : listeners) {
                    ActionResult result = listener.interact(firework);

                    if(result != ActionResult.PASS) {
                        return result;
                    }
                }

                return ActionResult.PASS;
            });

    ActionResult interact(FireworkRocketEntity firework);
}
