package com.javazilla.bukkitfabric.mixin.entity;

import java.util.UUID;

import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftAbstractVillager;
import org.bukkit.craftbukkit.entity.CraftArmorStand;
import org.bukkit.craftbukkit.entity.CraftCreature;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftFallingBlock;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.craftbukkit.entity.CraftItem;
import org.bukkit.craftbukkit.entity.CraftLivingEntity;
import org.bukkit.craftbukkit.entity.CraftMonster;
import org.bukkit.craftbukkit.entity.CraftPlayer;
import org.bukkit.craftbukkit.entity.CraftVillager;
import org.bukkit.projectiles.ProjectileSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.javazilla.bukkitfabric.impl.entity.AnimalsImpl;
import com.javazilla.bukkitfabric.impl.entity.CaveSpiderImpl;
import com.javazilla.bukkitfabric.impl.entity.ChickenImpl;
import com.javazilla.bukkitfabric.impl.entity.CowImpl;
import com.javazilla.bukkitfabric.impl.entity.DrownedImpl;
import com.javazilla.bukkitfabric.impl.entity.EggImpl;
import com.javazilla.bukkitfabric.impl.entity.EndermiteImpl;
import com.javazilla.bukkitfabric.impl.entity.ExperienceOrbImpl;
import com.javazilla.bukkitfabric.impl.entity.GiantImpl;
import com.javazilla.bukkitfabric.impl.entity.HuskImpl;
import com.javazilla.bukkitfabric.impl.entity.LightningStrikeImpl;
import com.javazilla.bukkitfabric.impl.entity.MagmaCubeImpl;
import com.javazilla.bukkitfabric.impl.entity.MushroomImpl;
import com.javazilla.bukkitfabric.impl.entity.PigZombieImpl;
import com.javazilla.bukkitfabric.impl.entity.SkeletonImpl;
import com.javazilla.bukkitfabric.impl.entity.SlimeImpl;
import com.javazilla.bukkitfabric.impl.entity.SnowballImpl;
import com.javazilla.bukkitfabric.impl.entity.SpiderImpl;
import com.javazilla.bukkitfabric.impl.entity.StrayImpl;
import com.javazilla.bukkitfabric.impl.entity.UnknownEntity;
import com.javazilla.bukkitfabric.impl.entity.VillagerZombieImpl;
import com.javazilla.bukkitfabric.impl.entity.WanderingTraderImpl;
import com.javazilla.bukkitfabric.impl.entity.WitherSkeletonImpl;
import com.javazilla.bukkitfabric.impl.entity.ZombieImpl;
import com.javazilla.bukkitfabric.interfaces.IMixinCommandOutput;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.AbstractTraderEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ExplosiveProjectileEntity;
import net.minecraft.entity.projectile.PersistentProjectileEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

@Mixin(Entity.class)
public class MixinEntity implements IMixinCommandOutput, IMixinEntity {

    public org.bukkit.entity.Entity bukkit;
    public org.bukkit.projectiles.ProjectileSource projectileSource;

    public MixinEntity() {
        this.bukkit = getEntity(CraftServer.INSTANCE, (Entity)(Object)this);//new CraftEntity2((Entity) (Object) this);
    }

    public void sendSystemMessage(Text message) {
        ((Entity) (Object) this).sendSystemMessage(message, UUID.randomUUID());
    }

    @Inject(at = @At(value = "HEAD"), method = "tick()V")
    private void setBukkit(CallbackInfo callbackInfo) {
        if (null == bukkit)
            this.bukkit = getEntity(CraftServer.INSTANCE, (Entity)(Object)this);
    }

    @Override
    public CommandSender getBukkitSender(ServerCommandSource serverCommandSource) {
        return bukkit;
    }

    @Override
    public org.bukkit.entity.Entity getBukkitEntity() {
        return bukkit;
    }

    @Override
    public void setProjectileSourceBukkit(ProjectileSource source) {
        this.projectileSource = source;
    }

