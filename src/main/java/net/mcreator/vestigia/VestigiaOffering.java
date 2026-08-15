package net.mcreator.vestigia;

import com.mojang.brigadier.arguments.IntegerArgumentType;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.phys.Vec3;

import net.mcreator.vestigia.entity.TlalocEntity;
import net.mcreator.vestigia.init.VestigiaModEntities;
import net.mcreator.vestigia.item.OfferingKnifeItem;
import net.mcreator.vestigia.world.inventory.WishGUIMenu;

import io.netty.buffer.Unpooled;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = "vestigia")
public class VestigiaOffering {
	private static final int BASE_CAP = 100;
	private static final int MAX_CAP = 500;
	private static final int CAP_STEP = 30;
	private static final float SACRIFICE_DMG = 10.0F;
	private static final int GREEN = 0x2BE33A, YELLOW = 0xFFE23A, GOLD = 0xD9A441, TURQ = 0x2E9C9C, JADE = 0x2AA77E;
	private static final Set<UUID> ACTIVE_RITUAL = new HashSet<>();
	private static final Set<UUID> PENDING_WISH = new HashSet<>();

	private static int getSouls(Player p) {
		return p.getPersistentData().getIntOr("vestigia_souls", 0);
	}

	private static void setSouls(Player p, int v) {
		p.getPersistentData().putInt("vestigia_souls", v);
	}

	public static void addSouls(Player p, int n) {
		setSouls(p, getSouls(p) + n);
	}

	private static int getCap(Player p) {
		return p.getPersistentData().getIntOr("vestigia_cap", BASE_CAP);
	}

