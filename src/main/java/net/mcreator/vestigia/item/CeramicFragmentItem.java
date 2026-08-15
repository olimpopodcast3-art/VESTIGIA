package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class CeramicFragmentItem extends Item {
	public CeramicFragmentItem(Item.Properties properties) {
		super(properties.rarity(Rarity.UNCOMMON));
	}
}