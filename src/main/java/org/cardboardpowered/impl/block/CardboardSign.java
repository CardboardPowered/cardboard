package org.cardboardpowered.impl.block;

import java.util.List;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.block.sign.Side;
import org.bukkit.block.sign.SignSide;
import org.bukkit.craftbukkit.util.CraftChatMessage;
import org.jetbrains.annotations.NotNull;

import com.javazilla.bukkitfabric.interfaces.IMixinSignBlockEntity;

import net.kyori.adventure.text.Component;
import net.minecraft.block.entity.SignBlockEntity;
import net.minecraft.text.Text;

@SuppressWarnings("deprecation")
public class CardboardSign extends CardboardBlockEntityState<SignBlockEntity> implements Sign {

    private String[] lines;
    private boolean editable;

    public CardboardSign(final Block block) {
        super(block, SignBlockEntity.class);
    }

    public CardboardSign(final Material material, final SignBlockEntity te) {
        super(material, te);
    }

    @Override
    public void load(SignBlockEntity sign) {
        super.load(sign);
        lines = new String[((IMixinSignBlockEntity)sign).getTextBF().length];
        System.arraycopy(revertComponents(((IMixinSignBlockEntity)sign).getTextBF()), 0, lines, 0, lines.length);
       // editable = sign.editable;
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

    @Override
    public DyeColor getColor() {
        return DyeColor.getByWoolData((byte) getSnapshot().getFrontText().getColor().getId());
    }

    @Override
    public void setColor(DyeColor color) {
        getSnapshot().getFrontText().withColor(net.minecraft.util.DyeColor.byId(color.getWoolData()));
    }

    @Override
    public void applyTo(SignBlockEntity sign) {
        super.applyTo(sign);

        Text[] newLines = sanitizeLines(lines);
        System.arraycopy(newLines, 0, ((IMixinSignBlockEntity)sign).getTextBF(), 0, 4);
      //  sign.editable = true;
    }

    public static Text[] sanitizeLines(String[] lines) {
        Text[] components = new Text[4];
        for (int i = 0; i < 4; i++)
            components[i] = (i < lines.length && lines[i] != null) ? CraftChatMessage.fromString(lines[i])[0] : Text.of("");
        return components;
    }

    public static String[] revertComponents(Text[] components) {
        String[] lines = new String[components.length];
        for (int i = 0; i < lines.length; i++)
            lines[i] = CraftChatMessage.fromComponent(components[i]);
        return lines;
    }

    @Override
    public Component line(int arg0) throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void line(int arg0, @NotNull Component arg1) throws IndexOutOfBoundsException {
        // TODO Auto-generated method stub
        
    }

    @Override
    public List<Component> lines() {
        // TODO Auto-generated method stub
        return null;
    }

    // 1.17 API Start
    
    @Override
    public boolean isGlowingText() {
        return getSnapshot().getFrontText().isGlowing();
    }

    @Override
    public void setGlowingText(boolean arg0) {
        getSnapshot().getFrontText().withGlowing(arg0);
    }

	@Override
    public SignSide getSide(Side side) {
        // Preconditions.checkArgument((side != null ? 1 : 0) != 0, (Object)"side == null");
        switch (side) {
            //case FRONT: {
             //   return this.front;
            //}
            //case BACK: {
           //     return this.back;
           // }
        }
        // TODO!
        return null;
        //throw new IllegalArgumentException();
    }

}