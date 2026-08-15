package net.mcreator.vestigia.client.gui;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.network.chat.Component;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.mcreator.vestigia.world.inventory.SieveGUIMenu;
import net.mcreator.vestigia.block.entity.SieveBlockEntity;
import net.mcreator.vestigia.init.VestigiaModScreens;

import com.mojang.blaze3d.platform.InputConstants;

public class SieveGUIScreen extends AbstractContainerScreen<SieveGUIMenu> implements VestigiaModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;

	private static final int WOOD_FRAME = 0xFF17100A;
	private static final int WOOD_TOP = 0xFF8A6A3F;
	private static final int WOOD_BASE = 0xFF7A5A34;
	private static final int WOOD_BASE_DARK = 0xFF5F4526;
	private static final int BEVEL_LIGHT = 0xFFA5814E;
	private static final int BEVEL_DARK = 0xFF3D2B18;
	private static final int PLANK_LINE = 0x22000000;
	private static final int PANEL_RECESS = 0x33000000;
	private static final int SLOT_OUTER = 0xFF2B1D0F;
	private static final int SLOT_INNER = 0xFF241A0E;
	private static final int SLOT_SHADOW = 0xFF17100A;
	private static final int SLOT_LIGHT = 0xFF4A3620;
	private static final int AMBER = 0xFFD9A441;
	private static final int ARROW_EMPTY = 0xFF6E5426;
	private static final int TEXT_DARK = 0xFF2B1D0F;
	private static final int TEXT_SOFT = 0xFF6B5233;

	public SieveGUIScreen(SieveGUIMenu container, Inventory inventory, Component text) {
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
		g.fill(l - 3, t - 3, l + w + 3, t + h + 3, WOOD_FRAME);
		g.fillGradient(l, t, l + w, t + h, WOOD_BASE, WOOD_BASE_DARK);
		g.fill(l, t, l + w, t + 2, WOOD_TOP);
		g.fill(l, t, l + 2, t + h, BEVEL_LIGHT);
		g.fill(l, t + h - 2, l + w, t + h, BEVEL_DARK);
		g.fill(l + w - 2, t, l + w, t + h, BEVEL_DARK);
		for (int i = 1; i < 6; i++)
			g.fill(l + 3, t + 4 + i * 12, l + w - 3, t + 5 + i * 12, PLANK_LINE);
		g.fill(l + 6, t + 78, l + w - 6, t + h - 6, PANEL_RECESS);
		g.fill(l + 6, t + 4, l + w - 6, t + 17, PANEL_RECESS);
		drawSlot(g, l + 24, t + 7);
		drawSlot(g, l + 24, t + 34);
		drawSlot(g, l + 24, t + 61);
		drawSlot(g, l + 123, t + 7);
		drawSlot(g, l + 123, t + 34);
		drawSlot(g, l + 123, t + 61);
		float fill = this.menu.getProgress() / (float) SieveBlockEntity.PROGRESS_MAX;
		drawArrow(g, l + 50, t + 42, fill);
		for (int r = 0; r < 3; r++)
			for (int c = 0; c < 9; c++)
				drawSlot(g, l + 7 + c * 18, t + 83 + r * 18);
		for (int c = 0; c < 9; c++)
			drawSlot(g, l + 7 + c * 18, t + 141);
	}

	private static void drawSlot(GuiGraphicsExtractor g, int x, int y) {
		g.fill(x, y, x + 18, y + 18, SLOT_OUTER);
		g.fill(x + 1, y + 1, x + 17, y + 17, SLOT_INNER);
		g.fill(x + 1, y + 1, x + 17, y + 2, SLOT_SHADOW);
		g.fill(x + 1, y + 1, x + 2, y + 17, SLOT_SHADOW);
		g.fill(x + 16, y + 2, x + 17, y + 17, SLOT_LIGHT);
		g.fill(x + 2, y + 16, x + 17, y + 17, SLOT_LIGHT);
	}

	private static void drawArrow(GuiGraphicsExtractor g, int x, int cy, float fill) {
		drawArrowShape(g, x, cy, ARROW_EMPTY);
		int w = Math.round(68 * Math.max(0f, Math.min(1f, fill)));
		if (w > 0) {
			g.enableScissor(x, cy - 12, x + w, cy + 13);
			drawArrowShape(g, x, cy, AMBER);
			g.disableScissor();
		}
	}

	private static void drawArrowShape(GuiGraphicsExtractor g, int x, int cy, int color) {
		int shaftEnd = x + 58;
		g.fill(x, cy - 2, shaftEnd, cy + 3, color);
		for (int i = 0; i < 10; i++) {
			int reach = 10 - i;
			g.fill(shaftEnd + i, cy - reach, shaftEnd + i + 1, cy + reach + 1, color);
		}
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
	protected void extractLabels(GuiGraphicsExtractor g, int mouseX, int mouseY) {
		g.centeredText(this.font, Component.translatable("gui.vestigia.sieve_gui.label_sieve"), this.imageWidth / 2, 6, TEXT_DARK);
	}

	@Override
	public void init() {
		super.init();
	}
}
