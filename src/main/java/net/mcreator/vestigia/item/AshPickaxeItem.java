package net.mcreator.vestigia.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class AshPickaxeItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 1521, 15f, 0, 22, TagKey.create(Registries.ITEM, Identifier.parse("vestigia:ash_pickaxe_repair_items")));

	public AshPickaxeItem(Item.Properties properties) {
		super(properties.pickaxe(TOOL_MATERIAL, 4f, -2.9f).rarity(Rarity.UNCOMMON));
	}
}