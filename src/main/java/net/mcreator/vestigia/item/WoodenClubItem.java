package net.mcreator.vestigia.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class WoodenClubItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_WOODEN_TOOL, 500, 4f, 0, 12, TagKey.create(Registries.ITEM, Identifier.parse("vestigia:wooden_club_repair_items")));

	public WoodenClubItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 5f, -2.9f));
	}
}