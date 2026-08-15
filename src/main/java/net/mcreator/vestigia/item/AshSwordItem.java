package net.mcreator.vestigia.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class AshSwordItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 1551, 15f, 0, 22, TagKey.create(Registries.ITEM, Identifier.parse("vestigia:ash_sword_repair_items")));

	public AshSwordItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 6.2f, -2.7f).rarity(Rarity.UNCOMMON));
	}
}