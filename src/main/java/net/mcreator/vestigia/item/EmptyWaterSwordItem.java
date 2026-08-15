package net.mcreator.vestigia.item;

import net.minecraft.world.item.Item;

public class EmptyWaterSwordItem extends Item {
	public EmptyWaterSwordItem(Item.Properties properties) {
		super(properties.stacksTo(1).fireResistant());
	}
}