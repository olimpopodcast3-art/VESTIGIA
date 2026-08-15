package net.mcreator.vestigia.client.screens;

import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.api.distmarker.Dist;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.mcreator.vestigia.client.VestigiaIncursionClient;

@EventBusSubscriber(Dist.CLIENT)
public class TimerIncursionOverlay {
	private static final int FRAME_DK = 0xFF0A1B0E;
	private static final int PANEL_TOP = 0xE61D4127;
	private static final int PANEL_BOT = 0xE6102616;
	private static final int GOLD = 0xFFD9A441;
	private static final int GOLD_BRIGHT = 0xFFF3D688;
	private static final int GOLD_DK = 0xFF8A5A1F;
	private static final int JADE = 0xFF2AA77E;
	private static final int TURQ = 0xFF2E9C9C;
	private static final int RED = 0xFFE0503C;
	private static final int RED_DK = 0xFF7A241C;
	private static final int WHITE = 0xFFFFF3D0;

	@SubscribeEvent(priority = EventPriority.NORMAL)
	public static void eventHandler(RenderGuiEvent.Pre event) {
		if (!VestigiaIncursionClient.isActive())
			return;
		Minecraft mc = Minecraft.getInstance();
		if (mc.player == null || mc.level == null || mc.options.hideGui)
			return;
		GuiGraphicsExtractor g = event.getGuiGraphics();
		Font font = mc.font;
		int w = g.guiWidth();
		int h = g.guiHeight();
		long gt = mc.level.getGameTime();
		float pulse = 0.5F + 0.5F * (float) Math.sin(gt * 0.15);

		long rem = VestigiaIncursionClient.remaining();
		int sec = (int) (rem / 20L);
		int mm = sec / 60, ss = sec % 60;
		String time = mm + ":" + (ss < 10 ? "0" + ss : "" + ss);
		float frac = Math.max(0.0F, Math.min(1.0F, rem / (float) VestigiaIncursionClient.total()));
		boolean low = sec < 30;
		boolean boss = VestigiaIncursionClient.bossActive();

		int pw = 178, ph = 46;
		int px = w / 2 - pw / 2, py = 6;
		int frame = boss ? blend(RED_DK, RED, pulse) : GOLD;

		g.fill(px - 3, py - 3, px + pw + 3, py + ph + 3, alpha(boss ? RED : GOLD, 0.15F + 0.2F * pulse));
		g.fill(px - 2, py - 2, px + pw + 2, py + ph + 2, FRAME_DK);
		g.fillGradient(px, py, px + pw, py + ph, PANEL_TOP, PANEL_BOT);
		g.fill(px, py, px + pw, py + 2, frame);
		g.fill(px, py + ph - 2, px + pw, py + ph, GOLD_DK);
		g.fill(px, py, px + 2, py + ph, GOLD_DK);
		g.fill(px + pw - 2, py, px + pw, py + ph, GOLD_DK);

		for (int i = 0; i < pw - 24; i += 10) {
			g.fill(px + 12 + i, py + 4, px + 12 + i + 5, py + 6, TURQ);
			g.fill(px + 12 + i + 2, py + 6, px + 12 + i + 8, py + 7, JADE);
		}

		glyph(g, px + 5, py + 12, boss ? RED : GOLD);
		glyph(g, px + pw - 17, py + 12, boss ? RED : GOLD);

		String title = boss ? "☠ ANCIENT ECHO ☠" : "☀ ECHO INCURSION ☀";
		g.centeredText(font, title, w / 2, py + 6, boss ? RED : GOLD_BRIGHT);

		int timeCol = low ? (pulse > 0.5F ? RED : WHITE) : GOLD_BRIGHT;
		var pose = g.pose();
		pose.pushMatrix();
		pose.translate(w / 2.0F, py + 17.0F);
		pose.scale(1.9F, 1.9F);
		g.centeredText(font, time, 0, 0, timeCol);
		pose.popMatrix();

		int bx = px + 12, by = py + ph - 9, bw = pw - 24, bh = 5;
		g.fill(bx - 1, by - 1, bx + bw + 1, by + bh + 1, FRAME_DK);
		g.fill(bx, by, bx + bw, by + bh, 0xFF20301C);
		int fill = (int) (bw * frac);
		int barCol = low ? RED : (frac < 0.4F ? 0xFFE0A040 : JADE);
		g.fill(bx, by, bx + fill, by + bh, barCol);
		g.fill(bx, by, bx + fill, by + 1, alpha(WHITE, 0.4F));

		String info;
		if (boss) {
			info = "§c☠ BOSS §7— §fEnemies: " + VestigiaIncursionClient.enemies();
		} else {
			long bt = VestigiaIncursionClient.bossInTicks() / 20L;
			info = "§eEnemies: " + VestigiaIncursionClient.enemies() + " §7• §6Boss in " + (bt / 60) + ":" + (bt % 60 < 10 ? "0" + (bt % 60) : "" + (bt % 60));
		}
		int iw = font.width(info) + 12;
		g.fill(w / 2 - iw / 2, py + ph + 3, w / 2 + iw / 2, py + ph + 15, 0xC0102616);
		g.fill(w / 2 - iw / 2, py + ph + 3, w / 2 + iw / 2, py + ph + 4, alpha(GOLD, 0.5F));
		g.centeredText(font, info, w / 2, py + ph + 5, WHITE);

		if (VestigiaIncursionClient.breaking() && !VestigiaIncursionClient.leaving())
			drawBreaking(g, w, h, gt, VestigiaIncursionClient.breakIntensity());

		if (VestigiaIncursionClient.warning() && !VestigiaIncursionClient.leaving())
			drawWarnings(g, font, w, h, gt);

		if (VestigiaIncursionClient.leaving())
			drawLeaving(g, font, w, h, gt, pulse);
	}

