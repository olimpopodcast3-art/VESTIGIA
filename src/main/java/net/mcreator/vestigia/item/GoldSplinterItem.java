package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class GoldSplinterItem extends Item {
	public GoldSplinterItem(Item.Properties properties) {
		super(properties.rarity(Rarity.RARE));
	}
}