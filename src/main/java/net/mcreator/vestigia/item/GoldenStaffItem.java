package net.mcreator.vestigia.item;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class GoldenStaffItem extends Item {
	public GoldenStaffItem(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC).durability(5).repairable(TagKey.create(Registries.ITEM, Identifier.parse("vestigia:golden_staff_repair_items"))).fireResistant().enchantable(22));
	}

	@Override
	public float getDestroySpeed(ItemStack itemstack, BlockState state) {
		return 1.5f;
	}
}