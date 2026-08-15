package net.mcreator.vestigia.client.gui;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.mcreator.vestigia.world.inventory.RestorerGUIMenu;
import net.mcreator.vestigia.block.entity.RestorerBlockEntity;
import net.mcreator.vestigia.init.VestigiaModScreens;

import com.mojang.blaze3d.platform.InputConstants;

public class RestorerGUIScreen extends AbstractContainerScreen<RestorerGUIMenu> implements VestigiaModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;

	private static final int STONE_FRAME = 0xFF16140E;
	private static final int STONE = 0xFF423D33;
	private static final int STONE_DARK = 0xFF272419;
	private static final int BEVEL_LIGHT = 0xFF615A47;
	private static final int BEVEL_DARK = 0xFF201D15;
	private static final int PANEL_RECESS = 0x33000000;
	private static final int SLOT_OUTER = 0xFF17150F;
	private static final int SLOT_INNER = 0xFF211E15;
	private static final int SLOT_SHADOW = 0xFF100E09;
	private static final int SLOT_LIGHT = 0xFF44402E;
	private static final int GOLD = 0xFFD9A441;
	private static final int GOLD_BRIGHT = 0xFFF3D688;
	private static final int GOLD_DARK = 0xFF8A5A1F;
	private static final int GLOW_1 = 0x22D9A441;
	private static final int GLOW_2 = 0x38D9A441;
	private static final int TEXT_GOLD = 0xFFE7C266;

	private static final ItemStack CHISEL_HINT = new ItemStack(BuiltInRegistries.ITEM.getValue(Identifier.parse("vestigia:chisel")));

	public RestorerGUIScreen(RestorerGUIMenu container, Inventory inventory, Component text) {
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
		g.fill(l - 3, t - 3, l + w + 3, t + h + 3, STONE_FRAME);
		g.fillGradient(l, t, l + w, t + h, STONE, STONE_DARK);
		g.fill(l, t, l + w, t + 2, BEVEL_LIGHT);
		g.fill(l, t, l + 2, t + h, BEVEL_LIGHT);
		g.fill(l, t + h - 2, l + w, t + h, BEVEL_DARK);
		g.fill(l + w - 2, t, l + w, t + h, BEVEL_DARK);
		g.fill(l + 7, t + 17, l + w - 7, t + 18, GOLD_DARK);
		g.fill(l + 7, t + 18, l + w - 7, t + 19, GOLD);
		g.fill(l + 6, t + 78, l + w - 6, t + h - 6, PANEL_RECESS);
		float fill = this.menu.getProgress() / (float) RestorerBlockEntity.PROGRESS_MAX;
		boolean done = this.menu.slots.get(3).hasItem();
		long gt = this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getGameTime() : 0L;
		float glow = done ? 0.55F + 0.45F * (float) Math.sin(gt * 0.25) : 0F;
		connector(g, l + 34, t + 25, l + 52, t + 40);
		connector(g, l + 34, t + 61, l + 52, t + 46);
		arrow(g, l + 74, t + 43, fill);
		drawSlot(g, l + 15, t + 16);
		drawSlot(g, l + 51, t + 34);
		drawSlot(g, l + 15, t + 52);
		outputFrame(g, l + 132, t + 43, glow);
		for (int r = 0; r < 3; r++)
			for (int c = 0; c < 9; c++)
				drawSlot(g, l + 7 + c * 18, t + 83 + r * 18);
		for (int c = 0; c < 9; c++)
			drawSlot(g, l + 7 + c * 18, t + 141);
		if (!this.menu.slots.get(2).hasItem() && !CHISEL_HINT.isEmpty()) {
			g.item(CHISEL_HINT, l + 52, t + 35);
			g.fill(l + 52, t + 35, l + 68, t + 51, 0x66161207);
		}
	}

	private static void drawSlot(GuiGraphicsExtractor g, int x, int y) {
		g.fill(x, y, x + 18, y + 18, SLOT_OUTER);
		g.fill(x + 1, y + 1, x + 17, y + 17, SLOT_INNER);
		g.fill(x + 1, y + 1, x + 17, y + 2, SLOT_SHADOW);
		g.fill(x + 1, y + 1, x + 2, y + 17, SLOT_SHADOW);
		g.fill(x + 16, y + 2, x + 17, y + 17, SLOT_LIGHT);
		g.fill(x + 2, y + 16, x + 17, y + 17, SLOT_LIGHT);
	}

	private static void outputFrame(GuiGraphicsExtractor g, int cx, int cy, float glow) {
		if (glow > 0F) {
			int a1 = (int) (40 + 95 * glow);
			g.fill(cx - 25, cy - 25, cx + 25, cy + 25, (a1 << 24) | 0x00D9A441);
			int a2 = (int) (30 + 80 * glow);
			g.fill(cx - 21, cy - 21, cx + 21, cy + 21, (a2 << 24) | 0x00F3D688);
		}
		g.fill(cx - 20, cy - 20, cx + 20, cy + 20, GLOW_1);
		g.fill(cx - 17, cy - 17, cx + 17, cy + 17, GLOW_2);
		g.fill(cx - 16, cy - 16, cx + 16, cy + 16, GOLD_DARK);
		g.fill(cx - 15, cy - 15, cx + 15, cy + 15, glow > 0.5F ? GOLD_BRIGHT : GOLD);
		g.fill(cx - 14, cy - 14, cx + 14, cy + 14, GOLD_DARK);
		g.fill(cx - 3, cy - 16, cx + 3, cy - 13, GOLD_BRIGHT);
		g.fill(cx - 3, cy + 13, cx + 3, cy + 16, GOLD_BRIGHT);
		g.fill(cx - 16, cy - 3, cx - 13, cy + 3, GOLD_BRIGHT);
		g.fill(cx + 13, cy - 3, cx + 16, cy + 3, GOLD_BRIGHT);
		g.fill(cx - 9, cy - 9, cx + 9, cy + 9, SLOT_OUTER);
		g.fill(cx - 8, cy - 8, cx + 8, cy + 8, SLOT_INNER);
		g.fill(cx - 8, cy - 8, cx + 8, cy - 7, SLOT_SHADOW);
		g.fill(cx - 8, cy - 8, cx - 7, cy + 8, SLOT_SHADOW);
	}

	private static void connector(GuiGraphicsExtractor g, int x0, int y0, int x1, int y1) {
		int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
		for (int i = 0; i <= steps; i += 2) {
			int px = x0 + (x1 - x0) * i / steps;
			int py = y0 + (y1 - y0) * i / steps;
			g.fill(px, py, px + 2, py + 2, GOLD_DARK);
		}
	}

	private static void arrow(GuiGraphicsExtractor g, int x, int cy, float fill) {
		arrowShape(g, x, cy, GOLD_DARK);
		int w = Math.round(40 * Math.max(0F, Math.min(1F, fill)));
		if (w > 0) {
			g.enableScissor(x, cy - 12, x + w, cy + 13);
			arrowShape(g, x, cy, GOLD);
			g.disableScissor();
		}
	}

	private static void arrowShape(GuiGraphicsExtractor g, int x, int cy, int color) {
		int shaftEnd = x + 30;
		g.fill(x, cy - 2, shaftEnd, cy + 3, color);
		for (int i = 0; i < 9; i++) {
			int reach = 9 - i;
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
		g.centeredText(this.font, Component.literal("Restorer"), this.imageWidth / 2, 6, TEXT_GOLD);
	}

	@Override
	public void init() {
		super.init();
	}
}
