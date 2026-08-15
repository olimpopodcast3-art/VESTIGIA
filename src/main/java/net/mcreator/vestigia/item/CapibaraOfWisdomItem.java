package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class CapibaraOfWisdomItem extends Item {
	public CapibaraOfWisdomItem(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC).durability(5).fireResistant());
	}
}