    private static CraftEntity getEntity(CraftServer server, Entity entity) {
        /*
         * Order is *EXTREMELY* important -- keep it right! =D
         */
        // CHECKSTYLE:OFF
        if (entity instanceof LivingEntity) {
            // Players
            if (entity instanceof PlayerEntity) {
                if (entity instanceof ServerPlayerEntity) { return new CraftPlayer((ServerPlayerEntity) entity); }
                else { return new CraftHumanEntity((PlayerEntity) entity); }
            }
            // Water Animals
            else if (entity instanceof WaterCreatureEntity) {
                /*if (entity instanceof SquidEntity) { return new CraftSquid(server, (SquidEntity) entity); }
                else if (entity instanceof FishEntity) {
                    if (entity instanceof CodEntity) { return new CraftCod(server, (CodEntity) entity); }
                    else if (entity instanceof PufferfishEntity) { return new CraftPufferFish(server, (PufferfishEntity) entity); }
                    else if (entity instanceof SalmonEntity) { return new CraftSalmon(server, (SalmonEntity) entity); }
                    else if (entity instanceof TropicalFishEntity) { return new CraftTropicalFish(server, (TropicalFishEntity) entity); }
                    else { return new CraftFish(server, (FishEntity) entity); }
                }
                //else if (entity instanceof DolphinEntity) { return new CraftDolphin(server, (DolphinEntity) entity); }
                //else { return new CraftWaterMob(server, (WaterCreatureEntity) entity); }*/
            }
            else if (entity instanceof PathAwareEntity) {
                // Animals
                if (entity instanceof AnimalEntity) {
                    if (entity instanceof ChickenEntity) { return new ChickenImpl(server, (ChickenEntity) entity); }
                    else if (entity instanceof CowEntity) {
                        if (entity instanceof MooshroomEntity) { return new MushroomImpl(server, (MooshroomEntity) entity); }
                        else { return new CowImpl(server, (CowEntity) entity); }
                    }/*
                    else if (entity instanceof PigEntity) { return new CraftPig(server, (PigEntity) entity); }
                    else if (entity instanceof TameableEntity) {
                        if (entity instanceof WolfEntity) { return new CraftWolf(server, (WolfEntity) entity); }
                        else if (entity instanceof CatEntity) { return new CraftCat(server, (CatEntity) entity); }
                        else if (entity instanceof ParrotEntity) { return new CraftParrot(server, (ParrotEntity) entity); }
                    }
                    else if (entity instanceof SheepEntity) { return new CraftSheep(server, (SheepEntity) entity); }
                    else if (entity instanceof HorseBaseEntity) {
                        if (entity instanceof AbstractDonkeyEntity){
                            if (entity instanceof DonkeyEntity) { return new CraftDonkey(server, (DonkeyEntity) entity); }
                            else if (entity instanceof MuleEntity) { return new CraftMule(server, (MuleEntity) entity); }
                            else if (entity instanceof TraderLlamaEntity) { return new CraftTraderLlama(server, (TraderLlamaEntity) entity); }
                            else if (entity instanceof LlamaEntity) { return new CraftLlama(server, (LlamaEntity) entity); }
                        } else if (entity instanceof HorseEntity) { return new CraftHorse(server, (HorseEntity) entity); }
                        else if (entity instanceof SkeletonHorseEntity) { return new CraftSkeletonHorse(server, (SkeletonHorseEntity) entity); }
                        else if (entity instanceof ZombieHorseEntity) { return new CraftZombieHorse(server, (ZombieHorseEntity) entity); }
                    }*/
                    //else if (entity instanceof RabbitEntity) { return new CraftRabbit(server, (RabbitEntity) entity); }
                    //else if (entity instanceof PolarBearEntity) { return new CraftPolarBear(server, (PolarBearEntity) entity); }
                    //else if (entity instanceof TurtleEntity) { return new CraftTurtle(server, (TurtleEntity) entity); }
                    //else if (entity instanceof OcelotEntity) { return new CraftOcelot(server, (OcelotEntity) entity); }
                    //else if (entity instanceof PandaEntity) { return new CraftPanda(server, (PandaEntity) entity); }
                   // else if (entity instanceof FoxEntity) { return new CraftFox(server, (FoxEntity) entity); }
                    //else if (entity instanceof BeeEntity) { return new CraftBee(server, (BeeEntity) entity); }
                    //else if (entity instanceof HoglinEntity) { return new CraftHoglin(server, (HoglinEntity) entity); }
                    //else if (entity instanceof StriderEntity) { return new CraftStrider(server, (StriderEntity) entity); }
                    else  { return new AnimalsImpl(server, (AnimalEntity) entity); }
                }
                // Monsters
                else if (entity instanceof HostileEntity) {
                    if (entity instanceof ZombieEntity) {
                        if (entity instanceof ZombifiedPiglinEntity) { return new PigZombieImpl(server, (ZombifiedPiglinEntity) entity); }
                        else if (entity instanceof HuskEntity) { return new HuskImpl(server, (HuskEntity) entity); }
                        else if (entity instanceof ZombieVillagerEntity) { return new VillagerZombieImpl(server, (ZombieVillagerEntity) entity); }
                        else if (entity instanceof DrownedEntity) { return new DrownedImpl(server, (DrownedEntity) entity); }
                        else { return new ZombieImpl(server, (ZombieEntity) entity); }
                    }
                    //else if (entity instanceof CreeperEntity) { return new CraftCreeper(server, (CreeperEntity) entity); }
                    //else if (entity instanceof EndermanEntity) { return new CraftEnderman(server, (EndermanEntity) entity); }
                    //else if (entity instanceof SilverfishEntity) { return new CraftSilverfish(server, (SilverfishEntity) entity); }
                    else if (entity instanceof GiantEntity) { return new GiantImpl(server, (GiantEntity) entity); }
                    else if (entity instanceof AbstractSkeletonEntity) {
                        if (entity instanceof StrayEntity) { return new StrayImpl(server, (StrayEntity) entity); }
                        else if (entity instanceof WitherSkeletonEntity) { return new WitherSkeletonImpl(server, (WitherSkeletonEntity) entity); }
                        else { return new SkeletonImpl(server, (AbstractSkeletonEntity) entity); }
                    }
                    //else if (entity instanceof BlazeEntity) { return new CraftBlaze(server, (BlazeEntity) entity); }
                    //else if (entity instanceof WitchEntity) { return new CraftWitch(server, (WitchEntity) entity); }
                    //else if (entity instanceof WitherEntity) { return new CraftWither(server, (WitherEntity) entity); }
                    else if (entity instanceof SpiderEntity) {
                        if (entity instanceof CaveSpiderEntity) { return new CaveSpiderImpl(server, (CaveSpiderEntity) entity); }
                        else { return new SpiderImpl(server, (SpiderEntity) entity); }
                    }
                    else if (entity instanceof EndermiteEntity) { return new EndermiteImpl(server, (EndermiteEntity) entity); }
                    else if (entity instanceof GuardianEntity) {
                        //if (entity instanceof ElderGuardianEntity) { return new CraftElderGuardian(server, (ElderGuardianEntity) entity); }
                        //else { return new CraftGuardian(server, (GuardianEntity) entity); }
                    }
                    //else if (entity instanceof VexEntity) { return new CraftVex(server, (VexEntity) entity); }
                    else if (entity instanceof IllagerEntity) {
                        if (entity instanceof SpellcastingIllagerEntity) {
                            //if (entity instanceof EvokerEntity) { return new CraftEvoker(server, (EvokerEntity) entity); }
                            //else if (entity instanceof IllusionerEntity) { return new CraftIllusioner(server, (IllusionerEntity) entity); }
                            //else {  return new CraftSpellcaster(server, (SpellcastingIllagerEntity) entity); }
                        }
                        //else if (entity instanceof VindicatorEntity) { return new CraftVindicator(server, (VindicatorEntity) entity); }
                        //else if (entity instanceof PillagerEntity) { return new CraftPillager(server, (PillagerEntity) entity); }
                        //else { return new CraftIllager(server, (IllagerEntity) entity); }
                    }
                    //else if (entity instanceof RavagerEntity) { return new CraftRavager(server, (RavagerEntity) entity); }
                    //else if (entity instanceof AbstractPiglinEntity) {
                        //if (entity instanceof PiglinEntity) return new CraftPiglin(server, (PiglinEntity) entity);
                        //else if (entity instanceof PiglinBruteEntity) { return new CraftPiglinBrute(server, (PiglinBruteEntity) entity); }
                        //else { return new CraftPiglinAbstract(server, (AbstractPiglinEntity) entity); }
                    //}
                    //else if (entity instanceof ZoglinEntity) { return new CraftZoglin(server, (ZoglinEntity) entity); }

                    else  { return new CraftMonster(server, (HostileEntity) entity); }
                }
                //else if (entity instanceof GolemEntity) {
                    //if (entity instanceof SnowGolemEntity) { return new CraftSnowman(server, (SnowGolemEntity) entity); }
                    //else if (entity instanceof IronGolemEntity) { return new CraftIronGolem(server, (IronGolemEntity) entity); }
                    //else if (entity instanceof ShulkerEntity) { return new CraftShulker(server, (ShulkerEntity) entity); }
                //}
                else if (entity instanceof AbstractTraderEntity) {
                    if (entity instanceof VillagerEntity) { return new CraftVillager(server, (VillagerEntity) entity); }
                    else if (entity instanceof WanderingTraderEntity) { return new WanderingTraderImpl(server, (WanderingTraderEntity) entity); }
                    else { return new CraftAbstractVillager(server, (AbstractTraderEntity) entity); }
                }
                else { return new CraftCreature(server, (PathAwareEntity) entity); }
            }
            // Slimes are a special (and broken) case
            else if (entity instanceof SlimeEntity) {
                if (entity instanceof MagmaCubeEntity) { return new MagmaCubeImpl(server, (MagmaCubeEntity) entity); }
                else { return new SlimeImpl(server, (SlimeEntity) entity); }
            }
            // Flying
            //else if (entity instanceof FlyingEntity) {
                //if (entity instanceof GhastEntity) { return new CraftGhast(server, (GhastEntity) entity); }
                //else if (entity instanceof PhantomEntity) { return new CraftPhantom(server, (PhantomEntity) entity); }
                //else { return new CraftFlying(server, (FlyingEntity) entity); }
            //}
            //else if (entity instanceof EnderDragonEntity) {
                //return new CraftEnderDragon(server, (EnderDragonEntity) entity);
            //}
            // Ambient
            //else if (entity instanceof AmbientEntity) {
                //if (entity instanceof BatEntity) { return new CraftBat(server, (BatEntity) entity); }
                //else { return new CraftAmbient(server, (AmbientEntity) entity); }
            //}
            else if (entity instanceof ArmorStandEntity) { return new CraftArmorStand(server, (ArmorStandEntity) entity); }
            else  { return new CraftLivingEntity(server, (LivingEntity) entity); }
        }
        else if (entity instanceof EnderDragonPart) {
            EnderDragonPart part = (EnderDragonPart) entity;
            //if (part.owner instanceof EnderDragonEntity) { return new CraftEnderDragonPart(server, (EnderDragonPart) entity); }
           //else { return new CraftComplexPart(server, (EnderDragonPart) entity); }
        }
        else if (entity instanceof ExperienceOrbEntity) { return new ExperienceOrbImpl(server, (ExperienceOrbEntity) entity); }
        //else if (entity instanceof ArrowEntity) { return new CraftTippedArrow(server, (ArrowEntity) entity); }
        //else if (entity instanceof SpectralArrowEntity) { return new CraftSpectralArrow(server, (SpectralArrowEntity) entity); }
        else if (entity instanceof PersistentProjectileEntity) {
            //if (entity instanceof TridentEntity) { return new CraftTrident(server, (TridentEntity) entity); }
            //else { return new CraftArrow(server, (PersistentProjectileEntity) entity); }
        }
        //else if (entity instanceof BoatEntity) { return new CraftBoat(server, (BoatEntity) entity); }
        else if (entity instanceof ThrownEntity) {
            if (entity instanceof EggEntity) { return new EggImpl(server, (EggEntity) entity); }
            else if (entity instanceof SnowballEntity) { return new SnowballImpl(server, (SnowballEntity) entity); }
            //else if (entity instanceof PotionEntity) { return new CraftThrownPotion(server, (PotionEntity) entity); }
            //else if (entity instanceof EnderPearlEntity) { return new CraftEnderPearl(server, (EnderPearlEntity) entity); }
            //else if (entity instanceof ExperienceBottleEntity) { return new CraftThrownExpBottle(server, (ExperienceBottleEntity) entity); }
        }
        else if (entity instanceof FallingBlockEntity) { return new CraftFallingBlock(server, (FallingBlockEntity) entity); }
        else if (entity instanceof ExplosiveProjectileEntity) {
            //if (entity instanceof SmallFireballEntity) { return new CraftSmallFireball(server, (SmallFireballEntity) entity); }
            //else if (entity instanceof FireballEntity) { return new CraftLargeFireball(server, (FireballEntity) entity); }
           // else if (entity instanceof WitherSkullEntity) { return new CraftWitherSkull(server, (WitherSkullEntity) entity); }
           // else if (entity instanceof DragonFireballEntity) { return new CraftDragonFireball(server, (DragonFireballEntity) entity); }
            //else { return new CraftFireball(server, (ExplosiveProjectileEntity) entity); }
        }
        //else if (entity instanceof EyeOfEnderEntity) { return new CraftEnderSignal(server, (EyeOfEnderEntity) entity); }
        //else if (entity instanceof EndCrystalEntity) { return new CraftEnderCrystal(server, (EndCrystalEntity) entity); }
        //else if (entity instanceof FishingBobberEntity) { return new CraftFishHook(server, (FishingBobberEntity) entity); }
        else if (entity instanceof ItemEntity) { return new CraftItem(server, (ItemEntity) entity); }
        else if (entity instanceof LightningEntity) { return new LightningStrikeImpl(server, (LightningEntity) entity); }
        /*else if (entity instanceof AbstractMinecartEntity) {
            if (entity instanceof FurnaceMinecartEntity) { return new CraftMinecartFurnace(server, (FurnaceMinecartEntity) entity); }
            else if (entity instanceof ChestMinecartEntity) { return new CraftMinecartChest(server, (ChestMinecartEntity) entity); }
            else if (entity instanceof TntMinecartEntity) { return new CraftMinecartTNT(server, (TntMinecartEntity) entity); }
            else if (entity instanceof HopperMinecartEntity) { return new CraftMinecartHopper(server, (HopperMinecartEntity) entity); }
            else if (entity instanceof SpawnerMinecartEntity) { return new CraftMinecartMobSpawner(server, (SpawnerMinecartEntity) entity); }
            else if (entity instanceof MinecartEntity) { return new CraftMinecartRideable(server, (MinecartEntity) entity); }
            else if (entity instanceof CommandBlockMinecartEntity) { return new CraftMinecartCommand(server, (CommandBlockMinecartEntity) entity); }
        }*/ else if (entity instanceof AbstractDecorationEntity) {
            //if (entity instanceof PaintingEntity) { return new CraftPainting(server, (PaintingEntity) entity); }
            //else if (entity instanceof ItemFrameEntity) { return new CraftItemFrame(server, (ItemFrameEntity) entity); }
            //else if (entity instanceof LeashKnotEntity) { return new CraftLeash(server, (LeashKnotEntity) entity); }
            //else { return new CraftHanging(server, (AbstractDecorationEntity) entity); }
        }
        //else if (entity instanceof TntEntity) { return new CraftTNTPrimed(server, (TntEntity) entity); }
        //else if (entity instanceof FireworkRocketEntity) { return new CraftFirework(server, (FireworkRocketEntity) entity); }
        //else if (entity instanceof ShulkerBulletEntity) { return new CraftShulkerBullet(server, (ShulkerBulletEntity) entity); }
        //else if (entity instanceof AreaEffectCloudEntity) { return new CraftAreaEffectCloud(server, (AreaEffectCloudEntity) entity); }
        //else if (entity instanceof EvokerFangsEntity) { return new CraftEvokerFangs(server, (EvokerFangsEntity) entity); }
        //else if (entity instanceof LlamaSpitEntity) { return new CraftLlamaSpit(server, (LlamaSpitEntity) entity); }
        // CHECKSTYLE:ON

        return new UnknownEntity(entity); // TODO
        //throw new AssertionError("Unknown entity " + (entity == null ? null : entity.getClass()));
    }

    @Override
    public ProjectileSource getProjectileSourceBukkit() {
        return projectileSource;
    }

}