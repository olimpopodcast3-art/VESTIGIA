package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.food.FoodProperties;

public class CornItem extends Item {
	public CornItem(Item.Properties properties) {
		super(properties.rarity(Rarity.RARE).food((new FoodProperties.Builder()).nutrition(5).saturationModifier(0.3f).build()));
	}
}