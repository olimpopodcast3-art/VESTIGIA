package net.mcreator.vestigia;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import net.mcreator.vestigia.world.inventory.AltarGUIMenu;

import io.netty.buffer.Unpooled;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = "vestigia")
public class VestigiaAltar {
	private static final String STATUE = "aztec_statue";
	private static final String ALTAR = "altar";
	private static final String EMBLEM = "aztec_emblem";
	private static final int SOULS_REWARD = 50;
	private static final int GOLD = 0xD9A441, TURQ = 0x2E9C9C, JADE = 0x2AA77E;
	private static final Set<UUID> ACTIVE_TRIAL = new HashSet<>();

	@SubscribeEvent
	public static void onStatueUseBlock(PlayerInteractEvent.RightClickBlock event) {
		Identifier iid = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
		if (iid == null || !iid.getPath().equals(STATUE))
			return;
		Level lvl = event.getLevel();
		BlockPos pos = event.getPos();
		Identifier bid = BuiltInRegistries.BLOCK.getKey(lvl.getBlockState(pos).getBlock());
		boolean onAltar = bid != null && bid.getPath().equals(ALTAR);
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
		if (!(lvl instanceof ServerLevel sl))
			return;
		if (onAltar && event.getEntity() instanceof ServerPlayer p)
			openTrial(sl, p, pos.immutable());
		else if (!onAltar)
			strike(sl, event.getEntity());
	}

	@SubscribeEvent
	public static void onStatueUseAir(PlayerInteractEvent.RightClickItem event) {
		if (event.getHand() != InteractionHand.MAIN_HAND)
			return;
		Identifier iid = BuiltInRegistries.ITEM.getKey(event.getItemStack().getItem());
		if (iid == null || !iid.getPath().equals(STATUE))
			return;
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
		if (event.getLevel() instanceof ServerLevel sl)
			strike(sl, event.getEntity());
	}

	private static void strike(ServerLevel sl, Player p) {
		LightningBolt bolt = new LightningBolt(EntityType.LIGHTNING_BOLT, sl);
		bolt.snapTo(p.getX(), p.getY(), p.getZ(), 0.0F, 0.0F);
		sl.addFreshEntity(bolt);
	}

