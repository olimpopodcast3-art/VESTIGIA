package net.mcreator.vestigia.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class MacahuitlItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_STONE_TOOL, 200, 12f, 0, 22, TagKey.create(Registries.ITEM, Identifier.parse("vestigia:macahuitl_repair_items")));

	public MacahuitlItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 5.5f, -2.8f).rarity(Rarity.RARE));
	}
}