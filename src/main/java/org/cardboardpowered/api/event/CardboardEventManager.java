package org.cardboardpowered.api.event;

import com.javazilla.bukkitfabric.impl.BukkitEventFactory;
import net.minecraft.util.ActionResult;

public class CardboardEventManager {

    public static CardboardEventManager INSTANCE = new CardboardEventManager();

    public void callCardboardEvents() {
        this.callCardboardFireworkExplodeEvent();
    }

    private void callCardboardFireworkExplodeEvent() {
        CardboardFireworkExplodeEvent.EVENT.register((firework) -> {
            if (BukkitEventFactory.callFireworkExplodeEvent(firework).isCancelled()) {
                return ActionResult.FAIL;
            }else {
                return ActionResult.PASS;
            }
        });
    }
}
