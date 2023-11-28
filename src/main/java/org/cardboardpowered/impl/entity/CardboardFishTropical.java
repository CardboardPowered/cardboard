package org.cardboardpowered.impl.entity;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.entity.passive.TropicalFishEntity;
import org.bukkit.DyeColor;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TropicalFish;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import io.papermc.paper.entity.SchoolableFish;

@SuppressWarnings("deprecation")
public class CardboardFishTropical extends CardboardFish implements TropicalFish {

    public CardboardFishTropical(CraftServer server, TropicalFishEntity entity) {
        super(server, entity);
    }

    @Override
    public TropicalFishEntity getHandle() {
        return (TropicalFishEntity) nms;
    }

    @Override
    public String toString() {
        return "TropicalFish";
    }

    @Override
    public EntityType getType() {
        return EntityType.TROPICAL_FISH;
    }

    @Override
    public DyeColor getPatternColor() {
        return getPatternColor(getHandle().getVariant());
    }

    @Override
    public void setPatternColor(DyeColor color) {
        getHandle().setVariant(getData(color, getBodyColor(), getPattern()));
    }

    @Override
    public DyeColor getBodyColor() {
        return getBodyColor(getHandle().getVariant());
    }

    @Override
    public void setBodyColor(DyeColor color) {
        getHandle().setVariant(getData(getPatternColor(), color, getPattern()));
    }

    @Override
    public Pattern getPattern() {
        return getPattern(getHandle().getVariant());
    }

    @Override
    public void setPattern(Pattern pattern) {
        getHandle().setVariant(getData(getPatternColor(), getBodyColor(), pattern));
    }

    public static enum CraftPattern {
        KOB(0, false),
        SUNSTREAK(1, false),
        SNOOPER(2, false),
        DASHER(3, false),
        BRINELY(4, false),
        SPOTTY(5, false),
        FLOPPER(0, true),
        STRIPEY(1, true),
        GLITTER(2, true),
        BLOCKFISH(3, true),
        BETTY(4, true),
        CLAYFISH(5, true);

        private final int variant;
        private final boolean large;

        //
        private static final Map<Integer, Pattern> BY_DATA = new HashMap<>();

        static {
            for (CraftPattern type : values()) {
                BY_DATA.put(type.getDataValue(), Pattern.values()[type.ordinal()]);
            }
        }

        public static Pattern fromData(int data) {
            return BY_DATA.get(data);
        }

        private CraftPattern(int variant, boolean large) {
            this.variant = variant;
            this.large = large;
        }

        public int getDataValue() {
            return variant << 8 | ((large) ? 1 : 0);
        }
    }

    public static int getData(DyeColor patternColor, DyeColor bodyColor, Pattern type) {
        return patternColor.getWoolData() << 24 | bodyColor.getWoolData() << 16 | CraftPattern.values()[type.ordinal()].getDataValue();
    }

    public static DyeColor getPatternColor(int data) {
        return DyeColor.getByWoolData((byte) ((data >> 24) & 0xFF));
    }

    public static DyeColor getBodyColor(int data) {
        return DyeColor.getByWoolData((byte) ((data >> 16) & 0xFF));
    }

    public static Pattern getPattern(int data) {
        return CraftPattern.fromData(data & 0xFFFF);
    }

	@Override
	public int getMaxSchoolSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public @Nullable SchoolableFish getSchoolLeader() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int getSchoolSize() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void startFollowing(@NotNull SchoolableFish arg0) {
		// TODO Auto-generated method stub
	}

	@Override
	public void stopFollowing() {
		// TODO Auto-generated method stub
	}

}