package net.gospi.mountsofmayhem.entity;

import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.EventHooks;

import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.SpawnPlacementTypes;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.EquipmentSlot;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.BlockPos;

import net.gospi.mountsofmayhem.config.MountsOfMayhemModSpawnConfig;
import net.gospi.mountsofmayhem.init.MountsOfMayhemModEntities;
import net.gospi.mountsofmayhem.init.MountsOfMayhemModItems;

import javax.annotation.Nullable;
import java.util.UUID;

public class CamelHuskEntity extends Camel {

    private static final EntityDataAccessor<Boolean> DATA_CONVERTING_ID = SynchedEntityData.defineId(CamelHuskEntity.class, EntityDataSerializers.BOOLEAN);

    private int conversionTime;
    private UUID conversionStarter;

    public CamelHuskEntity(EntityType<? extends Camel> type, Level world) {
        super(type, world);
        xpReward = 5;
        setNoAi(false);
        this.setPathfindingMalus(PathType.WATER, 0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CONVERTING_ID, false);
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ConversionTime", this.isConverting() ? this.conversionTime : -1);
        if (this.conversionStarter != null) {
            tag.putUUID("ConversionPlayer", this.conversionStarter);
        }
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        // Clear fresh-spawn marker so it survives reloads without being filtered
        this.getPersistentData().remove("MountsOfMayhemFreshSpawn");
        if (tag.contains("ConversionTime", 99) && tag.getInt("ConversionTime") > -1) {
            this.startConverting(tag.hasUUID("ConversionPlayer") ? tag.getUUID("ConversionPlayer") : null, tag.getInt("ConversionTime"));
        }
    }

