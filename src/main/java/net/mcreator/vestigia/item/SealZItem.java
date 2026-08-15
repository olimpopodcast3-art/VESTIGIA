package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class SealZItem extends Item {
	public SealZItem(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC).fireResistant());
	}
}