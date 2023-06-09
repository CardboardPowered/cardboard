/**
 * Cardboard - Spigot/Paper for Fabric
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
package org.cardboardpowered.mixin.entity;

import java.util.ArrayList;
import java.util.UUID;

import net.minecraft.entity.projectile.*;
import net.minecraft.util.ActionResult;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.craftbukkit.CraftServer;
import org.bukkit.craftbukkit.entity.CraftEntity;
import org.bukkit.craftbukkit.entity.CraftHumanEntity;
import org.bukkit.entity.Pose;
import org.bukkit.event.entity.EntityAirChangeEvent;
import org.bukkit.event.entity.EntityCombustByBlockEvent;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityDropItemEvent;
import org.bukkit.event.entity.EntityPoseChangeEvent;
import org.bukkit.projectiles.ProjectileSource;
import org.cardboardpowered.api.event.CardboardEntityMountEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.cardboardpowered.impl.entity.*;

import com.javazilla.bukkitfabric.BukkitFabricMod;
import com.javazilla.bukkitfabric.interfaces.IMixinCommandOutput;
import com.javazilla.bukkitfabric.interfaces.IMixinEntity;
import me.isaiah.common.entity.IRemoveReason;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.ExperienceOrbEntity;
import net.minecraft.entity.FallingBlockEntity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LightningEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.entity.TntEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPart;
import net.minecraft.entity.decoration.AbstractDecorationEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.mob.AbstractSkeletonEntity;
import net.minecraft.entity.mob.AmbientEntity;
import net.minecraft.entity.mob.BlazeEntity;
import net.minecraft.entity.mob.CaveSpiderEntity;
import net.minecraft.entity.mob.CreeperEntity;
import net.minecraft.entity.mob.DrownedEntity;
import net.minecraft.entity.mob.ElderGuardianEntity;
import net.minecraft.entity.mob.EndermanEntity;
import net.minecraft.entity.mob.EndermiteEntity;
import net.minecraft.entity.mob.EvokerEntity;
import net.minecraft.entity.mob.FlyingEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.GiantEntity;
import net.minecraft.entity.mob.GuardianEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.HuskEntity;
import net.minecraft.entity.mob.IllagerEntity;
import net.minecraft.entity.mob.IllusionerEntity;
import net.minecraft.entity.mob.MagmaCubeEntity;
import net.minecraft.entity.mob.PathAwareEntity;
import net.minecraft.entity.mob.PhantomEntity;
import net.minecraft.entity.mob.PillagerEntity;
import net.minecraft.entity.mob.ShulkerEntity;
import net.minecraft.entity.mob.SilverfishEntity;
import net.minecraft.entity.mob.SlimeEntity;
import net.minecraft.entity.mob.SpellcastingIllagerEntity;
import net.minecraft.entity.mob.SpiderEntity;
import net.minecraft.entity.mob.StrayEntity;
import net.minecraft.entity.mob.VexEntity;
import net.minecraft.entity.mob.VindicatorEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.entity.mob.WitchEntity;
import net.minecraft.entity.mob.WitherSkeletonEntity;
import net.minecraft.entity.mob.ZombieEntity;
import net.minecraft.entity.mob.ZombieVillagerEntity;
import net.minecraft.entity.mob.ZombifiedPiglinEntity;
import net.minecraft.entity.passive.MerchantEntity;
import net.minecraft.entity.passive.AbstractDonkeyEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.BatEntity;
import net.minecraft.entity.passive.CatEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.CodEntity;
import net.minecraft.entity.passive.CowEntity;
import net.minecraft.entity.passive.DolphinEntity;
import net.minecraft.entity.passive.DonkeyEntity;
import net.minecraft.entity.passive.FishEntity;
import net.minecraft.entity.passive.GolemEntity;
import net.minecraft.entity.passive.AbstractHorseEntity;
import net.minecraft.entity.passive.HorseEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.passive.LlamaEntity;
import net.minecraft.entity.passive.MooshroomEntity;
import net.minecraft.entity.passive.MuleEntity;
import net.minecraft.entity.passive.OcelotEntity;
import net.minecraft.entity.passive.PandaEntity;
import net.minecraft.entity.passive.ParrotEntity;
import net.minecraft.entity.passive.PigEntity;
import net.minecraft.entity.passive.PolarBearEntity;
import net.minecraft.entity.passive.PufferfishEntity;
import net.minecraft.entity.passive.SalmonEntity;
import net.minecraft.entity.passive.SnowGolemEntity;
import net.minecraft.entity.passive.SquidEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.passive.TropicalFishEntity;
import net.minecraft.entity.passive.TurtleEntity;
import net.minecraft.entity.passive.VillagerEntity;
import net.minecraft.entity.passive.WanderingTraderEntity;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.thrown.EggEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.entity.projectile.thrown.ExperienceBottleEntity;
import net.minecraft.entity.projectile.thrown.PotionEntity;
import net.minecraft.entity.projectile.thrown.SnowballEntity;
import net.minecraft.entity.projectile.thrown.ThrownEntity;
import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.FurnaceMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.entity.vehicle.TntMinecartEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

@Mixin(Entity.class)
public class MixinEntity implements IMixinCommandOutput, IMixinEntity {

    public CraftEntity bukkit;
    public org.bukkit.projectiles.ProjectileSource projectileSource;
    private ArrayList<org.bukkit.inventory.ItemStack> drops = new ArrayList<org.bukkit.inventory.ItemStack>();
    private boolean forceDrops;

    @Override
    public ArrayList<org.bukkit.inventory.ItemStack> cardboard_getDrops() {
        return drops;
    }

    @Override
    public void cardboard_setDrops(ArrayList<org.bukkit.inventory.ItemStack> drops) {
        this.drops = drops;
    }

    @Override
    public boolean cardboard_getForceDrops() {return forceDrops;}

    @Override
    public void cardboard_setForceDrops(boolean forceDrops) {
        this.forceDrops = forceDrops;
    }

    @Shadow
    public World world;

    public MixinEntity() {
        this.bukkit = getEntity(CraftServer.INSTANCE, (Entity)(Object)this);
    }

    public void sendSystemMessage(Text message) {
        // TODO: 1.19
    	
    	((Entity) (Object) this).sendMessage(message);
    	//((Entity) (Object) this).sendSystemMessage(message, UUID.randomUUID());
    }

    public boolean valid = false;
    public Location origin_bukkit;

    @Override
    public Location getOriginBF() {
        return origin_bukkit;
    }

    @Override
    public void setOriginBF(Location loc) {
        this.origin_bukkit = loc;
    }

    @Override
    public boolean isValidBF() {
        return valid;
    }

    @Override
    public void setValid(boolean b) {
        this.valid = b;
    }

    @Inject(at = @At(value = "HEAD"), method = "tick()V")
    public void setBukkit(CallbackInfo callbackInfo) {
        if (null == bukkit) {
            this.bukkit = getEntity(CraftServer.INSTANCE, (Entity)(Object)this);
        }
    }

    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/world/World;spawnEntity(Lnet/minecraft/entity/Entity;)Z"), method = "dropStack(Lnet/minecraft/item/ItemStack;F)Lnet/minecraft/entity/ItemEntity;")
    public boolean dropStackEvent1(World world, Entity entity, ItemStack itemstack, float f) {
        if (itemstack.isEmpty())
            return false;

        boolean chick = (((Entity)(Object)this) instanceof ChickenEntity && itemstack.getItem() == Items.EGG);
        if (((Entity)(Object)this) instanceof net.minecraft.entity.LivingEntity && !this.forceDrops) {
            if (!chick) {
                this.drops.add(org.bukkit.craftbukkit.inventory.CraftItemStack.asBukkitCopy(itemstack));
                return false;
            }
        }
        ItemEntity entityitem = new ItemEntity(this.world, ((Entity) (Object) this).getX(), ((Entity) (Object) this).getY() + (double) f, ((Entity) (Object) this).getZ(), itemstack);

        entityitem.setToDefaultPickupDelay();

        EntityDropItemEvent event = new EntityDropItemEvent(this.getBukkitEntity(), (org.bukkit.entity.Item) ((IMixinEntity)entityitem).getBukkitEntity());
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled())
            return false;
        return this.world.spawnEntity(entityitem);
    }


    @Override
    public CommandSender getBukkitSender(ServerCommandSource serverCommandSource) {
        return bukkit;
    }

    @Override
    public CraftEntity getBukkitEntity() {
        if (null == bukkit) {
            this.bukkit = getEntity(CraftServer.INSTANCE, (Entity)(Object)this);
        }
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
                if (entity instanceof ServerPlayerEntity) { return new PlayerImpl((ServerPlayerEntity) entity); }
                else { return new CraftHumanEntity((PlayerEntity) entity); }
            }
            // Water Animals
            else if (entity instanceof WaterCreatureEntity) {
                if (entity instanceof SquidEntity) { return new CardboardSquid(server, (SquidEntity) entity); }
                else if (entity instanceof FishEntity) {
                    if (entity instanceof CodEntity) { return new CardboardFishCod(server, (CodEntity) entity); }
                    else if (entity instanceof PufferfishEntity) { return new CardboardFishPufferfish(server, (PufferfishEntity) entity); }
                    else if (entity instanceof SalmonEntity) { return new CardboardFishSalmon(server, (SalmonEntity) entity); }
                    else if (entity instanceof TropicalFishEntity) { return new CardboardFishTropical(server, (TropicalFishEntity) entity); }
                    else { return new CardboardFish(server, (FishEntity) entity); }
                }
                else if (entity instanceof DolphinEntity) { return new CardboardDolphin(server, (DolphinEntity) entity); }
                else { return new CardboardWaterMob(server, (WaterCreatureEntity) entity); }
            }
            else if (entity instanceof PathAwareEntity) {
                // Animals
                if (entity instanceof AnimalEntity) {
                    if (entity instanceof ChickenEntity) { return new ChickenImpl(server, (ChickenEntity) entity); }
                    else if (entity instanceof CowEntity) {
                        if (entity instanceof MooshroomEntity) { return new MushroomImpl(server, (MooshroomEntity) entity); }
                        else { return new CowImpl(server, (CowEntity) entity); }
                    }
                    else if (entity instanceof PigEntity) { return new CardboardPig(server, (PigEntity) entity); }
                    else if (entity instanceof TameableEntity) {
                        if (entity instanceof WolfEntity) { return new WolfImpl(server, (WolfEntity) entity); }
                        else if (entity instanceof CatEntity) { return new CardboardCat(server, (CatEntity) entity); }
                        else if (entity instanceof ParrotEntity) { return new ParrotImpl(server, (ParrotEntity) entity); }
                    }
                    //else if (entity instanceof SheepEntity) { return new CraftSheep(server, (SheepEntity) entity); }
                    else if (entity instanceof AbstractHorseEntity) {
                        if (entity instanceof AbstractDonkeyEntity){
                            if (entity instanceof DonkeyEntity) { return new CardboardDonkey(server, (DonkeyEntity) entity); }
                            else if (entity instanceof MuleEntity) { return new CardboardMule(server, (MuleEntity) entity); }
                            //else if (entity instanceof TraderLlamaEntity) { return new CardboardTraderLlama(server, (TraderLlamaEntity) entity); }
                            else if (entity instanceof LlamaEntity) { return new CardboardLlama(server, (LlamaEntity) entity); }
                        } else if (entity instanceof HorseEntity) { return new CardboardHorse(server, (HorseEntity) entity); }
                        //else if (entity instanceof SkeletonHorseEntity) { return new CraftSkeletonHorse(server, (SkeletonHorseEntity) entity); }
                        //else if (entity instanceof ZombieHorseEntity) { return new CraftZombieHorse(server, (ZombieHorseEntity) entity); }
                    }
                    //else if (entity instanceof RabbitEntity) { return new CraftRabbit(server, (RabbitEntity) entity); }
                    else if (entity instanceof PolarBearEntity) { return new PolarBearImpl(server, (PolarBearEntity) entity); }
                    else if (entity instanceof TurtleEntity) { return new TurtleImpl(server, (TurtleEntity) entity); }
                    else if (entity instanceof OcelotEntity) { return new OcelotImpl(server, (OcelotEntity) entity); }
                    else if (entity instanceof PandaEntity) { return new CardboardPanda(server, (PandaEntity) entity); }
                    //else if (entity instanceof FoxEntity) { return new CraftFox(server, (FoxEntity) entity); }
                    //else if (entity instanceof BeeEntity) { return new CraftBee(server, (BeeEntity) entity); }
                    //else if (entity instanceof HoglinEntity) { return new CraftHoglin(server, (HoglinEntity) entity); }
                    //else if (entity instanceof StriderEntity) { return new CraftStrider(server, (StriderEntity) entity); }
                    else  { return new AnimalsImpl(server, (AnimalEntity) entity); }
                }
                // Monsters
                else if (entity instanceof HostileEntity) {
                    if (entity instanceof ZombieEntity) {
                        if (entity instanceof ZombifiedPiglinEntity) { return new CardboardPigZombie(server, (ZombifiedPiglinEntity) entity); }
                        else if (entity instanceof HuskEntity) { return new CardboardHusk(server, (HuskEntity) entity); }
                        else if (entity instanceof ZombieVillagerEntity) { return new VillagerZombieImpl(server, (ZombieVillagerEntity) entity); }
                        else if (entity instanceof DrownedEntity) { return new CardboardDrowned(server, (DrownedEntity) entity); }
                        else { return new ZombieImpl(server, (ZombieEntity) entity); }
                    }
                    else if (entity instanceof CreeperEntity) { return new CreeperImpl(server, (CreeperEntity) entity); }
                    else if (entity instanceof EndermanEntity) { return new EndermanImpl(server, (EndermanEntity) entity); }
                    else if (entity instanceof SilverfishEntity) { return new CardboardSilverfish(server, (SilverfishEntity) entity); }
                    else if (entity instanceof GiantEntity) { return new CardboardGiant(server, (GiantEntity) entity); }
                    else if (entity instanceof AbstractSkeletonEntity) {
                        if (entity instanceof StrayEntity) { return new StrayImpl(server, (StrayEntity) entity); }
                        else if (entity instanceof WitherSkeletonEntity) { return new WitherSkeletonImpl(server, (WitherSkeletonEntity) entity); }
                        else { return new SkeletonImpl(server, (AbstractSkeletonEntity) entity); }
                    }
                    else if (entity instanceof BlazeEntity) { return new CardboardBlaze(server, (BlazeEntity) entity); }
                    else if (entity instanceof WitchEntity) { return new CardboardWitch(server, (WitchEntity) entity); }
                    else if (entity instanceof WitherEntity) { return new CardboardWither(server, (WitherEntity) entity); }
                    else if (entity instanceof SpiderEntity) {
                        if (entity instanceof CaveSpiderEntity) { return new CardboardCaveSpider(server, (CaveSpiderEntity) entity); }
                        else { return new SpiderImpl(server, (SpiderEntity) entity); }
                    }
                    else if (entity instanceof EndermiteEntity) { return new EndermiteImpl(server, (EndermiteEntity) entity); }
                    else if (entity instanceof GuardianEntity) {
                        if (entity instanceof ElderGuardianEntity) { return new CardboardGuardianElder(server, (ElderGuardianEntity) entity); }
                        else { return new CardboardGuardian(server, (GuardianEntity) entity); }
                    }
                    else if (entity instanceof VexEntity) { return new CardboardVex(server, (VexEntity) entity); }
                    else if (entity instanceof IllagerEntity) {
                        if (entity instanceof SpellcastingIllagerEntity) {;
                            if (entity instanceof EvokerEntity) { return new CardboardEvoker(server, (EvokerEntity) entity); }
                            else if (entity instanceof IllusionerEntity) { return new CardboardIllusioner(server, (IllusionerEntity) entity); }
                            else {  return new CardboardSpellcaster(server, (SpellcastingIllagerEntity) entity); }
                        }
                        else if (entity instanceof VindicatorEntity) { return new CardboardVindicator(server, (VindicatorEntity) entity); }
                        else if (entity instanceof PillagerEntity) { return new CardboardPillager(server, (PillagerEntity) entity); }
                        else { return new CardboardIllager(server, (IllagerEntity) entity); }
                    }
                    //else if (entity instanceof RavagerEntity) { return new CraftRavager(server, (RavagerEntity) entity); }
                    //else if (entity instanceof AbstractPiglinEntity) {
                        //if (entity instanceof PiglinEntity) return new CraftPiglin(server, (PiglinEntity) entity);
                        //else if (entity instanceof PiglinBruteEntity) { return new CraftPiglinBrute(server, (PiglinBruteEntity) entity); }
                        //else { return new CraftPiglinAbstract(server, (AbstractPiglinEntity) entity); }
                    //}
                    //else if (entity instanceof ZoglinEntity) { return new CraftZoglin(server, (ZoglinEntity) entity); }

                    else  { return new MonsterImpl(server, (HostileEntity) entity); }
                }
                else if (entity instanceof GolemEntity) {
                    if (entity instanceof SnowGolemEntity) { return new CardboardSnowman(server, (SnowGolemEntity) entity); }
                    else if (entity instanceof IronGolemEntity) { return new CardboardIronGolem(server, (IronGolemEntity) entity); }
                    else if (entity instanceof ShulkerEntity) { return new CardboardShulker(server, (ShulkerEntity) entity); }
                }
                else if (entity instanceof MerchantEntity) {
                    if (entity instanceof VillagerEntity) { return new VillagerImpl(server, (VillagerEntity) entity); }
                    else if (entity instanceof WanderingTraderEntity) { return new WanderingTraderImpl(server, (WanderingTraderEntity) entity); }
                    else { return new AbstractVillagerImpl(server, (MerchantEntity) entity); }
                }
                else { return new CreatureImpl(server, (PathAwareEntity) entity); }
            }
            // Slimes are a special (and broken) case
            else if (entity instanceof SlimeEntity) {
                if (entity instanceof MagmaCubeEntity) { return new CardboardMagmaCube(server, (MagmaCubeEntity) entity); }
                else { return new SlimeImpl(server, (SlimeEntity) entity); }
            }
            // Flying
            else if (entity instanceof FlyingEntity) {
                if (entity instanceof GhastEntity) { return new CardboardGhast(server, (GhastEntity) entity); }
                else if (entity instanceof PhantomEntity) { return new CardboardPhantom(server, (PhantomEntity) entity); }
                else { return new CardboardFlying(server, (FlyingEntity) entity); }
            }
            else if (entity instanceof EnderDragonEntity) {
                return new CardboardEnderdragon(server, (EnderDragonEntity) entity);
            }
            // Ambient
            else if (entity instanceof AmbientEntity) {
                if (entity instanceof BatEntity) { return new CardboardBat(server, (BatEntity) entity); }
                else { return new CardboardAmbient(server, (AmbientEntity) entity); }
            }
            else if (entity instanceof ArmorStandEntity) { return new ArmorStandImpl(server, (ArmorStandEntity) entity); }
            else  { return new LivingEntityImpl(server, (LivingEntity) entity); }
        }
        else if (entity instanceof EnderDragonPart) {
            EnderDragonPart part = (EnderDragonPart) entity;
            if (part.owner instanceof EnderDragonEntity) { return new CardboardDragonPart(server, (EnderDragonPart) entity); }
            else { return new CardboardComplexPart(server, (EnderDragonPart) entity); }
        }
        else if (entity instanceof ExperienceOrbEntity) { return new ExperienceOrbImpl(server, (ExperienceOrbEntity) entity); }
        //else if (entity instanceof ArrowEntity) { return new CraftTippedArrow(server, (ArrowEntity) entity); }
        //else if (entity instanceof SpectralArrowEntity) { return new CraftSpectralArrow(server, (SpectralArrowEntity) entity); }
        else if (entity instanceof PersistentProjectileEntity) {
            if (entity instanceof TridentEntity) { return new TridentImpl(server, (TridentEntity) entity); }
            else { return new ArrowImpl(server, (PersistentProjectileEntity) entity); }
        }
        //else if (entity instanceof BoatEntity) { return new CraftBoat(server, (BoatEntity) entity); }
        else if (entity instanceof ThrownEntity) {
            if (entity instanceof EggEntity) { return new EggImpl(server, (EggEntity) entity); }
            else if (entity instanceof SnowballEntity) { return new SnowballImpl(server, (SnowballEntity) entity); }
            else if (entity instanceof PotionEntity) { return new CardboardThrownPotion(server, (PotionEntity) entity); }
            else if (entity instanceof EnderPearlEntity) { return new CardboardEnderPearl(server, (EnderPearlEntity) entity); }
            else if (entity instanceof ExperienceBottleEntity) { return new CardboardThrownExpBottle(server, (ExperienceBottleEntity) entity); }
            
        }
        else if (entity instanceof FallingBlockEntity) { return new FallingBlockImpl(server, (FallingBlockEntity) entity); }
        else if (entity instanceof ExplosiveProjectileEntity) {
            //if (entity instanceof SmallFireballEntity) { return new CraftSmallFireball(server, (SmallFireballEntity) entity); }
            //else if (entity instanceof FireballEntity) { return new CraftLargeFireball(server, (FireballEntity) entity); }
           // else if (entity instanceof WitherSkullEntity) { return new CraftWitherSkull(server, (WitherSkullEntity) entity); }
           // else if (entity instanceof DragonFireballEntity) { return new CraftDragonFireball(server, (DragonFireballEntity) entity); }
            //else { return new CraftFireball(server, (ExplosiveProjectileEntity) entity); }
        }
        //else if (entity instanceof EyeOfEnderEntity) { return new CraftEnderSignal(server, (EyeOfEnderEntity) entity); }
        //else if (entity instanceof EndCrystalEntity) { return new CraftEnderCrystal(server, (EndCrystalEntity) entity); }
        else if (entity instanceof FishingBobberEntity) { return new CardboardFishHook(server, (FishingBobberEntity) entity); }
        else if (entity instanceof ItemEntity) { return new ItemEntityImpl(server, (ItemEntity) entity); }
        else if (entity instanceof LightningEntity) { return new LightningStrikeImpl(server, (LightningEntity) entity); }
        else if (entity instanceof AbstractMinecartEntity) {
            if (entity instanceof FurnaceMinecartEntity) { return new CardboardMinecartFurnace(server, (FurnaceMinecartEntity) entity); }
            else if (entity instanceof ChestMinecartEntity) { return new CardboardMinecartChest(server, (ChestMinecartEntity) entity); }
            else if (entity instanceof TntMinecartEntity) { return new CardboardTntCart(server, (TntMinecartEntity) entity); }
            //else if (entity instanceof HopperMinecartEntity) { return new CraftMinecartHopper(server, (HopperMinecartEntity) entity); }
            //else if (entity instanceof SpawnerMinecartEntity) { return new CraftMinecartMobSpawner(server, (SpawnerMinecartEntity) entity); }
            else if (entity instanceof MinecartEntity) { return new CardboardMinecartRideable(server, (MinecartEntity) entity); }
            //else if (entity instanceof CommandBlockMinecartEntity) { return new CraftMinecartCommand(server, (CommandBlockMinecartEntity) entity); }*/
            else return new CardboardMinecart(server, (AbstractMinecartEntity) entity);
        } else if (entity instanceof AbstractDecorationEntity) {
            //if (entity instanceof PaintingEntity) { return new CraftPainting(server, (PaintingEntity) entity); }
            //else if (entity instanceof ItemFrameEntity) { return new CraftItemFrame(server, (ItemFrameEntity) entity); }
            //else if (entity instanceof LeashKnotEntity) { return new CraftLeash(server, (LeashKnotEntity) entity); }
            //else { return new CraftHanging(server, (AbstractDecorationEntity) entity); }
            return new CardboardHanging(server, (AbstractDecorationEntity) entity);
        }
        else if (entity instanceof TntEntity) { return new TntImpl(server, (TntEntity) entity); }
        else if (entity instanceof FireworkRocketEntity) {return new CardboardFirework(server, (FireworkRocketEntity) entity); }
        //else if (entity instanceof ShulkerBulletEntity) { return new CraftShulkerBullet(server, (ShulkerBulletEntity) entity); }
        //else if (entity instanceof AreaEffectCloudEntity) { return new CraftAreaEffectCloud(server, (AreaEffectCloudEntity) entity); }
        //else if (entity instanceof EvokerFangsEntity) { return new CraftEvokerFangs(server, (EvokerFangsEntity) entity); }
        else if (entity instanceof LlamaSpitEntity) { return new CardboardLlamaSpit(server, (LlamaSpitEntity) entity); }
        // CHECKSTYLE:ON

        
        return (entity instanceof net.minecraft.entity.LivingEntity) ? new LivingEntityImpl(entity) : new UnknownEntity(entity); // TODO
        //throw new AssertionError("Unknown entity " + (entity == null ? null : entity.getClass()));
    }

    @Override
    public ProjectileSource getProjectileSourceBukkit() {
        return projectileSource;
    }

    @Inject(at = @At("HEAD"), method = "setPose(Lnet/minecraft/entity/EntityPose;)V", cancellable = true)
    public void setPoseBF(EntityPose entitypose, CallbackInfo ci) {
        if (entitypose == ((Entity)(Object)this).getPose()) {
            ci.cancel();
            return;
        }
        Pose b = Pose.STANDING;
        switch (entitypose) {
            case CROUCHING:
                b = Pose.SNEAKING;
                break;
            case DYING:
                b = Pose.DYING;
                break;
            case FALL_FLYING:
                b = Pose.FALL_FLYING;
                break;
            case LONG_JUMPING:
                // TODO 1.17ify
                break;
            case SLEEPING:
                b = Pose.SLEEPING;
                break;
            case SPIN_ATTACK:
                b = Pose.SPIN_ATTACK;
                break;
            case STANDING:
                b = Pose.STANDING;
                break;
            case SWIMMING:
                b = Pose.SWIMMING;
                break;
            default:
                break;  
        }
        Bukkit.getPluginManager().callEvent(new EntityPoseChangeEvent(this.getBukkitEntity(), b));
    }

    @Inject(at = @At("HEAD"), method = "setAir", cancellable = true)
    public void setAirBF(int i, CallbackInfo ci) {
        if (!valid) {
            ci.cancel();
            return;
        }

        EntityAirChangeEvent event = new EntityAirChangeEvent(this.getBukkitEntity(), i);
        event.getEntity().getServer().getPluginManager().callEvent(event);

        if (event.isCancelled()) {
            ci.cancel();
            return;
        }
        i = event.getAmount();
    }

    public void removeBF() {
        ((me.isaiah.common.cmixin.IMixinEntity)this).Iremove(IRemoveReason.DISCARDED);
    }

    @Shadow
    public void move(MovementType moveType, Vec3d vec3d) {
    }
    
    @Shadow
    private TeleportTarget getTeleportTarget(ServerWorld w) {
        return null;
    }

    /**
     * EntityCombustByBlockEvent
     * 
     * @author Arclight
     * @author Cardboard
     */
    @Redirect(method = "setOnFireFromLava", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;setOnFireFor(I)V"))
    public void arclight_setOnFireFromLava_bukkitEvent(Entity entity, int seconds) {
        if ((Object) this instanceof LivingEntity && ((Entity) (Object) this).fireTicks <= 0) {
            org.bukkit.block.Block damager = null;
            org.bukkit.entity.Entity damagee = this.getBukkitEntity();
            EntityCombustEvent combustEvent = new EntityCombustByBlockEvent(damager, damagee, 15);
            Bukkit.getPluginManager().callEvent(combustEvent);

            if (!combustEvent.isCancelled())
                ((Entity) (Object) this).setOnFireFor(combustEvent.getDuration());
        } else {
            // This will be called every single tick the entity is in lava, so don't throw an event
            ((Entity) (Object) this).setOnFireFor(15);
        }
    }

    @Inject(method = "addPassenger", at = @At(value = "INVOKE", target = "Lcom/google/common/collect/ImmutableList;isEmpty()Z"))
    private void fireCardboardEntityMountEvent(Entity passenger, CallbackInfo ci) {
        ActionResult result = CardboardEntityMountEvent.EVENT.invoker().interact(((Entity) (Object) this), passenger);

        if (result == ActionResult.FAIL) {
            ci.cancel();
        }
    }
}