	private static void openTrial(ServerLevel sl, ServerPlayer p, BlockPos pos) {
		consumeStatue(p);
		Vec3 c = new Vec3(pos.getX() + 0.5, pos.getY() + 1.2, pos.getZ() + 0.5);
		sl.playSound(null, pos, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 1.6F, 1.2F);
		for (int t = 0; t <= 40; t++) {
			final int tick = t;
			VestigiaMod.queueServerWork(t, () -> altarCharge(sl, c, tick));
		}
		VestigiaMod.queueServerWork(44, () -> {
			if (!p.isAlive())
				return;
			sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, c.x, c.y, c.z, 1, 0.0, 0.0, 0.0, 0.0);
			sl.sendParticles(new DustParticleOptions(GOLD, 2.2F), c.x, c.y, c.z, 60, 0.6, 0.6, 0.6, 0.15);
			sl.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.4F, 1.4F);
			ACTIVE_TRIAL.add(p.getUUID());
			p.openMenu(new MenuProvider() {
				@Override
				public Component getDisplayName() {
					return Component.literal("Altar");
				}

				@Override
				public AbstractContainerMenu createMenu(int id, Inventory inv, Player pl) {
					return new AltarGUIMenu(id, inv, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(pos));
				}
			}, pos);
		});
	}

	private static void altarCharge(ServerLevel sl, Vec3 c, int t) {
		double spin = t * 0.3;
		double rise = 0.03 * t;
		for (int ring = 1; ring <= 4; ring++) {
			double r = ring * 0.5;
			int count = 8 + ring * 4;
			double dir = (ring % 2 == 0) ? 1.0 : -1.0;
			double rs = spin * dir;
			int col = ring % 3 == 0 ? GOLD : (ring % 3 == 1 ? TURQ : JADE);
			DustParticleOptions dust = new DustParticleOptions(col, 1.6F);
			for (int i = 0; i < count; i++) {
				double a = rs + (Math.PI * 2 * i / count);
				sl.sendParticles(dust, c.x + Math.cos(a) * r, c.y + rise, c.z + Math.sin(a) * r, 1, 0.0, 0.0, 0.0, 0.0);
			}
		}
		sl.sendParticles(ParticleTypes.END_ROD, c.x, c.y + rise, c.z, 2, 0.05, 0.3, 0.05, 0.01);
		sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, c.x, c.y + 0.2, c.z, 2, 0.15, 0.2, 0.15, 0.01);
		if (t % 8 == 0)
			sl.playSound(null, BlockPos.containing(c), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.1F, 0.8F + t * 0.012F);
	}

	private static void onAltarWin(Player player) {
		if (!(player instanceof ServerPlayer p) || !(p.level() instanceof ServerLevel sl))
			return;
		if (!ACTIVE_TRIAL.remove(p.getUUID()))
			return;
		Item emblem = BuiltInRegistries.ITEM.getValue(Identifier.parse("vestigia:" + EMBLEM));
		if (emblem != null) {
			ItemStack st = new ItemStack(emblem);
			if (!p.addItem(st))
				p.drop(st, false);
		}
		VestigiaOffering.addSouls(p, SOULS_REWARD);
		p.sendOverlayMessage(Component.literal("§6✦ The ring accepts your offering — Aztec Emblem + 50 souls ✦"));
		Vec3 c = p.position().add(0.0, 1.0, 0.0);
		sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, c.x, c.y, c.z, 90, 0.6, 0.9, 0.6, 0.35);
		sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, c.x, c.y, c.z, 40, 0.6, 0.9, 0.6, 0.0);
		for (int i = 0; i < 40; i++) {
			double a = Math.PI * 2 * i / 40;
			sl.sendParticles(new DustParticleOptions(GOLD, 1.8F), c.x + Math.cos(a) * 1.5, c.y, c.z + Math.sin(a) * 1.5, 1, 0.0, 0.0, 0.0, 0.0);
		}
		sl.playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.2F, 1.0F);
		sl.playSound(null, p.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0F, 1.2F);
	}

	private static void consumeStatue(Player p) {
		for (InteractionHand h : InteractionHand.values()) {
			ItemStack s = p.getItemInHand(h);
			Identifier id = BuiltInRegistries.ITEM.getKey(s.getItem());
			if (id != null && id.getPath().equals(STATUE)) {
				s.shrink(1);
				return;
			}
		}
	}

	private static void onAltarFail(Player player) {
		if (!(player instanceof ServerPlayer p) || !(p.level() instanceof ServerLevel sl))
			return;
		if (!ACTIVE_TRIAL.remove(p.getUUID()))
			return;
		p.sendOverlayMessage(Component.literal("§c✘ The ring rejects you — the sky answers"));
		sl.playSound(null, p.blockPosition(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.HOSTILE, 1.6F, 0.6F);
		for (int t = 0; t <= 30; t++) {
			final int tick = t;
			VestigiaMod.queueServerWork(t, () -> doom(sl, p, tick));
		}
		VestigiaMod.queueServerWork(34, () -> {
			if (!p.isAlive())
				return;
			strike(sl, p);
			sl.sendParticles(new DustParticleOptions(0xE01010, 2.4F), p.getX(), p.getY() + 1.0, p.getZ(), 80, 0.5, 1.0, 0.5, 0.2);
		});
	}

	private static void doom(ServerLevel sl, Player p, int t) {
		Vec3 c = p.position().add(0.0, 0.3, 0.0);
		double prog = t / 30.0;
		double rad = 5.0 * (1.0 - prog) + 0.4;
		DustParticleOptions red = new DustParticleOptions(0xE01010, 1.7F);
		DustParticleOptions dark = new DustParticleOptions(0x7A0000, 2.3F);
		int spokes = 12;
		for (int s = 0; s < spokes; s++) {
			double a = (Math.PI * 2 * s / spokes) + t * 0.05;
			for (double d = 0.0; d <= 1.2; d += 0.3) {
				double rr = rad + d;
				sl.sendParticles(red, c.x + Math.cos(a) * rr, c.y + 0.2 + d * 0.15, c.z + Math.sin(a) * rr, 1, 0.0, 0.0, 0.0, 0.0);
			}
			sl.sendParticles(dark, c.x + Math.cos(a) * rad, c.y + 0.2, c.z + Math.sin(a) * rad, 1, 0.0, 0.0, 0.0, 0.0);
		}
		sl.sendParticles(ParticleTypes.ANGRY_VILLAGER, c.x, c.y + 1.2, c.z, 2, 0.3, 0.4, 0.3, 0.0);
		if (t % 6 == 0)
			sl.playSound(null, p.blockPosition(), SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.HOSTILE, 0.7F, 1.4F);
	}

	@SubscribeEvent
	public static void onClose(PlayerContainerEvent.Close event) {
		if (event.getContainer() instanceof AltarGUIMenu)
			ACTIVE_TRIAL.remove(event.getEntity().getUUID());
	}

	public record AltarWinPayload() implements CustomPacketPayload {
		public static final AltarWinPayload INSTANCE = new AltarWinPayload();
		public static final CustomPacketPayload.Type<AltarWinPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("vestigia:altar_win"));
		public static final StreamCodec<RegistryFriendlyByteBuf, AltarWinPayload> CODEC = StreamCodec.unit(INSTANCE);

		@Override
		public CustomPacketPayload.Type<AltarWinPayload> type() {
			return TYPE;
		}
	}

	public record AltarFailPayload() implements CustomPacketPayload {
		public static final AltarFailPayload INSTANCE = new AltarFailPayload();
		public static final CustomPacketPayload.Type<AltarFailPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("vestigia:altar_fail"));
		public static final StreamCodec<RegistryFriendlyByteBuf, AltarFailPayload> CODEC = StreamCodec.unit(INSTANCE);

		@Override
		public CustomPacketPayload.Type<AltarFailPayload> type() {
			return TYPE;
		}
	}

	@SubscribeEvent
	public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
		event.registrar("vestigia").playToServer(AltarWinPayload.TYPE, AltarWinPayload.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> onAltarWin(ctx.player())));
		event.registrar("vestigia").playToServer(AltarFailPayload.TYPE, AltarFailPayload.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> onAltarFail(ctx.player())));
	}
}
