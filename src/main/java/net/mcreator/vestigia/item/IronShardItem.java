package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class IronShardItem extends Item {
	public IronShardItem(Item.Properties properties) {
		super(properties.rarity(Rarity.UNCOMMON));
	}
}