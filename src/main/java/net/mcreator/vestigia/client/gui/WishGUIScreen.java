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

import net.mcreator.vestigia.world.inventory.WishGUIMenu;
import net.mcreator.vestigia.init.VestigiaModScreens;
import net.mcreator.vestigia.VestigiaOffering;

import com.mojang.blaze3d.platform.InputConstants;

public class WishGUIScreen extends AbstractContainerScreen<WishGUIMenu> implements VestigiaModScreens.ScreenAccessor {
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
	private static final int BTN_TOP = 0xFF25542F;
	private static final int BTN_BOT = 0xFF163A1F;
	private static final int BTN_HOVER_TOP = 0xFF3A7A45;
	private static final int BTN_HOVER_BOT = 0xFF245030;
	private static final int TEXT = 0xFFE7C266;
	private static final int WHITE = 0xFFFFF3D0;

	private static final int BTN_W = 156, BTN_H = 22, BTN_GAP = 26, BTN_X0 = 10, BTN_Y0 = 30;
	private static final String[] LABELS = {"Que llueva", "Quitar la lluvia", "Creativo (10 s)", "Volar (30 min)", "Inmortal (5 min)"};

	public WishGUIScreen(WishGUIMenu container, Inventory inventory, Component text) {
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
		float pulse = 0.5F + 0.5F * (float) Math.sin((gt + partialTicks) * 0.15);
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
		int glow = (int) (60 + 90 * pulse);
		g.fill(l + 30, t + 15, l + w - 30, t + 26, (glow << 24) | 0x00D9A441);
		String title = "☀ Elige tu deseo ☀";
		g.text(this.font, Component.literal(title), l + (w - this.font.width(title)) / 2, t + 17, GOLD_BRIGHT);
		for (int i = 0; i < 5; i++) {
			int bx = l + BTN_X0, by = t + BTN_Y0 + i * BTN_GAP;
			boolean hover = mouseX >= bx && mouseX <= bx + BTN_W && mouseY >= by && mouseY <= by + BTN_H;
			drawButton(g, bx, by, LABELS[i], hover, pulse);
		}
	}

	private void drawButton(GuiGraphicsExtractor g, int bx, int by, String label, boolean hover, float pulse) {
		g.fill(bx - 1, by - 1, bx + BTN_W + 1, by + BTN_H + 1, hover ? GOLD_BRIGHT : GOLD_DARK);
		g.fillGradient(bx, by, bx + BTN_W, by + BTN_H, hover ? BTN_HOVER_TOP : BTN_TOP, hover ? BTN_HOVER_BOT : BTN_BOT);
		g.fill(bx, by, bx + BTN_W, by + 1, hover ? GOLD_BRIGHT : JADE);
		g.fill(bx, by + BTN_H - 1, bx + BTN_W, by + BTN_H, JUNGLE_DARK);
		int gc = hover ? GOLD_BRIGHT : GOLD;
		g.fill(bx + 4, by + 4, bx + 8, by + BTN_H - 4, gc);
		g.fill(bx + 6, by + BTN_H / 2 - 1, bx + 10, by + BTN_H / 2 + 1, gc);
		g.fill(bx + BTN_W - 8, by + 4, bx + BTN_W - 4, by + BTN_H - 4, gc);
		g.fill(bx + BTN_W - 10, by + BTN_H / 2 - 1, bx + BTN_W - 6, by + BTN_H / 2 + 1, gc);
		int tw = this.font.width(label);
		g.text(this.font, Component.literal(label), bx + (BTN_W - tw) / 2, by + (BTN_H - 8) / 2, hover ? WHITE : TEXT);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0) {
			int bx = this.leftPos + BTN_X0;
			for (int i = 0; i < 5; i++) {
				int by = this.topPos + BTN_Y0 + i * BTN_GAP;
				if (event.x() >= bx && event.x() <= bx + BTN_W && event.y() >= by && event.y() <= by + BTN_H) {
					ClientPacketDistributor.sendToServer(new VestigiaOffering.WishChoicePayload(i));
					if (this.minecraft != null && this.minecraft.player != null)
						this.minecraft.player.closeContainer();
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
}
