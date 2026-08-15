package net.mcreator.vestigia.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

import net.minecraft.client.Minecraft;

@EventBusSubscriber(modid = "vestigia", value = Dist.CLIENT)
public class VestigiaIncursionClient {
	private static final int BOSS_ELAPSED = 800;
	private static final long FINAL_REMAINING = 2080L;
	private static final long WARN_ELAPSED = 2760L;
	private static boolean active = false;
	private static long startTime = 0L;
	private static long endTime = 0L;
	private static int phase = 0;
	private static int enemies = 0;
	private static boolean leaving = false;
	private static long leaveDeadline = 0L;

	public static void onState(boolean a, long start, long end, int ph, int en) {
		boolean was = active;
		active = a;
		startTime = start;
		endTime = end;
		phase = ph;
		enemies = en;
		if (!a) {
			leaving = false;
			return;
		}
		if (!was) {
			Minecraft mc = Minecraft.getInstance();
			if (mc.player != null)
				mc.getSoundManager().play(new IncursionSound());
		}
	}

	public static void onWarn(boolean lv, long deadline) {
		leaving = lv;
		leaveDeadline = deadline;
	}

	public static boolean isActive() {
		if (!active)
			return false;
		Minecraft mc = Minecraft.getInstance();
		if (mc.level != null && mc.level.getGameTime() > endTime + 20L) {
			active = false;
			return false;
		}
		return true;
	}

	public static long remaining() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return 0L;
		return Math.max(0L, endTime - mc.level.getGameTime());
	}

	public static long total() {
		return Math.max(1L, endTime - startTime);
	}

	public static int phase() {
		return phase;
	}

	public static int enemies() {
		return enemies;
	}

	public static boolean bossActive() {
		return phase >= 1;
	}

	public static long bossInTicks() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return 0L;
		return Math.max(0L, (startTime + BOSS_ELAPSED) - mc.level.getGameTime());
	}

	public static boolean breaking() {
		return isActive() && remaining() <= FINAL_REMAINING;
	}

	public static float breakIntensity() {
		if (!breaking())
			return 0.0F;
		return Math.max(0.0F, Math.min(1.0F, 1.0F - remaining() / (float) FINAL_REMAINING));
	}

	public static boolean warning() {
		return isActive() && (gameTime() - startTime) >= WARN_ELAPSED;
	}

	public static boolean leaving() {
		return active && leaving;
	}

	public static long leaveTicks() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.level == null)
			return 0L;
		return Math.max(0L, leaveDeadline - mc.level.getGameTime());
	}

	private static long gameTime() {
		Minecraft mc = Minecraft.getInstance();
		return mc.level != null ? mc.level.getGameTime() : 0L;
	}

	@SubscribeEvent
	public static void onFog(ViewportEvent.ComputeFogColor event) {
		if (!isActive())
			return;
		long period = breaking() ? (long) (40L - 26L * breakIntensity()) : 120L;
		if (period < 8L)
			period = 8L;
		float hue = (float) ((gameTime() % period) / (double) period);
		float sat = leaving ? 0.9F : 0.8F;
		float[] rgb = hsv(hue, sat, leaving ? 0.7F : 0.9F);
		if (leaving) {
			rgb[0] = Math.min(1.0F, rgb[0] * 0.5F + 0.5F);
		}
		event.setRed(rgb[0]);
		event.setGreen(rgb[1]);
		event.setBlue(rgb[2]);
	}

	@SubscribeEvent
	public static void onCamera(ViewportEvent.ComputeCameraAngles event) {
		if (!isActive())
			return;
		int elapsed = (int) (gameTime() - startTime);
		float burst = elapsed < 40 ? (1.0F - elapsed / 40.0F) : 0.0F;
		float amp = 0.18F + burst * 1.6F + (phase >= 2 ? 0.5F : phase >= 1 ? 0.3F : 0.0F);
		if (breaking())
			amp += 0.5F + breakIntensity() * 1.1F;
		if (warning())
			amp += 0.9F;
		if (leaving) {
			long lt = leaveTicks();
			amp += 0.6F + (1.0F - Math.min(1.0F, lt / 100.0F)) * 1.4F;
		}
		event.setRoll(event.getRoll() + (float) ((Math.random() * 2 - 1) * amp));
		event.setYaw(event.getYaw() + (float) ((Math.random() * 2 - 1) * amp * 0.6));
		event.setPitch(event.getPitch() + (float) ((Math.random() * 2 - 1) * amp * 0.6));
	}

	private static float[] hsv(float h, float s, float v) {
		int i = (int) (h * 6) % 6;
		float f = h * 6 - (float) Math.floor(h * 6);
		float p = v * (1 - s), q = v * (1 - f * s), t = v * (1 - (1 - f) * s);
		return switch (i) {
			case 0 -> new float[]{v, t, p};
			case 1 -> new float[]{q, v, p};
			case 2 -> new float[]{p, v, t};
			case 3 -> new float[]{p, q, v};
			case 4 -> new float[]{t, p, v};
			default -> new float[]{v, p, q};
		};
	}
}
