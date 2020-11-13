package org.cardboardpowered.impl.block;

import net.minecraft.block.entity.CommandBlockBlockEntity;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.CommandBlock;
import org.bukkit.craftbukkit.block.CraftBlockEntityState;
import org.bukkit.craftbukkit.util.CraftChatMessage;

public class CardboardCommandBlock extends CraftBlockEntityState<CommandBlockBlockEntity> implements CommandBlock {

    private String command;
    private String name;

    public CardboardCommandBlock(Block block) {
        super(block, CommandBlockBlockEntity.class);
    }

    public CardboardCommandBlock(final Material material, final CommandBlockBlockEntity cmdblock) {
        super(material, cmdblock);
    }

    @Override
    public void load(CommandBlockBlockEntity cmdblock) {
        super.load(cmdblock);
        command = cmdblock.getCommandExecutor().getCommand();
        name = CraftChatMessage.fromComponent(cmdblock.getCommandExecutor().getCustomName());
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public void setCommand(String command) {
        this.command = command != null ? command : "";
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name != null ? name : "@";
    }

    @Override
    public void applyTo(CommandBlockBlockEntity commandBlock) {
        super.applyTo(commandBlock);
        commandBlock.getCommandExecutor().setCommand(command);
        commandBlock.getCommandExecutor().setCustomName(CraftChatMessage.fromStringOrNull(name));
    }

}