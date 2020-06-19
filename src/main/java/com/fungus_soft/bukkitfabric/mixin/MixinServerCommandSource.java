package com.fungus_soft.bukkitfabric.mixin;

import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.server.command.ServerCommandSource;

import com.fungus_soft.bukkitfabric.interfaces.IMixinCommandOutput;
import com.fungus_soft.bukkitfabric.interfaces.IMixinServerCommandSource;

@Mixin(ServerCommandSource.class)
public class MixinServerCommandSource implements IMixinServerCommandSource {

    @Override
    public CommandSender getBukkitSender() {
        ServerCommandSource s = (ServerCommandSource) (Object) this;
        return ((IMixinCommandOutput)s.output).getBukkitSender((ServerCommandSource) (Object) this);
    }

}