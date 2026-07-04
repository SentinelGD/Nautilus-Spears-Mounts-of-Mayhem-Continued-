package net.gospi.mountsofmayhem.entity;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.common.NeoForgeMod;

import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.Difficulty;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;

import net.gospi.mountsofmayhem.init.MountsOfMayhemModEntities;

public class ZombieNautilusEntity extends Monster {
	public final AnimationState animationState0 = new AnimationState();

	public ZombieNautilusEntity(EntityType<ZombieNautilusEntity> type, Level world) {
		super(type, world);
		xpReward = 3;
		setNoAi(false);
		setPersistenceRequired();
		this.setPathfindingMalus(PathType.WATER, 0);
		this.moveControl = new MoveControl(this) {
			@Override
			public void tick() {
				if (ZombieNautilusEntity.this.isInWater())
					ZombieNautilusEntity.this.setDeltaMovement(ZombieNautilusEntity.this.getDeltaMovement().add(0, 0.005, 0));
				if (this.operation == MoveControl.Operation.MOVE_TO && !ZombieNautilusEntity.this.getNavigation().isDone()) {
					double dx = this.wantedX - ZombieNautilusEntity.this.getX();
					double dy = this.wantedY - ZombieNautilusEntity.this.getY();
					double dz = this.wantedZ - ZombieNautilusEntity.this.getZ();
					float f = (float) (Mth.atan2(dz, dx) * (double) (180 / Math.PI)) - 90;
					float f1 = (float) (this.speedModifier * ZombieNautilusEntity.this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
					ZombieNautilusEntity.this.setYRot(this.rotlerp(ZombieNautilusEntity.this.getYRot(), f, 10));
					ZombieNautilusEntity.this.yBodyRot = ZombieNautilusEntity.this.getYRot();
					ZombieNautilusEntity.this.yHeadRot = ZombieNautilusEntity.this.getYRot();
					if (ZombieNautilusEntity.this.isInWater()) {
						ZombieNautilusEntity.this.setSpeed((float) ZombieNautilusEntity.this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
						float f2 = -(float) (Mth.atan2(dy, (float) Math.sqrt(dx * dx + dz * dz)) * (180 / Math.PI));
						f2 = Mth.clamp(Mth.wrapDegrees(f2), -85, 85);
						ZombieNautilusEntity.this.setXRot(this.rotlerp(ZombieNautilusEntity.this.getXRot(), f2, 5));
						float f3 = Mth.cos(ZombieNautilusEntity.this.getXRot() * (float) (Math.PI / 180.0));
						ZombieNautilusEntity.this.setZza(f3 * f1);
						ZombieNautilusEntity.this.setYya((float) (f1 * dy));
					} else {
						ZombieNautilusEntity.this.setSpeed(f1 * 0.05F);
					}
				} else {
					ZombieNautilusEntity.this.setSpeed(0);
					ZombieNautilusEntity.this.setYya(0);
					ZombieNautilusEntity.this.setZza(0);
				}
			}
		};
	}

	@Override
	protected PathNavigation createNavigation(Level world) {
		return new WaterBoundPathNavigation(this, world);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.goalSelector.addGoal(1, new MeleeAttackGoal(this, 1.2, false) {
			@Override
			protected boolean canPerformAttack(LivingEntity entity) {
				return this.isTimeToAttack() && this.mob.distanceToSqr(entity) < (this.mob.getBbWidth() * this.mob.getBbWidth() + entity.getBbWidth()) && this.mob.getSensing().hasLineOfSight(entity);
			}
		});
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this));
		this.goalSelector.addGoal(3, new RandomSwimmingGoal(this, 1, 40));
		this.targetSelector.addGoal(4, new NearestAttackableTargetGoal(this, Player.class, false, false));
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	protected void dropCustomDeathLoot(ServerLevel serverLevel, DamageSource source, boolean recentlyHitIn) {
		super.dropCustomDeathLoot(serverLevel, source, recentlyHitIn);
		this.spawnAtLocation(new ItemStack(Items.NAUTILUS_SHELL));
	}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.cod.hurt"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("entity.cod.death"));
	}

	@Override
	public boolean hurt(DamageSource damagesource, float amount) {
		if (damagesource.is(DamageTypes.WITHER) || damagesource.is(DamageTypes.WITHER_SKULL))
			return false;
		return super.hurt(damagesource, amount);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.level().isClientSide()) {
			this.animationState0.animateWhen(true, this.tickCount);
		}
		
		// Предотвращаем деспаун если есть наездник
		if (!this.getPassengers().isEmpty()) {
			this.setPersistenceRequired();
		}
	}

	@Override
	public boolean checkSpawnObstruction(LevelReader world) {
		return world.isUnobstructed(this);
	}

	@Override
	public boolean canDrownInFluidType(FluidType type) {
		return false;
	}

	@Override
	public boolean isPushedByFluid() {
		return false;
	}

	// Исправленная логика спавна для водных существ
	public static boolean canSpawn(EntityType<ZombieNautilusEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, net.minecraft.util.RandomSource random) {
		int seaLevel = level.getSeaLevel();
		// Спавним только под водой и на достаточной глубине
		return pos.getY() < seaLevel - 2 && 
			   level.getFluidState(pos).isSource() && 
			   level.getDifficulty() != Difficulty.PEACEFUL &&
			   Monster.isDarkEnoughToSpawn(level, pos, random) && 
			   Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
	}

	public static void init(RegisterSpawnPlacementsEvent event) {
		// Используем IN_WATER для водных существ вместо ON_GROUND
		event.register(MountsOfMayhemModEntities.ZOMBIE_NAUTILUS.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				ZombieNautilusEntity::canSpawn,
				RegisterSpawnPlacementsEvent.Operation.REPLACE);
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 1);
		builder = builder.add(Attributes.MAX_HEALTH, 12);
		builder = builder.add(Attributes.ARMOR, 0);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 5);
		builder = builder.add(Attributes.FOLLOW_RANGE, 8);
		builder = builder.add(Attributes.STEP_HEIGHT, 1);
		builder = builder.add(Attributes.ATTACK_KNOCKBACK, 1);
		builder = builder.add(NeoForgeMod.SWIM_SPEED, 2);
		return builder;
	}
}