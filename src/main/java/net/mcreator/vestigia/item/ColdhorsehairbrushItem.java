package net.mcreator.vestigia.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BrushItem;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class ColdhorsehairbrushItem extends BrushItem {
	public ColdhorsehairbrushItem(Item.Properties properties) {
		super(properties.rarity(Rarity.UNCOMMON).durability(45).repairable(TagKey.create(Registries.ITEM, Identifier.parse("vestigia:coldhorsehairbrush_repair_items"))));
	}
}
