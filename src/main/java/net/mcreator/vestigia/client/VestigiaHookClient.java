package net.mcreator.vestigia.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LightLayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import net.mcreator.vestigia.item.HookItem;

@EventBusSubscriber(modid = "vestigia", value = Dist.CLIENT)
public class VestigiaHookClient {
	private static boolean ACTIVE = false;
	private static Vec3 ANCHOR = Vec3.ZERO;
	private static BlockPos ANCHOR_POS = BlockPos.ZERO;
	private static double ROPE_LEN = 0.0;
	private static int LAST_ANCHOR_TICK = -100;
	private static boolean WAS_AIRBORNE = false;

	private static final double MAX_RANGE = 64.0;
	private static final double MIN_LEN = 3.0;
	private static final double REEL_SPEED = 0.35;
	private static final double REEL_PULL = 0.09;
	private static final double TOP_SPEED = 1.8;
	private static final double SWING_BOOST = 1.03;

	public record HookedProperty() implements ConditionalItemModelProperty {
		public static final MapCodec<HookedProperty> MAP_CODEC = MapCodec.unit(new HookedProperty());

		@Override
		public boolean get(ItemStack itemStack, ClientLevel level, LivingEntity owner, int seed, ItemDisplayContext displayContext) {
			return ACTIVE && owner == Minecraft.getInstance().player && owner.getMainHandItem() == itemStack;
		}

		@Override
		public MapCodec<HookedProperty> type() {
			return MAP_CODEC;
		}
	}

	@SubscribeEvent
	public static void registerHookedProperty(RegisterConditionalItemModelPropertyEvent event) {
		event.register(Identifier.parse("vestigia:hooked"), HookedProperty.MAP_CODEC);
	}

	@SubscribeEvent
	public static void onUseKey(InputEvent.InteractionKeyMappingTriggered event) {
		if (!event.isUseItem() || event.getHand() != InteractionHand.MAIN_HAND)
			return;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || !(player.getMainHandItem().getItem() instanceof HookItem))
			return;
		event.setCanceled(true);
		event.setSwingHand(true);
		if (player.tickCount - LAST_ANCHOR_TICK < 3)
			return;
		HitResult hr = player.pick(MAX_RANGE, 1.0F, false);
		if (hr instanceof BlockHitResult bhr && hr.getType() == HitResult.Type.BLOCK) {
			attach(player, bhr.getLocation(), bhr.getBlockPos());
		} else {
			detach();
		}
	}

	private static void attach(LocalPlayer player, Vec3 point, BlockPos pos) {
		ANCHOR = point;
		ANCHOR_POS = pos.immutable();
		ROPE_LEN = Math.min(MAX_RANGE, Math.max(MIN_LEN, ANCHOR.distanceTo(player.position())));
		ACTIVE = true;
		WAS_AIRBORNE = false;
		LAST_ANCHOR_TICK = player.tickCount;
		Vec3 pull = ANCHOR.subtract(player.position());
		Vec3 flat = new Vec3(pull.x, 0.0, pull.z);
		if (flat.lengthSqr() > 1.0e-4)
			player.setDeltaMovement(player.getDeltaMovement().add(flat.normalize().scale(0.35)));
		player.level().playLocalSound(player.getX(), player.getY(), player.getZ(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.PLAYERS, 1.0F, 1.4F, false);
	}

	private static void detach() {
		ACTIVE = false;
		WAS_AIRBORNE = false;
	}

	@SubscribeEvent
	public static void onRenderRope(SubmitCustomGeometryEvent event) {
		if (!ACTIVE)
			return;
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer player = mc.player;
		if (player == null || !(player.getMainHandItem().getItem() instanceof HookItem))
			return;
		float pt = mc.getDeltaTracker().getGameTimeDeltaPartialTick(true);
		Vec3 cam = mc.gameRenderer.getMainCamera().position();
		Vec3 look = player.getViewVector(pt);
		Vec3 right = look.cross(new Vec3(0.0, 1.0, 0.0));
		if (right.lengthSqr() > 1.0e-4)
			right = right.normalize();
		Vec3 start = player.getEyePosition(pt).add(look.scale(0.4)).add(right.scale(0.35)).add(0.0, -0.3, 0.0);
		EntityRenderState.LeashState ls = new EntityRenderState.LeashState();
		ls.start = start;
		ls.end = ANCHOR;
		ls.offset = Vec3.ZERO;
		ls.slack = true;
		BlockPos sp = BlockPos.containing(start);
		BlockPos ep = BlockPos.containing(ANCHOR);
		ls.startBlockLight = player.level().getBrightness(LightLayer.BLOCK, sp);
		ls.endBlockLight = player.level().getBrightness(LightLayer.BLOCK, ep);
		ls.startSkyLight = player.level().getBrightness(LightLayer.SKY, sp);
		ls.endSkyLight = player.level().getBrightness(LightLayer.SKY, ep);
		PoseStack ps = event.getPoseStack();
		ps.pushPose();
		ps.translate(start.x - cam.x, start.y - cam.y, start.z - cam.z);
		event.getSubmitNodeCollector().submitLeash(ps, ls);
		ps.popPose();
	}

	@SubscribeEvent
	public static void onPlayerTick(PlayerTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		if (!ACTIVE || event.getEntity() != mc.player)
			return;
		LocalPlayer player = mc.player;
		if (!(player.getMainHandItem().getItem() instanceof HookItem)) {
			detach();
			return;
		}
		if (mc.options.keyShift.isDown()) {
			detach();
			return;
		}
		BlockState anchorState = player.level().getBlockState(ANCHOR_POS);
		if (anchorState.isAir()) {
			detach();
			return;
		}
		if (!player.onGround())
			WAS_AIRBORNE = true;
		else if (WAS_AIRBORNE && !mc.options.keyJump.isDown()) {
			detach();
			return;
		}
		Vec3 pos = player.position();
		Vec3 rope = ANCHOR.subtract(pos);
		double dist = rope.length();
		if (dist < 1.0e-4)
			return;
		Vec3 dir = rope.scale(1.0 / dist);
		boolean jump = mc.options.keyJump.isDown();
		if (jump && ROPE_LEN > MIN_LEN)
			ROPE_LEN = Math.max(MIN_LEN, ROPE_LEN - REEL_SPEED);
		Vec3 vel = player.getDeltaMovement();
		if (dist > ROPE_LEN) {
			Vec3 target = ANCHOR.subtract(dir.scale(ROPE_LEN));
			player.setPos(pos.x + (target.x - pos.x) * 0.6, pos.y + (target.y - pos.y) * 0.6, pos.z + (target.z - pos.z) * 0.6);
			double radial = vel.dot(dir);
			if (radial < 0)
				vel = vel.subtract(dir.scale(radial));
			double hsp = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
			if (hsp > 0.15 && hsp < TOP_SPEED)
				vel = new Vec3(vel.x * SWING_BOOST, vel.y, vel.z * SWING_BOOST);
		}
		if (jump)
			vel = vel.add(dir.scale(REEL_PULL));
		double hs = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
		if (hs > TOP_SPEED) {
			double s = TOP_SPEED / hs;
			vel = new Vec3(vel.x * s, vel.y, vel.z * s);
		}
		player.setDeltaMovement(vel);
		player.resetFallDistance();
		if (player.tickCount % 2 == 0 && player.level() instanceof ClientLevel cl) {
			Vec3 mid = pos.add(0.0, player.getBbHeight() * 0.7, 0.0).lerp(ANCHOR, 0.5);
			cl.addParticle(ParticleTypes.CRIT, mid.x, mid.y, mid.z, 0.0, 0.0, 0.0);
		}
	}
}
