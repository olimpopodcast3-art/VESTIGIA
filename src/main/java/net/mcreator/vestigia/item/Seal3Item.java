package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class Seal3Item extends Item {
	public Seal3Item(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC).fireResistant());
	}
}