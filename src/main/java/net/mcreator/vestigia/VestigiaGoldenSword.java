package net.mcreator.vestigia;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;

import net.mcreator.vestigia.entity.GoldenKnightEntity;
import net.mcreator.vestigia.init.VestigiaModEntities;
import net.mcreator.vestigia.item.GoldenSwordItem;

@EventBusSubscriber(modid = "vestigia")
public class VestigiaGoldenSword {
	private static final int CHARGE_TICKS = 460;
	private static final int EMPOWERED_HITS = 8;
	private static final float BONUS_DMG = 11.0F;
	private static final int HIT_COOLDOWN = 600;
	private static final int SUMMON_COOLDOWN = 2400;
	private static final int KNIGHT_LIFE = 600;
	private static final int GOLD = 0xFFD700;
	private static final int AMBER = 0xFFAA00;

	private static boolean isCharged(ItemStack stack) {
		CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
		return cd != null && cd.copyTag().getIntOr("gs_charged", 0) == 1;
	}

	private static int getHits(ItemStack stack) {
		CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
		return cd == null ? 0 : cd.copyTag().getIntOr("gs_hits", 0);
	}

	private static int getLvl(ItemStack stack) {
		CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
		return cd == null ? 0 : cd.copyTag().getIntOr("gs_lvl", 0);
	}

