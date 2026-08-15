package net.mcreator.vestigia.client.gui;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import net.mcreator.vestigia.world.inventory.AltarGUIMenu;
import net.mcreator.vestigia.init.VestigiaModScreens;
import net.mcreator.vestigia.VestigiaAltar;

import com.mojang.blaze3d.platform.InputConstants;

public class AltarGUIScreen extends AbstractContainerScreen<AltarGUIMenu> implements VestigiaModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;

	private static final int FRAME = 0xFF0A1B0E;
	private static final int JUNGLE = 0xFF1D4127;
	private static final int JUNGLE_DARK = 0xFF102616;
	private static final int GOLD = 0xFFD9A441;
	private static final int GOLD_BRIGHT = 0xFFF3D688;
	private static final int GOLD_DARK = 0xFF8A5A1F;
	private static final int JADE = 0xFF2AA77E;
	private static final int TURQ = 0xFF2E9C9C;
	private static final int STONE = 0xFF8E8676;
	private static final int STONE_DK = 0xFF5A5246;
	private static final int WHITE = 0xFFFFF3D0;
	private static final int GREEN = 0xFF3BE05A;
	private static final int RED = 0xFFE0503C;

	private static final int RING_Y = 46;
	private static final int CANNON_Y = 138;
	private static final int CENTER_X = 88;
	private static final float AMP = 52.0F;
	private static final int RING_INNER = 11;
	private static final int RING_OUTER = 18;
	private static final float TRAVEL = 11.0F;
	private static final float RING_SPEED = 0.11F;
	private static final int TARGET = 5;

	private int made = 0;
	private int attempts = 0;
	private float ballTick = -1.0F;
	private boolean lastHit = false;
	private float flashTick = -100.0F;
	private boolean won = false;
	private boolean sent = false;
	private float winTick = -1.0F;
	private boolean failed = false;
	private boolean failSent = false;
	private float failTick = -1.0F;

	public AltarGUIScreen(AltarGUIMenu container, Inventory inventory, Component text) {
		super(container, inventory, text, 176, 166);
		this.world = container.world;
		this.x = container.x;
		this.y = container.y;
		this.z = container.z;
		this.entity = container.entity;
	}

	@Override
	public void updateMenuState(int elementType, String name, Object elementState) {
		menuStateUpdateActive = true;
		menuStateUpdateActive = false;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
		super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);
	}

	private float now(float partialTicks) {
		long gt = this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getGameTime() : 0L;
		return gt + partialTicks;
	}

	private float ringX(float now) {
		return CENTER_X + AMP * (float) Math.sin(now * RING_SPEED);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(g, mouseX, mouseY, partialTicks);
		int l = this.leftPos, t = this.topPos, w = this.imageWidth, h = this.imageHeight;
		float now = now(partialTicks);
		float pulse = 0.5F + 0.5F * (float) Math.sin(now * 0.15);

		if (ballTick >= 0.0F && !won && !failed) {
			float e = now - ballTick;
			if (e >= TRAVEL) {
				float rx = ringX(now);
				lastHit = Math.abs(rx - CENTER_X) <= RING_INNER;
				flashTick = now;
				ballTick = -1.0F;
				if (lastHit) {
					made++;
					if (made >= TARGET) {
						won = true;
						winTick = now;
						if (!sent) {
							sent = true;
							ClientPacketDistributor.sendToServer(new VestigiaAltar.AltarWinPayload());
						}
					}
				} else {
					failed = true;
					failTick = now;
					if (!failSent) {
						failSent = true;
						ClientPacketDistributor.sendToServer(new VestigiaAltar.AltarFailPayload());
					}
				}
			}
		}

		g.fill(l - 4, t - 4, l + w + 4, t + h + 4, FRAME);
		g.fillGradient(l, t, l + w, t + h, JUNGLE, JUNGLE_DARK);
		g.fill(l, t, l + w, t + 3, GOLD);
		g.fill(l, t + h - 3, l + w, t + h, GOLD_DARK);
		g.fill(l, t, l + 3, t + h, GOLD_DARK);
		g.fill(l + w - 3, t, l + w, t + h, GOLD_DARK);
		for (int i = 0; i < w - 12; i += 12) {
			g.fill(l + 6 + i, t + 5, l + 6 + i + 6, t + 8, TURQ);
			g.fill(l + 6 + i + 3, t + 8, l + 6 + i + 9, t + 10, JADE);
		}
		String title = "☀ TRIAL OF THE RING ☀";
		g.text(this.font, Component.literal(title), l + (w - this.font.width(title)) / 2, t + 14, GOLD_BRIGHT);

		int aimX = l + CENTER_X;
		for (int yy = t + RING_Y + 4; yy < t + CANNON_Y - 6; yy += 8)
			g.fill(aimX - 1, yy, aimX + 1, yy + 4, (0x40 << 24) | (GOLD & 0xFFFFFF));

		float rx = ringX(now);
		drawRing(g, l + (int) rx, t + RING_Y, RING_OUTER, RING_INNER, now);

		int cx = l + CENTER_X, cy = t + CANNON_Y;
		g.fillGradient(cx - 12, cy, cx + 12, cy + 14, STONE, STONE_DK);
		g.fill(cx - 12, cy, cx + 12, cy + 2, GOLD_DARK);
		g.fill(cx - 6, cy - 6, cx + 6, cy, STONE_DK);
		g.fill(cx - 4, cy - 8, cx + 4, cy - 6, GOLD);

		if (ballTick >= 0.0F) {
			float e = Math.min(TRAVEL, now - ballTick);
			float frac = e / TRAVEL;
			int by = (int) (cy - 8 + ((t + RING_Y) - (cy - 8)) * frac);
			drawBall(g, cx, by);
		}

		if (now - flashTick < 16.0F) {
			String msg = lastHit ? "✔ SCORE!" : "✘ MISS";
			int col = lastHit ? GREEN : RED;
			g.text(this.font, Component.literal(msg), l + (w - this.font.width(msg)) / 2, t + RING_Y + RING_OUTER + 6, col);
		}

		int pipY = t + h - 22;
		int pipTotal = TARGET * 16 - 6;
		int pipX = l + (w - pipTotal) / 2;
		for (int i = 0; i < TARGET; i++) {
			int px = pipX + i * 16;
			boolean on = i < made;
			g.fill(px - 1, pipY - 1, px + 11, pipY + 11, on ? GOLD_BRIGHT : STONE_DK);
			g.fillGradient(px, pipY, px + 10, pipY + 10, on ? GOLD : JUNGLE_DARK, on ? GOLD_DARK : FRAME);
			if (on)
				g.fill(px + 3, pipY + 3, px + 7, pipY + 7, WHITE);
		}
		String info = "Sink 5 balls in the ring   " + made + " / " + TARGET;
		g.text(this.font, Component.literal(info), l + (w - this.font.width(info)) / 2, t + h - 34, WHITE);

		if (won) {
			int a = (int) Math.min(200, (now - winTick) * 14);
			g.fill(l, t, l + w, t + h, (a << 24) | (JADE & 0xFFFFFF));
			String vic = "✦ VICTORY ✦";
			var pose = g.pose();
			pose.pushMatrix();
			pose.translate(l + w / 2.0F, t + h / 2.0F - 8.0F);
			pose.scale(1.8F, 1.8F);
			g.centeredText(this.font, vic, 0, 0, pulse > 0.5F ? GOLD_BRIGHT : WHITE);
			pose.popMatrix();
			g.centeredText(this.font, "Aztec Emblem + 50 souls", l + w / 2, t + h / 2 + 16, GOLD_BRIGHT);
			if (now - winTick > 32.0F && this.minecraft != null && this.minecraft.player != null)
				this.minecraft.player.closeContainer();
		}

		if (failed) {
			int a = (int) Math.min(210, (now - failTick) * 22);
			g.fill(l, t, l + w, t + h, (a << 24) | (RED & 0xFFFFFF));
			var pose = g.pose();
			pose.pushMatrix();
			pose.translate(l + w / 2.0F, t + h / 2.0F - 6.0F);
			pose.scale(1.9F, 1.9F);
			g.centeredText(this.font, "✘ MISS!", 0, 0, pulse > 0.5F ? 0xFFFF3020 : WHITE);
			pose.popMatrix();
			g.centeredText(this.font, "The sky answers…", l + w / 2, t + h / 2 + 16, WHITE);
			if (now - failTick > 14.0F && this.minecraft != null && this.minecraft.player != null)
				this.minecraft.player.closeContainer();
		}
	}

	private void drawRing(GuiGraphicsExtractor g, int cx, int cy, int rOut, int rIn, float now) {
		boolean centered = Math.abs(cx - (this.leftPos + CENTER_X)) <= RING_INNER;
		int inner = centered ? GOLD_BRIGHT : GOLD;
		for (int i = 0; i < 48; i++) {
			double a = i / 48.0 * Math.PI * 2;
			int ox = cx + (int) Math.round(Math.cos(a) * rOut);
			int oy = cy + (int) Math.round(Math.sin(a) * rOut * 0.55);
			g.fill(ox - 2, oy - 2, ox + 2, oy + 2, (i % 4 == 0) ? STONE : STONE_DK);
			int ix = cx + (int) Math.round(Math.cos(a) * rIn);
			int iy = cy + (int) Math.round(Math.sin(a) * rIn * 0.55);
			g.fill(ix - 1, iy - 1, ix + 1, iy + 1, inner);
		}
	}

	private void drawBall(GuiGraphicsExtractor g, int cx, int cy) {
		g.fill(cx - 4, cy - 2, cx + 4, cy + 2, GOLD_DARK);
		g.fill(cx - 3, cy - 3, cx + 3, cy + 3, GOLD);
		g.fill(cx - 2, cy - 2, cx + 1, cy + 1, GOLD_BRIGHT);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0 && !won && !failed && made < TARGET && ballTick < 0.0F) {
			ballTick = now(0.0F);
			attempts++;
			return true;
		}
		return super.mouseClicked(event, doubleClick);
	}

	@Override
	public boolean keyPressed(KeyEvent event) {
		int key = InputConstants.getKey(event).getValue();
		if (key == 256) {
			this.minecraft.player.closeContainer();
			return true;
		}
		return super.keyPressed(event);
	}

	@Override
	protected void extractLabels(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY) {
	}

	@Override
	public void init() {
		super.init();
	}
}
