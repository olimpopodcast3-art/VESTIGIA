/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.vestigia.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.mcreator.vestigia.VestigiaMod;

public class VestigiaModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, VestigiaMod.MODID);
	public static final DeferredHolder<CreativeModeTab, CreativeModeTab> VESTIGIA = REGISTRY.register("vestigia",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.vestigia.vestigia")).icon(() -> new ItemStack(VestigiaModItems.COLDHORSEHAIRBRUSH.get())).displayItems((parameters, tabData) -> {
				tabData.accept(VestigiaModBlocks.ASH_DEPOSIT.get().asItem());
				tabData.accept(VestigiaModBlocks.BRONZE_DEPOSIT.get().asItem());
				tabData.accept(VestigiaModBlocks.MUD_DEPOSIT.get().asItem());
				tabData.accept(VestigiaModItems.COLDHORSEHAIRBRUSH.get());
				tabData.accept(VestigiaModItems.MUD_SEDIMENT.get());
				tabData.accept(VestigiaModItems.BRONZE_SEDIMENT.get());
				tabData.accept(VestigiaModItems.ASH_SEDIMENT.get());
				tabData.accept(VestigiaModBlocks.SIEVE.get().asItem());
				tabData.accept(VestigiaModBlocks.RESTORER.get().asItem());
				tabData.accept(VestigiaModItems.CHISEL.get());
				tabData.accept(VestigiaModItems.CERAMIC_FRAGMENT.get());
				tabData.accept(VestigiaModItems.METAL_SPLINTER.get());
				tabData.accept(VestigiaModItems.BROKEN_METAL_HOE.get());
				tabData.accept(VestigiaModItems.METAL_HOE.get());
				tabData.accept(VestigiaModItems.OXIDIZED_BRONZE_SPLINTER.get());
				tabData.accept(VestigiaModItems.REFUGEBELLBROKEN.get());
				tabData.accept(VestigiaModItems.REFUGE_BELL.get());
				tabData.accept(VestigiaModItems.ASH_SPLINTER.get());
				tabData.accept(VestigiaModItems.BROKEN_ASH_SWORD.get());
				tabData.accept(VestigiaModItems.ASH_SWORD.get());
				tabData.accept(VestigiaModItems.BROKEN_ASH_PICKAXE.get());
				tabData.accept(VestigiaModItems.ASH_PICKAXE.get());
				tabData.accept(VestigiaModItems.COAL_SPLINTER.get());
				tabData.accept(VestigiaModItems.COAL_HOE_BROKEN.get());
				tabData.accept(VestigiaModItems.COAL_HOE.get());
				tabData.accept(VestigiaModItems.OBSIDIAN_SPLINTER.get());
				tabData.accept(VestigiaModItems.ECHO.get());
				tabData.accept(VestigiaModItems.IRON_SHARD.get());
				tabData.accept(VestigiaModItems.IRON_DAGGER.get());
				tabData.accept(VestigiaModItems.GOLD_SPLINTER.get());
				tabData.accept(VestigiaModItems.GOLDEN_SWORD.get());
				tabData.accept(VestigiaModItems.ENDER_SPLINTER.get());
				tabData.accept(VestigiaModItems.RUBY_SPLINTER.get());
				tabData.accept(VestigiaModItems.RUBY.get());
				tabData.accept(VestigiaModItems.UNCHARGED_RUBY_BLADE.get());
				tabData.accept(VestigiaModItems.RUBY_BLADE.get());
				tabData.accept(VestigiaModItems.AURORA_SPLINTER.get());
				tabData.accept(VestigiaModItems.AURORA_KNIFE_BROKEN.get());
				tabData.accept(VestigiaModItems.AURORA_KNIFE.get());
				tabData.accept(VestigiaModItems.MYTHIC_SPLINTER.get());
				tabData.accept(VestigiaModItems.CAPIBARA_OF_WISDOM.get());
				tabData.accept(VestigiaModItems.UNCHARGED_GOLDEN_STAFF.get());
				tabData.accept(VestigiaModItems.GOLDEN_STAFF.get());
				tabData.accept(VestigiaModItems.WATER_SPLINTER.get());
				tabData.accept(VestigiaModItems.AMBER.get());
				tabData.accept(VestigiaModItems.AMBERPEARL.get());
				tabData.accept(VestigiaModItems.BRONZE.get());
				tabData.accept(VestigiaModItems.STRATIGRAPHIC_SHOVEL.get());
				tabData.accept(VestigiaModItems.BRONZE_VOLTIVE_AXE.get());
				tabData.accept(VestigiaModItems.PERCUSSION_HAMMER_BROKEN.get());
				tabData.accept(VestigiaModItems.PERCUSSION_HAMMER.get());
				tabData.accept(VestigiaModItems.HOOK_BROKEN.get());
				tabData.accept(VestigiaModItems.HOOK.get());
				tabData.accept(VestigiaModItems.STONE_MASON_PICK_BROKEN.get());
				tabData.accept(VestigiaModItems.STONE_MASON_PICKAXE.get());
				tabData.accept(VestigiaModItems.FLINT_SPLINTER.get());
				tabData.accept(VestigiaModItems.CRACKED_FLINT.get());
				tabData.accept(VestigiaModItems.REPAIRED_FLINT.get());
				tabData.accept(VestigiaModItems.WORKED_FLINT.get());
				tabData.accept(VestigiaModItems.FLINT_AXE.get());
				tabData.accept(VestigiaModItems.BOOMERANG.get());
				tabData.accept(VestigiaModItems.SLINGSHOT.get());
				tabData.accept(VestigiaModItems.PHANTOM_SPEAR.get());
				tabData.accept(VestigiaModItems.OFFERING_KNIFE.get());
				tabData.accept(VestigiaModItems.BELL_RINGERS_MACE.get());
				tabData.accept(VestigiaModItems.PORTADOR_SPAWN_EGG.get());
				tabData.accept(VestigiaModItems.WATER_ELEMENT_SWORD.get());
				tabData.accept(VestigiaModItems.UNCHARGED_RUBY_BLADE_BROKEN.get());
				tabData.accept(VestigiaModItems.EMPTY_WATER_SWORD.get());
				tabData.accept(VestigiaModItems.SEAL_1.get());
				tabData.accept(VestigiaModItems.SEAL_1_OFF.get());
				tabData.accept(VestigiaModItems.SEAL_2.get());
				tabData.accept(VestigiaModItems.SEAL_2_OFF.get());
				tabData.accept(VestigiaModItems.SEAL_3.get());
				tabData.accept(VestigiaModItems.SEAL_3_OFF.get());
				tabData.accept(VestigiaModItems.SEAL_Z.get());
				tabData.accept(VestigiaModItems.SEAL_Z_OFF.get());
				tabData.accept(VestigiaModItems.STELLAR_SEAL.get());
				tabData.accept(VestigiaModItems.STELLAR_SEAL_OFF.get());
				tabData.accept(VestigiaModBlocks.REVERBERATION_ANVIL.get().asItem());
				tabData.accept(VestigiaModItems.FIRE_EMBLEM.get());
				tabData.accept(VestigiaModItems.TWISTED_EMBLEM.get());
				tabData.accept(VestigiaModItems.AZTEC_STATUE.get());
				tabData.accept(VestigiaModItems.AZTEC_EMBLEM.get());
				tabData.accept(VestigiaModBlocks.ALTAR_DE_INVOCACION_NIVEL_1.get().asItem());
				tabData.accept(VestigiaModBlocks.ALTAR_DE_INVOCACION_NIVEL_2.get().asItem());
				tabData.accept(VestigiaModBlocks.ALTAR.get().asItem());
				tabData.accept(VestigiaModItems.JADE_EMBLEM.get());
				tabData.accept(VestigiaModItems.RUBY_EMBLEM.get());
				tabData.accept(VestigiaModItems.GOLDEN_EMBLEM.get());
				tabData.accept(VestigiaModItems.BLACK_JADE_EMBLEM.get());
				tabData.accept(VestigiaModItems.SEAL_EMBLEM.get());
				tabData.accept(VestigiaModItems.AURORA_EMBLEM.get());
				tabData.accept(VestigiaModItems.MACAHUITL.get());
				tabData.accept(VestigiaModItems.PEPPER.get());
				tabData.accept(VestigiaModItems.CORN.get());
				tabData.accept(VestigiaModItems.ANCIENT_FUIR.get());
				tabData.accept(VestigiaModItems.VESTIGIA_BOOK.get());
			}).build());
}