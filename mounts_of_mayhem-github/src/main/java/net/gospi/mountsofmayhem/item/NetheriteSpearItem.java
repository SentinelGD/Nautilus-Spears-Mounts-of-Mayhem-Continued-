package net.gospi.mountsofmayhem.item;

import net.minecraft.world.item.Item;
//
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;

import net.gospi.mountsofmayhem.procedures.WoodenSpearPriUdariePoSushchnostiPriedmietomProcedure;
import net.gospi.mountsofmayhem.procedures.SpearLKMProcedure;

public class NetheriteSpearItem extends SpearItem {
    public NetheriteSpearItem() {
        // Незеритовое: держать 3с (60 тиков), перезарядка 2.5с (50 тиков), прочность 2031
        super(new Item.Properties().durability(2031).attributes(SpearItem.createAttackAttributes(5.0, -2.8)), 40, 50);
    }

	@Override
	public boolean hurtEnemy(ItemStack itemstack, LivingEntity entity, LivingEntity sourceentity) {
		itemstack.hurtAndBreak(1, entity, LivingEntity.getSlotForHand(entity.getUsedItemHand()));
		WoodenSpearPriUdariePoSushchnostiPriedmietomProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ());
		return true;
	}

	@Override
	public boolean onEntitySwing(ItemStack itemstack, LivingEntity entity, InteractionHand hand) {
		boolean retval = super.onEntitySwing(itemstack, entity, hand);
		SpearLKMProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity, itemstack);
		return retval;
	}
}