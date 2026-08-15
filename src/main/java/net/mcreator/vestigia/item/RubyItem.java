package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class RubyItem extends Item {
	public RubyItem(Item.Properties properties) {
		super(properties.rarity(Rarity.RARE).fireResistant());
	}
}