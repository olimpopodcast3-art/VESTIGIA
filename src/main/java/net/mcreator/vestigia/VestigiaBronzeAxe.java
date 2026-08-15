package net.mcreator.vestigia;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.mcreator.vestigia.entity.BronzeAxeEntity;
import net.mcreator.vestigia.item.BronzeVoltiveAxeItem;

@EventBusSubscriber(modid = "vestigia")
public class VestigiaBronzeAxe {
	@SubscribeEvent
	public static void onThrow(PlayerInteractEvent.RightClickItem event) {
		ItemStack stack = event.getItemStack();
		if (!(stack.getItem() instanceof BronzeVoltiveAxeItem))
			return;
		Player player = event.getEntity();
		Level world = player.level();
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
		if (player.getCooldowns().isOnCooldown(stack))
			return;
		player.getCooldowns().addCooldown(stack, 15);
		if (!world.isClientSide()) {
			BronzeAxeEntity axe = new BronzeAxeEntity(world, player, stack.copyWithCount(1));
			world.addFreshEntity(axe);
			stack.shrink(1);
		}
		world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TRIDENT_THROW.value(), SoundSource.PLAYERS, 0.9F, 0.9F);
	}
}
