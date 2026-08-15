package net.mcreator.vestigia.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.AxeItem;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class FlintAxeItem extends AxeItem {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2031, 13f, 0, 2, TagKey.create(Registries.ITEM, Identifier.parse("vestigia:flint_axe_repair_items")));

	public FlintAxeItem(Item.Properties properties) {
		super(TOOL_MATERIAL, 8.8f, -2.8f, properties.rarity(Rarity.EPIC).fireResistant());
	}
}