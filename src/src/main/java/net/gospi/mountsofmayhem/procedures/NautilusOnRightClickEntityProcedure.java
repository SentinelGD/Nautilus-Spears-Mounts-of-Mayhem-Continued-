package net.gospi.mountsofmayhem.procedures;

import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.Event;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.GameType;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.InteractionHand;
import net.minecraft.tags.ItemTags;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.BlockPos;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.Minecraft;

import net.gospi.mountsofmayhem.init.MountsOfMayhemModItems;
import net.gospi.mountsofmayhem.entity.NautilusEntity;

import javax.annotation.Nullable;

@EventBusSubscriber
public class NautilusOnRightClickEntityProcedure {
	@SubscribeEvent
	public static void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
		if (event.getHand() != InteractionHand.MAIN_HAND)
			return;
		execute(event, event.getLevel(), event.getPos().getX(), event.getPos().getY(), event.getPos().getZ(), event.getTarget(), event.getEntity());
	}

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
		execute(null, world, x, y, z, entity, sourceentity);
	}

	private static void execute(@Nullable Event event, LevelAccessor world, double x, double y, double z, Entity entity, Entity sourceentity) {
		if (entity == null || sourceentity == null)
			return;
		if (entity instanceof NautilusEntity && (entity instanceof TamableAnimal _tamEnt ? _tamEnt.isTame() : false) && !(entity instanceof LivingEntity _livEnt2 && _livEnt2.isBaby())) {
			if (!((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).is(ItemTags.create(ResourceLocation.parse("minecraft:nautilus_armor"))))
					&& !((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.SHEARS)) {
				if (entity.getPersistentData().getBoolean("isSaddled") == true) {
					sourceentity.startRiding(entity);
				}
			}
			if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).is(ItemTags.create(ResourceLocation.parse("minecraft:nautilus_armor")))) {
				if (entity.getPersistentData().getBoolean("Armor") == false) {
					if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == MountsOfMayhemModItems.COPPER_NAUTILUS_SHELL.get()) {
						entity.getPersistentData().putBoolean("copperArmor", true);
						entity.getPersistentData().putBoolean("Armor", true);
						if (entity instanceof NautilusEntity nautilus) nautilus.syncArmorToClient();
						if (world instanceof Level _level) {
							if (!_level.isClientSide()) {
								_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("item.armor.equip_wolf")), SoundSource.NEUTRAL, 1, 1);
							} else {
								_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("item.armor.equip_wolf")), SoundSource.NEUTRAL, 1, 1, false);
							}
						}
						if (sourceentity instanceof LivingEntity _entity)
							_entity.swing(InteractionHand.MAIN_HAND, true);
						if (!(getEntityGameType(sourceentity) == GameType.CREATIVE)) {
							if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == MountsOfMayhemModItems.COPPER_NAUTILUS_SHELL.get()) {
								(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).shrink(1);
							} else {
								(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).shrink(1);
							}
						}
					} else if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == MountsOfMayhemModItems.IRON_NAUTILUS_SHELL.get()) {
						entity.getPersistentData().putBoolean("ironArmor", true);
						entity.getPersistentData().putBoolean("Armor", true);
						if (entity instanceof NautilusEntity nautilus) nautilus.syncArmorToClient();
						if (world instanceof Level _level) {
							if (!_level.isClientSide()) {
								_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("item.armor.equip_wolf")), SoundSource.NEUTRAL, 1, 1);
							} else {
								_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("item.armor.equip_wolf")), SoundSource.NEUTRAL, 1, 1, false);
							}
						}
						if (sourceentity instanceof LivingEntity _entity)
							_entity.swing(InteractionHand.MAIN_HAND, true);
						if (!(getEntityGameType(sourceentity) == GameType.CREATIVE)) {
							if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == MountsOfMayhemModItems.IRON_NAUTILUS_SHELL.get()) {
								(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).shrink(1);
							} else {
								(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).shrink(1);
							}
						}
					} else if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == MountsOfMayhemModItems.GOLDEN_NAUTILUS_SHELL.get()) {
						entity.getPersistentData().putBoolean("goldenArmor", true);
						entity.getPersistentData().putBoolean("Armor", true);
						if (entity instanceof NautilusEntity nautilus) nautilus.syncArmorToClient();
						if (world instanceof Level _level) {
							if (!_level.isClientSide()) {
								_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("item.armor.equip_wolf")), SoundSource.NEUTRAL, 1, 1);
							} else {
								_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("item.armor.equip_wolf")), SoundSource.NEUTRAL, 1, 1, false);
							}
						}
						if (sourceentity instanceof LivingEntity _entity)
							_entity.swing(InteractionHand.MAIN_HAND, true);
						if (!(getEntityGameType(sourceentity) == GameType.CREATIVE)) {
							if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == MountsOfMayhemModItems.GOLDEN_NAUTILUS_SHELL.get()) {
								(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).shrink(1);
							} else {
								(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).shrink(1);
							}
						}
					} else if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == MountsOfMayhemModItems.DIAMOND_NAUTILUS_SHELL.get()) {
						entity.getPersistentData().putBoolean("diamondArmor", true);
						entity.getPersistentData().putBoolean("Armor", true);
						if (entity instanceof NautilusEntity nautilus) nautilus.syncArmorToClient();
						if (world instanceof Level _level) {
							if (!_level.isClientSide()) {
								_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("item.armor.equip_wolf")), SoundSource.NEUTRAL, 1, 1);
							} else {
								_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("item.armor.equip_wolf")), SoundSource.NEUTRAL, 1, 1, false);
							}
						}
						if (sourceentity instanceof LivingEntity _entity)
							_entity.swing(InteractionHand.MAIN_HAND, true);
						if (!(getEntityGameType(sourceentity) == GameType.CREATIVE)) {
							if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == MountsOfMayhemModItems.DIAMOND_NAUTILUS_SHELL.get()) {
								(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).shrink(1);
							} else {
								(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).shrink(1);
							}
						}
					} else if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == MountsOfMayhemModItems.NETHERITE_NAUTILUS_SHELL.get()) {
						entity.getPersistentData().putBoolean("netheriteArmor", true);
						entity.getPersistentData().putBoolean("Armor", true);
						if (entity instanceof NautilusEntity nautilus) nautilus.syncArmorToClient();
						if (world instanceof Level _level) {
							if (!_level.isClientSide()) {
								_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("item.armor.equip_wolf")), SoundSource.NEUTRAL, 1, 1);
							} else {
								_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("item.armor.equip_wolf")), SoundSource.NEUTRAL, 1, 1, false);
							}
						}
						if (sourceentity instanceof LivingEntity _entity)
							_entity.swing(InteractionHand.MAIN_HAND, true);
						if (!(getEntityGameType(sourceentity) == GameType.CREATIVE)) {
							if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == MountsOfMayhemModItems.NETHERITE_NAUTILUS_SHELL.get()) {
								(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).shrink(1);
							} else {
								(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).shrink(1);
							}
						}
					}
				}
			}
			if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.SHEARS) {
				if (entity.getPersistentData().getBoolean("isSaddled") == true) {
					entity.getPersistentData().putBoolean("isSaddled", false);
					if (entity instanceof NautilusEntity nautilus) nautilus.syncArmorToClient();
					if (world instanceof Level _level) {
						if (!_level.isClientSide()) {
							_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("item.armor.unequip_wolf")), SoundSource.NEUTRAL, 1, 1);
						} else {
							_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("item.armor.unequip_wolf")), SoundSource.NEUTRAL, 1, 1, false);
						}
					}
					if (world instanceof ServerLevel _level) {
						ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(Items.SADDLE));
						entityToSpawn.setPickUpDelay(10);
						_level.addFreshEntity(entityToSpawn);
					}
					if (sourceentity instanceof LivingEntity _entity)
						_entity.swing(InteractionHand.MAIN_HAND, true);
					if (!(getEntityGameType(sourceentity) == GameType.CREATIVE)) {
						if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.SHEARS) {
							if (world instanceof ServerLevel _level) {
								(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).hurtAndBreak(1, _level, null, _stkprov -> {
								});
							}
						} else {
							if (world instanceof ServerLevel _level) {
								(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).hurtAndBreak(1, _level, null, _stkprov -> {
								});
							}
						}
					}
				}
				if (entity.getPersistentData().getBoolean("Armor") == true) {
					if (entity instanceof NautilusEntity nautilus) nautilus.syncArmorToClient();
					if (entity.getPersistentData().getBoolean("copperArmor") == true) {
						entity.getPersistentData().putBoolean("copperArmor", false);
						if (world instanceof ServerLevel _level) {
							ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(MountsOfMayhemModItems.COPPER_NAUTILUS_SHELL.get()));
							entityToSpawn.setPickUpDelay(10);
							_level.addFreshEntity(entityToSpawn);
						}
					}
					if (entity.getPersistentData().getBoolean("ironArmor") == true) {
						entity.getPersistentData().putBoolean("ironArmor", false);
						if (world instanceof ServerLevel _level) {
							ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(MountsOfMayhemModItems.IRON_NAUTILUS_SHELL.get()));
							entityToSpawn.setPickUpDelay(10);
							_level.addFreshEntity(entityToSpawn);
						}
					}
					if (entity.getPersistentData().getBoolean("goldenArmor") == true) {
						entity.getPersistentData().putBoolean("goldenArmor", false);
						if (world instanceof ServerLevel _level) {
							ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(MountsOfMayhemModItems.GOLDEN_NAUTILUS_SHELL.get()));
							entityToSpawn.setPickUpDelay(10);
							_level.addFreshEntity(entityToSpawn);
						}
					}
					if (entity.getPersistentData().getBoolean("diamondArmor") == true) {
						entity.getPersistentData().putBoolean("diamondArmor", false);
						if (world instanceof ServerLevel _level) {
							ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(MountsOfMayhemModItems.DIAMOND_NAUTILUS_SHELL.get()));
							entityToSpawn.setPickUpDelay(10);
							_level.addFreshEntity(entityToSpawn);
						}
					}
					if (entity.getPersistentData().getBoolean("netheriteArmor") == true) {
						entity.getPersistentData().putBoolean("netheriteArmor", false);
						if (world instanceof ServerLevel _level) {
							ItemEntity entityToSpawn = new ItemEntity(_level, x, y, z, new ItemStack(MountsOfMayhemModItems.NETHERITE_NAUTILUS_SHELL.get()));
							entityToSpawn.setPickUpDelay(10);
							_level.addFreshEntity(entityToSpawn);
						}
					}
					entity.getPersistentData().putBoolean("Armor", false);
					if (sourceentity instanceof LivingEntity _entity)
						_entity.swing(InteractionHand.MAIN_HAND, true);
					if (world instanceof Level _level) {
						if (!_level.isClientSide()) {
							_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("item.armor.unequip_wolf")), SoundSource.NEUTRAL, 1, 1);
						} else {
							_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("item.armor.unequip_wolf")), SoundSource.NEUTRAL, 1, 1, false);
						}
					}
					if (!(getEntityGameType(sourceentity) == GameType.CREATIVE)) {
						if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.SHEARS) {
							if (world instanceof ServerLevel _level) {
								(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).hurtAndBreak(1, _level, null, _stkprov -> {
								});
							}
						} else {
							if (world instanceof ServerLevel _level) {
								(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).hurtAndBreak(1, _level, null, _stkprov -> {
								});
							}
						}
					}
				}
			}
			if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.SADDLE) {
				if (entity.getPersistentData().getBoolean("isSaddled") == false) {
					entity.getPersistentData().putBoolean("isSaddled", true);
					if (entity instanceof NautilusEntity nautilus) nautilus.syncArmorToClient();
					if (world instanceof Level _level) {
						if (!_level.isClientSide()) {
							_level.playSound(null, BlockPos.containing(x, y, z), BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("mounts_of_mayhem:item.nautilus_saddle_equip")), SoundSource.NEUTRAL, 1, 1);
						} else {
							_level.playLocalSound(x, y, z, BuiltInRegistries.SOUND_EVENT.get(ResourceLocation.parse("mounts_of_mayhem:item.nautilus_saddle_equip")), SoundSource.NEUTRAL, 1, 1, false);
						}
					}
					if (sourceentity instanceof LivingEntity _entity)
						_entity.swing(InteractionHand.MAIN_HAND, true);
					if (!(getEntityGameType(sourceentity) == GameType.CREATIVE)) {
						if ((sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).getItem() == Items.SADDLE) {
							(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getMainHandItem() : ItemStack.EMPTY).shrink(1);
						} else {
							(sourceentity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).shrink(1);
						}
					}
				}
			}
		}
	}

	private static GameType getEntityGameType(Entity entity) {
		if (entity instanceof ServerPlayer serverPlayer) {
			return serverPlayer.gameMode.getGameModeForPlayer();
		} else if (entity instanceof Player player && player.level().isClientSide()) {
			PlayerInfo playerInfo = Minecraft.getInstance().getConnection().getPlayerInfo(player.getGameProfile().getId());
			if (playerInfo != null)
				return playerInfo.getGameMode();
		}
		return null;
	}
}
