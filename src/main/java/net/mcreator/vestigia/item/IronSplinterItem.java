package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class IronSplinterItem extends Item {
	public IronSplinterItem(Item.Properties properties) {
		super(properties.rarity(Rarity.UNCOMMON));
	}
}