	private static void setCap(Player p, int v) {
		p.getPersistentData().putInt("vestigia_cap", v);
	}

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		event.getDispatcher().register(Commands.literal("vestigia:souls").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("add")
						.then(Commands.argument("cantidad", IntegerArgumentType.integer())
								.then(Commands.argument("jugador", EntityArgument.players()).executes(ctx -> {
									int amount = IntegerArgumentType.getInteger(ctx, "cantidad");
									Collection<ServerPlayer> targets = EntityArgument.getPlayers(ctx, "jugador");
									for (ServerPlayer p : targets) {
										setSouls(p, getSouls(p) + amount);
										p.sendOverlayMessage(Component.literal("§5☠ +" + amount + " souls (" + getSouls(p) + "/" + getCap(p) + ")"));
									}
									ctx.getSource().sendSuccess(() -> Component.literal("Added " + amount + " souls to " + targets.size() + " player(s)"), true);
									return targets.size();
								})))));
	}

	@SubscribeEvent
	public static void onClone(PlayerEvent.Clone event) {
		CompoundTag old = event.getOriginal().getPersistentData();
		CompoundTag now = event.getEntity().getPersistentData();
		now.putInt("vestigia_souls", old.getIntOr("vestigia_souls", 0));
		now.putInt("vestigia_cap", old.getIntOr("vestigia_cap", BASE_CAP));
	}

	@SubscribeEvent
	public static void onKnifeUse(PlayerInteractEvent.RightClickItem event) {
		if (!(event.getItemStack().getItem() instanceof OfferingKnifeItem))
			return;
		Player p = event.getEntity();
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
		if (p.level().isClientSide())
			return;
		if (p.isShiftKeyDown()) {
			p.sendOverlayMessage(Component.literal("§5☠ Souls: " + getSouls(p) + " / " + getCap(p)));
			return;
		}
		if (ACTIVE_RITUAL.contains(p.getUUID()))
			return;
		p.invulnerableTime = 0;
		boolean wasAlive = p.getHealth() > 0.0F;
		if (p.level() instanceof ServerLevel sl) {
			sl.sendParticles(new DustParticleOptions(0xAA0000, 1.4F), p.getX(), p.getY() + 1.0, p.getZ(), 20, 0.35, 0.6, 0.35, 0.05);
			sl.sendParticles(ParticleTypes.SOUL, p.getX(), p.getY() + 0.8, p.getZ(), 8, 0.3, 0.4, 0.3, 0.02);
			sl.playSound(null, p.blockPosition(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 1.0F, 0.6F);
		}
		p.hurt(p.damageSources().magic(), SACRIFICE_DMG);
		if (wasAlive && p.getHealth() <= 0.0F)
			setSouls(p, getSouls(p) + (VestigiaEmblems.hasEmblem(p, "aztec_emblem") ? 4 : 1));
	}

	@SubscribeEvent
	public static void onKnifeKill(LivingDeathEvent event) {
		if (!(event.getSource().getEntity() instanceof Player p) || p.level().isClientSide())
			return;
		if (event.getEntity() == p || ACTIVE_RITUAL.contains(p.getUUID()))
			return;
		if (!(p.getMainHandItem().getItem() instanceof OfferingKnifeItem))
			return;
		int gain = (VestigiaEmblems.hasEmblem(p, "aztec_emblem") && event.getEntity() instanceof Zombie) ? 4 : 1;
		setSouls(p, getSouls(p) + gain);
		p.sendOverlayMessage(Component.literal("§5+" + gain + " soul  (" + getSouls(p) + "/" + getCap(p) + ")"));
	}

	@SubscribeEvent
	public static void onRitualCheck(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer p))
			return;
		if (getSouls(p) < getCap(p) || ACTIVE_RITUAL.contains(p.getUUID()))
			return;
		ACTIVE_RITUAL.add(p.getUUID());
		startRitual(p);
	}

	private static void startRitual(ServerPlayer p) {
		if (!(p.level() instanceof ServerLevel sl))
			return;
		p.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 2400, 255, false, false, false));
		p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 2400, 255, false, false, false));
		p.sendOverlayMessage(Component.literal("§2☠ The offering is complete ☠"));
		Vec3 look = p.getLookAngle();
		Vec3 center = p.position().add(look.x * 6.0, 0.0, look.z * 6.0);
		sl.playSound(null, BlockPos.containing(center), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 2.5F, 0.5F);
		for (int t = 0; t <= 60; t++) {
			final int tick = t;
			VestigiaMod.queueServerWork(t, () -> dragon(sl, center, tick));
		}
		VestigiaMod.queueServerWork(62, () -> impact(sl, center, p));
		VestigiaMod.queueServerWork(92, () -> openWish(p));
	}

	private static Vec3 dragonPath(Vec3 center, double u, int dur) {
		double descend = 1.0 - Math.min(1.0, u / dur);
		double ang = u * 0.32;
		double r = 1.0 + descend * 4.5;
		return new Vec3(center.x + Math.cos(ang) * r, center.y + descend * 24.0, center.z + Math.sin(ang) * r);
	}

	private static void dragon(ServerLevel sl, Vec3 center, int t) {
		int dur = 60, segments = 26;
		DustParticleOptions green = new DustParticleOptions(GREEN, 2.1F);
		DustParticleOptions yellow = new DustParticleOptions(YELLOW, 2.1F);
		for (int i = 0; i < segments; i++) {
			double u = t - i * 1.1;
			if (u < 0.0)
				continue;
			Vec3 pos = dragonPath(center, u, dur);
			double thick = 0.12 + 0.28 * Math.max(0.0, 1.0 - i / (double) segments);
			sl.sendParticles(i % 3 == 0 ? yellow : green, pos.x, pos.y, pos.z, 2, thick, thick, thick, 0.0);
			if (i == 0) {
				sl.sendParticles(ParticleTypes.END_ROD, pos.x, pos.y + 0.25, pos.z, 3, 0.15, 0.15, 0.15, 0.01);
				sl.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 5, 0.22, 0.22, 0.22, 0.03);
				sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.x, pos.y, pos.z, 4, 0.3, 0.3, 0.3, 0.0);
			}
			if (i % 4 == 0) {
				Vec3 dir = dragonPath(center, u + 1.0, dur).subtract(pos);
				Vec3 wing = new Vec3(-dir.z, 0.0, dir.x);
				if (wing.lengthSqr() > 1.0E-4) {
					wing = wing.normalize().scale(1.0 + Math.abs(Math.sin(t * 0.4)) * 1.3);
					sl.sendParticles(green, pos.x + wing.x, pos.y + 0.35, pos.z + wing.z, 1, 0.12, 0.12, 0.12, 0.0);
					sl.sendParticles(green, pos.x - wing.x, pos.y + 0.35, pos.z - wing.z, 1, 0.12, 0.12, 0.12, 0.0);
				}
			}
		}
	}

	private static void impact(ServerLevel sl, Vec3 c, ServerPlayer p) {
		sl.playSound(null, BlockPos.containing(c), SoundEvents.ENDER_DRAGON_GROWL, SoundSource.HOSTILE, 3.0F, 0.7F);
		sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y + 0.5, c.z, 1, 0.0, 0.0, 0.0, 0.0);
		sl.sendParticles(new DustParticleOptions(GREEN, 2.4F), c.x, c.y + 0.5, c.z, 160, 3.0, 1.0, 3.0, 0.12);
		sl.sendParticles(new DustParticleOptions(YELLOW, 2.4F), c.x, c.y + 0.5, c.z, 160, 3.0, 1.0, 3.0, 0.12);
		sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, c.x, c.y + 0.8, c.z, 90, 3.0, 1.0, 3.0, 0.0);
		for (int i = 0; i < 40; i++) {
			double a = Math.PI * 2 * i / 40;
			sl.sendParticles(new DustParticleOptions(GOLD, 1.8F), c.x + Math.cos(a) * 3.0, c.y + 0.2, c.z + Math.sin(a) * 3.0, 1, 0.0, 0.0, 0.0, 0.0);
		}
		TlalocEntity tlaloc = new TlalocEntity(VestigiaModEntities.TLALOC.get(), sl);
		tlaloc.snapTo(c.x, c.y, c.z, p.getYRot() + 180.0F, 0.0F);
		tlaloc.setNoAi(true);
		tlaloc.setPersistenceRequired();
		tlaloc.setInvulnerable(true);
		sl.addFreshEntity(tlaloc);
		p.getPersistentData().putString("vestigia_tlaloc", tlaloc.getStringUUID());
	}

	private static void openWish(ServerPlayer p) {
		p.openMenu(new MenuProvider() {
			@Override
			public Component getDisplayName() {
				return Component.literal("Wish");
			}

			@Override
			public AbstractContainerMenu createMenu(int id, Inventory inv, Player player) {
				return new WishGUIMenu(id, inv, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(p.blockPosition()));
			}
		}, p.blockPosition());
	}

	@SubscribeEvent
	public static void onContainerClose(PlayerContainerEvent.Close event) {
		if (!(event.getContainer() instanceof WishGUIMenu) || event.getEntity().level().isClientSide())
			return;
		UUID id = event.getEntity().getUUID();
		if (ACTIVE_RITUAL.contains(id) && !PENDING_WISH.contains(id) && event.getEntity() instanceof ServerPlayer p)
			VestigiaMod.queueServerWork(2, () -> {
				if (ACTIVE_RITUAL.contains(id) && !PENDING_WISH.contains(id))
					openWish(p);
			});
	}

	public record WishChoicePayload(int wish) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<WishChoicePayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("vestigia:wish_choice"));
		public static final StreamCodec<RegistryFriendlyByteBuf, WishChoicePayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, WishChoicePayload::wish, WishChoicePayload::new);

		@Override
		public CustomPacketPayload.Type<WishChoicePayload> type() {
			return TYPE;
		}
	}

	@SubscribeEvent
	public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
		event.registrar("vestigia").playToServer(WishChoicePayload.TYPE, WishChoicePayload.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> onWishChosen(ctx.player(), payload.wish())));
	}

	private static void onWishChosen(Player player, int wish) {
		if (!(player instanceof ServerPlayer p) || !(p.level() instanceof ServerLevel sl))
			return;
		UUID id = p.getUUID();
		if (!ACTIVE_RITUAL.contains(id) || PENDING_WISH.contains(id))
			return;
		PENDING_WISH.add(id);
		p.closeContainer();
		sl.playSound(null, p.blockPosition(), SoundEvents.WITCH_CELEBRATE, SoundSource.HOSTILE, 2.2F, 0.7F);
		sl.playSound(null, p.blockPosition(), SoundEvents.WITCH_AMBIENT, SoundSource.HOSTILE, 1.8F, 0.55F);
		Vec3 look = p.getLookAngle();
		final Vec3 center = p.getEyePosition().add(look.scale(4.5));
		Vec3 rt = look.cross(new Vec3(0.0, 1.0, 0.0));
		if (rt.lengthSqr() < 1.0E-4)
			rt = new Vec3(1.0, 0.0, 0.0);
		final Vec3 right = rt.normalize();
		final Vec3 up = right.cross(look).normalize();
		for (int t = 0; t <= 55; t++) {
			final int tick = t;
			VestigiaMod.queueServerWork(t, () -> calendar(sl, center, right, up, tick));
		}
		VestigiaMod.queueServerWork(58, () -> grantWish(p, sl, wish));
	}

	private static void calendar(ServerLevel sl, Vec3 c, Vec3 right, Vec3 up, int t) {
		double spin = t * 0.24;
		for (int ring = 1; ring <= 5; ring++) {
			double r = ring * 0.55;
			int count = 8 + ring * 4;
			double dir = (ring % 2 == 0) ? 1.0 : -1.0;
			double rs = spin * dir * (1.0 + ring * 0.12);
			int col = ring % 3 == 0 ? GOLD : (ring % 3 == 1 ? TURQ : JADE);
			DustParticleOptions dust = new DustParticleOptions(col, 1.7F);
			for (int i = 0; i < count; i++) {
				double a = rs + (Math.PI * 2 * i / count);
				Vec3 pt = c.add(right.scale(Math.cos(a) * r)).add(up.scale(Math.sin(a) * r));
				sl.sendParticles(dust, pt.x, pt.y, pt.z, 1, 0.0, 0.0, 0.0, 0.0);
			}
		}
		DustParticleOptions gold = new DustParticleOptions(GOLD, 1.6F);
		for (int s = 0; s < 8; s++) {
			double a = -spin + (Math.PI * 2 * s / 8);
			for (double rr = 0.3; rr <= 2.9; rr += 0.4) {
				Vec3 pt = c.add(right.scale(Math.cos(a) * rr)).add(up.scale(Math.sin(a) * rr));
				sl.sendParticles(gold, pt.x, pt.y, pt.z, 1, 0.0, 0.0, 0.0, 0.0);
			}
		}
		sl.sendParticles(ParticleTypes.END_ROD, c.x, c.y, c.z, 3, 0.12, 0.12, 0.12, 0.0);
		sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, c.x, c.y, c.z, 2, 0.15, 0.15, 0.15, 0.01);
	}

	private static void grantWish(ServerPlayer p, ServerLevel sl, int wish) {
		switch (wish) {
			case 0 -> sl.getServer().setWeatherParameters(0, 12000, true, false);
			case 1 -> sl.getServer().setWeatherParameters(6000, 0, false, false);
			case 2 -> {
				GameType prev = p.gameMode.getGameModeForPlayer();
				p.setGameMode(GameType.CREATIVE);
				VestigiaMod.queueServerWork(200, () -> {
					if (p.isAlive())
						p.setGameMode(prev == GameType.CREATIVE ? GameType.SURVIVAL : prev);
				});
			}
			case 3 -> {
				p.getAbilities().mayfly = true;
				p.onUpdateAbilities();
				VestigiaMod.queueServerWork(36000, () -> {
					if (p.isAlive()) {
						p.getAbilities().mayfly = false;
						p.getAbilities().flying = false;
						p.onUpdateAbilities();
					}
				});
			}
			case 4 -> {
				p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 6000, 4, false, false, true));
				p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 6000, 0, false, false, true));
				p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 6000, 3, false, false, true));
			}
		}
		p.removeEffect(MobEffects.DARKNESS);
		p.removeEffect(MobEffects.SLOWNESS);
		removeTlaloc(p, sl);
		int cap = getCap(p);
		setCap(p, cap >= MAX_CAP ? BASE_CAP : Math.min(MAX_CAP, cap + CAP_STEP));
		setSouls(p, 0);
		p.sendOverlayMessage(Component.literal("§6✦ Wish granted — next: " + getCap(p) + " souls ✦"));
		ACTIVE_RITUAL.remove(p.getUUID());
		PENDING_WISH.remove(p.getUUID());
	}

	private static void removeTlaloc(ServerPlayer p, ServerLevel sl) {
		String id = p.getPersistentData().getStringOr("vestigia_tlaloc", "");
		if (id.isEmpty())
			return;
		try {
			Entity t = sl.getEntity(UUID.fromString(id));
			if (t != null) {
				LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, sl);
				bolt.snapTo(t.getX(), t.getY(), t.getZ(), 0.0F, 0.0F);
				bolt.setVisualOnly(true);
				sl.addFreshEntity(bolt);
				sl.sendParticles(new DustParticleOptions(GREEN, 2.0F), t.getX(), t.getY() + 1.0, t.getZ(), 60, 0.4, 1.0, 0.4, 0.1);
				t.discard();
			}
		} catch (IllegalArgumentException ignored) {
		}
		p.getPersistentData().putString("vestigia_tlaloc", "");
	}
}
