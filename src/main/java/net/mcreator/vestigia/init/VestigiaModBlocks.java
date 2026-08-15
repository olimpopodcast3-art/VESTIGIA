/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.vestigia.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredBlock;

import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;

import net.mcreator.vestigia.block.*;
import net.mcreator.vestigia.VestigiaMod;

import java.util.function.Function;

public class VestigiaModBlocks {
	public static final DeferredRegister.Blocks REGISTRY = DeferredRegister.createBlocks(VestigiaMod.MODID);
	public static final DeferredBlock<Block> ASH_DEPOSIT;
	public static final DeferredBlock<Block> BRONZE_DEPOSIT;
	public static final DeferredBlock<Block> MUD_DEPOSIT;
	public static final DeferredBlock<Block> SIEVE;
	public static final DeferredBlock<Block> RESTORER;
	public static final DeferredBlock<Block> REVERBERATION_ANVIL;
	public static final DeferredBlock<Block> ALTAR_DE_INVOCACION_NIVEL_1;
	public static final DeferredBlock<Block> ALTAR_DE_INVOCACION_NIVEL_2;
	public static final DeferredBlock<Block> ALTAR;
	static {
		ASH_DEPOSIT = register("ash_deposit", AshDepositBlock::new);
		BRONZE_DEPOSIT = register("bronze_deposit", BronzeDepositBlock::new);
		MUD_DEPOSIT = register("mud_deposit", MudDepositBlock::new);
		SIEVE = register("sieve", SieveBlock::new);
		RESTORER = register("restorer", RestorerBlock::new);
		REVERBERATION_ANVIL = register("reverberation_anvil", ReverberationAnvilBlock::new);
		ALTAR_DE_INVOCACION_NIVEL_1 = register("altar_de_invocacion_nivel_1", AltarDeInvocacionNivel1Block::new);
		ALTAR_DE_INVOCACION_NIVEL_2 = register("altar_de_invocacion_nivel_2", AltarDeInvocacionNivel2Block::new);
		ALTAR = register("altar", AltarBlock::new);
	}

	// Start of user code block custom blocks
	// End of user code block custom blocks
	private static <B extends Block> DeferredBlock<B> register(String name, Function<BlockBehaviour.Properties, ? extends B> supplier) {
		return REGISTRY.registerBlock(name, supplier);
	}
}