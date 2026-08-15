package net.mcreator.vestigia.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class AuroraKnifeItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_STONE_TOOL, 1031, 12f, 0, 22, TagKey.create(Registries.ITEM, Identifier.parse("vestigia:aurora_knife_repair_items")));

	public AuroraKnifeItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 7.5f, -2f).rarity(Rarity.UNCOMMON).fireResistant());
	}
}