package net.mcreator.vestigia;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.registries.RegisterEvent;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.CriticalHitEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BrushItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BrushableBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import net.mcreator.vestigia.block.AshDepositBlock;
import net.mcreator.vestigia.block.BronzeDepositBlock;
import net.mcreator.vestigia.block.MudDepositBlock;
import net.mcreator.vestigia.item.ColdhorsehairbrushItem;
import net.mcreator.vestigia.item.PercussionHammerItem;
import net.mcreator.vestigia.item.CapibaraOfWisdomItem;
import net.mcreator.vestigia.item.RubyBladeItem;
import net.mcreator.vestigia.item.HookItem;
import net.mcreator.vestigia.item.GoldenStaffItem;
import net.mcreator.vestigia.entity.VestigiaEntities;
import net.mcreator.vestigia.init.VestigiaModBlocks;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.UUID;

@EventBusSubscriber(modid = "vestigia")
public class VestigiaEvents {
	@SubscribeEvent
	public static void addBrushableBlocks(BlockEntityTypeAddBlocksEvent event) {
		List<Block> brushables = new ArrayList<>();
		for (Block block : new Block[]{VestigiaModBlocks.ASH_DEPOSIT.get(), VestigiaModBlocks.BRONZE_DEPOSIT.get(), VestigiaModBlocks.MUD_DEPOSIT.get()}) {
			if (block instanceof BrushableBlock)
				brushables.add(block);
		}
		if (!brushables.isEmpty())
			event.modify(BlockEntityType.BRUSHABLE_BLOCK, brushables.toArray(new Block[0]));
	}

