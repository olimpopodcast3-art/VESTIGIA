package net.mcreator.vestigia.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class IronDaggerItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_IRON_TOOL, 1564, 8f, 0, 14, TagKey.create(Registries.ITEM, Identifier.parse("vestigia:iron_dagger_repair_items")));

	public IronDaggerItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 6f, -2f).rarity(Rarity.UNCOMMON));
	}
}