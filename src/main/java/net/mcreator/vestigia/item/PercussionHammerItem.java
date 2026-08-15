package net.mcreator.vestigia.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class PercussionHammerItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1521, 12f, 0, 22, TagKey.create(Registries.ITEM, Identifier.parse("vestigia:percussion_hammer_repair_items")));

	public PercussionHammerItem(Item.Properties properties) {
		super(properties.pickaxe(TOOL_MATERIAL, 4.5f, -2.9f).rarity(Rarity.RARE));
	}
}