package net.mcreator.vestigia.client.gui;

import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.resources.Identifier;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import net.mcreator.vestigia.world.inventory.VestigiaBookGUIMenu;
import net.mcreator.vestigia.init.VestigiaModScreens;

import com.mojang.blaze3d.platform.InputConstants;

public class VestigiaBookGUIScreen extends AbstractContainerScreen<VestigiaBookGUIMenu> implements VestigiaModScreens.ScreenAccessor {
	private final Level world;
	private final int x, y, z;
	private final Player entity;
	private boolean menuStateUpdateActive = false;

	private static final int LEATHER = 0xFF3A2413;
	private static final int LEATHER_LT = 0xFF5A3A1E;
	private static final int PARCH_TOP = 0xFFEAD9AE;
	private static final int PARCH_BOT = 0xFFD6C089;
	private static final int SPINE = 0xFF2A1A0D;
	private static final int INK = 0xFF3A2A14;
	private static final int INK_SOFT = 0xFF6B5533;
	private static final int GOLD = 0xFFB5832E;
	private static final int GOLD_BRIGHT = 0xFFE7C266;
	private static final int TURQ = 0xFF2E9C9C;
	private static final int JADE = 0xFF3E8C5A;

	private static final int BW = 288, BH = 198;
	private static final int MAX_SPREAD = 22;
	private static final float ANIM = 9.0F;
	private static final int IDX_W = 120, IDX_H = 15, IDX_GAP = 19;
	private static final int[] CHAPTERS = {1, 6, 8, 11, 15, 18, 21};

	private static final String[][] RECIPES = {
			{"vestigia:broken_metal_hoe", "vestigia:metal_splinter", "vestigia:metal_hoe"}, {"vestigia:broken_ash_pickaxe", "vestigia:ash_splinter", "vestigia:ash_pickaxe"},
			{"vestigia:broken_ash_sword", "vestigia:ash_splinter", "vestigia:ash_sword"}, {"minecraft:flint", "vestigia:flint_splinter", "vestigia:cracked_flint"},
			{"vestigia:repaired_flint", "", "vestigia:worked_flint"}, {"minecraft:diamond_axe", "vestigia:worked_flint", "vestigia:flint_axe"},
			{"vestigia:iron_shard", "", "vestigia:iron_dagger"}, {"vestigia:coal_hoe_broken", "vestigia:coal_splinter", "vestigia:coal_hoe"},
			{"vestigia:aurora_knife_broken", "vestigia:aurora_splinter", "vestigia:aurora_knife"}, {"minecraft:totem_of_undying", "vestigia:mythic_splinter", "vestigia:capibara_of_wisdom"},
			{"vestigia:uncharged_golden_staff", "vestigia:mythic_splinter", "vestigia:golden_staff"}, {"vestigia:hook_broken", "vestigia:oxidized_bronze_splinter", "vestigia:hook"},
			{"vestigia:percussion_hammer_broken", "vestigia:oxidized_bronze_splinter", "vestigia:percussion_hammer"}, {"vestigia:stone_mason_pick_broken", "vestigia:oxidized_bronze_splinter", "vestigia:stone_mason_pickaxe"},
			{"minecraft:golden_sword", "vestigia:gold_splinter", "vestigia:golden_sword"}, {"vestigia:uncharged_ruby_blade_broken", "vestigia:ruby", "vestigia:uncharged_ruby_blade"},
			{"vestigia:uncharged_ruby_blade", "vestigia:ruby_splinter", "vestigia:ruby_blade"}, {"minecraft:ender_eye", "vestigia:ender_splinter", "vestigia:echo"},
			{"vestigia:empty_water_sword", "vestigia:water_splinter", "vestigia:water_element_sword"}, {"vestigia:refugebellbroken", "vestigia:oxidized_bronze_splinter", "vestigia:refuge_bell"}};

	private int cur = 0;
	private boolean animating = false;
	private int fromSpread = 0, toSpread = 0, animDir = 1;
	private float animStart = 0.0F;
	private boolean opened = false;

	private static ItemStack st(String id) {
		if (id == null || id.isEmpty())
			return ItemStack.EMPTY;
		var it = BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
		return it == null ? ItemStack.EMPTY : new ItemStack(it);
	}