    @Override
    public boolean isBaby() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return true;
    }

    @Override
    public boolean isSunBurnTick() {
        return false;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.is(net.minecraft.world.damagesource.DamageTypes.WITHER) || source.is(net.minecraft.world.damagesource.DamageTypes.WITHER_SKULL))
            return false;
        return super.hurt(source, amount);
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide() && this.isAlive() && this.isConverting()) {
            int progress = this.getConversionProgress();
            this.conversionTime -= progress;
            if (this.conversionTime <= 0 && EventHooks.canLivingConvert(this, EntityType.CAMEL, (timer) -> this.conversionTime = timer)) {
                this.finishConversion((ServerLevel) this.level());
            }
        }
        super.tick();
    }

    private int getConversionProgress() {
        if (this.random.nextFloat() < 0.01f) {
            int count = 0;
            BlockPos.MutableBlockPos blockpos = new BlockPos.MutableBlockPos();
            for (int x = (int) this.getX() - 4; x < (int) this.getX() + 4 && count < 14; x++) {
                for (int y = (int) this.getY() - 4; y < (int) this.getY() + 4 && count < 14; y++) {
                    for (int z = (int) this.getZ() - 4; z < (int) this.getZ() + 4 && count < 14; z++) {
                        var state = this.level().getBlockState(blockpos.set(x, y, z));
                        if (state.is(net.minecraft.world.level.block.Blocks.IRON_BARS) || state.getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
                            if (this.random.nextFloat() < 0.3f) {
                                ((ServerLevel) this.level()).sendParticles(ParticleTypes.HAPPY_VILLAGER,
                                        blockpos.getX() + 0.5, blockpos.getY() + 1.0, blockpos.getZ() + 0.5,
                                        1, 0, 0, 0, 0);
                            }
                            count++;
                        }
                    }
                }
            }
        }
        return 1;
    }

    @Override
    public SoundEvent getAmbientSound() {
        return SoundEvents.ZOMBIE_AMBIENT;
    }

    @Override
    public SoundEvent getHurtSound(DamageSource ds) {
        return SoundEvents.ZOMBIE_HURT;
    }

    @Override
    public SoundEvent getDeathSound() {
        return SoundEvents.ZOMBIE_DEATH;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor world, DifficultyInstance difficulty, MobSpawnType reason, @Nullable SpawnGroupData livingdata) {
        SpawnGroupData retval = super.finalizeSpawn(world, difficulty, reason, livingdata);
        if (!this.level().isClientSide()) {
            this.getPersistentData().putBoolean("MountsOfMayhemFreshSpawn", true);
            this.setAge(0);
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(32.0);
            this.setHealth(this.getMaxHealth());
            if ((reason == MobSpawnType.NATURAL || reason == MobSpawnType.CHUNK_GENERATION)
                    && MountsOfMayhemModSpawnConfig.CONFIG.camelHuskSpawnRiders.get()) {
                this.spawnRiders(world);
            }
        }
        return retval;
    }

    private void spawnRiders(ServerLevelAccessor world) {
        if (world instanceof ServerLevel serverLevel) {
            if (!this.getPassengers().isEmpty()) return;

            var husk = net.minecraft.world.entity.EntityType.HUSK.create(serverLevel);
            if (husk != null) {
                husk.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                husk.setPersistenceRequired();
                husk.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(MountsOfMayhemModItems.IRON_SPEAR.get()));
                husk.setDropChance(EquipmentSlot.MAINHAND, 0.05f);
                serverLevel.addFreshEntity(husk);
                if (!husk.startRiding(this, true)) husk.discard();
            }

            var parched = MountsOfMayhemModEntities.PARCHED.get().create(serverLevel);
            if (parched != null) {
                parched.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                parched.setPersistenceRequired();
                serverLevel.addFreshEntity(parched);
                if (!parched.startRiding(this, false)) parched.discard();
            }

            this.setPersistenceRequired();
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);

        if (itemstack.is(Items.RABBIT_FOOT) && this.getPassengers().isEmpty() && !this.isConverting()) {
            if (!this.level().isClientSide()) {
                this.startConverting(player.getUUID(), 3600 + this.random.nextInt(2401));
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }
            }
            return InteractionResult.sidedSuccess(this.level().isClientSide());
        }

        return InteractionResult.FAIL;
    }

    @Override
    public boolean canFallInLove() {
        return false;
    }

    @Override
    public void setInLove(@Nullable Player player) {
    }

    private void startConverting(@Nullable UUID uuid, int conversionTimeIn) {
        this.conversionStarter = uuid;
        this.conversionTime = conversionTimeIn;
        this.getEntityData().set(DATA_CONVERTING_ID, true);
        this.level().broadcastEntityEvent(this, (byte) 16);
    }

    private void finishConversion(ServerLevel level) {
        Camel camel = this.convertTo(EntityType.CAMEL, false);
        if (camel != null) {
            camel.setAge(0);
            if (this.hasCustomName()) {
                camel.setCustomName(this.getCustomName());
            }
            camel.setHealth(camel.getMaxHealth());
            level.broadcastEntityEvent(camel, (byte) 16);
        }
    }

    public boolean isConverting() {
        return this.getEntityData().get(DATA_CONVERTING_ID);
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == 16) {
            if (!this.isSilent()) {
                this.level().playLocalSound(this.getX(), this.getEyeY(), this.getZ(),
                        SoundEvents.ZOMBIE_VILLAGER_CURE, this.getSoundSource(),
                        1.0f + this.random.nextFloat(),
                        this.random.nextFloat() * 0.7f + 0.3f,
                        false);
            }
        } else {
            super.handleEntityEvent(id);
        }
    }

    @Override
    public boolean checkSpawnObstruction(LevelReader world) {
        return world.isUnobstructed(this);
    }

    @Override
    public boolean canBreed() {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    public static boolean canSpawn(EntityType<CamelHuskEntity> entityType, ServerLevelAccessor level, MobSpawnType spawnType, BlockPos pos, net.minecraft.util.RandomSource random) {
        return level.getDifficulty() != net.minecraft.world.Difficulty.PEACEFUL
                && Monster.isDarkEnoughToSpawn(level, pos, random)
                && Mob.checkMobSpawnRules(entityType, level, spawnType, pos, random);
    }

    public static void init(RegisterSpawnPlacementsEvent event) {
        event.register(MountsOfMayhemModEntities.CAMEL_HUSK.get(), SpawnPlacementTypes.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                CamelHuskEntity::canSpawn,
                RegisterSpawnPlacementsEvent.Operation.REPLACE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        AttributeSupplier.Builder builder = Mob.createMobAttributes();
        builder = builder.add(Attributes.MOVEMENT_SPEED, 0.15);
        builder = builder.add(Attributes.MAX_HEALTH, 32);
        builder = builder.add(Attributes.ARMOR, 0);
        builder = builder.add(Attributes.ATTACK_DAMAGE, 3);
        builder = builder.add(Attributes.FOLLOW_RANGE, 16);
        builder = builder.add(Attributes.STEP_HEIGHT, 1.5);
        builder = builder.add(Attributes.KNOCKBACK_RESISTANCE, 0.5);
        builder = builder.add(Attributes.JUMP_STRENGTH, 0.42);
        return builder;
    }
}
