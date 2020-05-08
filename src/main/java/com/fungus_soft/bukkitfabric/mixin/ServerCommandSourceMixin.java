package com.fungus_soft.bukkitfabric.mixin;

import java.lang.reflect.Field;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.spongepowered.asm.mixin.Mixin;
import net.minecraft.server.command.CommandOutput;
import net.minecraft.server.command.ServerCommandSource;

import com.fungus_soft.bukkitfabric.interfaces.IMixinCommandOutput;
import com.fungus_soft.bukkitfabric.interfaces.IMixinServerCommandSource;

@Mixin(ServerCommandSource.class)
public class ServerCommandSourceMixin implements IMixinServerCommandSource {

    @Override
    public CommandSender getBukkitSender() {
        String name = "output";

        ServerCommandSource s = (ServerCommandSource) (Object) this;
        Field[] fields = s.getClass().getDeclaredFields();
        for (Field f : fields) {
            if (f.getType().getName().equalsIgnoreCase("net.minecraft.server.command.CommandOutput")) {
                name = f.getName();
                break;
            }
        }

        CommandOutput out = (CommandOutput) getField(name);
        return ((IMixinCommandOutput)out).getBukkitSender((ServerCommandSource) (Object) this);
    }

    private Object getField(String name) {
        try {
            ServerCommandSource s = (ServerCommandSource) (Object) this;
            Field f = s.getClass().getDeclaredField(name);
            f.setAccessible(true);
            return f.get(s);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}