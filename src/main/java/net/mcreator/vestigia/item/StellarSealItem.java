package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class StellarSealItem extends Item {
	public StellarSealItem(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC).fireResistant());
	}
}