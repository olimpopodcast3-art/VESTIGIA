package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class Seal2Item extends Item {
	public Seal2Item(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC).fireResistant());
	}
}