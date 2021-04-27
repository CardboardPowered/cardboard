package org.cardboardpowered.mixin;

import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.server.command.ServerCommandSource;

import com.javazilla.bukkitfabric.interfaces.IMixinCommandOutput;
import com.javazilla.bukkitfabric.interfaces.IMixinServerCommandSource;

@Mixin(ServerCommandSource.class)
public class MixinServerCommandSource implements IMixinServerCommandSource {

    @Override
    public CommandSender getBukkitSender() {
        ServerCommandSource s = (ServerCommandSource) (Object) this;
        return ((IMixinCommandOutput)s.output).getBukkitSender(s);
    }

}