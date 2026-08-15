package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class AuroraSplinterItem extends Item {
	public AuroraSplinterItem(Item.Properties properties) {
		super(properties.rarity(Rarity.RARE).fireResistant());
	}
}