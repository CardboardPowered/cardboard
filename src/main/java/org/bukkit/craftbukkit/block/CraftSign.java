package org.bukkit.craftbukkit.block;

import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.craftbukkit.util.CraftChatMessage;

import com.javazilla.bukkitfabric.interfaces.IMixinSignBlockEntity;

public class CraftSign extends CraftBlockEntityState<SignBlockEntity> implements Sign {

    private String[] lines;
    private boolean editable;

    public CraftSign(final Block block) {
        super(block, SignBlockEntity.class);
    }

    public CraftSign(final Material material, final SignBlockEntity te) {
        super(material, te);
    }

    @Override
    public void load(SignBlockEntity sign) {
        super.load(sign);

        lines = new String[((IMixinSignBlockEntity)sign).getTextBF().length];
        System.arraycopy(revertComponents(((IMixinSignBlockEntity)sign).getTextBF()), 0, lines, 0, lines.length);
        editable = sign.editable;
    }

    @Override
    public String[] getLines() {
        return lines;
    }

    @Override
    public String getLine(int index) throws IndexOutOfBoundsException {
        return lines[index];
    }

    @Override
    public void setLine(int index, String line) throws IndexOutOfBoundsException {
        lines[index] = line;
    }

    @Override
    public boolean isEditable() {
        return this.editable;
    }

    @Override
    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    @SuppressWarnings("deprecation")
    @Override
    public DyeColor getColor() {
        return DyeColor.getByWoolData((byte) getSnapshot().getTextColor().getId());
    }

    @SuppressWarnings("deprecation")
    @Override
    public void setColor(DyeColor color) {
        getSnapshot().setTextColor(net.minecraft.util.DyeColor.byId(color.getWoolData()));
    }

    @Override
    public void applyTo(SignBlockEntity sign) {
        super.applyTo(sign);

        Text[] newLines = sanitizeLines(lines);
        System.arraycopy(newLines, 0, ((IMixinSignBlockEntity)sign).getTextBF(), 0, 4);
        sign.setEditable(editable);
    }

    public static Text[] sanitizeLines(String[] lines) {
        Text[] components = new Text[4];
        for (int i = 0; i < 4; i++)
            components[i] = (i < lines.length && lines[i] != null) ? CraftChatMessage.fromString(lines[i])[0] : new LiteralText("");

        return components;
    }

    public static String[] revertComponents(Text[] components) {
        String[] lines = new String[components.length];
        for (int i = 0; i < lines.length; i++)
            lines[i] = CraftChatMessage.fromComponent(components[i]);
        return lines;
    }

}