package com.fungus_soft.bukkitfabric.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;

import com.fungus_soft.bukkitfabric.BukkitLogger;
import com.mojang.brigadier.tree.CommandNode;

import net.minecraft.server.command.ServerCommandSource;

@Mixin(CommandNode.class)
public class MixinCommandNode {

	@Overwrite
    public boolean canUse(final ServerCommandSource source) {
		CommandNode<ServerCommandSource> node = (CommandNode)(Object)this;

        // CraftBukkit start
		BukkitLogger.getLogger().info("canUse: " + source.getClass().getName());
        // CraftBukkit end
        return node.getRequirement().test(source);
    }

}