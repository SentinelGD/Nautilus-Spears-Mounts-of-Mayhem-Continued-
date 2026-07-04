package net.gospi.mountsofmayhem.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
//
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;

import net.gospi.mountsofmayhem.procedures.WoodenSpearPriUdariePoSushchnostiPriedmietomProcedure;
import net.gospi.mountsofmayhem.procedures.SpearLKMProcedure;

public class StoneSpearItem extends SpearItem {
    public StoneSpearItem() {
        // Каменное: держать ~6с (120 тиков), перезарядка 2с (40 тиков), прочность 131
        super(new Item.Properties().durability(131).attributes(SpearItem.createAttackAttributes(2.0, -2.8)), 130, 40);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return Ingredient.of(ItemTags.create(ResourceLocation.parse("minecraft:stones"))).test(repair);
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