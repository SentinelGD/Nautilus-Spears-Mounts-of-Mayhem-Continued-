package net.gospi.mountsofmayhem.item;

import net.minecraft.world.item.Item;
//
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;

import net.gospi.mountsofmayhem.procedures.WoodenSpearPriUdariePoSushchnostiPriedmietomProcedure;
import net.gospi.mountsofmayhem.procedures.SpearLKMProcedure;

public class GoldenSpearItem extends SpearItem {
    public GoldenSpearItem() {
        // Золотое: держать ~5с (100 тиков), перезарядка 2с (40 тиков), прочность 33
        super(new Item.Properties().durability(33).attributes(SpearItem.createAttackAttributes(1.0, -2.8)), 90, 40);
    }

    @Override
    public boolean isValidRepairItem(ItemStack toRepair, ItemStack repair) {
        return Ingredient.of(Items.GOLD_INGOT).test(repair);
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