package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;

public class EnderSplinterItem extends Item {
	public EnderSplinterItem(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC).stacksTo(16).fireResistant());
	}

	@Override
	public boolean isPiglinCurrency(ItemStack stack) {
		return true;
	}
}