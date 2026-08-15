package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class WaterSplinterItem extends Item {
	public WaterSplinterItem(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC));
	}
}