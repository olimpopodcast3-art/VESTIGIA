/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.vestigia.init;

import net.neoforged.neoforge.transfer.item.WorldlyContainerWrapper;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;
import net.minecraft.core.registries.BuiltInRegistries;

import net.mcreator.vestigia.block.entity.SieveBlockEntity;
import net.mcreator.vestigia.block.entity.ReverberationAnvilBlockEntity;
import net.mcreator.vestigia.block.entity.RestorerBlockEntity;
import net.mcreator.vestigia.block.entity.AltarBlockEntity;
import net.mcreator.vestigia.VestigiaMod;

@EventBusSubscriber
public class VestigiaModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, VestigiaMod.MODID);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SieveBlockEntity>> SIEVE = register("sieve", VestigiaModBlocks.SIEVE, SieveBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RestorerBlockEntity>> RESTORER = register("restorer", VestigiaModBlocks.RESTORER, RestorerBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ReverberationAnvilBlockEntity>> REVERBERATION_ANVIL = register("reverberation_anvil", VestigiaModBlocks.REVERBERATION_ANVIL, ReverberationAnvilBlockEntity::new);
	public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<AltarBlockEntity>> ALTAR = register("altar", VestigiaModBlocks.ALTAR, AltarBlockEntity::new);

	// Start of user code block custom block entities
	// End of user code block custom block entities
	private static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> register(String registryname, DeferredHolder<Block, Block> block, BlockEntityType.BlockEntitySupplier<T> supplier) {
		return REGISTRY.register(registryname, () -> new BlockEntityType(supplier, block.get()));
	}

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerBlockEntity(Capabilities.Item.BLOCK, SIEVE.get(), WorldlyContainerWrapper::new);
		event.registerBlockEntity(Capabilities.Item.BLOCK, RESTORER.get(), WorldlyContainerWrapper::new);
		event.registerBlockEntity(Capabilities.Item.BLOCK, REVERBERATION_ANVIL.get(), WorldlyContainerWrapper::new);
		event.registerBlockEntity(Capabilities.Item.BLOCK, ALTAR.get(), WorldlyContainerWrapper::new);
	}
}