	private static void setLvl(ItemStack stack, int lvl) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putInt("gs_lvl", lvl);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	private static void setCharged(ItemStack stack, boolean charged, int hits) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putInt("gs_charged", charged ? 1 : 0);
		tag.putInt("gs_hits", hits);
		tag.putInt("gs_lvl", 0);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, charged);
	}

	@SubscribeEvent
	public static void onChargeTick(PlayerTickEvent.Post event) {
		Player p = event.getEntity();
		if (p.level().isClientSide() || !p.isUsingItem())
			return;
		ItemStack use = p.getUseItem();
		if (!(use.getItem() instanceof GoldenSwordItem))
			return;
		if (isCharged(use) || p.getCooldowns().isOnCooldown(use)) {
			p.stopUsingItem();
			return;
		}
		int ticksUsed = p.getTicksUsingItem();
		int need = Math.max(1, (int) Math.round(CHARGE_TICKS * VestigiaSellos.chargeFactor(use)));
		if (p.level() instanceof ServerLevel loop) {
			int level = Math.min(8, 1 + (int) (ticksUsed * 8.0 / need));
			int prev = getLvl(use);
			if (level != prev) {
				if (level > prev)
					loop.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.35F + level * 0.075F, 0.6F + level * 0.16F);
				setLvl(use, level);
			}
			if (ticksUsed % 70 == 0)
				loop.playSound(null, p.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.6F, 1.0F);
		}
		if (ticksUsed < need)
			return;
		setCharged(use, true, EMPOWERED_HITS);
		p.sendOverlayMessage(Component.literal("§6✦ Golden Sword charged ✦"));
		if (p.level() instanceof ServerLevel sl) {
			sl.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.0F, 1.4F);
			sl.sendParticles(new DustParticleOptions(GOLD, 1.6F), p.getX(), p.getY() + 1.0, p.getZ(), 40, 0.4, 0.7, 0.4, 0.02);
			sl.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1.0, p.getZ(), 20, 0.4, 0.7, 0.4, 0.02);
		}
		p.stopUsingItem();
	}

	@SubscribeEvent
	public static void onGoldenHit(LivingIncomingDamageEvent event) {
		if (!(event.getSource().getEntity() instanceof Player p) || p.level().isClientSide())
			return;
		ItemStack weapon = p.getMainHandItem();
		if (!(weapon.getItem() instanceof GoldenSwordItem) || !isCharged(weapon))
			return;
		event.setAmount(event.getAmount() + BONUS_DMG);
		int hits = getHits(weapon) - 1;
		LivingEntity victim = event.getEntity();
		if (p.level() instanceof ServerLevel sl) {
			sl.sendParticles(new DustParticleOptions(GOLD, 1.4F), victim.getX(), victim.getY() + victim.getBbHeight() * 0.5, victim.getZ(), 18, 0.3, 0.3, 0.3, 0.15);
			sl.sendParticles(ParticleTypes.CRIT, victim.getX(), victim.getY() + victim.getBbHeight() * 0.5, victim.getZ(), 12, 0.3, 0.3, 0.3, 0.4);
		}
		if (hits <= 0) {
			setCharged(weapon, false, 0);
			p.getCooldowns().addCooldown(weapon, HIT_COOLDOWN);
			p.sendOverlayMessage(Component.literal("§7Golden Sword discharged"));
		} else {
			CompoundTag tag = weapon.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
			tag.putInt("gs_hits", hits);
			weapon.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		}
	}

	@SubscribeEvent
	public static void onGoldenSummon(PlayerInteractEvent.RightClickBlock event) {
		ItemStack weapon = event.getItemStack();
		if (!(weapon.getItem() instanceof GoldenSwordItem) || !isCharged(weapon))
			return;
		Player p = event.getEntity();
		if (p.getCooldowns().isOnCooldown(weapon))
			return;
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
		if (p.level().isClientSide())
			return;
		if (!VestigiaSellos.canSummon(p, weapon))
			return;
		int knightLife = VestigiaSellos.lifetimeFor(weapon, KNIGHT_LIFE);
		setCharged(weapon, false, 0);
		p.getCooldowns().addCooldown(weapon, SUMMON_COOLDOWN);
		Vec3 center = event.getHitVec().getLocation();
		startRitual(p, center, knightLife);
	}

	private static void startRitual(Player owner, Vec3 c, int knightLife) {
		if (!(owner.level() instanceof ServerLevel sl))
			return;
		sl.playSound(null, BlockPos.containing(c), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.3F, 0.6F);
		for (int t = 0; t <= 48; t++) {
			final int tick = t;
			VestigiaMod.queueServerWork(t, () -> ring(sl, c, tick));
		}
		for (int t = 40; t <= 90; t++) {
			VestigiaMod.queueServerWork(t, () -> meteor(sl, c));
		}
		VestigiaMod.queueServerWork(92, () -> boom(sl, c));
		VestigiaMod.queueServerWork(98, () -> summonKnight(sl, c, owner, knightLife));
	}

	private static void ring(ServerLevel sl, Vec3 c, int t) {
		double base = t * 0.2;
		int points = 32;
		for (int i = 0; i < points; i++) {
			double a = base + (Math.PI * 2 * i / points);
			double x = c.x + Math.cos(a) * 4.0;
			double z = c.z + Math.sin(a) * 4.0;
			sl.sendParticles(new DustParticleOptions(GOLD, 1.5F), x, c.y + 0.15, z, 1, 0.0, 0.0, 0.0, 0.0);
			if (i % 4 == 0)
				sl.sendParticles(ParticleTypes.END_ROD, x, c.y + 0.25, z, 1, 0.0, 0.02, 0.0, 0.0);
		}
	}

	private static void meteor(ServerLevel sl, Vec3 c) {
		RandomSource r = sl.getRandom();
		for (int i = 0; i < 3; i++) {
			double ang = r.nextDouble() * Math.PI * 2;
			double rad = r.nextDouble() * 4.0;
			double x = c.x + Math.cos(ang) * rad;
			double z = c.z + Math.sin(ang) * rad;
			double y = c.y + 12.0 + r.nextDouble() * 5.0;
			sl.sendParticles(ParticleTypes.FLAME, x, y, z, 0, 0.0, -1.4, 0.0, 1.0);
			sl.sendParticles(new DustParticleOptions(AMBER, 1.6F), x, y - 0.6, z, 2, 0.05, 0.3, 0.05, 0.05);
			sl.sendParticles(ParticleTypes.LAVA, x, y, z, 1, 0.05, 0.05, 0.05, 0.0);
		}
	}

	private static void boom(ServerLevel sl, Vec3 c) {
		sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y + 0.5, c.z, 1, 0.0, 0.0, 0.0, 0.0);
		sl.sendParticles(ParticleTypes.FLAME, c.x, c.y + 0.5, c.z, 130, 2.2, 1.0, 2.2, 0.16);
		sl.sendParticles(ParticleTypes.END_ROD, c.x, c.y + 0.6, c.z, 90, 2.4, 1.1, 2.4, 0.22);
		sl.sendParticles(new DustParticleOptions(GOLD, 2.0F), c.x, c.y + 0.6, c.z, 110, 2.6, 1.2, 2.6, 0.1);
		sl.playSound(null, BlockPos.containing(c), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.5F, 0.7F);
	}

	private static void summonKnight(ServerLevel sl, Vec3 c, Player owner, int knightLife) {
		if (owner == null || !owner.isAlive())
			return;
		GoldenKnightEntity knight = new GoldenKnightEntity(VestigiaModEntities.GOLDEN_KNIGHT.get(), sl);
		knight.snapTo(c.x, c.y, c.z, owner.getYRot(), 0.0F);
		VestigiaPortadores.makeCombatAlly(knight, owner, knightLife, true);
		VestigiaSellos.tagAlly(knight, "golden_sword");
		sl.addFreshEntity(knight);
		sl.sendParticles(ParticleTypes.EXPLOSION, c.x, c.y + 1.0, c.z, 1, 0.0, 0.0, 0.0, 0.0);
		sl.sendParticles(new DustParticleOptions(GOLD, 1.8F), c.x, c.y + 1.0, c.z, 60, 0.4, 0.9, 0.4, 0.05);
		sl.playSound(null, BlockPos.containing(c), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.4F, 0.9F);
	}
}
