package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class FireEmblemItem extends Item {
	public FireEmblemItem(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC).fireResistant());
	}
}