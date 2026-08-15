package net.mcreator.vestigia.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.entity.LivingEntity;

import net.mcreator.vestigia.procedures.AncientFuirPlayerFinishesUsingItemProcedure;

public class AncientFuirItem extends Item {
	public AncientFuirItem(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC).food((new FoodProperties.Builder()).nutrition(8).saturationModifier(3f).build()));
	}

	@Override
	public ItemStack finishUsingItem(ItemStack itemstack, Level world, LivingEntity entity) {
		ItemStack retval = super.finishUsingItem(itemstack, world, entity);
		AncientFuirPlayerFinishesUsingItemProcedure.execute(entity);
		return retval;
	}
}