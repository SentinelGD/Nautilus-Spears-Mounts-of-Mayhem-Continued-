/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.gospi.mountsofmayhem.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.core.registries.Registries;

import net.gospi.mountsofmayhem.entity.ZombieNautilusEntity;
import net.gospi.mountsofmayhem.entity.ParchedEntityProjectile;
import net.gospi.mountsofmayhem.entity.ParchedEntity;
import net.gospi.mountsofmayhem.entity.NautilusEntity;
import net.gospi.mountsofmayhem.MountsOfMayhemMod;

@EventBusSubscriber(bus = EventBusSubscriber.Bus.MOD)
public class MountsOfMayhemModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, MountsOfMayhemMod.MODID);
	public static final DeferredHolder<EntityType<?>, EntityType<NautilusEntity>> NAUTILUS = register("nautilus",
			EntityType.Builder.<NautilusEntity>of(NautilusEntity::new, MobCategory.WATER_CREATURE).setShouldReceiveVelocityUpdates(true).setTrackingRange(16).setUpdateInterval(3)

					.sized(1.1f, 1.2f));
	public static final DeferredHolder<EntityType<?>, EntityType<ZombieNautilusEntity>> ZOMBIE_NAUTILUS = register("zombie_nautilus",
			EntityType.Builder.<ZombieNautilusEntity>of(ZombieNautilusEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(16).setUpdateInterval(3)

					.sized(1.1f, 1.2f));
	public static final DeferredHolder<EntityType<?>, EntityType<ParchedEntity>> PARCHED = register("parched",
			EntityType.Builder.<ParchedEntity>of(ParchedEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<ParchedEntityProjectile>> PARCHED_PROJECTILE = register("projectile_parched",
			EntityType.Builder.<ParchedEntityProjectile>of(ParchedEntityProjectile::new, MobCategory.MISC).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(1).sized(0.5f, 0.5f));

	// Start of user code block custom entities
	// End of user code block custom entities
	private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(registryname));
	}

	@SubscribeEvent
	public static void init(RegisterSpawnPlacementsEvent event) {
		NautilusEntity.init(event);
		ZombieNautilusEntity.init(event);
		ParchedEntity.init(event);
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(NAUTILUS.get(), NautilusEntity.createAttributes().build());
		event.put(ZOMBIE_NAUTILUS.get(), ZombieNautilusEntity.createAttributes().build());
		event.put(PARCHED.get(), ParchedEntity.createAttributes().build());
	}
}