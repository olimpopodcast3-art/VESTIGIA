package net.mcreator.vestigia.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

import net.mcreator.vestigia.entity.PebbleEntity;

public class SlingshotItem extends Item {
	public SlingshotItem(Item.Properties properties) {
		super(properties.rarity(Rarity.EPIC).durability(700).repairable(TagKey.create(Registries.ITEM, Identifier.parse("vestigia:slingshot_repair_items"))).enchantable(22));
	}

	@Override
	public ItemUseAnimation getUseAnimation(ItemStack itemstack) {
		return ItemUseAnimation.BOW;
	}

	@Override
	public int getUseDuration(ItemStack itemstack, LivingEntity livingEntity) {
		return 72000;
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		if (!hasAmmo(player))
			return InteractionResult.FAIL;
		player.startUsingItem(hand);
		return InteractionResult.CONSUME;
	}

	@Override
	public boolean releaseUsing(ItemStack stack, Level world, LivingEntity entity, int remainingTime) {
		if (!(entity instanceof Player player))
			return false;
		int held = this.getUseDuration(stack, entity) - remainingTime;
		if (held < 3)
			return false;
		if (!world.isClientSide()) {
			if (!consumeAmmo(player))
				return false;
			float frac = Math.min(held / 20.0F, 1.0F);
			float power = 0.8F + frac * 2.2F;
			PebbleEntity pebble = new PebbleEntity(world, player, new ItemStack(Items.COBBLESTONE));
			pebble.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, power, 1.0F);
			world.addFreshEntity(pebble);
			world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.8F, 0.9F + frac * 0.5F);
		}
		return true;
	}

	private static boolean hasAmmo(Player player) {
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack s = inv.getItem(i);
			if (s.is(Items.COBBLESTONE) || s.is(Items.STONE))
				return true;
		}
		return false;
	}

	private static boolean consumeAmmo(Player player) {
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack s = inv.getItem(i);
			if (s.is(Items.COBBLESTONE) || s.is(Items.STONE)) {
				s.shrink(1);
				return true;
			}
		}
		return false;
	}
}
