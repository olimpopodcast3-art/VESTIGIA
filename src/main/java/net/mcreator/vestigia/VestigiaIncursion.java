package net.mcreator.vestigia;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;

import net.mcreator.vestigia.entity.CaveManEntity;
import net.mcreator.vestigia.entity.GladiatorEntity;
import net.mcreator.vestigia.entity.GoldenKnightEntity;
import net.mcreator.vestigia.entity.MexicaEntity;
import net.mcreator.vestigia.entity.MummyEntity;
import net.mcreator.vestigia.entity.PharaohEntity;
import net.mcreator.vestigia.entity.PortadorEntity;
import net.mcreator.vestigia.entity.RomanEntity;
import net.mcreator.vestigia.entity.TheZagalEntity;
import net.mcreator.vestigia.init.VestigiaModEntities;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = "vestigia")
public class VestigiaIncursion {
	private static final String ALTAR1 = "altar_de_invocacion_nivel_1";
	private static final String ALTAR2 = "altar_de_invocacion_nivel_2";
	private static final int DURATION = 4020;
	private static final int BOSS_ELAPSED = 800;
	private static final int ADD_INTERVAL = 300;
	private static final int LEAVE_COUNTDOWN = 100;
	private static final double BARRIER = 20.0;
	private static final double PARTICIPATE = 40.0;
	private static final double ARENA = 96.0;

	private static final Map<GlobalPos, Inc> ACTIVE = new HashMap<>();
	private static final Set<GlobalPos> WARMING = new HashSet<>();

	private static class Inc {
		int tier;
		String sealPath;
		long startTime;
		long endTime;
		int elapsed;
		int players = 1;
		boolean bossSpawned;
		boolean fury;
		boolean warning;
		int leaveGrace;
		float bossHp;
		float bossMaxHp;
		UUID bossId;
		ServerBossEvent bar;
		final Set<UUID> participants = new HashSet<>();
		final Set<UUID> spawned = new HashSet<>();
	}

	private static String posKey(BlockPos pos) {
		return pos.getX() + "_" + pos.getY() + "_" + pos.getZ();
	}

	private static AABB arena(BlockPos pos, double r) {
		return new AABB(pos).inflate(r);
	}

	private static int tierOf(String sealPath) {
		switch (sealPath) {
			case "seal_2_off":
				return 2;
			case "seal_3_off":
				return 3;
			case "seal_z_off":
				return 4;
			case "stellar_seal_off":
				return 5;
			default:
				return 1;
		}
	}

	private static float bossHpFor(int tier) {
		switch (tier) {
			case 2:
				return 160.0F;
			case 3:
				return 300.0F;
			case 4:
				return 500.0F;
			case 5:
				return 600.0F;
			default:
				return 80.0F;
		}
	}

	private static String emblemFor(String sealPath) {
		switch (sealPath) {
			case "seal_2_off":
				return "fire_emblem";
			case "seal_3_off":
				return "aurora_emblem";
			case "seal_z_off":
				return "golden_emblem";
			case "stellar_seal_off":
				return "seal_emblem";
			default:
				return "twisted_emblem";
		}
	}

	@SubscribeEvent
	public static void onAltarUse(PlayerInteractEvent.RightClickBlock event) {
		Level lvl = event.getLevel();
		BlockPos pos = event.getPos();
		Identifier bid = BuiltInRegistries.BLOCK.getKey(lvl.getBlockState(pos).getBlock());
		if (bid == null)
			return;
		int altar;
		if (bid.getPath().equals(ALTAR1))
			altar = 1;
		else if (bid.getPath().equals(ALTAR2))
			altar = 2;
		else
			return;
		ItemStack held = event.getItemStack();
		Identifier iid = BuiltInRegistries.ITEM.getKey(held.getItem());
		String sealPath = iid == null ? "" : iid.getPath();
		boolean valid = altar == 1
				? (sealPath.equals("seal_1_off") || sealPath.equals("seal_2_off") || sealPath.equals("seal_3_off"))
				: (sealPath.equals("seal_z_off") || sealPath.equals("stellar_seal_off"));
		if (!valid) {
			if (!lvl.isClientSide() && (sealPath.startsWith("seal_") || sealPath.startsWith("stellar_")))
				event.getEntity().sendSystemMessage(Component.literal(altar == 1 ? "§7This altar uses spent Seals 1, 2 or 3" : "§7This altar uses a spent Seal Z or Black Hole"));
			return;
		}
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
		if (!(lvl instanceof ServerLevel sl))
			return;
		GlobalPos gp = GlobalPos.of(sl.dimension(), pos.immutable());
		if (ACTIVE.containsKey(gp) || WARMING.contains(gp)) {
			event.getEntity().sendSystemMessage(Component.literal("§7An Echo Incursion is already active here"));
			return;
		}
		held.shrink(1);
		warmup(sl, pos, gp, sealPath);
	}

