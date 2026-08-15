/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.vestigia.init;

import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.mcreator.vestigia.client.gui.*;

@EventBusSubscriber(Dist.CLIENT)
public class VestigiaModScreens {
	@SubscribeEvent
	public static void clientLoad(RegisterMenuScreensEvent event) {
		event.register(VestigiaModMenus.SIEVE_GUI.get(), SieveGUIScreen::new);
		event.register(VestigiaModMenus.RESTORER_GUI.get(), RestorerGUIScreen::new);
		event.register(VestigiaModMenus.WISH_GUI.get(), WishGUIScreen::new);
		event.register(VestigiaModMenus.REVERBERATION_GUI.get(), ReverberationGUIScreen::new);
		event.register(VestigiaModMenus.ALTAR_GUI.get(), AltarGUIScreen::new);
		event.register(VestigiaModMenus.STELLAR_EMBLEM_GUI.get(), StellarEmblemGUIScreen::new);
		event.register(VestigiaModMenus.VESTIGIA_BOOK_GUI.get(), VestigiaBookGUIScreen::new);
	}

	public interface ScreenAccessor {
		void updateMenuState(int elementType, String name, Object elementState);
	}
}