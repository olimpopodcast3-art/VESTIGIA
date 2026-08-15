package net.mcreator.vestigia;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
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
import net.minecraft.world.MenuProvider;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import net.mcreator.vestigia.world.inventory.StellarEmblemGUIMenu;

import top.theillusivec4.curios.api.CuriosApi;

import io.netty.buffer.Unpooled;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = "vestigia")
public class VestigiaEmblems {
	private static final int SHADOW_TICKS = 1000;
	private static final int SEAL_BOOST_TICKS = 6000;
	private static final int PURPLE = 0x7A3AD6, BLUE = 0x2A6AE0, GOLD = 0xD9A441, ORANGE = 0xFF9030, SHADOW_COL = 0x241633;
	private static final Set<UUID> SHADOW = new HashSet<>();
	private static boolean doubling = false;

	public static boolean hasEmblem(LivingEntity e, String path) {
		Item it = BuiltInRegistries.ITEM.getValue(Identifier.parse("vestigia:" + path));
		if (it == null)
			return false;
		return CuriosApi.getCuriosInventory(e).map(inv -> inv.findFirstCurio(it).isPresent()).orElse(false);
	}

	@SubscribeEvent
	public static void onTick(PlayerTickEvent.Post event) {
		if (!(event.getEntity() instanceof ServerPlayer p) || !(p.level() instanceof ServerLevel sl))
			return;
		if (hasEmblem(p, "fire_emblem")) {
			p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40, 0, false, false, false));
			if (p.getRemainingFireTicks() > 0)
				p.setRemainingFireTicks(0);
		}
		if (hasEmblem(p, "aurora_emblem") && sl.getGameTime() % 10L == 0L) {
			for (Mob m : sl.getEntitiesOfClass(Mob.class, p.getBoundingBox().inflate(32.0)))
				m.addEffect(new MobEffectInstance(MobEffects.GLOWING, 30, 0, false, false, false));
		}
	}

	@SubscribeEvent
	public static void onEffectAdded(MobEffectEvent.Added event) {
		if (doubling)
			return;
		if (!(event.getEntity() instanceof Player p) || p.level().isClientSide())
			return;
		if (!hasEmblem(p, "aurora_emblem") && !hasEmblem(p, "golden_emblem"))
			return;
		MobEffectInstance inst = event.getEffectInstance();
		if (inst.getEffect().value().getCategory() != MobEffectCategory.BENEFICIAL)
			return;
		if (inst.getEffect().equals(MobEffects.FIRE_RESISTANCE) || inst.getEffect().equals(MobEffects.GLOWING) || inst.getEffect().equals(MobEffects.INVISIBILITY))
			return;
		int amp = inst.getAmplifier();
		int boosted = amp * 2 + 1;
		if (boosted <= amp)
			return;
		doubling = true;
		p.addEffect(new MobEffectInstance(inst.getEffect(), inst.getDuration(), boosted, inst.isAmbient(), inst.isVisible(), inst.showIcon()));
		doubling = false;
	}

	@SubscribeEvent
	public static void onDamage(LivingIncomingDamageEvent event) {
		if (event.getEntity() instanceof Player vic && !vic.level().isClientSide() && event.getSource().is(net.minecraft.world.damagesource.DamageTypes.FALL)
				&& (SHADOW.contains(vic.getUUID()) || hasEmblem(vic, "fire_emblem"))) {
			event.setCanceled(true);
			return;
		}
		if (!(event.getSource().getEntity() instanceof Player p) || p.level().isClientSide())
			return;
		float amt = event.getAmount();
		Identifier wid = BuiltInRegistries.ITEM.getKey(p.getMainHandItem().getItem());
		String wp = wid == null ? "" : wid.getPath();
		if (!wp.isEmpty() && wp.contains("golden") && hasEmblem(p, "golden_emblem"))
			amt *= 3.0F;
		long until = p.getPersistentData().getLongOr("vestigia_seal_boost", 0L);
		if (p.level().getGameTime() < until && hasEmblem(p, "seal_emblem"))
			amt *= 6.0F;
		if (amt != event.getAmount())
			event.setAmount(amt);
	}

	@SubscribeEvent
	public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
		Player p = event.getEntity();
		Identifier wid = BuiltInRegistries.ITEM.getKey(p.getMainHandItem().getItem());
		if (wid != null && wid.getPath().contains("golden") && hasEmblem(p, "golden_emblem"))
			event.setNewSpeed(event.getOriginalSpeed() * 3.0F);
	}

	private static void startShadow(Player player) {
		if (!(player instanceof ServerPlayer p) || !(p.level() instanceof ServerLevel sl))
			return;
		if (!hasEmblem(p, "twisted_emblem"))
			return;
		if (!SHADOW.add(p.getUUID()))
			return;
		p.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, SHADOW_TICKS, 0, false, false, true));
		shadowAttrs(p, true);
		p.sendOverlayMessage(Component.literal("§8☾ You melt into shadow — 50s"));
		sl.playSound(null, p.blockPosition(), SoundEvents.WARDEN_SONIC_CHARGE, SoundSource.PLAYERS, 0.8F, 1.6F);
		sl.sendParticles(new DustParticleOptions(SHADOW_COL, 2.0F), p.getX(), p.getY() + 1.0, p.getZ(), 60, 0.4, 0.9, 0.4, 0.1);
		sl.sendParticles(ParticleTypes.SQUID_INK, p.getX(), p.getY() + 1.0, p.getZ(), 20, 0.3, 0.6, 0.3, 0.02);
		VestigiaMod.queueServerWork(SHADOW_TICKS, () -> endShadow(p));
	}

	private static void endShadow(ServerPlayer p) {
		if (!SHADOW.remove(p.getUUID()))
			return;
		shadowAttrs(p, false);
		p.removeEffect(MobEffects.INVISIBILITY);
		if (p.level() instanceof ServerLevel sl)
			sl.sendParticles(new DustParticleOptions(SHADOW_COL, 1.8F), p.getX(), p.getY() + 1.0, p.getZ(), 40, 0.4, 0.9, 0.4, 0.05);
		p.sendOverlayMessage(Component.literal("§8☾ The shadow releases you"));
	}

	private static void shadowAttrs(Player p, boolean on) {
		attr(p, Attributes.MOVEMENT_SPEED, "vestigia:shadow_speed", 4.0, on);
		attr(p, Attributes.JUMP_STRENGTH, "vestigia:shadow_jump", 4.0, on);
	}

	private static void attr(Player p, net.minecraft.core.Holder<net.minecraft.world.entity.ai.attributes.Attribute> which, String id, double amount, boolean on) {
		AttributeInstance inst = p.getAttribute(which);
		if (inst == null)
			return;
		Identifier rid = Identifier.parse(id);
		if (on) {
			if (inst.getModifier(rid) == null)
				inst.addTransientModifier(new AttributeModifier(rid, amount, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
		} else {
			inst.removeModifier(rid);
		}
	}

	private static void openStellar(Player player) {
		if (!(player instanceof ServerPlayer p))
			return;
		if (!hasEmblem(p, "seal_emblem"))
			return;
		p.openMenu(new MenuProvider() {
			@Override
			public Component getDisplayName() {
				return Component.literal("Stellar Emblem");
			}

			@Override
			public AbstractContainerMenu createMenu(int id, Inventory inv, Player pl) {
				return new StellarEmblemGUIMenu(id, inv, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(p.blockPosition()));
			}
		}, p.blockPosition());
	}

	private static void stellarAction(Player player, int action) {
		if (!(player instanceof ServerPlayer p) || !(p.level() instanceof ServerLevel sl))
			return;
		if (!hasEmblem(p, "seal_emblem"))
			return;
		switch (action) {
			case 0 -> {
				sl.getServer().setWeatherParameters(6000, 0, false, false);
				p.sendOverlayMessage(Component.literal("§b✦ The skies clear"));
			}
			case 1 -> {
				sl.getServer().setWeatherParameters(0, 6000, true, false);
				p.sendOverlayMessage(Component.literal("§9✦ The storm answers"));
			}
			case 2 -> {
				runCmd(sl, "time set day");
				p.sendOverlayMessage(Component.literal("§e✦ Dawn breaks"));
			}
			case 3 -> {
				runCmd(sl, "time set night");
				p.sendOverlayMessage(Component.literal("§5✦ Night falls"));
			}
			case 4 -> offerDiamonds(p, sl);
			default -> {
			}
		}
		sl.playSound(null, p.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0F, 1.4F);
		sl.sendParticles(ParticleTypes.REVERSE_PORTAL, p.getX(), p.getY() + 1.2, p.getZ(), 30, 0.4, 0.6, 0.4, 0.05);
	}

	private static void runCmd(ServerLevel sl, String cmd) {
		var server = sl.getServer();
		if (server != null)
			server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), cmd);
	}

	private static void offerDiamonds(ServerPlayer p, ServerLevel sl) {
		Inventory inv = p.getInventory();
		int have = 0;
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack s = inv.getItem(i);
			if (s.getItem() == Items.DIAMOND)
				have += s.getCount();
		}
		if (have < 64) {
			p.sendOverlayMessage(Component.literal("§7You need a full stack of diamonds (" + have + "/64)"));
			sl.playSound(null, p.blockPosition(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 1.0F, 1.0F);
			return;
		}
		int rem = 64;
		for (int i = 0; i < inv.getContainerSize() && rem > 0; i++) {
			ItemStack s = inv.getItem(i);
			if (s.getItem() == Items.DIAMOND) {
				int take = Math.min(rem, s.getCount());
				s.shrink(take);
				rem -= take;
			}
		}
		p.getPersistentData().putLong("vestigia_seal_boost", sl.getGameTime() + SEAL_BOOST_TICKS);
		p.sendOverlayMessage(Component.literal("§d★ COSMIC POWER — x6 damage for 5 minutes ★"));
		sl.playSound(null, p.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.2F, 0.8F);
		sl.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.4F, 0.7F);
		sl.sendParticles(new DustParticleOptions(PURPLE, 2.2F), p.getX(), p.getY() + 1.0, p.getZ(), 80, 0.6, 1.0, 0.6, 0.2);
		sl.sendParticles(new DustParticleOptions(BLUE, 2.2F), p.getX(), p.getY() + 1.0, p.getZ(), 80, 0.6, 1.0, 0.6, 0.2);
		sl.sendParticles(ParticleTypes.END_ROD, p.getX(), p.getY() + 1.0, p.getZ(), 40, 0.5, 1.0, 0.5, 0.15);
	}

	public record EmblemKeyPayload() implements CustomPacketPayload {
		public static final EmblemKeyPayload INSTANCE = new EmblemKeyPayload();
		public static final CustomPacketPayload.Type<EmblemKeyPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("vestigia:emblem_key"));
		public static final StreamCodec<RegistryFriendlyByteBuf, EmblemKeyPayload> CODEC = StreamCodec.unit(INSTANCE);

		@Override
		public CustomPacketPayload.Type<EmblemKeyPayload> type() {
			return TYPE;
		}
	}

	public record OpenStellarPayload() implements CustomPacketPayload {
		public static final OpenStellarPayload INSTANCE = new OpenStellarPayload();
		public static final CustomPacketPayload.Type<OpenStellarPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("vestigia:open_stellar"));
		public static final StreamCodec<RegistryFriendlyByteBuf, OpenStellarPayload> CODEC = StreamCodec.unit(INSTANCE);

		@Override
		public CustomPacketPayload.Type<OpenStellarPayload> type() {
			return TYPE;
		}
	}

	public record StellarActionPayload(int action) implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<StellarActionPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("vestigia:stellar_action"));
		public static final StreamCodec<RegistryFriendlyByteBuf, StellarActionPayload> CODEC = StreamCodec.composite(ByteBufCodecs.VAR_INT, StellarActionPayload::action, StellarActionPayload::new);

		@Override
		public CustomPacketPayload.Type<StellarActionPayload> type() {
			return TYPE;
		}
	}

	@SubscribeEvent
	public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
		event.registrar("vestigia").playToServer(EmblemKeyPayload.TYPE, EmblemKeyPayload.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> startShadow(ctx.player())));
		event.registrar("vestigia").playToServer(OpenStellarPayload.TYPE, OpenStellarPayload.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> openStellar(ctx.player())));
		event.registrar("vestigia").playToServer(StellarActionPayload.TYPE, StellarActionPayload.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> stellarAction(ctx.player(), payload.action())));
	}
}
