package net.mcreator.vestigia.item;

import net.minecraft.world.item.Item;

public class UnchargedRubyBladeBrokenItem extends Item {
	public UnchargedRubyBladeBrokenItem(Item.Properties properties) {
		super(properties.stacksTo(1).fireResistant());
	}
}