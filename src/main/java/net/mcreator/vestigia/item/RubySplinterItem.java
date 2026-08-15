package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class RubySplinterItem extends Item {
	public RubySplinterItem(Item.Properties properties) {
		super(properties.rarity(Rarity.RARE));
	}
}