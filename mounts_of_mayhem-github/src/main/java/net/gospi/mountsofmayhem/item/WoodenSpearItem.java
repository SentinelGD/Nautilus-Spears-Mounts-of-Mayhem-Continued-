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

public class WoodenSpearItem extends SpearItem {
    public WoodenSpearItem() {
        super(new Item.Properties().durability(59).attributes(SpearItem.createAttackAttributes(1.0, -2.8)), 150, 40);
 //держать 150 тиков, перезарядка 40 тиков
    }

    @Override
    public boolean isValidRepairItem(net.minecraft.world.item.ItemStack toRepair, net.minecraft.world.item.ItemStack repair) {
        return Ingredient.of(ItemTags.create(ResourceLocation.parse("minecraft:planks"))).test(repair);
    }
	@Override
	public boolean onEntitySwing(ItemStack itemstack, LivingEntity entity, InteractionHand hand) {
		boolean retval = super.onEntitySwing(itemstack, entity, hand);
		SpearLKMProcedure.execute(entity.level(), entity.getX(), entity.getY(), entity.getZ(), entity, itemstack);
		return retval;
	}
}