	public VestigiaBookGUIScreen(VestigiaBookGUIMenu container, Inventory inventory, Component text) {
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

	private void sound(float pitch) {
		if (this.minecraft != null)
			this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.BOOK_PAGE_TURN, pitch));
	}

	private void txt(GuiGraphicsExtractor g, String s, int x, int y, int color) {
		g.text(this.font, Component.literal(s), x, y, color, false);
	}

	private int bx() {
		return (this.width - BW) / 2;
	}

	private int by() {
		return (this.height - BH) / 2;
	}

	private float now(float pt) {
		return (this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getGameTime() : 0L) + pt;
	}

	@Override
	public void extractBackground(GuiGraphicsExtractor g, int mouseX, int mouseY, float partialTicks) {
		super.extractBackground(g, mouseX, mouseY, partialTicks);
		int bx = bx(), by = by();
		float now = now(partialTicks);
		drawBook(g, bx, by);
		if (animating) {
			float p = (now - animStart) / ANIM;
			if (p >= 1.0F) {
				cur = toSpread;
				animating = false;
				drawSpread(g, cur, bx, by, mouseX, mouseY);
			} else {
				drawSpread(g, toSpread, bx, by, mouseX, mouseY);
				drawFlip(g, bx, by, p, animDir);
			}
		} else {
			drawSpread(g, cur, bx, by, mouseX, mouseY);
		}
		drawChrome(g, bx, by, mouseX, mouseY);
	}

	private void drawBook(GuiGraphicsExtractor g, int bx, int by) {
		g.fill(bx - 6, by - 6, bx + BW + 6, by + BH + 6, LEATHER);
		g.fill(bx - 5, by - 5, bx + BW + 5, by - 3, LEATHER_LT);
		g.fillGradient(bx, by, bx + BW, by + BH, PARCH_TOP, PARCH_BOT);
		int spine = bx + BW / 2;
		g.fillGradient(spine - 8, by, spine, by + BH, 0x00000000, 0x40000000);
		g.fillGradient(spine, by, spine + 8, by + BH, 0x40000000, 0x00000000);
		g.fill(spine - 1, by, spine + 1, by + BH, SPINE);
		g.fill(bx, by, bx + BW, by + 2, GOLD);
		g.fill(bx, by + BH - 2, bx + BW, by + BH, 0xFF7A5220);
		g.fill(bx, by, bx + 2, by + BH, 0xFF7A5220);
		g.fill(bx + BW - 2, by, bx + BW, by + BH, 0xFF7A5220);
		for (int i = 0; i < BW - 20; i += 12) {
			g.fill(bx + 10 + i, by + 5, bx + 10 + i + 6, by + 7, TURQ);
			g.fill(bx + 10 + i + 3, by + 7, bx + 10 + i + 9, by + 8, JADE);
		}
	}

	private void drawSpread(GuiGraphicsExtractor g, int spread, int bx, int by, int mouseX, int mouseY) {
		int lx = bx + 20, rx = bx + BW / 2 + 14;
		switch (spread) {
			case 0 -> {
				page(g, lx, by, "Vestigia", new String[]{"Long after the first", "Incursion tore the", "sky, the world will", "not rest. The ages", "bleed together — lost", "civilizations rise", "into the present,", "stone by stone.", "Unchecked, the", "collapse devours all.", "", "Uncover who woke the", "Incursions and how", "to end them. Explore,", "fuse and restore", "relics — seize their", "power to command the", "Incursions."});
				page(g, rx, by, "Contents", new String[0]);
				drawIndex(g, rx, by, mouseX, mouseY);
			}
			case 1 -> {
				page(g, lx, by, "1 · Finding Relics", new String[]{"Relics sleep beneath", "the earth. Find them", "two ways:", "", "• Seek Archaeologist", "  Houses across the", "  world.", "", "• Or dig veins of", "  Ash, Mud and", "  Bronze Deposits:"});
				icons3(g, lx + 6, by + BH - 40, st("vestigia:ash_deposit"), st("vestigia:mud_deposit"), st("vestigia:bronze_deposit"));
				page(g, rx, by, "Brush the Ages", new String[]{"Sweep a deposit with", "the fine brush to", "gather Sediment.", "Craft the brush —", "3 String, a Brush:"});
				craftGrid(g, rx, by + 74, new ItemStack[]{st("minecraft:string"), st("minecraft:string"), st("minecraft:string"), ItemStack.EMPTY, st("minecraft:brush"), ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY}, st("vestigia:coldhorsehairbrush"));
			}
			case 2 -> {
				page(g, lx, by, "The Sieve", new String[]{"Pour x3 Sediments", "into the Sieve and", "shake it through.", "", "It yields Splinters:", "shards of lost", "relics."});
				iconArrow(g, lx + 6, by + BH - 40, st("vestigia:sieve"), st("vestigia:ash_splinter"), st("vestigia:gold_splinter"), st("vestigia:iron_shard"));
				page(g, rx, by, "The Restorer", new String[]{"Relic + material +", "Chisel restores it.", "Craft a Chisel:"});
				craftGrid(g, rx, by + 62, new ItemStack[]{ItemStack.EMPTY, st("vestigia:bronze"), ItemStack.EMPTY, ItemStack.EMPTY, st("minecraft:stick"), ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY, ItemStack.EMPTY}, st("vestigia:chisel"));
				txt(g, "Bronze — smelt an", rx, by + 122, INK);
				txt(g, "Oxidized Splinter:", rx, by + 130, INK);
				smelt(g, rx, by + 142, st("vestigia:oxidized_bronze_splinter"), st("vestigia:bronze"));
			}
			case 3 -> {
				txt(g, "Restore · Ruby", lx, by + 16, GOLD);
				g.fill(lx, by + 26, lx + 108, by + 27, alpha(GOLD, 0.6F));
				txt(g, "At the Restorer set", lx, by + 32, INK);
				txt(g, "the broken blade, a", lx, by + 40, INK);
				txt(g, "Ruby and the Chisel:", lx, by + 48, INK);
				restorerRow(g, lx, by + 64, st("vestigia:uncharged_ruby_blade_broken"), st("vestigia:ruby"), st("vestigia:chisel"), st("vestigia:uncharged_ruby_blade"));
				txt(g, "→ Uncharged Blade", lx, by + 92, INK_SOFT);
				txt(g, "Slots: item · material", lx, by + 112, INK_SOFT);
				txt(g, "· chisel → result", lx, by + 120, INK_SOFT);
				txt(g, "Then Recharge", rx, by + 16, GOLD);
				g.fill(rx, by + 26, rx + 108, by + 27, alpha(GOLD, 0.6F));
				txt(g, "Feed the blade a", rx, by + 32, INK);
				txt(g, "Ruby Splinter (with", rx, by + 40, INK);
				txt(g, "the Chisel) to wake", rx, by + 48, INK);
				txt(g, "its power:", rx, by + 56, INK);
				restorerRow(g, rx, by + 72, st("vestigia:uncharged_ruby_blade"), st("vestigia:ruby_splinter"), st("vestigia:chisel"), st("vestigia:ruby_blade"));
				txt(g, "→ The Ruby Blade", rx, by + 100, INK_SOFT);
			}
			case 4 -> {
				listPage(g, lx, by, "Restoration List 1", 0, 5);
				listPage(g, rx, by, "Restoration List 2", 5, 10);
			}
			case 5 -> {
				listPage(g, lx, by, "Restoration List 3", 10, 15);
				listPage(g, rx, by, "Restoration List 4", 15, 20);
			}
			case 6 -> {
				txt(g, "Craft · Slingshot", lx, by + 16, GOLD);
				g.fill(lx, by + 26, lx + 108, by + 27, alpha(GOLD, 0.6F));
				txt(g, "Planks, Lead, Rubies", lx, by + 32, INK);
				txt(g, "and a Mythic Splinter:", lx, by + 40, INK);
				craftGrid(g, lx, by + 54, new ItemStack[]{st("minecraft:oak_planks"), st("minecraft:lead"), st("minecraft:oak_planks"), st("vestigia:ruby"), st("vestigia:mythic_splinter"), st("vestigia:ruby"), ItemStack.EMPTY, st("minecraft:oak_planks"), ItemStack.EMPTY}, st("vestigia:slingshot"));
				txt(g, "Craft · Ruby", rx, by + 16, GOLD);
				g.fill(rx, by + 26, rx + 108, by + 27, alpha(GOLD, 0.6F));
				txt(g, "Cut a Ruby Splinter", rx, by + 32, INK);
				txt(g, "on the Stonecutter", rx, by + 40, INK);
				txt(g, "to shape a Ruby:", rx, by + 48, INK);
				single(g, rx, by + 66, st("vestigia:ruby_splinter"), st("vestigia:ruby"), "Stonecutter");
				txt(g, "Rubies charge and", rx, by + 100, INK_SOFT);
				txt(g, "restore relics.", rx, by + 108, INK_SOFT);
			}
			case 7 -> {
				txt(g, "Craft · Boomerang", lx, by + 16, GOLD);
				g.fill(lx, by + 26, lx + 108, by + 27, alpha(GOLD, 0.6F));
				txt(g, "A Lead and Planks", lx, by + 32, INK);
				txt(g, "in an L-shape:", lx, by + 40, INK);
				craftGrid(g, lx, by + 54, new ItemStack[]{st("minecraft:lead"), st("minecraft:oak_planks"), st("minecraft:oak_planks"), st("minecraft:oak_planks"), st("minecraft:oak_planks"), ItemStack.EMPTY, st("minecraft:oak_planks"), ItemStack.EMPTY, ItemStack.EMPTY}, st("vestigia:boomerang"));
				txt(g, "Repaired Flint", rx, by + 16, GOLD);
				g.fill(rx, by + 26, rx + 108, by + 27, alpha(GOLD, 0.6F));
				txt(g, "Smelt a Cracked", rx, by + 32, INK);
				txt(g, "Flint in a furnace:", rx, by + 40, INK);
				smelt(g, rx, by + 58, st("vestigia:cracked_flint"), st("vestigia:repaired_flint"));
				txt(g, "Restore it further", rx, by + 92, INK_SOFT);
				txt(g, "into Worked Flint at", rx, by + 100, INK_SOFT);
				txt(g, "the Restorer.", rx, by + 108, INK_SOFT);
			}
			case 8 -> {
				page(g, lx, by, "3 · Bearer Relics", new String[]{"Ancient weapons keep", "a warrior's echo.", "Charge them, then", "unleash the bearer:", "", "• Golden Sword &", "  Slingshot: HOLD", "  right-click.", "", "• Spear & Echo:", "  charge by HITTING", "  enemies."});
				bearer(g, rx, by, "Slingshot", "vestigia:slingshot", new String[]{"Fires a shot and", "summons The Zagal.", "", "It lures nearby", "hostile mobs to", "itself and drags", "them far away from", "you."});
			}
			case 9 -> {
				bearer(g, lx, by, "Golden Sword", "vestigia:golden_sword", new String[]{"Hold right-click to", "charge. On a full", "charge it gains a", "damage boost for a", "few strikes.", "", "Right-click the", "ground to summon the", "Golden Knight bearer", "to fight with you."});
				bearer(g, rx, by, "Spear", "vestigia:phantom_spear", new String[]{"Charge it by", "striking foes.", "", "Summons a Gladiator", "that hurls spears", "at hostile mobs from", "a distance."});
			}
			case 10 -> {
				bearer(g, lx, by, "Echo", "vestigia:echo", new String[]{"Charge it by", "striking foes.", "", "Summons a Portador", "that attacks nearby", "hostiles for 15", "seconds, then fades."});
				page(g, rx, by, "The Bearers Answer", new String[]{"Wield their echoes", "to hold back the", "Incursions.", "", "But the raids hold", "greater secrets —", "and greater power.", "", "Turn the page…"});
			}
			case 11 -> {
				page(g, lx, by, "4 · Incursions", new String[]{"Open temporary", "bubbles and begin", "an Echo Incursion", "to advance.", "", "Feed an Altar a", "spent 'Off' Seal.", "Survive the raid", "and it returns", "charged, with an", "Emblem too.", "", "Beware: if you die", "or leave the ring,", "the bubble bursts", "and the Seal is", "lost. Each win", "mends broken time."});
				page(g, rx, by, "The Altars", new String[]{"Two altars await:", "", "Level 1 uses spent", "Seals 1, 2 and 3.", "", "Level 2 uses spent", "Seal Z and the", "Black Hole seal.", "", "Right-click with a", "Seal to open the", "bubble (radius 20).", "Stay inside!"});
				iconsN(g, rx + 6, by + BH - 32, new ItemStack[]{st("vestigia:altar_de_invocacion_nivel_1"), st("vestigia:altar_de_invocacion_nivel_2")});
			}
			case 12 -> {
				page(g, lx, by, "The Bubble", new String[]{"When it opens:", "", "• Rainbow sky.", "• Screen shake.", "• Barrier ring of", "  radius 20.", "• Incursion music.", "", "You get 3:21 — win", "before time ends.", "The TimerIncursion", "overlay counts down.", "Leave the ring and", "the raid collapses."});
				page(g, rx, by, "The Golden Knight", new String[]{"At 2:00 remaining", "the boss arrives:", "the Golden Knight.", "", "He wields a boss", "health bar, far", "more life, and new", "attacks — sonic", "slams, meteors and", "fury shockwaves.", "", "Fell him and the", "Incursion ends;", "the sky calms."});
			}
			case 13 -> {
				page(g, lx, by, "Seal Waves I", new String[]{"Each Seal calls a", "different horde:", "", "Seal 1: Portadores", "only. Weak boss.", "", "Seal 2: + Gladi-", "ators. Tougher.", "", "Seal 3: + Zagales,", "fast and fierce.", "Very tough boss."});
				iconsN(g, lx + 6, by + BH - 32, new ItemStack[]{st("vestigia:seal_1"), st("vestigia:seal_2"), st("vestigia:seal_3")});
				page(g, rx, by, "Seal Waves II", new String[]{"Seal Z: adds", "vanilla Zombies,", "Skeletons, Creepers", "and Phantoms.", "Huge-HP boss.", "", "Black Hole: all of", "Seal Z, plus TNT", "raining inside the", "ring. Colossal", "boss."});
				iconsN(g, rx + 6, by + BH - 32, new ItemStack[]{st("vestigia:seal_z"), st("vestigia:stellar_seal")});
			}
			case 14 -> {
				page(g, lx, by, "The Spoils", new String[]{"Win and you keep", "the charged Seal", "AND an Emblem:", "", "Seal 1 → Twisted", "Seal 2 → Fire", "Seal 3 → Aurora", "Seal Z → Golden", "Black Hole → Seal"});
				iconsN(g, lx + 4, by + BH - 32, new ItemStack[]{st("vestigia:twisted_emblem"), st("vestigia:fire_emblem"), st("vestigia:aurora_emblem"), st("vestigia:golden_emblem"), st("vestigia:seal_emblem")});
				page(g, rx, by, "Seals on Weapons", new String[]{"Seals empower the", "Bearer summon:", "", "I: lasts 25s", "   (was 15).", "II: charge fills", "    40% faster.", "III: two Portadores", "at once, different", "arms — the final", "power fantasy:", "Captain + Watcher."});
				iconsN(g, rx + 6, by + BH - 32, new ItemStack[]{st("vestigia:seal_1"), st("vestigia:seal_2"), st("vestigia:seal_3")});
			}
			case 15 -> {
				page(g, lx, by, "5 · Reverberation", new String[]{"The Reverberation", "Anvil binds a Seal's", "power into a Bearer", "weapon.", "", "Set the weapon and a", "Seal; the anvil", "forges them over a", "few moments.", "", "Take the sealed", "weapon from the", "output slot."});
				page(g, rx, by, "The Slots", new String[]{"1: a Bearer weapon", "(Slingshot, Sword,", "Spear or Echo).", "", "2: a charged Seal", "(I, II or III).", "", "3: the result —", "your sealed weapon."});
				iconsN(g, rx + 4, by + BH - 32, new ItemStack[]{st("vestigia:slingshot"), st("vestigia:golden_sword"), st("vestigia:phantom_spear"), st("vestigia:echo")});
			}
			case 16 -> {
				txt(g, "Upgrade Seals", lx, by + 16, GOLD);
				g.fill(lx, by + 26, lx + 108, by + 27, alpha(GOLD, 0.6F));
				txt(g, "Combine two Seals in", lx, by + 32, INK);
				txt(g, "the anvil to forge a", lx, by + 40, INK);
				txt(g, "stronger 'Off' Seal:", lx, by + 48, INK);
				anvilRow(g, lx, by + 66, st("vestigia:seal_1"), st("vestigia:seal_1"), st("vestigia:seal_2_off"), "= Seal 2 Off");
				anvilRow(g, lx, by + 100, st("vestigia:seal_1"), st("vestigia:seal_2"), st("vestigia:seal_3_off"), "= Seal 3 Off");
				txt(g, "Greater Fusions", rx, by + 16, GOLD);
				g.fill(rx, by + 26, rx + 108, by + 27, alpha(GOLD, 0.6F));
				txt(g, "The mightiest Seals:", rx, by + 32, INK);
				anvilRow(g, rx, by + 50, st("vestigia:seal_3"), st("vestigia:seal_3"), st("vestigia:seal_z_off"), "= Seal Z Off");
				anvilRow(g, rx, by + 84, st("vestigia:seal_z"), st("vestigia:seal_z"), st("vestigia:stellar_seal_off"), "= Stellar Off");
				txt(g, "'Off' Seals arm the", rx, by + 120, INK_SOFT);
				txt(g, "altars for a new", rx, by + 128, INK_SOFT);
				txt(g, "Incursion.", rx, by + 136, INK_SOFT);
			}
			case 17 -> {
				txt(g, "Sealing · Example", lx, by + 16, GOLD);
				g.fill(lx, by + 26, lx + 108, by + 27, alpha(GOLD, 0.6F));
				txt(g, "Feed the anvil a", lx, by + 32, INK);
				txt(g, "weapon and a Seal:", lx, by + 40, INK);
				anvilRow(g, lx, by + 56, st("vestigia:golden_sword"), st("vestigia:seal_3"), st("vestigia:golden_sword"), "Sword + Seal III");
				txt(g, "The blade returns", lx, by + 90, INK);
				txt(g, "bearing Seal III —", lx, by + 98, INK);
				txt(g, "twin Portadores!", lx, by + 106, INK);
				page(g, rx, by, "Seal Tiers", new String[]{"I: summon lasts 25s", "  (up from 15).", "II: charge fills", "  40% faster.", "III: two Portadores", "  of different arms.", "", "Higher Seals (Z,", "Black Hole) bind", "greater echoes."});
				iconsN(g, rx + 6, by + BH - 32, new ItemStack[]{st("vestigia:seal_1"), st("vestigia:seal_2"), st("vestigia:seal_3")});
			}
			case 18 -> {
				page(g, lx, by, "6 · Emblems", new String[]{"Emblems slot into", "the Curios 'Emblem'", "slots — up to four", "at once.", "", "Won from Incursions,", "each grants a", "distinct power.", "", "Some awaken with the", "'B' key."});
				bearer(g, rx, by, "Twisted Emblem", "vestigia:twisted_emblem", new String[]{"Press 'B' to melt", "into shadow:", "Invisibility for 50s", "with x5 speed and", "x5 jump.", "", "Strike from the dark."});
			}
			case 19 -> {
				bearer(g, lx, by, "Fire Emblem", "vestigia:fire_emblem", new String[]{"Immune to fire and", "lava.", "", "Double-jump, then", "hold jump: a fiery", "jetpack lifts you", "for 5 seconds.", "Cooldown 2 minutes."});
				bearer(g, rx, by, "Aurora Emblem", "vestigia:aurora_emblem", new String[]{"Doubles every good", "effect you hold.", "", "The fog drifts slow", "blues, and you see", "creatures glow", "through walls."});
			}
			case 20 -> {
				bearer(g, lx, by, "Golden Emblem", "vestigia:golden_emblem", new String[]{"Gold weapons (with", "the Golden Sword &", "Staff) deal triple", "damage.", "", "Gold tools mine much", "faster; the Staff's", "buffs double again."});
				bearer(g, rx, by, "Seal Emblem", "vestigia:seal_emblem", new String[]{"Press 'B' for the", "Stellar Emblem GUI:", "", "Change weather and", "day or night.", "", "Offer a diamond", "stack for x6 damage", "over 5 minutes."});
			}
			case 21 -> {
				aztecBg(g, bx, by);
				aztecPage(g, lx, by, "7 · The Mexica", new String[]{"The first civili-", "zation to arrive", "after the first", "Incursion.", "", "Sun-priests of the", "old world, they", "trade in souls and", "in the favor of", "the rain-god", "Tlaloc.", "", "Learn their rites", "and grow strong."});
				aztecPage(g, rx, by, "Offering Knife", new String[]{"The Offering Knife", "harvests souls.", "", "Right-click to cut", "yourself: if it", "kills you, +1 soul.", "", "Slay mobs with it", "for +1 soul each.", "", "Sneak + right-click", "to read your count.", "", "Reach 100 souls and", "Tlaloc grants you", "a wish."});
			}
			case 22 -> {
				aztecBg(g, bx, by);
				aztecPage(g, lx, by, "The Aztec Pyramid", new String[]{"Deep in jungles and", "other lands stands", "an Aztec Pyramid.", "", "Atop it wait the", "Invocation Altars", "and a lone Altar.", "", "Use an Aztec Statue", "on that Altar to", "play a sacred game.", "", "Win with no misses", "to earn 50 souls", "and an Aztec Emblem."});
				aztecBearer(g, rx, by, "Aztec Emblem", "vestigia:aztec_emblem", new String[]{"Worn in a Curios", "Emblem slot.", "", "With the Offering", "Knife, sacrificing", "yourself or slaying", "Zombies grants x4", "souls instead of", "one."});
			}
			default -> {
			}
		}
	}

	private void page(GuiGraphicsExtractor g, int x0, int by, String title, String[] lines) {
		if (title != null && !title.isEmpty()) {
			txt(g, title, x0, by + 16, GOLD);
			g.fill(x0, by + 26, x0 + 108, by + 27, alpha(GOLD, 0.6F));
		}
		int y = by + 32;
		for (String s : lines) {
			if (!s.isEmpty())
				txt(g, s, x0, y, INK);
			y += 8;
		}
	}

	private void bearer(GuiGraphicsExtractor g, int x0, int by, String title, String iconId, String[] lines) {
		txt(g, title, x0, by + 16, GOLD);
		g.fill(x0, by + 26, x0 + 108, by + 27, alpha(GOLD, 0.6F));
		int y = by + 32;
		for (String s : lines) {
			if (!s.isEmpty())
				txt(g, s, x0, y, INK);
			y += 8;
		}
		int ix = x0 + 44, iy = by + BH - 34;
		g.fill(ix - 4, iy - 4, ix + 20, iy + 20, alpha(GOLD, 0.5F));
		g.fillGradient(ix - 3, iy - 3, ix + 19, iy + 19, 0xFFCDB98A, 0xFFB29A6C);
		ItemStack icon = st(iconId);
		if (!icon.isEmpty())
			g.item(icon, ix, iy);
	}

	private void drawIndex(GuiGraphicsExtractor g, int x0, int by, int mouseX, int mouseY) {
		String[] titles = {"1 · Archaeologist", "2 · Crafting", "3 · Bearer Relics", "✦ Incursions·Seals", "5 · Reverberation", "6 · Emblems", "☀ 7 · The Mexica"};
		int ex = x0 - 2, ew = IDX_W;
		long gt = this.minecraft != null && this.minecraft.level != null ? this.minecraft.level.getGameTime() : 0L;
		for (int i = 0; i < 7; i++) {
			int ey = by + 32 + i * IDX_GAP;
			boolean hover = mouseX >= ex && mouseX <= ex + ew && mouseY >= ey && mouseY <= ey + IDX_H;
			if (i == 6) {
				g.fill(ex - 1, ey - 1, ex + ew + 1, ey + IDX_H + 1, hover ? 0xFFE7C266 : 0xFF2E9C9C);
				g.fillGradient(ex, ey, ex + ew, ey + IDX_H, hover ? 0xFF2A5540 : 0xFF1E3A2A, 0xFF102618);
				txt(g, titles[i], ex + 7, ey + 4, hover ? 0xFFFFF3D0 : 0xFFE7C266);
			} else if (i == 3) {
				g.fill(ex - 2, ey - 2, ex + ew + 2, ey + IDX_H + 2, hover ? 0xFFE7C266 : 0xFF7A3AD6);
				g.fillGradient(ex, ey, ex + ew, ey + IDX_H, hover ? 0xFF3A1E70 : 0xFF241147, 0xFF0C0A22);
				for (int s = 0; s < 6; s++) {
					int sxp = ex + 8 + (s * 79 % (ew - 16));
					int syp = ey + 3 + (s * 41 % (IDX_H - 6));
					float tw = 0.5F + 0.5F * (float) Math.sin(gt * 0.12 + s);
					if (tw > 0.45F)
						g.fill(sxp, syp, sxp + 1, syp + 1, alpha(0xFFEAF0FF, tw));
				}
				txt(g, titles[i], ex + 7, ey + 4, hover ? 0xFFFFF3D0 : 0xFF43D6E0);
			} else {
				g.fill(ex - 1, ey - 1, ex + ew + 1, ey + IDX_H + 1, hover ? GOLD_BRIGHT : GOLD);
				g.fillGradient(ex, ey, ex + ew, ey + IDX_H, hover ? 0xFFF3E4BE : 0xFFE3CE9E, 0xFFCDB27C);
				txt(g, titles[i], ex + 7, ey + 4, hover ? 0xFF5A3A12 : INK);
			}
		}
	}

	private void icons3(GuiGraphicsExtractor g, int x, int y, ItemStack a, ItemStack b, ItemStack c) {
		g.fill(x - 3, y - 3, x + 55, y + 21, alpha(0xFF7A5220, 0.35F));
		if (!a.isEmpty())
			g.item(a, x, y);
		if (!b.isEmpty())
			g.item(b, x + 19, y);
		if (!c.isEmpty())
			g.item(c, x + 38, y);
	}

	private void aztecBg(GuiGraphicsExtractor g, int bx, int by) {
		int spine = bx + BW / 2;
		g.fillGradient(bx + 2, by + 2, spine - 1, by + BH - 2, 0xFF2A5540, 0xFF163023);
		g.fillGradient(spine + 1, by + 2, bx + BW - 2, by + BH - 2, 0xFF2A5540, 0xFF163023);
		g.fill(bx + 2, by + 2, bx + BW - 2, by + 4, GOLD);
		g.fill(bx + 2, by + BH - 4, bx + BW - 2, by + BH - 2, 0xFF7A5220);
		sunGlyph(g, bx + BW / 4, by + BH / 2 + 6, 30, alpha(0xFFE7C266, 0.09F));
		sunGlyph(g, bx + 3 * BW / 4, by + BH / 2 + 6, 30, alpha(0xFFE7C266, 0.09F));
		aztecFret(g, bx + 8, by + 8, BW - 16);
		aztecFret(g, bx + 8, by + BH - 12, BW - 16);
	}

	private void aztecFret(GuiGraphicsExtractor g, int x, int y, int w) {
		for (int i = 0; i + 10 < w; i += 12) {
			g.fill(x + i, y, x + i + 8, y + 2, GOLD);
			g.fill(x + i, y, x + i + 2, y + 5, GOLD);
			g.fill(x + i + 5, y + 3, x + i + 9, y + 5, TURQ);
		}
	}

	private void sunGlyph(GuiGraphicsExtractor g, int cx, int cy, int r, int col) {
		for (int a = 0; a < 12; a++) {
			double ang = a * Math.PI / 6;
			int px = cx + (int) (Math.cos(ang) * r), py = cy + (int) (Math.sin(ang) * r * 0.8);
			g.fill(px - 1, py - 1, px + 1, py + 1, col);
		}
		for (int a = 0; a < 28; a++) {
			double ang = a * Math.PI / 14;
			int px = cx + (int) (Math.cos(ang) * (r - 9)), py = cy + (int) (Math.sin(ang) * (r - 9) * 0.8);
			g.fill(px, py, px + 1, py + 1, col);
		}
		g.fill(cx - 2, cy - 2, cx + 2, cy + 2, col);
	}

	private void aztecPage(GuiGraphicsExtractor g, int x0, int by, String title, String[] lines) {
		txt(g, title, x0, by + 16, 0xFFF3D688);
		g.fill(x0, by + 26, x0 + 108, by + 27, alpha(GOLD, 0.7F));
		g.fill(x0, by + 27, x0 + 40, by + 28, alpha(TURQ, 0.7F));
		int y = by + 32;
		for (String s : lines) {
			if (!s.isEmpty())
				txt(g, s, x0, y, 0xFFF0E4C4);
			y += 8;
		}
	}

	private void aztecBearer(GuiGraphicsExtractor g, int x0, int by, String title, String iconId, String[] lines) {
		aztecPage(g, x0, by, title, lines);
		int ix = x0 + 44, iy = by + BH - 34;
		g.fill(ix - 4, iy - 4, ix + 20, iy + 20, GOLD);
		g.fillGradient(ix - 3, iy - 3, ix + 19, iy + 19, 0xFF2A5540, 0xFF163023);
		ItemStack icon = st(iconId);
		if (!icon.isEmpty())
			g.item(icon, ix, iy);
	}

	private void iconsN(GuiGraphicsExtractor g, int x, int y, ItemStack[] arr) {
		g.fill(x - 3, y - 3, x + arr.length * 19 - 4, y + 21, alpha(0xFF7A5220, 0.35F));
		for (int i = 0; i < arr.length; i++)
			if (!arr[i].isEmpty())
				g.item(arr[i], x + i * 19, y);
	}

	private void iconArrow(GuiGraphicsExtractor g, int x, int y, ItemStack from, ItemStack a, ItemStack b, ItemStack c) {
		g.fill(x - 3, y - 3, x + 74, y + 21, alpha(0xFF7A5220, 0.35F));
		if (!from.isEmpty())
			g.item(from, x, y);
		txt(g, "→", x + 19, y + 4, INK);
		if (!a.isEmpty())
			g.item(a, x + 28, y);
		if (!b.isEmpty())
			g.item(b, x + 44, y);
		if (!c.isEmpty())
			g.item(c, x + 60, y);
	}

	private void cell(GuiGraphicsExtractor g, int x, int y) {
		g.fill(x - 1, y - 1, x + 17, y + 17, 0xFF8A6A3A);
		g.fillGradient(x, y, x + 16, y + 16, 0xFFC9B486, 0xFFB29A6C);
	}

	private void craftGrid(GuiGraphicsExtractor g, int x, int y, ItemStack[] grid, ItemStack result) {
		for (int r = 0; r < 3; r++)
			for (int c = 0; c < 3; c++) {
				int cx = x + c * 18, cy = y + r * 18;
				cell(g, cx, cy);
				ItemStack s = grid[r * 3 + c];
				if (s != null && !s.isEmpty())
					g.item(s, cx, cy);
			}
		txt(g, "→", x + 58, y + 22, INK);
		cell(g, x + 70, y + 18);
		if (!result.isEmpty())
			g.item(result, x + 70, y + 18);
	}

	private void smelt(GuiGraphicsExtractor g, int x, int y, ItemStack in, ItemStack out) {
		cell(g, x, y);
		if (!in.isEmpty())
			g.item(in, x, y);
		g.fillGradient(x + 20, y, x + 36, y + 16, 0xFF3A2A1A, 0xFF120A06);
		g.fill(x + 24, y + 6, x + 32, y + 15, 0xFFFF7020);
		g.fill(x + 26, y + 3, x + 30, y + 9, 0xFFFFC040);
		txt(g, "→", x + 38, y + 4, INK);
		cell(g, x + 48, y);
		if (!out.isEmpty())
			g.item(out, x + 48, y);
	}

	private void single(GuiGraphicsExtractor g, int x, int y, ItemStack in, ItemStack out, String caption) {
		cell(g, x, y);
		if (!in.isEmpty())
			g.item(in, x, y);
		txt(g, "→", x + 20, y + 4, INK);
		cell(g, x + 30, y);
		if (!out.isEmpty())
			g.item(out, x + 30, y);
		txt(g, caption, x, y + 20, INK_SOFT);
	}

	private void restorerRow(GuiGraphicsExtractor g, int x, int y, ItemStack a, ItemStack b, ItemStack chisel, ItemStack out) {
		cell(g, x, y);
		if (!a.isEmpty())
			g.item(a, x, y);
		cell(g, x + 18, y);
		if (!b.isEmpty())
			g.item(b, x + 18, y);
		cell(g, x + 36, y);
		if (!chisel.isEmpty())
			g.item(chisel, x + 36, y);
		txt(g, "→", x + 56, y + 4, INK);
		cell(g, x + 66, y);
		if (!out.isEmpty())
			g.item(out, x + 66, y);
	}

	private void anvilRow(GuiGraphicsExtractor g, int x, int y, ItemStack item, ItemStack seal, ItemStack out, String caption) {
		cell(g, x, y);
		if (!item.isEmpty())
			g.item(item, x, y);
		txt(g, "+", x + 18, y + 4, INK_SOFT);
		cell(g, x + 26, y);
		if (!seal.isEmpty())
			g.item(seal, x + 26, y);
		txt(g, "→", x + 46, y + 4, INK);
		cell(g, x + 56, y);
		if (!out.isEmpty())
			g.item(out, x + 56, y);
		if (caption != null)
			txt(g, caption, x, y + 20, INK_SOFT);
	}

	private void listPage(GuiGraphicsExtractor g, int x0, int by, String title, int from, int to) {
		txt(g, title, x0, by + 16, GOLD);
		g.fill(x0, by + 26, x0 + 108, by + 27, alpha(GOLD, 0.6F));
		int y = by + 34;
		if (from == 0) {
			txt(g, "broken + material →", x0, y, INK_SOFT);
			txt(g, "result  (+ Chisel)", x0, y + 8, INK_SOFT);
			y += 20;
		}
		for (int i = from; i < to && i < RECIPES.length; i++) {
			listRow(g, x0, y, RECIPES[i]);
			y += 20;
		}
	}

	private void listRow(GuiGraphicsExtractor g, int x, int y, String[] r) {
		g.item(st(r[0]), x, y);
		txt(g, "+", x + 17, y + 4, INK_SOFT);
		ItemStack b = st(r[1]);
		if (!b.isEmpty())
			g.item(b, x + 25, y);
		else
			txt(g, "·", x + 29, y + 4, INK_SOFT);
		txt(g, "→", x + 45, y + 4, INK);
		g.item(st(r[2]), x + 56, y);
	}

	private void drawFlip(GuiGraphicsExtractor g, int bx, int by, float p, int dir) {
		int spine = bx + BW / 2;
		int pageH = BH - 12;
		int half = BW / 2 - 16;
		float sx = 1.0F - p;
		var pose = g.pose();
		pose.pushMatrix();
		pose.translate(spine, by + 6);
		pose.scale(sx, 1.0F);
		int x0 = dir > 0 ? 0 : -half;
		g.fillGradient(x0, 0, x0 + half, pageH, PARCH_TOP, PARCH_BOT);
		g.fill(dir > 0 ? x0 + half - 2 : x0, 0, dir > 0 ? x0 + half : x0 + 2, pageH, GOLD);
		for (int i = 1; i <= 5; i++)
			g.fill(x0 + 8, 14 + i * 12, x0 + half - 8, 15 + i * 12, alpha(INK_SOFT, 0.35F));
		pose.popMatrix();
		int edge = dir > 0 ? (int) (spine + half * sx) : (int) (spine - half * sx);
		g.fillGradient(edge, by + 6, edge + (dir > 0 ? 10 : -10), by + 6 + pageH, alpha(0xFF000000, 0.35F * p), 0x00000000);
	}

	private void drawChrome(GuiGraphicsExtractor g, int bx, int by, int mouseX, int mouseY) {
		int shown = animating ? toSpread : cur;
		if (shown > 0) {
			arrow(g, bx + 14, by + BH - 16, "‹ Prev", mouseX, mouseY);
			arrow(g, bx + 14, by + 6, "≡ Index", mouseX, mouseY);
		}
		if (shown < MAX_SPREAD)
			arrow(g, bx + BW - 60, by + BH - 16, "Next ›", mouseX, mouseY);
		String pg = (shown + 1) + " / " + (MAX_SPREAD + 1);
		txt(g, pg, bx + BW / 2 - this.font.width(pg) / 2, by + BH - 14, alpha(0xFF7A5220, 0.9F));
	}

	private void arrow(GuiGraphicsExtractor g, int x, int y, String label, int mouseX, int mouseY) {
		int w = this.font.width(label) + 8;
		boolean hover = mouseX >= x && mouseX <= x + w && mouseY >= y - 2 && mouseY <= y + 10;
		g.fill(x - 2, y - 2, x + w, y + 10, alpha(hover ? GOLD_BRIGHT : 0xFF7A5220, hover ? 0.9F : 0.5F));
		txt(g, label, x + 2, y, hover ? 0xFF3A2410 : 0xFF2A1A0D);
	}

	@Override
	public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
		if (event.button() == 0 && !animating) {
			int bx = bx(), by = by();
			double mx = event.x(), my = event.y();
			if (cur == 0) {
				int ex = bx + BW / 2 + 14 - 2;
				for (int i = 0; i < 7; i++) {
					int ey = by + 32 + i * IDX_GAP;
					if (mx >= ex && mx <= ex + IDX_W && my >= ey && my <= ey + IDX_H) {
						navigate(CHAPTERS[i], 1);
						return true;
					}
				}
			}
			int shown = cur;
			if (shown > 0) {
				if (hit(mx, my, bx + 14, by + BH - 16, this.font.width("‹ Prev") + 8)) {
					navigate(shown - 1, -1);
					return true;
				}
				if (hit(mx, my, bx + 14, by + 6, this.font.width("≡ Index") + 8)) {
					navigate(0, -1);
					return true;
				}
			}
			if (shown < MAX_SPREAD && hit(mx, my, bx + BW - 60, by + BH - 16, this.font.width("Next ›") + 8)) {
				navigate(shown + 1, 1);
				return true;
			}
		}
		return super.mouseClicked(event, doubleClick);
	}

	private boolean hit(double mx, double my, int x, int y, int w) {
		return mx >= x - 2 && mx <= x + w && my >= y - 2 && my <= y + 10;
	}

	private void navigate(int to, int dir) {
		if (animating || to == cur || to < 0 || to > MAX_SPREAD)
			return;
		animating = true;
		fromSpread = cur;
		toSpread = to;
		animDir = dir;
		animStart = now(0.0F);
		sound(1.0F);
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
		if (!opened) {
			opened = true;
			sound(1.1F);
		}
	}

	@Override
	public void removed() {
		sound(0.85F);
		super.removed();
	}

	private static int alpha(int rgb, float a) {
		int ai = Math.max(0, Math.min(255, (int) (a * 255)));
		return (ai << 24) | (rgb & 0xFFFFFF);
	}
}
