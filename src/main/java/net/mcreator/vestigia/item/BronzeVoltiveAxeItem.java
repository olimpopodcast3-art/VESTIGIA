package net.mcreator.vestigia.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.AxeItem;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class BronzeVoltiveAxeItem extends AxeItem {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2042, 4f, 0, 22, TagKey.create(Registries.ITEM, Identifier.parse("vestigia:bronze_voltive_axe_repair_items")));

	public BronzeVoltiveAxeItem(Item.Properties properties) {
		super(TOOL_MATERIAL, 7.7f, -2.5f, properties.rarity(Rarity.EPIC).fireResistant());
	}
}