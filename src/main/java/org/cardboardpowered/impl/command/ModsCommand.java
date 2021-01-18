package org.cardboardpowered.impl.command;

import com.google.common.collect.ImmutableList;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

import java.util.Arrays;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

public class ModsCommand extends Command {

    public static String BRANCH = "master";

    public ModsCommand(String name) {
        super(name);

        this.description = "Gets the version of this server including any plugins in use";
        this.usageMessage = "/fabricmods";
        this.setPermission("cardboard.command.mods");
        this.setAliases(Arrays.asList("fabricmods"));
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (args.length == 0) {
            String mods = "";
            for (ModContainer mod : FabricLoader.getInstance().getAllMods()) {
                mods += ", " + mod.getMetadata().getName();
            }
            sender.sendMessage("Mods: " + mods.substring(2));
        }
        return true;
    }

    @Override
    public List<String> tabComplete(CommandSender sender, String alias, String[] args) {
        return ImmutableList.of();
    }

}