package com.fungus_soft.bukkitfabric.mixin;

import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;

import com.fungus_soft.bukkitfabric.interfaces.IMixinCommandOutput;

@Mixin(CommandOutput.class)
public interface MixinCommandOutput extends IMixinCommandOutput {

    @Override
    public CommandSender getBukkitSender(ServerCommandSource source);

}