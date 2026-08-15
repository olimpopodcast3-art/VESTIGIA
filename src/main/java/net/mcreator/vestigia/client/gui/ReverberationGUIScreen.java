package net.mcreator.vestigia.client.gui;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

import net.mcreator.vestigia.VestigiaSellos;
import net.mcreator.vestigia.world.inventory.ReverberationGUIMenu;
import net.mcreator.vestigia.init.VestigiaModScreens;

import com.mojang.blaze3d.platform.InputConstants;

public class ReverberationGUIScreen extends AbstractContainerScreen<ReverberationGUIMenu> implements VestigiaModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;
	private boolean prevDone = false;
	private float forgeGlow = 0.0F;
	private int flash = 0;

	private static final int VOID = 0xFF0B0A1E;
	private static final int PANEL_TOP = 0xFF241C4A;
	private static final int PANEL_BOT = 0xFF120E28;
	private static final int BORDER = 0xFFC9A24B;
	private static final int BORDER_DARK = 0xFF6E4E1C;
	private static final int CYAN = 0x34E0F0;
	private static final int CYAN_BRIGHT = 0x9BF6FF;
	private static final int VIOLET = 0x8A5CFF;
	private static final int VIOLET_BRIGHT = 0xC7A8FF;
	private static final int TEAL = 0x4FE3D0;
	private static final int SLOT_BG = 0xFF0E0B22;
	private static final int SLOT_FRAME = 0xFF3A2E6E;
	private static final int TEXT_GOLD = 0xFFF3D688;
	private static final int INK = 0xFFB9A9E8;

	private static final int S0X = 35, S1X = 71, S2X = 124, SY = 33;

	public ReverberationGUIScreen(ReverberationGUIMenu container, Inventory inventory, Component text) {
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

	private static int alpha(int rgb, float a) {
		int ai = Math.max(0, Math.min(255, (int) (a * 255)));
		return (ai << 24) | (rgb & 0xFFFFFF);
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(g, mouseX, mouseY, partialTicks);
		int l = this.leftPos, t = this.topPos, w = this.imageWidth, h = this.imageHeight;
		long gt = this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getGameTime() : 0L;
		double time = gt + partialTicks;
		float pulse = 0.5F + 0.5F * (float) Math.sin(time * 0.12);
		float pulse2 = 0.5F + 0.5F * (float) Math.sin(time * 0.19 + 1.7);

		g.fill(l - 5, t - 5, l + w + 5, t + h + 5, 0xFF05040F);
		g.fill(l - 3, t - 3, l + w + 3, t + h + 3, alpha(VIOLET, 0.25F + 0.25F * pulse));
		g.fillGradient(l, t, l + w, t + h, PANEL_TOP, PANEL_BOT);
		g.fill(l, t, l + w, t + h, alpha(VOID, 0.35F));

		starfield(g, l, t, w, h, time);

		g.fill(l, t, l + w, t + 3, BORDER);
		g.fill(l, t + h - 3, l + w, t + h, BORDER_DARK);
		g.fill(l, t, l + 3, t + h, BORDER_DARK);
		g.fill(l + w - 3, t, l + w, t + h, BORDER_DARK);
		g.fill(l + 3, t + 3, l + w - 3, t + 4, alpha(CYAN, 0.5F));
		g.fill(l + 3, t + h - 4, l + w - 3, t + h - 3, alpha(VIOLET, 0.5F));

		for (int i = 0; i < w - 16; i += 14) {
			int v = ((i / 14) + (int) (time * 0.05)) % 5;
			rune(g, l + 8 + i, t + 6, v, alpha(TEAL, 0.45F + 0.5F * pulse));
			rune(g, l + 8 + i, t + h - 14, (v + 2) % 5, alpha(VIOLET_BRIGHT, 0.4F + 0.5F * pulse2));
		}

		String title = "✦ Reverberation Anvil ✦";
		int glowW = this.font.width(title) + 16;
		g.fill(l + (w - glowW) / 2, t + 16, l + (w + glowW) / 2, t + 27, alpha(VIOLET, 0.18F + 0.2F * pulse));
		g.text(this.font, Component.literal(title), l + (w - this.font.width(title)) / 2, t + 18, TEXT_GOLD);

		int cy = t + SY + 8;
		int s0cx = l + S0X + 8, s1cx = l + S1X + 8, s2cx = l + S2X + 8;

		magicCircle(g, s0cx, cy, time, pulse, CYAN, CYAN_BRIGHT, 13);
		magicCircle(g, s1cx, cy, time * -1.0, pulse2, VIOLET, VIOLET_BRIGHT, 13);
		magicCircle(g, s2cx, cy, time, pulse, TEAL, CYAN_BRIGHT, 18);
		dotRing(g, s2cx, cy, 22.5, 24, -time * 0.03, alpha(VIOLET_BRIGHT, 0.35F + 0.4F * pulse), 1);

		plus(g, (s0cx + s1cx) / 2, cy, alpha(TEXT_GOLD, 0.7F + 0.3F * pulse));
		chevrons(g, s1cx + 12, s2cx - 12, cy, time);

		ItemStack wI = this.menu.slots.get(0).getItem();
		ItemStack seI = this.menu.slots.get(1).getItem();
		ItemStack ouI = this.menu.slots.get(2).getItem();
		boolean forging = ouI.isEmpty() && VestigiaSellos.wouldApply(wI, seI);
		boolean done = !ouI.isEmpty();
		forgeGlow += ((forging ? 1.0F : 0.0F) - forgeGlow) * 0.12F;
		if (done && !prevDone)
			flash = 16;
		prevDone = done;
		if (flash > 0)
			flash--;
		if (forgeGlow > 0.02F) {
			stream(g, s0cx, cy, s2cx, cy, time, CYAN_BRIGHT, forgeGlow);
			stream(g, s1cx, cy, s2cx, cy, time, VIOLET_BRIGHT, forgeGlow);
			g.fill(s2cx - 11, cy - 11, s2cx + 11, cy + 11, alpha(TEAL, 0.05F + 0.16F * forgeGlow));
			dotRing(g, s2cx, cy, 20.0, 16, time * 0.14, alpha(CYAN_BRIGHT, 0.3F * forgeGlow), 1);
		}
		if (flash > 0) {
			float f = flash / 16.0F;
			dotRing(g, s2cx, cy, 6.0 + 22.0 * (1.0F - f), 22, time * 0.1, alpha(0xFFF3D0, f), 1);
			g.fill(s2cx - 12, cy - 12, s2cx + 12, cy + 12, alpha(0xFFF3D0, 0.4F * f));
		}

		slot(g, l + S0X, t + SY, mouseX, mouseY, CYAN_BRIGHT, pulse);
		slot(g, l + S1X, t + SY, mouseX, mouseY, VIOLET_BRIGHT, pulse2);
		slot(g, l + S2X, t + SY, mouseX, mouseY, TEAL, pulse);

		miniLabel(g, l + S0X, t + SY, "Object");
		miniLabel(g, l + S1X, t + SY, "Seal");
		miniLabel(g, l + S2X, t + SY, "Outcome");

		inventoryArea(g, l, t);
	}

	private void magicCircle(GuiGraphicsExtractor g, int cx, int cy, double time, float pulse, int col, int colBright, double radius) {
		g.fill(cx - 9, cy - 9, cx + 9, cy + 9, alpha(col, 0.10F + 0.14F * pulse));
		dotRing(g, cx, cy, radius, 12, time * 0.05, alpha(col, 0.55F + 0.35F * pulse), 1);
		dotRing(g, cx, cy, radius - 3.5, 8, -time * 0.07, alpha(colBright, 0.5F + 0.4F * pulse), 1);
		for (int i = 0; i < 4; i++) {
			double a = time * 0.05 + i * (Math.PI / 2);
			int x = cx + (int) Math.round(Math.cos(a) * radius);
			int yy = cy + (int) Math.round(Math.sin(a) * radius);
			g.fill(x - 1, yy - 1, x + 2, yy + 2, alpha(colBright, 0.7F + 0.3F * pulse));
		}
	}

	private void dotRing(GuiGraphicsExtractor g, int cx, int cy, double radius, int count, double phase, int color, int size) {
		for (int i = 0; i < count; i++) {
			double a = phase + i * (Math.PI * 2 / count);
			int x = cx + (int) Math.round(Math.cos(a) * radius);
			int yy = cy + (int) Math.round(Math.sin(a) * radius);
			g.fill(x - size, yy - size, x + size + 1, yy + size + 1, color);
		}
	}

	private void stream(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1, double time, int color, float intensity) {
		int dots = 5;
		for (int i = 0; i < dots; i++) {
			double p = ((time * 0.04) + i / (double) dots) % 1.0;
			int x = (int) Math.round(x0 + (x1 - x0) * p);
			int y = (int) Math.round(y0 + (y1 - y0) * p);
			g.fill(x - 1, y - 1, x + 1, y + 1, alpha(color, intensity * (0.35F + 0.65F * (float) Math.sin(p * Math.PI))));
		}
	}

	private void plus(GuiGraphicsExtractor g, int cx, int cy, int color) {
		g.fill(cx - 4, cy - 1, cx + 5, cy + 2, color);
		g.fill(cx - 1, cy - 4, cx + 2, cy + 5, color);
	}

	private void chevrons(GuiGraphicsExtractor g, int x0, int x1, int cy, double time) {
		int span = x1 - x0;
		if (span < 6)
			return;
		for (int cxo = 0; cxo < span; cxo += 8) {
			float phase = (float) ((time * 0.10 - cxo * 0.20));
			float b = 0.35F + 0.5F * (0.5F + 0.5F * (float) Math.sin(phase));
			int cx = x0 + cxo;
			g.fill(cx, cy - 3, cx + 2, cy - 1, alpha(TEAL, b));
			g.fill(cx + 2, cy - 1, cx + 4, cy + 1, alpha(CYAN_BRIGHT, b));
			g.fill(cx, cy + 1, cx + 2, cy + 3, alpha(TEAL, b));
		}
	}

	private void slot(GuiGraphicsExtractor g, int sx, int sy, int mouseX, int mouseY, int accent, float pulse) {
		boolean hover = mouseX >= sx && mouseX < sx + 16 && mouseY >= sy && mouseY < sy + 16;
		g.fill(sx - 3, sy - 3, sx + 19, sy + 19, alpha(accent, hover ? 0.5F : 0.20F + 0.20F * pulse));
		g.fill(sx - 2, sy - 2, sx + 18, sy + 18, SLOT_FRAME);
		g.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_BG);
		g.fill(sx - 1, sy - 1, sx + 17, sy, alpha(accent, 0.6F));
		g.fill(sx - 1, sy + 16, sx + 17, sy + 17, alpha(accent, 0.25F));
	}

	private void miniLabel(GuiGraphicsExtractor g, int sx, int sy, String s) {
		int tw = this.font.width(s);
		g.text(this.font, Component.literal(s), sx + 8 - tw / 2, sy + 20, INK);
	}

	private void starfield(GuiGraphicsExtractor g, int l, int t, int w, int h, double time) {
		int seed = 1;
		for (int i = 0; i < 26; i++) {
			seed = seed * 1103515245 + 12345;
			int px = l + 6 + Math.abs(seed % (w - 12));
			seed = seed * 1103515245 + 12345;
			int py = t + 30 + Math.abs(seed % (h - 60));
			float tw = 0.2F + 0.5F * (0.5F + 0.5F * (float) Math.sin(time * 0.08 + i));
			g.fill(px, py, px + 1, py + 1, alpha(i % 2 == 0 ? CYAN_BRIGHT : VIOLET_BRIGHT, tw));
		}
	}

	private void rune(GuiGraphicsExtractor g, int x, int y, int variant, int color) {
		switch (variant % 5) {
			case 0 -> {
				g.fill(x + 2, y, x + 3, y + 8, color);
				g.fill(x, y + 1, x + 2, y + 2, color);
				g.fill(x + 3, y + 6, x + 5, y + 7, color);
			}
			case 1 -> {
				g.fill(x + 2, y, x + 3, y + 8, color);
				g.fill(x, y + 3, x + 5, y + 4, color);
			}
			case 2 -> {
				g.fill(x, y, x + 1, y + 8, color);
				g.fill(x + 4, y, x + 5, y + 8, color);
				g.fill(x + 1, y, x + 4, y + 1, color);
			}
			case 3 -> {
				g.fill(x, y, x + 2, y + 2, color);
				g.fill(x + 2, y + 3, x + 3, y + 5, color);
				g.fill(x + 3, y + 6, x + 5, y + 8, color);
			}
			default -> {
				g.fill(x, y, x + 1, y + 8, color);
				g.fill(x + 4, y, x + 5, y + 8, color);
				g.fill(x + 1, y + 3, x + 4, y + 5, color);
			}
		}
	}

	private void inventoryArea(GuiGraphicsExtractor g, int l, int t) {
		g.fill(l + 6, t + 82, l + 170, t + 160, alpha(VOID, 0.55F));
		g.fill(l + 6, t + 82, l + 170, t + 83, alpha(BORDER, 0.5F));
		for (int row = 0; row < 3; row++)
			for (int col = 0; col < 9; col++)
				cell(g, l + 8 + col * 18, t + 84 + row * 18);
		for (int col = 0; col < 9; col++)
			cell(g, l + 8 + col * 18, t + 142);
	}

	private void cell(GuiGraphicsExtractor g, int sx, int sy) {
		g.fill(sx - 1, sy - 1, sx + 17, sy + 17, SLOT_FRAME);
		g.fill(sx, sy, sx + 16, sy + 16, SLOT_BG);
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