	private static void drawBreaking(GuiGraphicsExtractor g, int w, int h, long gt, float intensity) {
		float flick = 0.82F + 0.18F * (float) Math.sin(gt * 0.6);
		float amt = Math.max(0.12F, intensity) * flick;
		int cx = w / 2, cy = h / 2;
		int crackCol = alpha(0xEFF0FF, 0.22F + 0.4F * amt);
		int darkCol = alpha(0x0B0B14, 0.55F * amt + 0.2F);
		int cracks = 7 + (int) (intensity * 6);
		double len = Math.max(w, h) * 1.1;
		for (int c = 0; c < cracks; c++) {
			double ang = (Math.PI * 2 * c / cracks) + c * 1.37;
			double px = cx, py = cy;
			int steps = 22;
			for (int s = 1; s <= steps; s++) {
				double fr = s / (double) steps;
				int seed = (c * 7919 + s * 131) & 0xFFFF;
				double jit = ((seed % 100) / 100.0 - 0.5) * 12.0 * fr;
				double nx = cx + Math.cos(ang) * len * fr - Math.sin(ang) * jit;
				double ny = cy + Math.sin(ang) * len * fr + Math.cos(ang) * jit;
				drawSeg(g, px, py, nx, ny, crackCol, darkCol);
				if (s % 7 == 0 && c % 2 == 0) {
					double ba = ang + (seed % 2 == 0 ? 0.6 : -0.6);
					double bx = px + Math.cos(ba) * 14 * fr, by = py + Math.sin(ba) * 14 * fr;
					drawSeg(g, px, py, bx, by, crackCol, darkCol);
				}
				px = nx;
				py = ny;
			}
		}
		int vig = alpha(0x2A0006, 0.35F * amt);
		g.fillGradient(0, 0, w, 46, vig, 0x00000000);
		g.fillGradient(0, h - 46, w, h, 0x00000000, vig);
		g.fill(0, 0, 4, h, alpha(0x2A0006, 0.3F * amt));
		g.fill(w - 4, 0, w, h, alpha(0x2A0006, 0.3F * amt));
	}

	private static void drawWarnings(GuiGraphicsExtractor g, Font font, int w, int h, long gt) {
		int n = 9;
		for (int i = 0; i < n; i++) {
			int seed = (i * 48611 + 12345) & 0xFFFF;
			int x = 24 + (seed % Math.max(1, w - 48));
			int y = 26 + ((seed >> 4) % Math.max(1, h - 80));
			float blink = (float) Math.sin(gt * 0.35 + i * 1.3);
			if (blink < 0.05F)
				continue;
			int s = 12 + (seed % 8);
			drawWarnTri(g, x, y, s, blink);
		}
		float top = 0.5F + 0.5F * (float) Math.sin(gt * 0.5);
		if (top > 0.35F) {
			String warn = "⚠ WARNING ⚠";
			g.centeredText(font, warn, w / 2, 68, alpha(0xFFCC22, 0.4F + 0.6F * top));
		}
	}

