package net.mcreator.vestigia.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class PhantomSpearItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1564, 18f, 0, 22, TagKey.create(Registries.ITEM, Identifier.parse("vestigia:phantom_spear_repair_items")));

	public PhantomSpearItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 7f, -2.7f).rarity(Rarity.EPIC).fireResistant());
	}
}