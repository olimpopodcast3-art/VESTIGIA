package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class BronzeSedimentItem extends Item {
	public BronzeSedimentItem(Item.Properties properties) {
		super(properties.rarity(Rarity.UNCOMMON));
	}
}