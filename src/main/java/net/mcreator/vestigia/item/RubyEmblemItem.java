package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class RubyEmblemItem extends Item {
	public RubyEmblemItem(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC).fireResistant());
	}
}