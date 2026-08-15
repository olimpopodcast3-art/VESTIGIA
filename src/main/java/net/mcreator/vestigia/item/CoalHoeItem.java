package net.mcreator.vestigia.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.HoeItem;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class CoalHoeItem extends HoeItem {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 2564, 10f, 0, 22, TagKey.create(Registries.ITEM, Identifier.parse("vestigia:coal_hoe_repair_items")));

	public CoalHoeItem(Item.Properties properties) {
		super(TOOL_MATERIAL, 8f, -2.4f, properties.rarity(Rarity.RARE).fireResistant());
	}
}