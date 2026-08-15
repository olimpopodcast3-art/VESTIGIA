package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class RepairedFlintItem extends Item {
	public RepairedFlintItem(Item.Properties properties) {
		super(properties.rarity(Rarity.UNCOMMON));
	}
}