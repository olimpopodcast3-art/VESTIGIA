package net.mcreator.vestigia.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

import net.mcreator.vestigia.VestigiaEmblems;

import org.lwjgl.glfw.GLFW;

@EventBusSubscriber(modid = "vestigia", value = Dist.CLIENT)
public class VestigiaEmblemsClient {
	private static final int JET_TICKS = 100;
	private static final int JET_COOLDOWN = 2400;
	private static int jetTicks = -1;
	private static int jetCd = 0;

	@SubscribeEvent
	public static void onKey(InputEvent.Key event) {
		if (event.getAction() != GLFW.GLFW_PRESS)
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.screen != null)
			return;
		LocalPlayer p = mc.player;
		if (event.getKey() == GLFW.GLFW_KEY_B) {
			if (VestigiaEmblems.hasEmblem(p, "seal_emblem"))
				ClientPacketDistributor.sendToServer(VestigiaEmblems.OpenStellarPayload.INSTANCE);
			else if (VestigiaEmblems.hasEmblem(p, "twisted_emblem"))
				ClientPacketDistributor.sendToServer(VestigiaEmblems.EmblemKeyPayload.INSTANCE);
			return;
		}
		if (mc.options.keyJump.getKey().getValue() == event.getKey() && !p.onGround() && jetTicks < 0 && jetCd <= 0 && VestigiaEmblems.hasEmblem(p, "fire_emblem"))
			jetTicks = JET_TICKS;
	}

	@SubscribeEvent
	public static void onClientTick(ClientTickEvent.Post event) {
		Minecraft mc = Minecraft.getInstance();
		LocalPlayer p = mc.player;
		if (p == null || mc.level == null) {
			jetTicks = -1;
			return;
		}
		if (jetCd > 0)
			jetCd--;
		if (jetTicks < 0)
			return;
		if (p.onGround() || !mc.options.keyJump.isDown()) {
			endJet();
			return;
		}
		Vec3 m = p.getDeltaMovement();
		p.setDeltaMovement(m.x, Math.min(0.62, m.y + 0.16), m.z);
		p.resetFallDistance();
		for (int i = 0; i < 4; i++) {
			double ox = (Math.random() - 0.5) * 0.6, oz = (Math.random() - 0.5) * 0.6;
			mc.level.addParticle(ParticleTypes.FLAME, p.getX() + ox, p.getY() + 0.1, p.getZ() + oz, ox * 0.2, -0.3, oz * 0.2);
			mc.level.addParticle(ParticleTypes.SMOKE, p.getX() + ox, p.getY() + 0.2, p.getZ() + oz, 0.0, -0.1, 0.0);
		}
		jetTicks--;
		if (jetTicks <= 0)
			endJet();
	}

	private static void endJet() {
		if (jetTicks >= 0)
			jetCd = JET_COOLDOWN;
		jetTicks = -1;
	}

	@SubscribeEvent
	public static void onFog(ViewportEvent.ComputeFogColor event) {
		if (VestigiaIncursionClient.isActive())
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null)
			return;
		if (!VestigiaEmblems.hasEmblem(mc.player, "aurora_emblem"))
			return;
		float t = 0.5F + 0.5F * (float) Math.sin(mc.level.getGameTime() * 0.008);
		event.setRed(0.02F + 0.04F * t);
		event.setGreen(0.10F + 0.28F * t);
		event.setBlue(0.45F + 0.35F * t);
	}
}
