package net.gospi.mountsofmayhem.entity;

import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

@OnlyIn(value = Dist.CLIENT, _interface = ItemSupplier.class)
public class ParchedEntityProjectile extends AbstractArrow implements ItemSupplier {
	public static final ItemStack PROJECTILE_ITEM = new ItemStack(Items.TIPPED_ARROW);

	public ParchedEntityProjectile(EntityType<? extends ParchedEntityProjectile> type, Level world) {
		super(type, world);
	}

	public ParchedEntityProjectile(EntityType<? extends ParchedEntityProjectile> type, double x, double y, double z, Level world) {
		super(type, x, y, z, world, PROJECTILE_ITEM, null);
	}

	public ParchedEntityProjectile(EntityType<? extends ParchedEntityProjectile> type, LivingEntity entity, Level world) {
		super(type, entity, world, PROJECTILE_ITEM, null);
	}

	@Override
	protected void doPostHurtEffects(LivingEntity livingEntity) {
		super.doPostHurtEffects(livingEntity);
		livingEntity.setArrowCount(livingEntity.getArrowCount() - 1);
		if (!this.level().isClientSide()) {
			livingEntity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 200, 0));
		}
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public ItemStack getItem() {
		return PROJECTILE_ITEM;
	}

	@Override
	protected ItemStack getDefaultPickupItem() {
		return new ItemStack(Items.TIPPED_ARROW);
	}
}