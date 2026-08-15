package net.mcreator.vestigia.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class StoneMasonPickaxeItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 1521, 4f, 0, 22, TagKey.create(Registries.ITEM, Identifier.parse("vestigia:stone_mason_pickaxe_repair_items")));

	public StoneMasonPickaxeItem(Item.Properties properties) {
		super(properties.pickaxe(TOOL_MATERIAL, 3f, -3f).rarity(Rarity.RARE));
	}
}