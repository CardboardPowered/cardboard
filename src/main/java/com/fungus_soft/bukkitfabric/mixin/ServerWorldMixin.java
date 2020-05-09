package com.fungus_soft.bukkitfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import com.fungus_soft.bukkitfabric.bukkitimpl.FakeWorld;
import com.fungus_soft.bukkitfabric.interfaces.IMixinBukkitGetter;
import net.minecraft.server.world.ServerWorld;

@Mixin(ServerWorld.class)
public class ServerWorldMixin implements IMixinBukkitGetter {

    private FakeWorld bukkit;

    public ServerWorldMixin() {
        this.bukkit = new FakeWorld((ServerWorld) (Object) this);
    }

    @Override
    public FakeWorld getBukkitObject() {
        if (null == bukkit)
            this.bukkit = new FakeWorld((ServerWorld) (Object) this);
        return bukkit;
    }

}
