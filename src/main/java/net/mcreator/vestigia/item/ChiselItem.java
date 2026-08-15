package net.mcreator.vestigia.item;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class ChiselItem extends Item {
	public ChiselItem(Item.Properties properties) {
		super(properties.rarity(Rarity.UNCOMMON).durability(150).repairable(TagKey.create(Registries.ITEM, Identifier.parse("vestigia:chisel_repair_items"))).enchantable(22));
	}

	@Override
	public float getDestroySpeed(ItemStack itemstack, BlockState state) {
		return 2f;
	}
}