package net.mcreator.vestigia;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import net.mcreator.vestigia.item.VestigiaBookItem;
import net.mcreator.vestigia.world.inventory.VestigiaBookGUIMenu;

import io.netty.buffer.Unpooled;

@EventBusSubscriber(modid = "vestigia")
public class VestigiaBook {
	@SubscribeEvent
	public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer p))
			return;
		if (p.getPersistentData().getBooleanOr("vestigia_book_given", false))
			return;
		p.getPersistentData().putBoolean("vestigia_book_given", true);
		Item book = BuiltInRegistries.ITEM.getValue(Identifier.parse("vestigia:vestigia_book"));
		if (book == null)
			return;
		ItemStack st = new ItemStack(book);
		if (!p.addItem(st))
			p.drop(st, false);
	}

	@SubscribeEvent
	public static void onUse(PlayerInteractEvent.RightClickItem event) {
		if (event.getHand() != InteractionHand.MAIN_HAND || !(event.getItemStack().getItem() instanceof VestigiaBookItem))
			return;
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
		if (event.getEntity() instanceof ServerPlayer p)
			p.openMenu(new MenuProvider() {
				@Override
				public Component getDisplayName() {
					return Component.literal("Vestigia");
				}

				@Override
				public AbstractContainerMenu createMenu(int id, Inventory inv, Player pl) {
					return new VestigiaBookGUIMenu(id, inv, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(p.blockPosition()));
				}
			}, p.blockPosition());
	}
}
