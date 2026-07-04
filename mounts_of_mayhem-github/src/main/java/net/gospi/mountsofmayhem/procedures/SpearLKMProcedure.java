package net.gospi.mountsofmayhem.procedures;

import net.neoforged.neoforge.network.PacketDistributor;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;

import net.gospi.mountsofmayhem.network.PlayPlayerAnimationMessage;
import net.gospi.mountsofmayhem.MountsOfMayhemMod;

import java.util.Comparator;

public class SpearLKMProcedure {
	private static double getSpearDamage(ItemStack stack) {
		String id = BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
		switch (id) {
			case "mounts_of_mayhem:wooden_spear": return 1.0;
			case "mounts_of_mayhem:stone_spear": return 2.0;
			case "mounts_of_mayhem:copper_spear": return 2.0;
			case "mounts_of_mayhem:golden_spear": return 1.0;
			case "mounts_of_mayhem:iron_spear": return 3.0;
			case "mounts_of_mayhem:diamond_spear": return 4.0;
			case "mounts_of_mayhem:netherite_spear": return 5.0;
			default: return 1.0;
		}
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
		if (entity == null)
			return;
		double dist = 0;
		double dotProduct = 0;
		boolean hitSomething = false;
		double spearDmg = getSpearDamage(itemstack);

		if (entity instanceof Player) {
			if (entity.level().isClientSide()) {
				CompoundTag data = entity.getPersistentData();
				data.putString("PlayerCurrentAnimation", "mounts_of_mayhem:attack");
				data.putBoolean("OverrideCurrentAnimation", true);
			} else {
				PacketDistributor.sendToPlayersInDimension((ServerLevel) entity.level(), new PlayPlayerAnimationMessage(entity.getId(), "mounts_of_mayhem:attack", true));
			}
		}
		{
			final Vec3 _center = new Vec3(x, y, z);
			for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(10 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
				if (entityiterator instanceof Mob && ((Mob) entityiterator).getSensing().hasLineOfSight(entity)) {
					dist = Math.pow(x - entityiterator.getX(), 2) + Math.pow(z - entityiterator.getZ(), 2);
					if (Math.abs((y + 0.9) - entityiterator.getY()) < 1 && dist < Math.pow(entityiterator.getBbWidth() / 2d + 5, 2)) {
						Vec3 vecToTarget = new Vec3(entityiterator.getX(), 0, entityiterator.getZ()).subtract(new Vec3(entity.getX(), 0, entity.getZ())).normalize();
						dotProduct = entity.getLookAngle().dot(vecToTarget);
						if (dotProduct > 0.9) {
							entityiterator.hurt(new DamageSource(world.holderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("mounts_of_mayhem:spear_damage"))), entity), (float) spearDmg);
							hitSomething = true;
						}
					}
				}
			}
		}
		// Only play attack sound if actually hit something
		if (hitSomething) {
			if (world instanceof Level _level) {
				if (!_level.isClientSide()) {
					_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("mounts_of_mayhem:item.spear.attack")), SoundSource.NEUTRAL, 1, 1);
				} else {
					_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("mounts_of_mayhem:item.spear.attack")), SoundSource.NEUTRAL, 1, 1, false);
				}
			}
		}
		if ((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
				.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("mounts_of_mayhem:lunge")))) != 0
				&& !(entity instanceof Player _plrCldCheck15 && _plrCldCheck15.getCooldowns().isOnCooldown((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem()))) {
			entity.setDeltaMovement(new Vec3(
					(((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getEnchantmentLevel(
							world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("mounts_of_mayhem:lunge")))) / 1.25) * entity.getLookAngle().x),
					(0 * entity.getLookAngle().y),
					(((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
							.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("mounts_of_mayhem:lunge")))) / 1.25)
							* entity.getLookAngle().z)));
			if (world instanceof Level _level) {
				if (!_level.isClientSide()) {
					_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("mounts_of_mayhem:item.spear.lunge")), SoundSource.NEUTRAL, 1, 1);
				} else {
					_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("mounts_of_mayhem:item.spear.lunge")), SoundSource.NEUTRAL, 1, 1, false);
				}
			}
			MountsOfMayhemMod.queueServerWork(2, () -> {
				{
					final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
					for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
						if (!(entityiterator instanceof Player)) {
							entityiterator.hurt(new DamageSource(world.holderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("mounts_of_mayhem:spear_damage")))),
									(float) spearDmg);
						}
					}
				}
				MountsOfMayhemMod.queueServerWork(2, () -> {
					{
						final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
						for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
							if (!(entityiterator instanceof Player)) {
								entityiterator.hurt(new DamageSource(world.holderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("mounts_of_mayhem:spear_damage")))),
										(float) spearDmg);
							}
						}
					}
					MountsOfMayhemMod.queueServerWork(2, () -> {
						{
							final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
							for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center))).toList()) {
								if (!(entityiterator instanceof Player)) {
									entityiterator.hurt(new DamageSource(world.holderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("mounts_of_mayhem:spear_damage")))),
											(float) spearDmg);
								}
							}
						}
						MountsOfMayhemMod.queueServerWork(2, () -> {
							{
								final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
								for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
										.toList()) {
									if (!(entityiterator instanceof Player)) {
										entityiterator.hurt(new DamageSource(world.holderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("mounts_of_mayhem:spear_damage")))),
												(float) spearDmg);
									}
								}
							}
							MountsOfMayhemMod.queueServerWork(2, () -> {
								{
									final Vec3 _center = new Vec3((entity.getX()), (entity.getY()), (entity.getZ()));
									for (Entity entityiterator : world.getEntitiesOfClass(Entity.class, new AABB(_center, _center).inflate(2 / 2d), e -> true).stream().sorted(Comparator.comparingDouble(_entcnd -> _entcnd.distanceToSqr(_center)))
											.toList()) {
										if (!(entityiterator instanceof Player)) {
											entityiterator.hurt(new DamageSource(world.holderOrThrow(ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.parse("mounts_of_mayhem:spear_damage")))),
													(float) spearDmg);
										}
									}
								}
							});
						});
					});
				});
			});
			if (entity instanceof Player _player)
				_player.getCooldowns().addCooldown((entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem(), 20);
			if ((entity instanceof Player _plr ? _plr.getFoodData().getFoodLevel() : 0) > 0) {
				if (entity instanceof Player _player)
					_player.getFoodData().setFoodLevel((entity instanceof Player _plr ? _plr.getFoodData().getFoodLevel() : 0) - (entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
							.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("mounts_of_mayhem:lunge")))));
			} else if ((entity instanceof Player _plr ? _plr.getFoodData().getFoodLevel() : 0) == 0) {
				if (world instanceof ServerLevel _level) {
					(entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).hurtAndBreak(
							(entity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY)
									.getEnchantmentLevel(world.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(ResourceKey.create(Registries.ENCHANTMENT, ResourceLocation.parse("mounts_of_mayhem:lunge")))) * 43,
							_level, null, _stkprov -> {
							});
				}
			}
			if (entity instanceof LivingEntity _entity && !_entity.level().isClientSide())
				_entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 40, 3, false, false));
		}
	}
}
