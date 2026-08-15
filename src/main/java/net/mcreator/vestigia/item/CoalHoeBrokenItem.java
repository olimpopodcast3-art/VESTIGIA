package net.mcreator.vestigia.item;

import net.minecraft.world.item.Item;

public class CoalHoeBrokenItem extends Item {
	public CoalHoeBrokenItem(Item.Properties properties) {
		super(properties.stacksTo(1).fireResistant());
	}
}