	@SubscribeEvent
	public static void onDepositBrushAttempt(PlayerInteractEvent.RightClickBlock event) {
		Block block = event.getLevel().getBlockState(event.getPos()).getBlock();
		if (!(block instanceof AshDepositBlock || block instanceof BronzeDepositBlock || block instanceof MudDepositBlock))
			return;
		Item held = event.getItemStack().getItem();
		if (held instanceof BrushItem && !(held instanceof ColdhorsehairbrushItem)) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.FAIL);
			if (!event.getLevel().isClientSide() && event.getEntity() != null) {
				event.getEntity().sendOverlayMessage(Component.literal("You can't excavate this with that brush. Craft the Fine Bristle Brush: 3 string on top + 1 brush."));
			}
		}
	}

	@SubscribeEvent
	public static void onPercussionHammerBreak(BreakBlockEvent event) {
		Player player = event.getPlayer();
		if (player == null || !(player.getMainHandItem().getItem() instanceof PercussionHammerItem))
			return;
		Level level = player.level();
		if (level.isClientSide())
			return;
		HitResult hit = player.pick(6.0, 0.0F, false);
		if (!(hit instanceof BlockHitResult bhr))
			return;
		Direction.Axis axis = bhr.getDirection().getAxis();
		BlockPos center = event.getPos();
		for (int u = -1; u <= 1; u++) {
			for (int v = -1; v <= 1; v++) {
				if (u == 0 && v == 0)
					continue;
				BlockPos np = switch (axis) {
					case Y -> center.offset(u, 0, v);
					case X -> center.offset(0, u, v);
					case Z -> center.offset(u, v, 0);
				};
				BlockState st = level.getBlockState(np);
				if (!st.isAir() && st.getDestroySpeed(level, np) >= 0)
					level.destroyBlock(np, true, player);
			}
		}
	}

	@SubscribeEvent
	public static void onCapibaraSave(LivingDeathEvent event) {
		if (!(event.getEntity() instanceof Player player) || player.level().isClientSide())
			return;
		ItemStack capi = null;
		InteractionHand capiHand = null;
		for (InteractionHand hand : InteractionHand.values()) {
			ItemStack held = player.getItemInHand(hand);
			if (held.getItem() instanceof CapibaraOfWisdomItem) {
				capi = held;
				capiHand = hand;
				break;
			}
		}
		if (capi == null)
			return;
		int uses = capi.getMaxDamage() - capi.getDamageValue();
		float chance = switch (uses) {
			case 5 -> 1.0F;
			case 4 -> 0.8F;
			case 3 -> 0.5F;
			case 2 -> 0.25F;
			default -> 0.15F;
		};
		boolean saved = player.getRandom().nextFloat() < chance;
		int nextDamage = capi.getDamageValue() + 1;
		if (nextDamage >= capi.getMaxDamage())
			player.setItemInHand(capiHand, new ItemStack(Items.TOTEM_OF_UNDYING));
		else
			capi.setDamageValue(nextDamage);
		if (saved) {
			event.setCanceled(true);
			player.setHealth(1.0F);
			player.removeAllEffects();
			player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 900, 1));
			player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 100, 1));
			player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 800, 0));
			if (player.level() instanceof ServerLevel sl)
				sl.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 60, 0.35, 0.6, 0.35, 0.25);
			player.level().broadcastEntityEvent(player, (byte) 35);
		}
	}

	@SubscribeEvent
	public static void onRubyBladeCrit(CriticalHitEvent event) {
		if (!event.isCriticalHit())
			return;
		Player player = event.getEntity();
		if (player.level().isClientSide() || !(player.getMainHandItem().getItem() instanceof RubyBladeItem))
			return;
		Entity target = event.getTarget();
		if (target instanceof LivingEntity le) {
			le.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 80, 5));
			le.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 2));
			le.igniteForSeconds(7.0F);
		}
		if (player.level() instanceof ServerLevel sl) {
			double tx = target.getX();
			double ty = target.getY() + target.getBbHeight() * 0.5;
			double tz = target.getZ();
			sl.sendParticles(ParticleTypes.FLAME, tx, ty, tz, 70, 0.5, 0.6, 0.5, 0.12);
			sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, tx, ty, tz, 35, 0.45, 0.55, 0.45, 0.08);
			sl.sendParticles(ParticleTypes.LAVA, tx, ty, tz, 14, 0.3, 0.3, 0.3, 0.0);
			sl.sendParticles(ParticleTypes.CRIT, tx, ty, tz, 45, 0.5, 0.5, 0.5, 0.5);
			sl.sendParticles(ParticleTypes.ENCHANTED_HIT, tx, ty, tz, 30, 0.5, 0.5, 0.5, 0.35);
			sl.playSound(null, target.blockPosition(), SoundEvents.FIRECHARGE_USE, SoundSource.PLAYERS, 1.3F, 0.8F);
			sl.playSound(null, target.blockPosition(), SoundEvents.BLAZE_SHOOT, SoundSource.PLAYERS, 1.0F, 1.4F);
		}
		ItemStack blade = player.getMainHandItem();
		int dmg = blade.getDamageValue() + 1;
		if (dmg >= blade.getMaxDamage())
			player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("vestigia:uncharged_ruby_blade"))));
		else
			blade.setDamageValue(dmg);
	}

	@SubscribeEvent
	public static void onHookFall(LivingFallEvent event) {
		if (event.getEntity() instanceof Player player && player.getMainHandItem().getItem() instanceof HookItem)
			event.setCanceled(true);
	}

	private static final int RING_TICKS = 80;
	private static final int BOOST_TICKS = 2300;
	private static final int AURA_START = RING_TICKS + 1340;
	private static final int BOOST_END = RING_TICKS + BOOST_TICKS;
	private static final int DOUBLE_TICKS = 340;
	private static final Map<UUID, Integer> GOLD_TICK = new HashMap<>();
	private static final Set<UUID> AURA_PLAYERS = new HashSet<>();

	public record PowerUpMusicPayload() implements CustomPacketPayload {
		public static final CustomPacketPayload.Type<PowerUpMusicPayload> TYPE = new CustomPacketPayload.Type<>(Identifier.parse("vestigia:power_up_music"));
		public static final StreamCodec<RegistryFriendlyByteBuf, PowerUpMusicPayload> CODEC = StreamCodec.unit(new PowerUpMusicPayload());

		@Override
		public CustomPacketPayload.Type<PowerUpMusicPayload> type() {
			return TYPE;
		}
	}

	@SubscribeEvent
	public static void onRegisterPayloads(RegisterPayloadHandlersEvent event) {
		event.registrar("vestigia").playToClient(PowerUpMusicPayload.TYPE, PowerUpMusicPayload.CODEC, (payload, ctx) -> ctx.enqueueWork(() -> net.mcreator.vestigia.client.VestigiaClientPayloads.playPowerUp()));
	}

	private static boolean goldenActivate(Player player, ItemStack stack) {
		if (!(stack.getItem() instanceof GoldenStaffItem))
			return false;
		if (!player.level().isClientSide() && !GOLD_TICK.containsKey(player.getUUID()) && !player.getCooldowns().isOnCooldown(stack)) {
			GOLD_TICK.put(player.getUUID(), 0);
			player.getCooldowns().addCooldown(stack, BOOST_END + 40);
			if (player.level() instanceof ServerLevel sl)
				sl.playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 0.8F);
		}
		return true;
	}

	@SubscribeEvent
	public static void onGoldenStaffUseItem(PlayerInteractEvent.RightClickItem event) {
		if (goldenActivate(event.getEntity(), event.getItemStack())) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}

	@SubscribeEvent
	public static void onGoldenStaffUseBlock(PlayerInteractEvent.RightClickBlock event) {
		if (goldenActivate(event.getEntity(), event.getItemStack())) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}

	@SubscribeEvent
	public static void onGoldenStaffTick(PlayerTickEvent.Post event) {
		Player player = event.getEntity();
		if (player.level().isClientSide())
			return;
		UUID id = player.getUUID();
		Integer prev = GOLD_TICK.get(id);
		if (prev == null)
			return;
		int t = prev + 1;
		GOLD_TICK.put(id, t);
		if (!(player.level() instanceof ServerLevel sl))
			return;
		double cx = player.getX(), cy = player.getY(), cz = player.getZ();
		DustParticleOptions gold = new DustParticleOptions(0xFFD700, 1.4F);
		if (t <= RING_TICKS) {
			double base = t * 0.25;
			int pts = 20;
			for (int i = 0; i < pts; i++) {
				double a = base + (Math.PI * 2 * i / pts);
				double px = cx + Math.cos(a);
				double pz = cz + Math.sin(a);
				sl.sendParticles(gold, px, cy + 0.15, pz, 1, 0.0, 0.0, 0.0, 0.0);
				if (i % 5 == 0)
					sl.sendParticles(ParticleTypes.END_ROD, px, cy + 0.15, pz, 1, 0.0, 0.02, 0.0, 0.0);
			}
		}
		if (t == RING_TICKS) {
			applyOpEffects(player);
			if (player instanceof ServerPlayer sp)
				PacketDistributor.sendToPlayer(sp, new PowerUpMusicPayload());
			sl.playSound(null, player.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 1.4F, 1.0F);
		}
		if (t == AURA_START) {
			AURA_PLAYERS.add(id);
			applyDoubledEffects(player);
			sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, cx, cy + 1.0, cz, 1, 0.0, 0.0, 0.0, 0.0);
			sl.playSound(null, player.blockPosition(), SoundEvents.TRIDENT_THUNDER.value(), SoundSource.PLAYERS, 1.5F, 1.0F);
		}
		if (t >= AURA_START && t < BOOST_END) {
			for (int i = 0; i < 8; i++) {
				double a = player.getRandom().nextDouble() * Math.PI * 2;
				double r = player.getRandom().nextDouble() * 0.65;
				double px = cx + Math.cos(a) * r;
				double pz = cz + Math.sin(a) * r;
				double py = cy + player.getRandom().nextDouble() * 1.9;
				sl.sendParticles(gold, px, py, pz, 0, 0.0, 0.25, 0.0, 1.0);
			}
			sl.sendParticles(ParticleTypes.FLAME, cx, cy + 0.2, cz, 4, 0.35, 0.1, 0.35, 0.02);
			sl.sendParticles(ParticleTypes.END_ROD, cx, cy + 1.0, cz, 2, 0.4, 0.6, 0.4, 0.01);
		}
		if (t >= BOOST_END) {
			AURA_PLAYERS.remove(id);
			GOLD_TICK.remove(id);
			consumeStaff(player);
			sl.playSound(null, player.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 1.0F, 1.0F);
		}
	}

	@SubscribeEvent
	public static void onGoldenStaffAura(LivingIncomingDamageEvent event) {
		Entity src = event.getSource().getEntity();
		if (src instanceof Player p && p != event.getEntity() && AURA_PLAYERS.contains(p.getUUID()))
			event.setAmount(event.getAmount() + 5.0F);
	}

	private static void applyOpEffects(Player p) {
		int d = BOOST_TICKS;
		p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, d, 3, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, d, 2, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, d, 3, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.SPEED, d, 3, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.HASTE, d, 6, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, d, 4, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, d, 0, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, d, 3, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.SATURATION, d, 0, false, false));
		p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, d, 0, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.GLOWING, d, 0, false, true));
	}

	private static void applyDoubledEffects(Player p) {
		int d = DOUBLE_TICKS;
		p.addEffect(new MobEffectInstance(MobEffects.STRENGTH, d, 7, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, d, 5, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, d, 7, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.SPEED, d, 7, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.HASTE, d, 13, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, d, 9, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, d, 1, false, true));
		p.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, d, 7, false, true));
	}

	private static void consumeStaff(Player player) {
		if (player.getMainHandItem().getItem() instanceof GoldenStaffItem) {
			consumeInHand(player, InteractionHand.MAIN_HAND);
			return;
		}
		if (player.getOffhandItem().getItem() instanceof GoldenStaffItem) {
			consumeInHand(player, InteractionHand.OFF_HAND);
			return;
		}
		Inventory inv = player.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack s = inv.getItem(i);
			if (s.getItem() instanceof GoldenStaffItem) {
				int dmg = s.getDamageValue() + 1;
				if (dmg >= s.getMaxDamage())
					inv.setItem(i, unchargedStaff());
				else
					s.setDamageValue(dmg);
				return;
			}
		}
	}

	private static void consumeInHand(Player player, InteractionHand hand) {
		ItemStack s = player.getItemInHand(hand);
		int dmg = s.getDamageValue() + 1;
		if (dmg >= s.getMaxDamage())
			player.setItemInHand(hand, unchargedStaff());
		else
			s.setDamageValue(dmg);
	}

	private static ItemStack unchargedStaff() {
		return new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("vestigia:uncharged_golden_staff")));
	}

	@SubscribeEvent
	public static void onRegisterEntities(RegisterEvent event) {
		event.register(Registries.ENTITY_TYPE, VestigiaEntities.BOOMERANG_KEY.identifier(), () -> VestigiaEntities.BOOMERANG);
		event.register(Registries.ENTITY_TYPE, VestigiaEntities.PEBBLE_KEY.identifier(), () -> VestigiaEntities.PEBBLE);
		event.register(Registries.ENTITY_TYPE, VestigiaEntities.PHANTOM_SPEAR_KEY.identifier(), () -> VestigiaEntities.PHANTOM_SPEAR);
		event.register(Registries.ENTITY_TYPE, VestigiaEntities.BRONZE_AXE_KEY.identifier(), () -> VestigiaEntities.BRONZE_AXE);
	}
}
