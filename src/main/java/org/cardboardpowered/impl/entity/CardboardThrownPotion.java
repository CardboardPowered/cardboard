package org.cardboardpowered.impl.entity;

import com.google.common.collect.ImmutableList;
import java.util.Collection;

import net.kyori.adventure.text.Component;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.potion.PotionUtil;
import org.apache.commons.lang.Validate;
import org.bukkit.Material;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.inventory.CraftItemStack;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.cardboardpowered.impl.CardboardPotionUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CardboardThrownPotion extends ProjectileImpl implements ThrownPotion {

    public CardboardThrownPotion(CraftServer server, PotionEntity entity) {
        super(server, entity);
    }

    @Override
    public Collection<PotionEffect> getEffects() {
        ImmutableList.Builder<PotionEffect> builder = ImmutableList.builder();
        for (StatusEffectInstance effect : PotionUtil.getPotionEffects(getHandle().getStack()))
            builder.add(CardboardPotionUtil.toBukkit(effect));
        return builder.build();
    }

    @Override
    public ItemStack getItem() {
        return CraftItemStack.asBukkitCopy(getHandle().getStack());
    }

    @Override
    public void setItem(ItemStack item) {
        Validate.notNull(item, "ItemStack cannot be null.");
        Validate.isTrue(item.getType() == Material.LINGERING_POTION || item.getType() == Material.SPLASH_POTION, "ItemStack must be a lingering or splash potion. This item stack was " + item.getType() + ".");

        getHandle().setItem(CraftItemStack.asNMSCopy(item));
    }

    @Override
    public PotionEntity getHandle() {
        return (PotionEntity) nms;
    }

    @Override
    public EntityType getType() {
        return EntityType.SPLASH_POTION;
    }

    @Override
    public @Nullable Component customName() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void customName(@Nullable Component arg0) {
        // TODO Auto-generated method stub
        
    }

	@Override
	public @NotNull PotionMeta getPotionMeta() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setPotionMeta(@NotNull PotionMeta arg0) {
		// TODO Auto-generated method stub
		
	}
    
}