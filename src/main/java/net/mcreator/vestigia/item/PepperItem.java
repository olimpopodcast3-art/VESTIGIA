package net.mcreator.vestigia.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;

import net.mcreator.vestigia.procedures.PepperPlayerFinishesUsingItemProcedure;

public class PepperItem extends Item {
	public PepperItem(Item.Properties properties) {
		super(properties.rarity(Rarity.RARE).fireResistant().food((new FoodProperties.Builder()).nutrition(9).saturationModifier(0.3f).build()));
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
		ItemStack retval = super.finishUsingItem(itemstack, world, entity);
		PepperPlayerFinishesUsingItemProcedure.execute(entity);
		return retval;
	}
}