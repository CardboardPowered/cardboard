package org.cardboardpowered.impl.command;

import com.google.common.collect.ImmutableList;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.server.world.ServerWorld;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

/**
 * Provides a /fabricmods command
 */
public class ModsCommand extends Command {

    public ModsCommand(String name) {
        super(name);

        this.description = "Gets the version of this server including any plugins in use";
        this.usageMessage = "/fabricmods";
        this.setPermission("cardboard.command.mods");
        this.setAliases(Arrays.asList("fabricmods"));
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (sender.hasPermission("cardboard.command.mods")) {
            String mods = "";
            for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
                String name = mod.getMetadata().getName();

                if (name.startsWith("Fabric") && name.endsWith(")")) continue; // Don't list all modules of FAPI
                if (name.startsWith("Fabric API Base")) name = "Fabric API";
                if (name.startsWith("OpenJDK")) name = name.replace(" 64-Bit Server VM",""); // Shorten
                if (name.startsWith("Minecraft")) continue;

                if (!mods.contains(name)) {
                	mods += ", " + ChatColor.GREEN + name + ChatColor.WHITE;
                }
            }
            sender.sendMessage("Mods: " + mods.substring(2));
        } else {
            sender.sendMessage("No Permission for command!");
        }
        
        Method[] mm = ServerWorld.class.getMethods();
        for (Method m : mm) {
        	 sender.sendMessage("M: " + m.getName());
        }
        
        Method[] mm1 = ServerWorld.class.getDeclaredMethods();
        for (Method m : mm1) {
        	 sender.sendMessage("DM: " + m.getName());
        }
        
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return ImmutableList.of();
    }

}