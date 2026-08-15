/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.vestigia.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.client.Minecraft;

import net.mcreator.vestigia.world.inventory.*;
import net.mcreator.vestigia.network.MenuStateUpdateMessage;
import net.mcreator.vestigia.VestigiaMod;

import java.util.Map;

public class VestigiaModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(Registries.MENU, VestigiaMod.MODID);
	public static final DeferredHolder<MenuType<?>, MenuType<SieveGUIMenu>> SIEVE_GUI = REGISTRY.register("sieve_gui", () -> IMenuTypeExtension.create(SieveGUIMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<RestorerGUIMenu>> RESTORER_GUI = REGISTRY.register("restorer_gui", () -> IMenuTypeExtension.create(RestorerGUIMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<WishGUIMenu>> WISH_GUI = REGISTRY.register("wish_gui", () -> IMenuTypeExtension.create(WishGUIMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<ReverberationGUIMenu>> REVERBERATION_GUI = REGISTRY.register("reverberation_gui", () -> IMenuTypeExtension.create(ReverberationGUIMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<AltarGUIMenu>> ALTAR_GUI = REGISTRY.register("altar_gui", () -> IMenuTypeExtension.create(AltarGUIMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<StellarEmblemGUIMenu>> STELLAR_EMBLEM_GUI = REGISTRY.register("stellar_emblem_gui", () -> IMenuTypeExtension.create(StellarEmblemGUIMenu::new));
	public static final DeferredHolder<MenuType<?>, MenuType<VestigiaBookGUIMenu>> VESTIGIA_BOOK_GUI = REGISTRY.register("vestigia_book_gui", () -> IMenuTypeExtension.create(VestigiaBookGUIMenu::new));

	public interface MenuAccessor {
		Map<String, Object> getMenuState();

		Map<Integer, Slot> getSlots();

		default void sendMenuStateUpdate(Player player, int elementType, String name, Object elementState, boolean needClientUpdate) {
			getMenuState().put(elementType + ":" + name, elementState);
			if (player instanceof ServerPlayer serverPlayer) {
				PacketDistributor.sendToPlayer(serverPlayer, new MenuStateUpdateMessage(elementType, name, elementState));
			} else if (player.level().isClientSide()) {
				if (Minecraft.getInstance().screen instanceof VestigiaModScreens.ScreenAccessor accessor && needClientUpdate)
					accessor.updateMenuState(elementType, name, elementState);
				ClientPacketDistributor.sendToServer(new MenuStateUpdateMessage(elementType, name, elementState));
			}
		}

		default <T> T getMenuState(int elementType, String name, T defaultValue) {
			try {
				return (T) getMenuState().getOrDefault(elementType + ":" + name, defaultValue);
			} catch (ClassCastException e) {
				return defaultValue;
			}
		}
	}
}