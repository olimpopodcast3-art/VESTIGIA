package net.mcreator.vestigia;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@EventBusSubscriber(modid = "vestigia")
public class VestigiaAltarBeam {
	private static final int BEAM_HEIGHT = 48;
	private static final int SCAN_RADIUS = 20;
	private static final Map<ResourceKey<Level>, List<BlockPos>> CACHE = new HashMap<>();

	@SubscribeEvent
	public static void onTick(ServerTickEvent.Post event) {
		for (ServerLevel level : event.getServer().getAllLevels()) {
			long gt = level.getGameTime();
			ResourceKey<Level> key = level.dimension();
			if (gt % 40L == 0L)
				refresh(level, key);
			List<BlockPos> altars = CACHE.get(key);
			if (altars == null || altars.isEmpty())
				continue;
			for (BlockPos p : altars)
				beam(level, p, gt);
		}
	}

	private static void refresh(ServerLevel level, ResourceKey<Level> key) {
		List<BlockPos> found = new ArrayList<>();
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		for (ServerPlayer pl : level.players()) {
			BlockPos c = pl.blockPosition();
			for (int dx = -SCAN_RADIUS; dx <= SCAN_RADIUS; dx++)
				for (int dy = -SCAN_RADIUS; dy <= SCAN_RADIUS; dy++)
					for (int dz = -SCAN_RADIUS; dz <= SCAN_RADIUS; dz++) {
						m.set(c.getX() + dx, c.getY() + dy, c.getZ() + dz);
						Identifier id = BuiltInRegistries.BLOCK.getKey(level.getBlockState(m).getBlock());
						if (id != null && (id.getPath().equals("altar_de_invocacion_nivel_1") || id.getPath().equals("altar_de_invocacion_nivel_2"))) {
							BlockPos ip = m.immutable();
							if (!found.contains(ip))
								found.add(ip);
						}
					}
		}
		CACHE.put(key, found);
	}

	private static void beam(ServerLevel level, BlockPos p, long gt) {
		RandomSource rnd = level.getRandom();
		double cx = p.getX() + 0.5, cz = p.getZ() + 0.5, base = p.getY() + 0.9;
		for (int i = 0; i < 10; i++) {
			double h = rnd.nextDouble() * BEAM_HEIGHT;
			level.sendParticles(ParticleTypes.END_ROD, cx + (rnd.nextDouble() - 0.5) * 0.16, base + h, cz + (rnd.nextDouble() - 0.5) * 0.16, 1, 0.0, 0.02, 0.0, 0.0);
		}
		if (gt % 3L == 0L) {
			level.sendParticles(ParticleTypes.GLOW, cx, base, cz, 2, 0.12, 0.1, 0.12, 0.0);
			level.sendParticles(ParticleTypes.FIREWORK, cx, base + 0.5, cz, 1, 0.06, 0.3, 0.06, 0.0);
		}
	}
}
