package net.mcreator.vestigia.item.inventory;

import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemAccessItemHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.event.entity.item.ItemTossEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponents;

import net.mcreator.vestigia.world.inventory.VestigiaBookGUIMenu;
import net.mcreator.vestigia.init.VestigiaModItems;

@EventBusSubscriber
public class VestigiaBookInventoryCapability extends ItemAccessItemHandler {
	@SubscribeEvent
	public static void onItemDropped(ItemTossEvent event) {
		if (event.getEntity().getItem().getItem() == VestigiaModItems.VESTIGIA_BOOK.get()) {
			Player player = event.getPlayer();
			if (player.containerMenu instanceof VestigiaBookGUIMenu)
				player.closeContainer();
		}
	}

	public VestigiaBookInventoryCapability(ItemAccess access) {
		super(access, DataComponents.CONTAINER, 9);
	}

	@Override
	public boolean isValid(int index, ItemResource resource) {
		return super.isValid(index, resource) && resource.getItem() != VestigiaModItems.VESTIGIA_BOOK.get();
	}
}