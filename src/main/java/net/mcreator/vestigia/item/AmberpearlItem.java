package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class AmberpearlItem extends Item {
	public AmberpearlItem(Item.Properties properties) {
		super(properties.rarity(Rarity.UNCOMMON));
	}
}