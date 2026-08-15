package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class HookItem extends Item {
	public HookItem(Item.Properties properties) {
		super(properties.rarity(Rarity.RARE).stacksTo(1));
	}
}