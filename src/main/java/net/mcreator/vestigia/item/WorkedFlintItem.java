package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class WorkedFlintItem extends Item {
	public WorkedFlintItem(Item.Properties properties) {
		super(properties.rarity(Rarity.RARE));
	}
}