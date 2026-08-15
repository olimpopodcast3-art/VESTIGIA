package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class BronzeItem extends Item {
	public BronzeItem(Item.Properties properties) {
		super(properties.rarity(Rarity.UNCOMMON));
	}
}