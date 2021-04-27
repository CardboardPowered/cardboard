package org.cardboardpowered.mixin;

import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;

import com.javazilla.bukkitfabric.interfaces.IMixinCommandOutput;

@Mixin(CommandOutput.class)
public interface MixinCommandOutput extends IMixinCommandOutput {

    @Override
    public CommandSender getBukkitSender(ServerCommandSource source);

}