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

import net.mcreator.vestigia.world.inventory.StellarEmblemGUIMenu;
import net.mcreator.vestigia.init.VestigiaModScreens;
import net.mcreator.vestigia.VestigiaEmblems;

import com.mojang.blaze3d.platform.InputConstants;

public class StellarEmblemGUIScreen extends AbstractContainerScreen<StellarEmblemGUIMenu> implements VestigiaModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;

	private static final int VOID_TOP = 0xFF0A0620;
	private static final int VOID_BOT = 0xFF05030F;
	private static final int FRAME = 0xFF120A2E;
	private static final int PURPLE = 0xFF7A3AD6;
	private static final int PURPLE_DK = 0xFF3A1E70;
	private static final int CYAN = 0xFF43D6E0;
	private static final int BLUE = 0xFF2A6AE0;
	private static final int GOLD = 0xFFF3D688;
	private static final int GOLD_DK = 0xFF8A5A1F;
	private static final int WHITE = 0xFFFFFFFF;
	private static final int STAR = 0xFFEAF0FF;

	private static final int BTN_W = 150, BTN_H = 15, BTN_GAP = 17, BTN_X0 = 13, BTN_Y0 = 72;
	private static final String[] LABELS = {"☀ Clear Skies", "☂ Summon Rain", "☼ Bring Day", "☾ Bring Night", "◆ Offer 64 Diamonds — x6 DMG 5m"};

	public StellarEmblemGUIScreen(StellarEmblemGUIMenu container, Inventory inventory, Component text) {
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

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(g, mouseX, mouseY, partialTicks);
		int l = this.leftPos, t = this.topPos, w = this.imageWidth, h = this.imageHeight;
		long gt = this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getGameTime() : 0L;
		float now = gt + partialTicks;
		float pulse = 0.5F + 0.5F * (float) Math.sin(now * 0.12);

		g.fill(l - 4, t - 4, l + w + 4, t + h + 4, FRAME);
		g.fillGradient(l, t, l + w, t + h, VOID_TOP, VOID_BOT);
		g.fill(l, t, l + w, t + 2, PURPLE);
		g.fill(l, t + h - 2, l + w, t + h, PURPLE_DK);
		g.fill(l, t, l + 2, t + h, PURPLE_DK);
		g.fill(l + w - 2, t, l + w, t + h, PURPLE_DK);

		drawStars(g, l, t, w, h, now);
		drawBlackHole(g, l + w / 2, t + 38, now);

		String title = "✦ STELLAR EMBLEM ✦";
		g.centeredText(this.font, title, l + w / 2, t + 6, blend(CYAN, GOLD, pulse));

		for (int i = 0; i < LABELS.length; i++) {
			int bx = l + BTN_X0, by = t + BTN_Y0 + i * BTN_GAP;
			boolean hover = mouseX >= bx && mouseX <= bx + BTN_W && mouseY >= by && mouseY <= by + BTN_H;
			drawButton(g, bx, by, LABELS[i], hover, pulse, i == 4);
		}
	}

	private void drawStars(GuiGraphicsExtractor g, int l, int t, int w, int h, float now) {
		for (int i = 0; i < 64; i++) {
			int seed = (i * 99181 + 7) & 0xFFFF;
			int sx = seed % w;
			int sy = (seed * 7 >> 3) % h;
			if (i % 5 == 0)
				sx = (int) ((seed + now * 0.35F) % w);
			float tw = 0.5F + 0.5F * (float) Math.sin(now * 0.1 + i * 1.3);
			if (tw < 0.28F)
				continue;
			int col = alpha(STAR, 0.3F + 0.7F * tw);
			g.fill(l + sx, t + sy, l + sx + 1, t + sy + 1, col);
			if (tw > 0.85F) {
				g.fill(l + sx - 1, t + sy, l + sx + 2, t + sy + 1, alpha(STAR, 0.4F));
				g.fill(l + sx, t + sy - 1, l + sx + 1, t + sy + 2, alpha(STAR, 0.4F));
			}
		}
	}

	private void drawBlackHole(GuiGraphicsExtractor g, int cx, int cy, float now) {
		for (int arm = 0; arm < 3; arm++) {
			for (int i = 0; i < 44; i++) {
				double a = now * 0.06 + arm * (Math.PI * 2 / 3) + i * 0.17;
				double rr = 9 + i * 0.42;
				int px = cx + (int) Math.round(Math.cos(a) * rr);
				int py = cy + (int) Math.round(Math.sin(a) * rr * 0.55);
				int col = blend(PURPLE, GOLD, i / 44.0F);
				g.fill(px, py, px + 1, py + 1, alpha(col, 0.35F + 0.5F * (1.0F - i / 44.0F)));
			}
		}
		for (int i = 0; i < 40; i++) {
			double a = Math.PI * 2 * i / 40;
			int px = cx + (int) Math.round(Math.cos(a) * 9);
			int py = cy + (int) Math.round(Math.sin(a) * 9 * 0.55);
			g.fill(px - 1, py - 1, px + 1, py + 1, alpha(0xFFFFC060, 0.8F));
		}
		g.fill(cx - 7, cy - 4, cx + 7, cy + 4, 0xFF04030A);
		g.fill(cx - 5, cy - 3, cx + 5, cy + 3, 0xFF010005);
	}

	private void drawButton(GuiGraphicsExtractor g, int bx, int by, String label, boolean hover, float pulse, boolean special) {
		int edge = hover ? (special ? GOLD : CYAN) : (special ? GOLD_DK : PURPLE_DK);
		g.fill(bx - 1, by - 1, bx + BTN_W + 1, by + BTN_H + 1, edge);
		g.fillGradient(bx, by, bx + BTN_W, by + BTN_H, hover ? 0xFF2A1C5A : 0xFF180F3A, hover ? 0xFF14204A : 0xFF0C0A22);
		g.fill(bx, by, bx + BTN_W, by + 1, hover ? (special ? GOLD : CYAN) : BLUE);
		int glow = (int) (40 + 70 * pulse);
		if (hover)
			g.fill(bx, by, bx + BTN_W, by + BTN_H, (glow << 24) | ((special ? GOLD : CYAN) & 0xFFFFFF));
		int tw = this.font.width(label);
		g.text(this.font, Component.literal(label), bx + (BTN_W - tw) / 2, by + (BTN_H - 8) / 2, hover ? WHITE : (special ? GOLD : 0xFFCBD8FF));
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			int bx = this.leftPos + BTN_X0;
			for (int i = 0; i < LABELS.length; i++) {
				int by = this.topPos + BTN_Y0 + i * BTN_GAP;
				if (event.x() >= bx && event.x() <= bx + BTN_W && event.y() >= by && event.y() <= by + BTN_H) {
					ClientPacketDistributor.sendToServer(new VestigiaEmblems.StellarActionPayload(i));
					return true;
				}
			}
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

	private static int alpha(int rgb, float a) {
		int ai = Math.max(0, Math.min(255, (int) (a * 255)));
		return (ai << 24) | (rgb & 0xFFFFFF);
	}

	private static int blend(int c0, int c1, float tf) {
		int r0 = (c0 >> 16) & 0xFF, g0 = (c0 >> 8) & 0xFF, b0 = c0 & 0xFF;
		int r1 = (c1 >> 16) & 0xFF, g1 = (c1 >> 8) & 0xFF, b1 = c1 & 0xFF;
		int r = (int) (r0 + (r1 - r0) * tf), gg = (int) (g0 + (g1 - g0) * tf), b = (int) (b0 + (b1 - b0) * tf);
		return 0xFF000000 | (r << 16) | (gg << 8) | b;
	}
}