	private static void warmup(ServerLevel sl, BlockPos pos, GlobalPos gp, String sealPath) {
		WARMING.add(gp);
		int level = tierOf(sealPath) >= 4 ? 2 : 1;
		long introEnd = sl.getGameTime() + 100L;
		broadcastIntro(sl, pos, introEnd, level);
		sl.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.2F, 0.6F);
		for (int s = 0; s < 5; s++) {
			final float pitch = 0.7F + s * 0.14F;
			VestigiaMod.queueServerWork(s * 20, () -> sl.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.HOSTILE, 2.4F, pitch));
		}
		for (int t = 0; t <= 100; t += 2) {
			final int tick = t;
			VestigiaMod.queueServerWork(t, () -> warmupTick(sl, pos, level, tick));
		}
		if (level >= 2) {
			VestigiaMod.queueServerWork(2, () -> setTime(sl, 23000));
			VestigiaMod.queueServerWork(27, () -> setTime(sl, 6000));
			VestigiaMod.queueServerWork(52, () -> setTime(sl, 12000));
			VestigiaMod.queueServerWork(77, () -> setTime(sl, 18000));
		}
		VestigiaMod.queueServerWork(100, () -> {
			WARMING.remove(gp);
			LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, sl);
			bolt.snapTo(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 0.0F, 0.0F);
			bolt.setVisualOnly(true);
			sl.addFreshEntity(bolt);
			sl.playSound(null, pos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.HOSTILE, 3.0F, 1.0F);
			start(sl, pos, gp, sealPath);
		});
	}

	private static void warmupTick(ServerLevel sl, BlockPos pos, int level, int t) {
		double cx = pos.getX() + 0.5, cy = pos.getY() + 1.0, cz = pos.getZ() + 0.5;
		float frac = t / 100.0F;
		double r = frac * BARRIER;
		int pts = 44;
		for (int i = 0; i < pts; i++) {
			double a = Math.PI * 2 * i / pts + t * 0.05;
			sl.sendParticles(new DustParticleOptions(0x8A5CFF, 1.5F), cx + Math.cos(a) * r, cy, cz + Math.sin(a) * r, 1, 0.0, 0.0, 0.0, 0.0);
		}
		sl.sendParticles(ParticleTypes.END_ROD, cx, cy + frac * 4.0, cz, 2, 0.08, 0.2, 0.08, 0.01);
		for (int i = 0; i < 20; i++) {
			double a = Math.PI * 2 * i / 20 + t * 0.08;
			double h = sl.getRandom().nextDouble() * (0.5 + frac * 6.0);
			sl.sendParticles(new DustParticleOptions(0x9B5CFF, 1.2F), cx + Math.cos(a) * BARRIER, cy + h, cz + Math.sin(a) * BARRIER, 1, 0.0, 0.0, 0.0, 0.0);
		}
		if (level >= 2)
			riftTentacles(sl, cx, cy + 5.0, cz, t);
	}

	private static void riftTentacles(ServerLevel sl, double x, double y, double z, int t) {
		for (int i = 0; i < 22; i++) {
			double a = Math.PI * 2 * i / 22 + t * 0.2;
			sl.sendParticles(new DustParticleOptions(0xE0A030, 1.6F), x + Math.cos(a) * 2.5, y, z + Math.sin(a) * 2.5, 1, 0.0, 0.0, 0.0, 0.0);
			sl.sendParticles(new DustParticleOptions(0xFF6A00, 1.3F), x + Math.cos(a) * 1.7, y, z + Math.sin(a) * 1.7, 1, 0.0, 0.0, 0.0, 0.0);
		}
		for (int arm = 0; arm < 6; arm++) {
			double baseA = arm * (Math.PI * 2 / 6) + t * 0.04;
			for (double s = 0.0; s <= 3.2; s += 0.4) {
				double wob = Math.sin(t * 0.2 + s * 2.0) * 0.7;
				double a = baseA + wob;
				double rr = 0.6 + s;
				sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x + Math.cos(a) * rr, y - s * 0.9, z + Math.sin(a) * rr, 1, 0.0, 0.0, 0.0, 0.01);
			}
		}
		sl.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 4, 0.4, 0.4, 0.4, 0.05);
	}

	private static void setTime(ServerLevel sl, int time) {
		MinecraftServer server = sl.getServer();
		if (server != null)
			server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), "time set " + time);
	}

	private static void broadcastIntro(ServerLevel sl, BlockPos pos, long introEnd, int level) {
		IncursionIntroPayload p = new IncursionIntroPayload(introEnd, level);
		for (Player pl : sl.getEntitiesOfClass(Player.class, arena(pos, PARTICIPATE), Player::isAlive))
			if (pl instanceof ServerPlayer sp)
				PacketDistributor.sendToPlayer(sp, p);
	}

	private static void start(ServerLevel sl, BlockPos pos, GlobalPos gp, String sealPath) {
		Inc inc = new Inc();
		inc.tier = tierOf(sealPath);
		inc.sealPath = sealPath;
		inc.startTime = sl.getGameTime();
		inc.endTime = inc.startTime + DURATION;
		inc.players = Math.max(1, sl.getEntitiesOfClass(Player.class, arena(pos, PARTICIPATE), Player::isAlive).size());
		ACTIVE.put(gp, inc);
		spawnWave(sl, pos, inc);
		announce(sl, pos, "§b⚡ You create a temporary bubble — survive the Echo Incursion!");
		sl.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 4.0F, 0.5F);
		sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 140, 1.5, 2.5, 1.5, 0.12);
		sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 1, 0, 0, 0, 0);
		broadcastState(sl, pos, inc, true);
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		if (ACTIVE.isEmpty())
			return;
		for (GlobalPos gp : new ArrayList<>(ACTIVE.keySet())) {
			Inc inc = ACTIVE.get(gp);
			ServerLevel sl = event.getServer().getLevel(gp.dimension());
			BlockPos pos = gp.pos();
			if (sl == null || !sl.isLoaded(pos))
				continue;
			Identifier bid = BuiltInRegistries.BLOCK.getKey(sl.getBlockState(pos).getBlock());
			if (bid == null || (!bid.getPath().equals(ALTAR1) && !bid.getPath().equals(ALTAR2))) {
				end(sl, gp, false);
				continue;
			}
			long now = sl.getGameTime();
			inc.elapsed = (int) (now - inc.startTime);
			barrier(sl, pos, now);

			List<Player> near = sl.getEntitiesOfClass(Player.class, arena(pos, PARTICIPATE), p -> p.isAlive() && !p.isSpectator());
			for (Player p : near)
				inc.participants.add(p.getUUID());
			boolean inside = false;
			for (Player p : near)
				if (p.distanceToSqr(pos.getX() + 0.5, p.getY(), pos.getZ() + 0.5) <= BARRIER * BARRIER) {
					inside = true;
					break;
				}
			if (!inside) {
				if (inc.leaveGrace == 0)
					broadcastWarn(sl, inc, true, now + LEAVE_COUNTDOWN);
				inc.warning = true;
				if (++inc.leaveGrace >= LEAVE_COUNTDOWN) {
					end(sl, gp, false);
					continue;
				}
			} else {
				if (inc.warning)
					broadcastWarn(sl, inc, false, 0L);
				inc.warning = false;
				inc.leaveGrace = 0;
			}

			if (now >= inc.endTime) {
				end(sl, gp, false);
				continue;
			}
			if (inc.tier >= 5 && inc.elapsed % 30 == 0)
				rainTnt(sl, pos);
				tickPharaohs(sl, pos, inc);
			if (!inc.bossSpawned && inc.elapsed > 0 && inc.elapsed % ADD_INTERVAL == 0)
				spawnWave(sl, pos, inc);
			if (!inc.bossSpawned && inc.elapsed >= BOSS_ELAPSED) {
				inc.players = Math.max(1, near.size());
				spawnBoss(sl, pos, inc);
				inc.bossSpawned = true;
				broadcastState(sl, pos, inc, true);
			}
			if (inc.bossSpawned)
				tickBoss(sl, pos, gp, inc, near);
			if (inc.elapsed % 20 == 0)
				broadcastState(sl, pos, inc, true);
		}
	}

	private static void tickBoss(ServerLevel sl, BlockPos pos, GlobalPos gp, Inc inc, List<Player> near) {
		Entity be = inc.bossId == null ? null : sl.getEntity(inc.bossId);
		if (!(be instanceof LivingEntity boss) || !boss.isAlive() || inc.bossHp <= 0.0F) {
			end(sl, gp, true);
			return;
		}
		if (inc.bar != null) {
			inc.bar.setProgress(Mth.clamp(inc.bossHp / inc.bossMaxHp, 0.0F, 1.0F));
			inc.bar.removeAllPlayers();
			for (Player p : near)
				if (p instanceof ServerPlayer sp)
					inc.bar.addPlayer(sp);
		}
		if (!inc.fury && inc.bossHp < inc.bossMaxHp * 0.5F) {
			inc.fury = true;
			enterFury(sl, boss);
			if (inc.bar != null)
				inc.bar.setColor(BossEvent.BossBarColor.RED);
		}
		int aoe = inc.fury ? 60 : 100;
		int meteor = inc.fury ? 110 : 200;
		if (inc.elapsed % aoe == 0)
			sonicSlam(sl, boss);
		if (inc.elapsed % meteor == 40)
			meteorStrike(sl, boss);
		if (inc.fury && inc.elapsed % 150 == 75)
			shockwave(sl, boss);
		if (inc.elapsed % 160 == 0)
			for (int i = 0; i < inc.tier; i++)
				spawnMob(sl, pos, inc, 0);
	}

	private static void enterFury(ServerLevel sl, LivingEntity boss) {
		boss.addEffect(new MobEffectInstance(MobEffects.SPEED, 999999, 1, false, false));
		boss.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 999999, 1, false, false));
		sl.sendParticles(ParticleTypes.FLAME, boss.getX(), boss.getY() + 1.0, boss.getZ(), 120, 1.2, 1.2, 1.2, 0.15);
		sl.sendParticles(new DustParticleOptions(0xFF3020, 2.4F), boss.getX(), boss.getY() + 1.0, boss.getZ(), 90, 1.0, 1.2, 1.0, 0.1);
		sl.playSound(null, boss.blockPosition(), SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 3.0F, 1.3F);
	}

	private static void sonicSlam(ServerLevel sl, LivingEntity boss) {
		for (Player p : sl.getEntitiesOfClass(Player.class, boss.getBoundingBox().inflate(7.0), Player::isAlive)) {
			p.hurt(sl.damageSources().mobAttack(boss), 6.0F);
			p.knockback(1.4, boss.getX() - p.getX(), boss.getZ() - p.getZ());
		}
		sl.sendParticles(ParticleTypes.SONIC_BOOM, boss.getX(), boss.getY() + 1.0, boss.getZ(), 1, 0, 0, 0, 0);
		sl.sendParticles(new DustParticleOptions(0xFFD700, 2.0F), boss.getX(), boss.getY() + 1.0, boss.getZ(), 60, 3.0, 1.0, 3.0, 0.1);
		sl.playSound(null, boss.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 2.0F, 1.0F);
	}

	private static void meteorStrike(ServerLevel sl, LivingEntity boss) {
		List<Player> ps = sl.getEntitiesOfClass(Player.class, boss.getBoundingBox().inflate(28.0), Player::isAlive);
		if (ps.isEmpty())
			return;
		Player target = ps.get(sl.getRandom().nextInt(ps.size()));
		double tx = target.getX(), ty = target.getY(), tz = target.getZ();
		for (int t = 0; t <= 30; t++) {
			final int tick = t;
			VestigiaMod.queueServerWork(t, () -> {
				double rad = 3.0 * (1.0 - tick / 30.0) + 0.4;
				int pts = 20;
				for (int i = 0; i < pts; i++) {
					double a = i * (Math.PI * 2 / pts);
					sl.sendParticles(new DustParticleOptions(0xFF5522, 1.4F), tx + Math.cos(a) * rad, ty + 0.1, tz + Math.sin(a) * rad, 1, 0, 0, 0, 0);
				}
				sl.sendParticles(ParticleTypes.SMALL_FLAME, tx, ty + 8.0 - tick * 0.25, tz, 3, 0.2, 0.2, 0.2, 0.0);
			});
		}
		VestigiaMod.queueServerWork(32, () -> {
			sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, tx, ty + 0.3, tz, 1, 0, 0, 0, 0);
			sl.sendParticles(ParticleTypes.FLAME, tx, ty + 0.3, tz, 80, 2.0, 0.6, 2.0, 0.15);
			sl.playSound(null, BlockPos.containing(tx, ty, tz), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.HOSTILE, 2.0F, 0.9F);
			for (Player p : sl.getEntitiesOfClass(Player.class, new AABB(BlockPos.containing(tx, ty, tz)).inflate(3.5), Player::isAlive)) {
				p.hurt(sl.damageSources().magic(), 8.0F);
				p.knockback(0.8, tx - p.getX(), tz - p.getZ());
			}
		});
	}

	private static void shockwave(ServerLevel sl, LivingEntity boss) {
		double cx = boss.getX(), cy = boss.getY(), cz = boss.getZ();
		for (int step = 0; step <= 10; step++) {
			final double rad = 2.0 + step * 1.4;
			VestigiaMod.queueServerWork(step * 2, () -> {
				int pts = (int) (rad * 6);
				for (int i = 0; i < pts; i++) {
					double a = i * (Math.PI * 2 / pts);
					sl.sendParticles(new DustParticleOptions(0xFFC030, 1.5F), cx + Math.cos(a) * rad, cy + 0.2, cz + Math.sin(a) * rad, 1, 0, 0, 0, 0);
				}
				for (Player p : sl.getEntitiesOfClass(Player.class, new AABB(BlockPos.containing(cx, cy, cz)).inflate(rad + 1.5), Player::isAlive)) {
					double d = Math.sqrt(p.distanceToSqr(cx, p.getY(), cz));
					if (Math.abs(d - rad) < 1.6) {
						p.hurt(sl.damageSources().mobAttack(boss), 5.0F);
						p.knockback(1.0, p.getX() - cx, p.getZ() - cz);
					}
				}
			});
		}
		sl.playSound(null, boss.blockPosition(), SoundEvents.WARDEN_SONIC_BOOM, SoundSource.HOSTILE, 2.5F, 0.6F);
	}

	private static void spawnWave(ServerLevel sl, BlockPos pos, Inc inc) {
		int extra = Math.max(0, inc.players - 1);
		int portadores = 2 + extra;
		for (int i = 0; i < portadores; i++)
			spawnMob(sl, pos, inc, 0);
		if (inc.tier >= 2)
			for (int i = 0; i < 1 + extra / 2; i++)
				spawnMob(sl, pos, inc, 1);
		if (inc.tier >= 3)
			spawnMob(sl, pos, inc, 2);
		if (inc.tier == 3) {
			spawnMob(sl, pos, inc, 4);
			spawnMob(sl, pos, inc, 7);
		}
		if (inc.tier >= 4) {
			int eras = 1 + extra / 2;
			for (int i = 0; i < eras; i++) {
				spawnMob(sl, pos, inc, 3);
				spawnMob(sl, pos, inc, 4);
				spawnMob(sl, pos, inc, 5);
				spawnMob(sl, pos, inc, 6);
				spawnMob(sl, pos, inc, 7);
			}
			spawnVanilla(sl, pos, inc, sl.getRandom().nextInt(4));
		}
		sl.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.HOSTILE, 2.0F, 0.7F);
	}

	private static double[] ring(ServerLevel sl, BlockPos pos, double min, double max) {
		double ang = sl.getRandom().nextDouble() * Math.PI * 2;
		double dist = min + sl.getRandom().nextDouble() * (max - min);
		return new double[]{pos.getX() + 0.5 + Math.cos(ang) * dist, pos.getZ() + 0.5 + Math.sin(ang) * dist};
	}

	private static double groundY(ServerLevel sl, double x, double z) {
		return sl.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, (int) Math.floor(x), (int) Math.floor(z));
	}

	private static double spawnYNear(ServerLevel sl, double x, double z, int altarY) {
		int ix = (int) Math.floor(x), iz = (int) Math.floor(z);
		int best = Integer.MIN_VALUE, bestd = 999;
		for (int y = altarY + 3; y >= altarY - 8; y--) {
			BlockPos feet = new BlockPos(ix, y, iz);
			if (sl.getBlockState(feet.below()).blocksMotion() && !sl.getBlockState(feet).blocksMotion() && !sl.getBlockState(feet.above()).blocksMotion()) {
				int d = Math.abs(y - altarY);
				if (d < bestd) {
					bestd = d;
					best = y;
				}
			}
		}
		return best != Integer.MIN_VALUE ? best : Math.max(groundY(sl, x, z), altarY);
	}

	private static void emerge(ServerLevel sl, double x, double y, double z) {
		sl.sendParticles(ParticleTypes.SOUL, x, y + 0.2, z, 30, 0.4, 0.2, 0.4, 0.05);
		sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + 0.2, z, 15, 0.4, 0.2, 0.4, 0.03);
		sl.playSound(null, BlockPos.containing(x, y, z), SoundEvents.SOUL_ESCAPE.value(), SoundSource.HOSTILE, 0.9F, 0.7F);
	}

	private static void spawnMob(ServerLevel sl, BlockPos pos, Inc inc, int type) {
		double[] xz = ring(sl, pos, 4.0, 10.0);
		double gy = spawnYNear(sl, xz[0], xz[1], pos.getY());
		Mob m;
		if (type == 1) {
			m = new GladiatorEntity(VestigiaModEntities.GLADIATOR.get(), sl);
		} else if (type == 2) {
			TheZagalEntity z = new TheZagalEntity(VestigiaModEntities.THE_ZAGAL.get(), sl);
			AttributeInstance spd = z.getAttribute(Attributes.MOVEMENT_SPEED);
			if (spd != null)
				spd.setBaseValue(0.42);
			m = z;
		} else {
			m = switch (type) {
					case 3 -> new MexicaEntity(VestigiaModEntities.MEXICA.get(), sl);
					case 4 -> new CaveManEntity(VestigiaModEntities.CAVE_MAN.get(), sl);
					case 5 -> new MummyEntity(VestigiaModEntities.MUMMY.get(), sl);
					case 6 -> new PharaohEntity(VestigiaModEntities.PHARAOH.get(), sl);
					case 7 -> new RomanEntity(VestigiaModEntities.ROMAN.get(), sl);
					default -> new PortadorEntity(VestigiaModEntities.PORTADOR.get(), sl);
				};
		}
		m.snapTo(xz[0], gy, xz[1], (float) (sl.getRandom().nextDouble() * 360.0), 0.0F);
		m.getPersistentData().putString("vestigia_incursion", posKey(pos));
		m.setPersistenceRequired();
		m.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(m, Player.class, true));
		sl.addFreshEntity(m);
		inc.spawned.add(m.getUUID());
		emerge(sl, xz[0], gy, xz[1]);
	}

	private static void tickPharaohs(ServerLevel sl, BlockPos pos, Inc inc) {
		if (sl.getGameTime() % 20L != 0L)
			return;
		String key = posKey(pos);
		long now = sl.getGameTime();
		for (PharaohEntity ph : sl.getEntitiesOfClass(PharaohEntity.class, arena(pos, ARENA), e -> e.isAlive() && key.equals(e.getPersistentData().getStringOr("vestigia_incursion", "")))) {
			if (now < ph.getPersistentData().getLongOr("vestigia_cast", 0L))
				continue;
			ph.getPersistentData().putLong("vestigia_cast", now + 140L);
			pharaohCast(sl, pos, inc, ph);
		}
	}

	private static void pharaohCast(ServerLevel sl, BlockPos pos, Inc inc, PharaohEntity ph) {
		ph.swing(InteractionHand.MAIN_HAND);
		sl.playSound(null, ph.blockPosition(), SoundEvents.EVOKER_PREPARE_SUMMON, SoundSource.HOSTILE, 1.3F, 0.9F);
		sl.playSound(null, ph.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.HOSTILE, 1.2F, 0.8F);
		sl.sendParticles(ParticleTypes.ENCHANT, ph.getX(), ph.getY() + 2.2, ph.getZ(), 40, 0.5, 0.6, 0.5, 0.8);
		sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, ph.getX(), ph.getY() + 1.5, ph.getZ(), 12, 0.4, 0.5, 0.4, 0.02);
		int count = 1 + sl.getRandom().nextInt(2);
		for (int i = 0; i < count; i++) {
			double ang = sl.getRandom().nextDouble() * Math.PI * 2;
			double d = 2.0 + sl.getRandom().nextDouble() * 3.0;
			double x = ph.getX() + Math.cos(ang) * d, z = ph.getZ() + Math.sin(ang) * d;
			double gy = spawnYNear(sl, x, z, pos.getY());
			MummyEntity mummy = new MummyEntity(VestigiaModEntities.MUMMY.get(), sl);
			mummy.snapTo(x, gy, z, (float) (sl.getRandom().nextDouble() * 360.0), 0.0F);
			mummy.getPersistentData().putString("vestigia_incursion", posKey(pos));
			mummy.setPersistenceRequired();
			mummy.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(mummy, Player.class, true));
			sl.addFreshEntity(mummy);
			inc.spawned.add(mummy.getUUID());
			emerge(sl, x, gy, z);
			sl.sendParticles(ParticleTypes.CRIT, x, gy + 0.2, z, 20, 0.3, 0.1, 0.3, 0.1);
		}
	}

	private static void spawnVanilla(ServerLevel sl, BlockPos pos, Inc inc, int which) {
		double[] xz = ring(sl, pos, 4.0, 10.0);
		double gy = spawnYNear(sl, xz[0], xz[1], pos.getY());
		double y = gy;
		Mob m;
		switch (which) {
			case 1:
				Skeleton sk = new Skeleton(EntityType.SKELETON, sl);
				sk.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
				m = sk;
				break;
			case 2:
				m = new Creeper(EntityType.CREEPER, sl);
				break;
			case 3:
				m = new Phantom(EntityType.PHANTOM, sl);
				y = pos.getY() + 8.0;
				break;
			default:
				m = new Zombie(EntityType.ZOMBIE, sl);
				break;
		}
		m.snapTo(xz[0], y, xz[1], (float) (sl.getRandom().nextDouble() * 360.0), 0.0F);
		m.finalizeSpawn(sl, sl.getCurrentDifficultyAt(m.blockPosition()), EntitySpawnReason.EVENT, null);
		m.getPersistentData().putString("vestigia_incursion", posKey(pos));
		m.setPersistenceRequired();
		sl.addFreshEntity(m);
		inc.spawned.add(m.getUUID());
		if (which != 3)
			emerge(sl, xz[0], gy, xz[1]);
	}

	private static void rainTnt(ServerLevel sl, BlockPos pos) {
		double[] xz = ring(sl, pos, 0.0, BARRIER * 0.9);
		PrimedTnt tnt = new PrimedTnt(sl, xz[0], pos.getY() + 18.0, xz[1], null);
		tnt.setFuse(70 + sl.getRandom().nextInt(25));
		tnt.getPersistentData().putInt("vestigia_inc_tnt", 1);
		sl.addFreshEntity(tnt);
		sl.sendParticles(ParticleTypes.LAVA, xz[0], pos.getY() + 18.0, xz[1], 3, 0.1, 0.1, 0.1, 0.0);
	}

	@SubscribeEvent
	public static void onDetonate(ExplosionEvent.Detonate event) {
		Explosion exp = event.getExplosion();
		Entity src = exp.getDirectSourceEntity();
		if (src == null || src.getPersistentData().getIntOr("vestigia_inc_tnt", 0) != 1)
			return;
		event.getAffectedBlocks().clear();
		event.getAffectedEntities().removeIf(e -> !(e instanceof Player));
	}

	private static void spawnBoss(ServerLevel sl, BlockPos pos, Inc inc) {
		GoldenKnightEntity boss = new GoldenKnightEntity(VestigiaModEntities.GOLDEN_KNIGHT.get(), sl);
		double[] xz = ring(sl, pos, 3.0, 5.0);
		boss.snapTo(xz[0], pos.getY() + 0.5, xz[1], (float) (sl.getRandom().nextDouble() * 360.0), 0.0F);
		boss.getPersistentData().putString("vestigia_incursion", posKey(pos));
		boss.setPersistenceRequired();
		inc.bossMaxHp = bossHpFor(inc.tier) * (1.0F + 0.6F * (inc.players - 1));
		inc.bossHp = inc.bossMaxHp;
		AttributeInstance hp = boss.getAttribute(Attributes.MAX_HEALTH);
		if (hp != null)
			hp.setBaseValue(Math.max(50.0, inc.bossMaxHp));
		AttributeInstance atk = boss.getAttribute(Attributes.ATTACK_DAMAGE);
		if (atk != null)
			atk.setBaseValue(10.0 + inc.tier * 3.0);
		AttributeInstance kbr = boss.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
		if (kbr != null)
			kbr.setBaseValue(1.0);
		boss.setHealth(boss.getMaxHealth());
		boss.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(boss, Player.class, true));
		inc.bossId = boss.getUUID();
		inc.spawned.add(boss.getUUID());
		inc.bar = new ServerBossEvent(UUID.nameUUIDFromBytes(("vestigia_incursion_" + posKey(pos)).getBytes()), Component.literal("§6☀ Ancient Echo §7— §eGolden Knight"), BossEvent.BossBarColor.YELLOW, BossEvent.BossBarOverlay.NOTCHED_10);
		inc.bar.setProgress(1.0F);
		sl.addFreshEntity(boss);
		LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, sl);
		bolt.snapTo(boss.getX(), boss.getY(), boss.getZ(), 0.0F, 0.0F);
		bolt.setVisualOnly(true);
		sl.addFreshEntity(bolt);
		sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, boss.getX(), boss.getY() + 1.0, boss.getZ(), 3, 1.0, 1.0, 1.0, 0.0);
		sl.sendParticles(new DustParticleOptions(0xFFD700, 2.4F), boss.getX(), boss.getY() + 1.0, boss.getZ(), 150, 1.5, 2.0, 1.5, 0.15);
		sl.sendParticles(ParticleTypes.END_ROD, boss.getX(), boss.getY() + 1.0, boss.getZ(), 80, 0.6, 2.5, 0.6, 0.2);
		sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, boss.getX(), boss.getY() + 0.5, boss.getZ(), 60, 1.2, 1.0, 1.2, 0.08);
		sl.playSound(null, pos, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 4.0F, 0.8F);
		sl.playSound(null, pos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.HOSTILE, 3.0F, 0.7F);
	}

	@SubscribeEvent
	public static void onAttackBoss(AttackEntityEvent event) {
		Player p = event.getEntity();
		if (p.level().isClientSide())
			return;
		UUID tid = event.getTarget().getUUID();
		for (Inc inc : ACTIVE.values()) {
			if (inc.bossSpawned && tid.equals(inc.bossId) && inc.bossHp > 0.0F) {
				float str = p.getAttackStrengthScale(0.5F);
				float dmg = (float) p.getAttributeValue(Attributes.ATTACK_DAMAGE) * (0.25F + 0.75F * str);
				if (dmg < 1.0F)
					dmg = 1.0F;
				inc.bossHp -= dmg;
				if (p.level() instanceof ServerLevel sl) {
					Entity boss = event.getTarget();
					sl.sendParticles(ParticleTypes.CRIT, boss.getX(), boss.getY() + 1.0, boss.getZ(), 12, 0.3, 0.4, 0.3, 0.3);
					sl.sendParticles(new DustParticleOptions(0xFFD700, 1.4F), boss.getX(), boss.getY() + 1.0, boss.getZ(), 10, 0.3, 0.4, 0.3, 0.05);
					sl.playSound(null, boss.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.HOSTILE, 0.6F, 1.4F);
				}
				return;
			}
		}
	}

	@SubscribeEvent
	public static void onPlayerDeath(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof Player p) || p.level().isClientSide())
			return;
		UUID id = p.getUUID();
		for (Map.Entry<GlobalPos, Inc> e : new ArrayList<>(ACTIVE.entrySet())) {
			if (e.getValue().participants.contains(id) && p.level().getServer() != null) {
				ServerLevel sl = p.level().getServer().getLevel(e.getKey().dimension());
				if (sl != null)
					end(sl, e.getKey(), false);
			}
		}
	}

	private static void barrier(ServerLevel sl, BlockPos pos, long now) {
		double cx = pos.getX() + 0.5, cy = pos.getY(), cz = pos.getZ() + 0.5;
		int pts = 44;
		double phase = (now % 40) * (Math.PI * 2 / 40);
		for (int i = 0; i < pts; i++) {
			double a = phase + i * (Math.PI * 2 / pts);
			double px = cx + Math.cos(a) * BARRIER;
			double pz = cz + Math.sin(a) * BARRIER;
			double py = cy + 0.5 + ((i * 3 + now) % 12) * 0.6;
			sl.sendParticles(new DustParticleOptions(0x8A5CFF, 1.5F), px, py, pz, 1, 0, 0, 0, 0);
			if (i % 6 == 0)
				sl.sendParticles(ParticleTypes.END_ROD, px, cy + 0.4, pz, 1, 0, 0.02, 0, 0);
		}
	}

	private static int countEnemies(ServerLevel sl, BlockPos pos, Inc inc) {
		String key = posKey(pos);
		return sl.getEntitiesOfClass(Mob.class, arena(pos, ARENA), m -> m.isAlive() && !m.getUUID().equals(inc.bossId) && key.equals(m.getPersistentData().getStringOr("vestigia_incursion", ""))).size();
	}

	private static void end(ServerLevel sl, GlobalPos gp, boolean win) {
		Inc inc = ACTIVE.remove(gp);
		if (inc == null)
			return;
		BlockPos pos = gp.pos();
		String key = posKey(pos);
		for (UUID u : inc.spawned) {
			Entity e = sl.getEntity(u);
			if (e != null)
				e.discard();
		}
		for (Entity e : sl.getEntitiesOfClass(Entity.class, arena(pos, ARENA), e -> !(e instanceof Player) && key.equals(e.getPersistentData().getStringOr("vestigia_incursion", ""))))
			e.discard();
		if (inc.bar != null) {
			inc.bar.removeAllPlayers();
			inc.bar.setVisible(false);
		}
		for (UUID u : inc.participants) {
			ServerPlayer sp = sl.getServer().getPlayerList().getPlayer(u);
			if (sp != null)
				PacketDistributor.sendToPlayer(sp, new IncursionStatePayload(false, 0L, 0L, 0, 0));
		}
		if (win) {
			victoryReward(sl, pos, inc);
			announce(sl, pos, "§6✦ Echo Incursion cleared! §e Seal recovered! §6✦");
			sl.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 3.0F, 1.2F);
			sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 160, 0.8, 1.0, 0.8, 0.5);
		} else {
			announce(sl, pos, "§c✖ The Echo Incursion has ended ✖");
			sl.playSound(null, pos, SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 2.0F, 0.7F);
		}
	}

	private static void victoryReward(ServerLevel sl, BlockPos pos, Inc inc) {
		String charged = inc.sealPath.replace("_off", "");
		dropItem(sl, pos, charged);
		if (sl.getRandom().nextFloat() < 0.5F)
			dropItem(sl, pos, charged);
		dropItem(sl, pos, emblemFor(inc.sealPath));
		if (inc.tier >= 4 && sl.getRandom().nextFloat() < 0.4F)
			dropItem(sl, pos, "stellar_seal");
		int orbs = 20 + inc.tier * 8;
		for (int i = 0; i < orbs; i++) {
			double[] xz = ring(sl, pos, 0.0, 4.0);
			ExperienceOrb orb = new ExperienceOrb(sl, xz[0], pos.getY() + 8.0 + sl.getRandom().nextDouble() * 4.0, xz[1], 6 + sl.getRandom().nextInt(6));
			orb.setDeltaMovement((sl.getRandom().nextDouble() - 0.5) * 0.2, -0.1, (sl.getRandom().nextDouble() - 0.5) * 0.2);
			sl.addFreshEntity(orb);
		}
	}

	private static void dropItem(ServerLevel sl, BlockPos pos, String path) {
		Item it = BuiltInRegistries.ITEM.getValue(Identifier.parse("vestigia:" + path));
		if (it == Items.AIR)
			return;
		ItemEntity ie = new ItemEntity(sl, pos.getX() + 0.5, pos.getY() + 1.4, pos.getZ() + 0.5, new ItemStack(it));
		ie.setDeltaMovement(0, 0.25, 0);
		sl.addFreshEntity(ie);
	}

	private static void announce(ServerLevel sl, BlockPos pos, String msg) {
		for (Player p : sl.getEntitiesOfClass(Player.class, arena(pos, ARENA), Player::isAlive))
			p.sendSystemMessage(Component.literal(msg));
	}

	private static void broadcastState(ServerLevel sl, BlockPos pos, Inc inc, boolean active) {
		int enemies = countEnemies(sl, pos, inc);
		int phase = inc.bossSpawned ? (inc.fury ? 2 : 1) : 0;
		IncursionStatePayload p = new IncursionStatePayload(active, inc.startTime, inc.endTime, phase, enemies);
		for (Player pl : sl.getEntitiesOfClass(Player.class, arena(pos, PARTICIPATE), Player::isAlive))
			if (pl instanceof ServerPlayer sp) {
				inc.participants.add(sp.getUUID());
				PacketDistributor.sendToPlayer(sp, p);
			}
	}

	private static void broadcastWarn(ServerLevel sl, Inc inc, boolean leaving, long deadline) {
		IncursionWarnPayload p = new IncursionWarnPayload(leaving, deadline);
		for (UUID u : inc.participants) {
			ServerPlayer sp = sl.getServer().getPlayerList().getPlayer(u);
			if (sp != null)
				PacketDistributor.sendToPlayer(sp, p);
		}
	}

	@SubscribeEvent
	public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
		event.registrar("vestigia").playToClient(IncursionStatePayload.TYPE, IncursionStatePayload.CODEC,
				(payload, ctx) -> ctx.enqueueWork(() -> net.mcreator.vestigia.client.VestigiaIncursionClient.onState(payload.active(), payload.startTime(), payload.endTime(), payload.phase(), payload.enemies())));
		event.registrar("vestigia").playToClient(IncursionWarnPayload.TYPE, IncursionWarnPayload.CODEC,
				(payload, ctx) -> ctx.enqueueWork(() -> net.mcreator.vestigia.client.VestigiaIncursionClient.onWarn(payload.leaving(), payload.deadline())));
		event.registrar("vestigia").playToClient(IncursionIntroPayload.TYPE, IncursionIntroPayload.CODEC,
				(payload, ctx) -> ctx.enqueueWork(() -> net.mcreator.vestigia.client.VestigiaIncursionClient.onIntro(payload.introEnd(), payload.level())));
	}

	public record IncursionIntroPayload(long introEnd, int level) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<IncursionIntroPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("vestigia:incursion_intro"));
		public static final StreamCodec<RegistryFriendlyByteBuf, IncursionIntroPayload> CODEC = StreamCodec.composite(
				ByteBufCodecs.VAR_LONG, IncursionIntroPayload::introEnd,
				ByteBufCodecs.VAR_INT, IncursionIntroPayload::level,
				IncursionIntroPayload::new);

		@Override
		public CustomPacketPayload.Type<IncursionIntroPayload> type() {
			return TYPE;
		}
	}

	public record IncursionStatePayload(boolean active, long startTime, long endTime, int phase, int enemies) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<IncursionStatePayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("vestigia:incursion_state"));
		public static final StreamCodec<RegistryFriendlyByteBuf, IncursionStatePayload> CODEC = StreamCodec.composite(
				ByteBufCodecs.BOOL, IncursionStatePayload::active,
				ByteBufCodecs.VAR_LONG, IncursionStatePayload::startTime,
				ByteBufCodecs.VAR_LONG, IncursionStatePayload::endTime,
				ByteBufCodecs.VAR_INT, IncursionStatePayload::phase,
				ByteBufCodecs.VAR_INT, IncursionStatePayload::enemies,
				IncursionStatePayload::new);

		@Override
		public CustomPacketPayload.Type<IncursionStatePayload> type() {
			return TYPE;
		}
	}

	public record IncursionWarnPayload(boolean leaving, long deadline) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<IncursionWarnPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("vestigia:incursion_warn"));
		public static final StreamCodec<RegistryFriendlyByteBuf, IncursionWarnPayload> CODEC = StreamCodec.composite(
				ByteBufCodecs.BOOL, IncursionWarnPayload::leaving,
				ByteBufCodecs.VAR_LONG, IncursionWarnPayload::deadline,
				IncursionWarnPayload::new);

		@Override
		public CustomPacketPayload.Type<IncursionWarnPayload> type() {
			return TYPE;
		}
	}
}