	private static void drawWarnTri(GuiGraphicsExtractor g, int x, int y, int s, float a) {
		int yellow = alpha(0xFFCC22, 0.45F + 0.5F * a);
		int border = alpha(0x1A1400, 0.7F * a + 0.2F);
		int redBar = alpha(0xE0301C, 0.6F + 0.4F * a);
		for (int row = 0; row < s; row++) {
			int half = (row * (s / 2)) / s;
			int yy = y + row;
			g.fill(x - half - 1, yy, x + half + 2, yy + 1, border);
			g.fill(x - half, yy, x + half + 1, yy + 1, yellow);
		}
		g.fill(x - half0(s) - 1, y + s, x + half0(s) + 2, y + s + 1, border);
		g.fill(x, y + 4, x + 1, y + s - 5, redBar);
		g.fill(x, y + s - 4, x + 1, y + s - 2, redBar);
	}

	private static int half0(int s) {
		return s / 2;
	}

	private static void drawSeg(GuiGraphicsExtractor g, double x0, double y0, double x1, double y1, int col, int dark) {
		double dist = Math.hypot(x1 - x0, y1 - y0);
		int n = Math.max(1, (int) (dist / 2.0));
		for (int i = 0; i <= n; i++) {
			double f = i / (double) n;
			int x = (int) (x0 + (x1 - x0) * f);
			int y = (int) (y0 + (y1 - y0) * f);
			g.fill(x - 1, y - 1, x + 2, y + 2, dark);
			g.fill(x, y, x + 1, y + 1, col);
		}
	}

	private static void drawLeaving(GuiGraphicsExtractor g, Font font, int w, int h, long gt, float pulse) {
		long lt = VestigiaIncursionClient.leaveTicks();
		int count = (int) Math.ceil(lt / 20.0);
		if (count < 1)
			count = 1;
		float urg = 1.0F - Math.min(1.0F, lt / 100.0F);
		int edge = (int) (60 + 150 * pulse * (0.4F + urg));

		g.fillGradient(0, 0, w, 60, alpha(RED, 0.55F * (0.5F + pulse * 0.5F)), 0x00000000);
		g.fillGradient(0, h - 60, w, h, 0x00000000, alpha(RED, 0.55F * (0.5F + pulse * 0.5F)));
		g.fill(0, 0, 6, h, alpha(RED, 0.4F + 0.3F * pulse));
		g.fill(w - 6, 0, w, h, alpha(RED, 0.4F + 0.3F * pulse));

		for (int i = 0; i < 10; i++) {
			int seed = (i * 928371 + (int) (gt / 3)) & 0xFFFF;
			int sx = (seed % w);
			int len = 30 + (seed % 60);
			boolean top = (i % 2) == 0;
			int sy = top ? 0 : h - len;
			g.fill(sx, sy, sx + 2, sy + len, alpha(RED, 0.3F + 0.4F * pulse));
		}

		String warn = "RETURN TO THE BUBBLE!";
		g.centeredText(font, warn, w / 2, h / 2 - 40, (edge << 24) | 0x00FFFFFF | (RED & 0xFFFFFF));
		var pose = g.pose();
		pose.pushMatrix();
		pose.translate(w / 2.0F, h / 2.0F - 10.0F);
		float s = 5.0F + urg * 2.0F;
		pose.scale(s, s);
		g.centeredText(font, "" + count, 0, 0, pulse > 0.5F ? 0xFFFF3020 : WHITE);
		pose.popMatrix();
		g.centeredText(font, "The bubble is collapsing…", w / 2, h / 2 + 30, alpha(RED, 0.7F + 0.3F * pulse));
	}

	private static void glyph(GuiGraphicsExtractor g, int x, int y, int col) {
		g.fill(x + 4, y, x + 8, y + 2, col);
		g.fill(x + 2, y + 2, x + 10, y + 4, col);
		g.fill(x, y + 4, x + 12, y + 6, col);
		g.fill(x + 5, y + 6, x + 7, y + 11, col);
	}

	private static int alpha(int rgb, float a) {
		int ai = Math.max(0, Math.min(255, (int) (a * 255)));
		return (ai << 24) | (rgb & 0xFFFFFF);
	}

	private static int blend(int c0, int c1, float t) {
		int r0 = (c0 >> 16) & 0xFF, g0 = (c0 >> 8) & 0xFF, b0 = c0 & 0xFF;
		int r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
		int r = (int) (r0 + (r1 - r0) * t), gg = (int) (g0 + (g1 - g0) * t), b = (int) (b0 + (b1 - b0) * t);
		return 0xFF000000 | (r << 16) | (gg << 8) | b;
	}
}
