package com.fungus_soft.bukkitfabric.command;

import java.util.Collection;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;

import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;

public class ModListCommand extends BukkitCommand {

    public ModListCommand(String name) {
        super(name);
        this.description = "Gets a list of fabric mods running on the server";
        this.usageMessage = "/fabricmods";
        this.setPermission("bukkit.command.plugins");
    }

    @Override
    public boolean execute(CommandSender sender, String currentAlias, String[] args) {
        if (!testPermission(sender)) return true;

        String modList = null;
        if (args.length <= 0)
            modList = getModList(false);
        else if (args[0].equalsIgnoreCase("all"))
            modList = getModList(true);

        sender.sendMessage("Fabric Mods " + modList);
        return true;
    }

    private String getModList(boolean all) {
        StringBuilder modList = new StringBuilder();
        Collection<ModContainer> mods = FabricLoader.getInstance().getAllMods();
        int size = mods.size();

        String[] toHide = {"Fabric Networking Block Entity", "Fabric Containers", "fabric-dimensions", "Fabric Biomes", "Fabric Events Interaction",
                "Fabric Crash Report Info", "Fabric Rendering Data Attachment", "Fabric Resource Loader", "Fabric Content Registeries",
                "Fabric Content Registries", "Fabric Tag Extensions", "Fabric Commands", "Fabric Registry Sync", "Fabric Mining Levels",
                "Fabric Events Lifecycle",  "Fabric Loot Tables", "Fabric Item Groups", "fabric-particles", "Fabric Object Builders", "Fabric Networking"};
        int hidden = 0;

        for (ModContainer mod : mods) {
            String name = mod.getMetadata().getName();
            boolean hide = false;
            if (!all)
                for (String s : toHide)
                    if (name.startsWith(s))
                        hide = true;
            if (hide) {
                hidden++;
                continue;
            }

            if (modList.length() > 0) {
                modList.append(ChatColor.WHITE);
                modList.append(", ");
            }

            // TODO detect of mod is enabled
            modList.append(ChatColor.GREEN);
            modList.append(mod.getMetadata().getName());
        }
        if (!all) modList.append(", and " + hidden + " more.");

        return "(" + size + "): " + modList.toString();
    }

}