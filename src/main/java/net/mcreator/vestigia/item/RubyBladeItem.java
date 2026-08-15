package net.mcreator.vestigia.item;

import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.component.DataComponents;

public class RubyBladeItem extends Item {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 25, 15f, 0, 24, TagKey.create(Registries.ITEM, Identifier.parse("vestigia:ruby_blade_repair_items")));

	public RubyBladeItem(Item.Properties properties) {
		super(properties.sword(TOOL_MATERIAL, 7f, -2.5f).rarity(Rarity.RARE).fireResistant().component(DataComponents.WEAPON, new Weapon(0)));
	}
}