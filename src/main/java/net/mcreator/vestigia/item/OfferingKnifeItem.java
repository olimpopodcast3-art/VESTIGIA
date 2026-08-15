package net.mcreator.vestigia.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class OfferingKnifeItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 100, 11f, 0, 22, TagKey.create(Registries.ITEM, Identifier.parse("vestigia:offering_knife_repair_items")));

	public OfferingKnifeItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 5f, -2.6f).fireResistant());
	}
}