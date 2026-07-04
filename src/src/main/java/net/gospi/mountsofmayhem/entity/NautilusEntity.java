package net.gospi.mountsofmayhem.entity;

import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.EventHooks;
import net.neoforged.neoforge.common.NeoForgeMod;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.TryFindWaterGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.FollowParentGoal;
import net.minecraft.world.entity.ai.goal.BreedGoal;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.util.Mth;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.core.registries.BuiltInRegistries;

import net.gospi.mountsofmayhem.procedures.NautilusOnTickProcedure;
import net.gospi.mountsofmayhem.procedures.NautilusOnDeathProcedure;
import net.gospi.mountsofmayhem.procedures.NautilusOnSpawnProcedure;
import net.gospi.mountsofmayhem.init.MountsOfMayhemModEntities;

import javax.annotation.Nullable;

public class NautilusEntity extends TamableAnimal {
	public final AnimationState animationState0 = new AnimationState();
	private Vec3 externalMotion = Vec3.ZERO;

	// Synched data keys for armor rendering (sync to client automatically)
	private static final EntityDataAccessor<Boolean> DATA_COPPER_ARMOR = SynchedEntityData.defineId(NautilusEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_IRON_ARMOR = SynchedEntityData.defineId(NautilusEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_GOLDEN_ARMOR = SynchedEntityData.defineId(NautilusEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_DIAMOND_ARMOR = SynchedEntityData.defineId(NautilusEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_NETHERITE_ARMOR = SynchedEntityData.defineId(NautilusEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_SADDLED = SynchedEntityData.defineId(NautilusEntity.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> DATA_DASH = SynchedEntityData.defineId(NautilusEntity.class, EntityDataSerializers.BOOLEAN);

	// Dash boost system
	private Vec3 dashBoost = Vec3.ZERO;
	private int dashTicks = 0;

	public NautilusEntity(EntityType<NautilusEntity> type, Level world) {
		super(type, world);
		xpReward = 3;
		setNoAi(false);
		this.setPathfindingMalus(PathType.WATER, 0);
		this.moveControl = new MoveControl(this) {
			@Override
			public void tick() {
				if (this.operation == MoveControl.Operation.MOVE_TO && !NautilusEntity.this.getNavigation().isDone()) {
					double dx = this.wantedX - NautilusEntity.this.getX();
					double dy = this.wantedY - NautilusEntity.this.getY();
					double dz = this.wantedZ - NautilusEntity.this.getZ();
					float f = (float) (Mth.atan2(dz, dx) * (double) (180 / Math.PI)) - 90;
					float f1 = (float) (this.speedModifier * NautilusEntity.this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
					NautilusEntity.this.setYRot(this.rotlerp(NautilusEntity.this.getYRot(), f, 10));
					NautilusEntity.this.yBodyRot = NautilusEntity.this.getYRot();
					NautilusEntity.this.yHeadRot = NautilusEntity.this.getYRot();
					if (NautilusEntity.this.isInWater()) {
						NautilusEntity.this.setSpeed((float) NautilusEntity.this.getAttribute(Attributes.MOVEMENT_SPEED).getValue());
						float f2 = -(float) (Mth.atan2(dy, (float) Math.sqrt(dx * dx + dz * dz)) * (180 / Math.PI));
						f2 = Mth.clamp(Mth.wrapDegrees(f2), -85, 85);
						NautilusEntity.this.setXRot(this.rotlerp(NautilusEntity.this.getXRot(), f2, 5));
						float f3 = Mth.cos(NautilusEntity.this.getXRot() * (float) (Math.PI / 180.0));
						NautilusEntity.this.setZza(f3 * f1);
						NautilusEntity.this.setYya((float) (f1 * dy));
					} else {
						NautilusEntity.this.setSpeed(f1 * 0.05F);
					}
				} else {
					NautilusEntity.this.setSpeed(0);
					NautilusEntity.this.setYya(0);
					NautilusEntity.this.setZza(0);
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
		this.goalSelector.addGoal(1, new RandomSwimmingGoal(this, 1, 40));
		this.targetSelector.addGoal(2, new HurtByTargetGoal(this).setAlertOthers());
		this.targetSelector.addGoal(3, new NearestAttackableTargetGoal(this, Pufferfish.class, false, true));
		this.goalSelector.addGoal(4, new FollowParentGoal(this, 0.8));
		this.goalSelector.addGoal(5, new BreedGoal(this, 1));
		this.goalSelector.addGoal(6, new TryFindWaterGoal(this));
		this.goalSelector.addGoal(7, new TemptGoal(this, 1, Ingredient.of(Items.PUFFERFISH), false));
	}

	@Override
	public boolean removeWhenFarAway(double distanceToClosestPlayer) {
		return false;
	}

	@Override
	public SoundEvent getAmbientSound() {
		return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("mounts_of_mayhem:entity.nautilus.ambient"));
	}

	@Override
	public SoundEvent getHurtSound(DamageSource ds) {
		return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("mounts_of_mayhem:entity.nautilus.hurt"));
	}

	@Override
	public SoundEvent getDeathSound() {
		return BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("mounts_of_mayhem:entity.nautilus.death"));
	}

	@Override
	public void die(DamageSource source) {
		super.die(source);
		NautilusOnDeathProcedure.execute(this.level(), this.getX(), this.getY(), this.getZ());
	}

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        // Clear fresh-spawn marker so it survives reloads without being filtered
        this.getPersistentData().remove("MountsOfMayhemFreshSpawn");
        // Sync armor from loaded NBT to entityData so spawn packet has correct values
        if (!this.level().isClientSide()) {
            this.syncArmorToClient();
        }
    }

	@Override
	public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingdata) {
		SpawnGroupData retval = super.finalizeSpawn(world, difficulty, reason, livingdata);
		NautilusOnSpawnProcedure.execute(this);
		if (!this.level().isClientSide()) {
			this.getPersistentData().putBoolean("MountsOfMayhemFreshSpawn", true);
			this.syncArmorToClient();
		}
		return retval;
	}

	@Override
	public InteractionResult mobInteract(Player sourceentity, InteractionHand hand) {
		ItemStack itemstack = sourceentity.getItemInHand(hand);
		InteractionResult retval = InteractionResult.sidedSuccess(this.level().isClientSide());
		Item item = itemstack.getItem();
		
		if (itemstack.getItem() instanceof SpawnEggItem) {
			retval = super.mobInteract(sourceentity, hand);
		} else if (this.level().isClientSide()) {
			retval = (this.isTame() && this.isOwnedBy(sourceentity) || this.isFood(itemstack)) ? InteractionResult.sidedSuccess(this.level().isClientSide()) : InteractionResult.PASS;
		} else {
			if (this.isTame()) {
				if (this.isOwnedBy(sourceentity)) {
					if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
						this.usePlayerItem(sourceentity, hand, itemstack);
						FoodProperties foodproperties = itemstack.getFoodProperties(this);
						float nutrition = foodproperties != null ? (float) foodproperties.nutrition() : 1;
						this.heal(nutrition);
						retval = InteractionResult.sidedSuccess(this.level().isClientSide());
					} else if (this.isFood(itemstack) && this.getHealth() < this.getMaxHealth()) {
						this.usePlayerItem(sourceentity, hand, itemstack);
						this.heal(4);
						retval = InteractionResult.sidedSuccess(this.level().isClientSide());
					} else {
						retval = super.mobInteract(sourceentity, hand);
					}
				}
			} else if (this.isFood(itemstack)) {
				this.usePlayerItem(sourceentity, hand, itemstack);
				if (this.random.nextInt(3) == 0 && !EventHooks.onAnimalTame(this, sourceentity)) {
					this.tame(sourceentity);
					this.level().broadcastEntityEvent(this, (byte) 7);
				} else {
					this.level().broadcastEntityEvent(this, (byte) 6);
				}
				this.setPersistenceRequired();
				retval = InteractionResult.sidedSuccess(this.level().isClientSide());
			} else {
				retval = super.mobInteract(sourceentity, hand);
				if (retval == InteractionResult.SUCCESS || retval == InteractionResult.CONSUME)
					this.setPersistenceRequired();
			}
		}
		
		return retval;
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		super.defineSynchedData(builder);
		builder.define(DATA_COPPER_ARMOR, false);
		builder.define(DATA_IRON_ARMOR, false);
		builder.define(DATA_GOLDEN_ARMOR, false);
		builder.define(DATA_DIAMOND_ARMOR, false);
		builder.define(DATA_NETHERITE_ARMOR, false);
		builder.define(DATA_SADDLED, false);
		builder.define(DATA_DASH, false);
	}

	@Override
	public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
		super.onSyncedDataUpdated(key);
		// Sync from synched data back to persistent data when received on client
		if (this.level().isClientSide()) {
			if (DATA_COPPER_ARMOR.equals(key)) {
				this.getPersistentData().putBoolean("copperArmor", this.entityData.get(DATA_COPPER_ARMOR));
			} else if (DATA_IRON_ARMOR.equals(key)) {
				this.getPersistentData().putBoolean("ironArmor", this.entityData.get(DATA_IRON_ARMOR));
			} else if (DATA_GOLDEN_ARMOR.equals(key)) {
				this.getPersistentData().putBoolean("goldenArmor", this.entityData.get(DATA_GOLDEN_ARMOR));
			} else if (DATA_DIAMOND_ARMOR.equals(key)) {
				this.getPersistentData().putBoolean("diamondArmor", this.entityData.get(DATA_DIAMOND_ARMOR));
			} else if (DATA_NETHERITE_ARMOR.equals(key)) {
				this.getPersistentData().putBoolean("netheriteArmor", this.entityData.get(DATA_NETHERITE_ARMOR));
			} else if (DATA_SADDLED.equals(key)) {
				this.getPersistentData().putBoolean("isSaddled", this.entityData.get(DATA_SADDLED));
			} else if (DATA_DASH.equals(key)) {
				this.getPersistentData().putBoolean("dash", this.entityData.get(DATA_DASH));
			}
		}
	}

	// Sync armor data from persistent NBT to synched data (for server-side changes)
	public void syncArmorToClient() {
		if (!this.level().isClientSide()) {
			this.entityData.set(DATA_COPPER_ARMOR, this.getPersistentData().getBoolean("copperArmor"));
			this.entityData.set(DATA_IRON_ARMOR, this.getPersistentData().getBoolean("ironArmor"));
			this.entityData.set(DATA_GOLDEN_ARMOR, this.getPersistentData().getBoolean("goldenArmor"));
			this.entityData.set(DATA_DIAMOND_ARMOR, this.getPersistentData().getBoolean("diamondArmor"));
			this.entityData.set(DATA_NETHERITE_ARMOR, this.getPersistentData().getBoolean("netheriteArmor"));
			this.entityData.set(DATA_SADDLED, this.getPersistentData().getBoolean("isSaddled"));
			this.entityData.set(DATA_DASH, this.getPersistentData().getBoolean("dash"));
		}
	}

	@Override
	public void tick() {
		super.tick();
		
		// Apply external motion if present (after travel)
		if (!this.externalMotion.equals(Vec3.ZERO)) {
			this.setDeltaMovement(this.getDeltaMovement().add(this.externalMotion));
			this.externalMotion = Vec3.ZERO;
		}
		
		// Dive tendency: push downward when idle above mid-depth
		if (this.isInWater() && !this.isVehicle() && this.level() != null) {
			int seaLevel = this.level().getSeaLevel();
			if (this.getY() > seaLevel - 12 && this.random.nextInt(20) == 0) {
				this.setDeltaMovement(this.getDeltaMovement().add(0, -0.04, 0));
			}
		}
		
		// Sync armor NBT to synched data each tick for reliability
		if (!this.level().isClientSide()) {
			this.syncArmorToClient();
		}
		
		if (this.level().isClientSide()) {
			this.animationState0.animateWhen(true, this.tickCount);
		}
	}

	// Method for adding external movement from procedures
	public void addExternalMotion(Vec3 motion) {
		this.externalMotion = this.externalMotion.add(motion);
	}

	// Sync the dash cooldown flag to both persistent data and entity data immediately
	public void setDashCooldown(boolean active) {
		this.getPersistentData().putBoolean("dash", active);
		if (!this.level().isClientSide()) {
			this.entityData.set(DATA_DASH, active);
		}
	}

	// Dash boost system — applies forward impulse each tick for several ticks
	public void triggerDash(int dirX, int dirY, int dirZ, int durationTicks) {
		this.dashBoost = new Vec3(dirX, dirY, dirZ);
		this.dashTicks = durationTicks;
		this.setDashCooldown(true);
	}

	@Override
	public void baseTick() {
		super.baseTick();
		NautilusOnTickProcedure.execute(this);
	}

	@Override
	public AgeableMob getBreedOffspring(ServerLevel serverWorld, AgeableMob ageable) {
		NautilusEntity retval = MountsOfMayhemModEntities.NAUTILUS.get().create(serverWorld);
		retval.finalizeSpawn(serverWorld, serverWorld.getCurrentDifficultyAt(retval.blockPosition()), MobSpawnType.BREEDING, null);
		return retval;
	}

	@Override
	public boolean isFood(ItemStack stack) {
		return Ingredient.of(new ItemStack(Items.PUFFERFISH), new ItemStack(Items.PUFFERFISH_BUCKET)).test(stack);
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

	@Override
	public void travel(Vec3 dir) {
		if (this.isVehicle()) {
			LivingEntity passenger = this.getControllingPassenger();
			if (passenger != null) {
				this.setYRot(passenger.getYRot());
				this.yRotO = this.getYRot();
				this.setXRot(passenger.getXRot() * 0.5F);
				this.setRot(this.getYRot(), this.getXRot());
				this.yBodyRot = this.getYRot();
				this.yHeadRot = this.getYRot();
				
				float forward = passenger.zza;
				float strafe = passenger.xxa;
				
				// Water movement
				if (this.isInWater()) {
					// Sprint when moving forward (can't detect sprint key
					// while mounted — Minecraft blocks it in Player.aiStep)
					this.setSprinting(Math.abs(forward) > 0.01f);
					float baseSpeed = (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.4f;
					if (this.isSprinting()) {
						baseSpeed *= 1.3f;
					}
					
					// Get player head pitch (-90 to 90 degrees)
					float pitch = passenger.getXRot();
					float pitchRad = pitch * ((float) Math.PI / 180F);
					float yawRad = passenger.getYRot() * ((float) Math.PI / 180F);
					
					// Vertical look factor (0 = looking straight, 1 = looking up/down)
					float verticalFactor = Math.abs(pitch) / 90.0f;
					
					// Horizontal speed decreases when looking up/down
					float horizontalSpeed = baseSpeed * (1.0f - verticalFactor * 0.7f);
					float verticalSpeed = baseSpeed * verticalFactor * 1.5f;
					
					// Forward/backward direction
					float moveX = -Mth.sin(yawRad);
					float moveZ = Mth.cos(yawRad);
					
					// Vertical direction
					float verticalDirection = -Mth.sin(pitchRad);
					
					// Combine movement
					double desiredX = 0;
					double desiredY = 0;
					double desiredZ = 0;
					
					if (forward != 0) {
						// Horizontal movement with speed reduction when looking up/down
						desiredX += moveX * forward * horizontalSpeed;
						desiredZ += moveZ * forward * horizontalSpeed;
						// Vertical movement
						desiredY += verticalDirection * Math.abs(forward) * verticalSpeed;
					}
					
					// Strafe movement (direction FIXED)
					if (strafe != 0) {
						// Correct strafe direction
						// A (strafe = -1) = left, D (strafe = 1) = right
						float strafeX = -Mth.sin(yawRad - (float) Math.PI / 2); // FIXED: minus instead of plus
						float strafeZ = Mth.cos(yawRad - (float) Math.PI / 2);  // FIXED: minus instead of plus
						desiredX += strafeX * strafe * horizontalSpeed;
						desiredZ += strafeZ * strafe * horizontalSpeed;
					}
					
					// Prevent surfacing during normal movement (only dash can breach)
					if (this.dashTicks <= 0 && desiredY > 0 && this.level().getFluidState(this.blockPosition().above()).isEmpty()) {
						desiredY = 0;
					}
					
					// Apply movement directly without smoothing
					this.setDeltaMovement(desiredX, desiredY, desiredZ);
				}
				
				// Dash boost while submerged (water only)
				if (this.isInWater() && this.dashTicks > 0 && !this.dashBoost.equals(Vec3.ZERO)) {
					Vec3 current = this.getDeltaMovement();
					this.setDeltaMovement(current.add(this.dashBoost));
					this.dashTicks--;
					if (this.dashTicks <= 0) {
						this.dashBoost = Vec3.ZERO;
					}
				} else if (!this.isInWater() && this.dashTicks > 0) {
					// Cancel dash when leaving water — prevents bounce on re-entry
					this.dashTicks = 0;
					this.dashBoost = Vec3.ZERO;
				}
				
				super.travel(new Vec3(strafe, 0, forward));
				this.tryCheckInsideBlocks();
				return;
			}
		}
		super.travel(dir);
	}
	
	@Nullable
	@Override
	public LivingEntity getControllingPassenger() {
		Entity passenger = this.getFirstPassenger();
		if (passenger instanceof LivingEntity living) {
			return living;
		}
		return null;
	}

	public static void init(RegisterSpawnPlacementsEvent event) {
		event.register(MountsOfMayhemModEntities.NAUTILUS.get(), SpawnPlacementTypes.IN_WATER, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
				(entityType, world, reason, pos, random) -> world.getFluidState(pos).isSource(), RegisterSpawnPlacementsEvent.Operation.REPLACE);
	}

	public static AttributeSupplier.Builder createAttributes() {
		AttributeSupplier.Builder builder = Mob.createMobAttributes();
		builder = builder.add(Attributes.MOVEMENT_SPEED, 1.0);
		builder = builder.add(Attributes.MAX_HEALTH, 20);
		builder = builder.add(Attributes.ARMOR, 0);
		builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
		builder = builder.add(Attributes.FOLLOW_RANGE, 16);
		builder = builder.add(Attributes.STEP_HEIGHT, 0.6);
		builder = builder.add(Attributes.KNOCKBACK_RESISTANCE, 0.1);
		builder = builder.add(Attributes.ATTACK_KNOCKBACK, 0.6);
		builder = builder.add(NeoForgeMod.SWIM_SPEED, 4.0);
		return builder;
	}
}