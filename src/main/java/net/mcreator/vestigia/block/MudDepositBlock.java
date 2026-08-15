package net.mcreator.vestigia.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BrushableBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.BlockPos;

import com.mojang.serialization.MapCodec;

public class MudDepositBlock extends BrushableBlock {
	public static final MapCodec<MudDepositBlock> CODEC = simpleCodec(MudDepositBlock::new);

	@Override
	@SuppressWarnings("unchecked")
	public MapCodec<BrushableBlock> codec() {
		return (MapCodec<BrushableBlock>) (MapCodec<?>) CODEC;
	}

	public MudDepositBlock(BlockBehaviour.Properties properties) {
		super(Blocks.AIR, SoundEvents.BRUSH_SAND, SoundEvents.BRUSH_SAND_COMPLETED,
				properties.sound(SoundType.MUD).strength(1f, 7f).requiresCorrectToolForDrops());
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.getBlockEntity(pos) instanceof BrushableBlockEntity be) {
			be.checkReset(level);
		}
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
		super.onPlace(state, level, pos, oldState, movedByPiston);
		if (level instanceof ServerLevel server && server.getBlockEntity(pos) instanceof BrushableBlockEntity be) {
			be.setLootTable(ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse("vestigia:brushing/mud_deposit")), pos.asLong());
		}
	}
}
