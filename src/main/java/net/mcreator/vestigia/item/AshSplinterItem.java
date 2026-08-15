package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class AshSplinterItem extends Item {
	public AshSplinterItem(Item.Properties properties) {
		super(properties.rarity(Rarity.RARE));
	}
}