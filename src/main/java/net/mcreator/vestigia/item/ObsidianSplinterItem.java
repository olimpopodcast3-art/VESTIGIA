package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class ObsidianSplinterItem extends Item {
	public ObsidianSplinterItem(Item.Properties properties) {
		super(properties.rarity(Rarity.RARE).fireResistant());
	}
}