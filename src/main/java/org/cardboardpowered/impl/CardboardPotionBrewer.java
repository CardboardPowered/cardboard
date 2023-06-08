package org.cardboardpowered.impl;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;

import io.papermc.paper.potion.PotionMix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.potion.Potion;

import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionBrewer;
import org.bukkit.potion.PotionData;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;
import org.jetbrains.annotations.NotNull;

public class CardboardPotionBrewer implements PotionBrewer {

    private static final Map<PotionType, Collection<PotionEffect>> cache = Maps.newHashMap();

    @Override
    public Collection<PotionEffect> getEffects(PotionType damage, boolean upgraded, boolean extended) {
        if (cache.containsKey(damage)) return cache.get(damage);

        List<StatusEffectInstance> mcEffects = Potion.byId(CardboardPotionUtil.fromBukkit(new PotionData(damage, extended, upgraded))).getEffects();

        ImmutableList.Builder<PotionEffect> builder = new ImmutableList.Builder<PotionEffect>();
        for (StatusEffectInstance effect : mcEffects) builder.add(CardboardPotionUtil.toBukkit(effect));

        cache.put(damage, builder.build());
        return cache.get(damage);
    }

    @Override
    public Collection<PotionEffect> getEffectsFromDamage(int damage) {
        return new ArrayList<PotionEffect>();
    }

    @SuppressWarnings("deprecation")
    @Override
    public PotionEffect createEffect(PotionEffectType potion, int duration, int amplifier) {
        return new PotionEffect(potion, potion.isInstant() ? 1 : (int) (duration * potion.getDurationModifier()), amplifier);
    }

	@Override
	public void addPotionMix(@NotNull PotionMix arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removePotionMix(@NotNull NamespacedKey arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void resetPotionMixes() {
		// TODO Auto-generated method stub
		
	}

}