package net.mcreator.vestigia.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

import net.mcreator.vestigia.entity.BoomerangEntity;

public class BoomerangItem extends Item {
	public BoomerangItem(Item.Properties properties) {
		super(properties.rarity(Rarity.RARE).durability(500).repairable(TagKey.create(Registries.ITEM, Identifier.parse("vestigia:boomerang_repair_items"))));
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		ItemStack stack = player.getItemInHand(hand);
		player.getCooldowns().addCooldown(stack, 12);
		if (!world.isClientSide()) {
			BoomerangEntity boomerang = new BoomerangEntity(world, player, stack.copyWithCount(1));
			world.addFreshEntity(boomerang);
			stack.shrink(1);
		}
		world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.6F, 1.2F);
		return InteractionResult.SUCCESS;
	}
}
