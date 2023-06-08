package org.cardboardpowered;

import java.util.Map;

import org.bukkit.Color;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;

public class ExtraPotionEffectTypeWrapper extends PotionEffectType {

	public ExtraPotionEffectTypeWrapper(int id, @NotNull String name) {
        super(id, NamespacedKey.minecraft(name));
    	// super(id);
    }

    @Override
    public double getDurationModifier() {
        return getType().getDurationModifier();
    }

    @NotNull
    @Override
    public String getName() {
        return getType().getName();
    }

    /**
     * Get the potion type bound to this wrapper.
     *
     * @return The potion effect type
     */
    @NotNull
    public PotionEffectType getType() {
        return this;
    	//return PotionEffectType.getById(getId());
    }

    @Override
    public boolean isInstant() {
        return getType().isInstant();
    }

    @NotNull
    @Override
    public Color getColor() {
        return getType().getColor();
    }

	@Override
	public @NotNull String translationKey() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double getAttributeModifierAmount(@NotNull Attribute arg0, int arg1) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public @NotNull Map<Attribute, AttributeModifier> getEffectAttributes() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public @NotNull Category getEffectCategory() {
		// TODO Auto-generated method stub
		return Category.NEUTRAL;
	}
}
