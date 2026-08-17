package net.mcreator.vestigia;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.event.entity.player.PlayerContainerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import net.mcreator.vestigia.item.EchoItem;
import net.mcreator.vestigia.item.GoldenSwordItem;
import net.mcreator.vestigia.item.PhantomSpearItem;
import net.mcreator.vestigia.item.SlingshotItem;
import net.mcreator.vestigia.world.inventory.ReverberationGUIMenu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@EventBusSubscriber(modid = "vestigia")
public class VestigiaSellos {
	private static final Set<GlobalPos> OPEN = new HashSet<>();
	private static final Set<GlobalPos> PERSIST = new HashSet<>();
	private static final Set<GlobalPos> READY = new HashSet<>();
	private static final Map<GlobalPos, Integer> FORGE = new HashMap<>();
	private static final int FORGE_TIME = 60;

	public static boolean isPortadorWeapon(ItemStack s) {
		Item it = s.getItem();
		return it instanceof EchoItem || it instanceof PhantomSpearItem || it instanceof SlingshotItem || it instanceof GoldenSwordItem;
	}

	public static String weaponKey(ItemStack s) {
		Item it = s.getItem();
		if (it instanceof PhantomSpearItem)
			return "phantom_spear";
		if (it instanceof SlingshotItem)
			return "slingshot";
		if (it instanceof GoldenSwordItem)
			return "golden_sword";
		return "echo";
	}

	private static int sealTier(ItemStack s) {
		if (s.isEmpty())
			return 0;
		Identifier id = BuiltInRegistries.ITEM.getKey(s.getItem());
		String p = id.getPath();
		if (p.equals("seal_3"))
			return 3;
		if (p.equals("seal_2"))
			return 2;
		if (p.equals("seal_1"))
			return 1;
		return 0;
	}

	public static int seal(ItemStack weapon) {
		CustomData cd = weapon.get(DataComponents.CUSTOM_DATA);
		return cd == null ? 0 : cd.copyTag().getIntOr("vestigia_seal", 0);
	}

	private static void setSeal(ItemStack weapon, int level) {
		CompoundTag tag = weapon.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putInt("vestigia_seal", level);
		weapon.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	public static int lifetimeFor(ItemStack weapon, int base) {
		return seal(weapon) >= 1 ? Math.max(base, 500) : base;
	}

	public static double chargeFactor(ItemStack weapon) {
		return seal(weapon) >= 2 ? 0.6 : 1.0;
	}

	public static void tagAlly(Mob ally, String weaponKey) {
		ally.getPersistentData().putString("vestigia_from", weaponKey);
	}

	public static boolean canSummon(Player player, ItemStack weapon) {
		if (!(player.level() instanceof ServerLevel sl))
			return true;
		String me = player.getUUID().toString();
		List<Mob> allies = sl.getEntitiesOfClass(Mob.class, player.getBoundingBox().inflate(160.0),
				m -> me.equals(m.getPersistentData().getStringOr("vestigia_owner", "")) && m.getPersistentData().getIntOr("vestigia_life", -1) > 0);
		int active = allies.size();
		int max = seal(weapon) >= 3 ? 2 : 1;
		if (active >= max) {
			player.sendSystemMessage(Component.literal(max == 2 ? "§cYou already have two Portadores active" : "§cYou already have a Portador active §7(Seal III allows two)"));
			return false;
		}
		if (active == 1 && max == 2 && allies.get(0).getPersistentData().getStringOr("vestigia_from", "").equals(weaponKey(weapon))) {
			player.sendSystemMessage(Component.literal("§cThe two Portadores must be from different weapons"));
			return false;
		}
		return true;
	}

	private static ItemStack compute(ItemStack a, ItemStack b) {
		if (a.isEmpty() || b.isEmpty())
			return ItemStack.EMPTY;
		if (a.getCount() == 1 && isPortadorWeapon(a)) {
			int tier = sealTier(b);
			if (tier > 0 && tier > seal(a)) {
				ItemStack result = a.copy();
				setSeal(result, tier);
				return result;
			}
		}
		String out = sealComboResult(pathOf(a), pathOf(b));
		if (out != null) {
			Item it = BuiltInRegistries.ITEM.getValue(Identifier.parse("vestigia:" + out));
			if (it != null)
				return new ItemStack(it);
		}
		return ItemStack.EMPTY;
	}

	private static String pathOf(ItemStack s) {
		Identifier id = BuiltInRegistries.ITEM.getKey(s.getItem());
		return id == null ? "" : id.getPath();
	}

	private static String sealComboResult(String a, String b) {
		if (a.equals("seal_1") && b.equals("seal_1"))
			return "seal_2_off";
		if ((a.equals("seal_1") && b.equals("seal_2")) || (a.equals("seal_2") && b.equals("seal_1")))
			return "seal_3_off";
		if (a.equals("seal_3") && b.equals("seal_3"))
			return "seal_z_off";
		if (a.equals("seal_z") && b.equals("seal_z"))
			return "stellar_seal_off";
		return null;
	}

	public static boolean wouldApply(ItemStack weapon, ItemStack sealStack) {
		return !compute(weapon, sealStack).isEmpty();
	}

	@SubscribeEvent
	public static void onOpen(PlayerContainerEvent.Open event) {
		if (event.getContainer() instanceof ReverberationGUIMenu menu && menu.world instanceof ServerLevel sl)
			OPEN.add(GlobalPos.of(sl.dimension(), new BlockPos(menu.x, menu.y, menu.z)));
	}

	@SubscribeEvent
	public static void onCloseAnvil(PlayerContainerEvent.Close event) {
		if (event.getContainer() instanceof ReverberationGUIMenu menu && menu.world instanceof ServerLevel sl)
			OPEN.remove(GlobalPos.of(sl.dimension(), new BlockPos(menu.x, menu.y, menu.z)));
	}

	@SubscribeEvent
	public static void onServerTick(ServerTickEvent.Post event) {
		if (OPEN.isEmpty() && PERSIST.isEmpty())
			return;
		MinecraftServer server = event.getServer();
		Set<GlobalPos> all = new HashSet<>(OPEN);
		all.addAll(PERSIST);
		for (GlobalPos gp : new ArrayList<>(all)) {
			ServerLevel sl = server.getLevel(gp.dimension());
			BlockPos pos = gp.pos();
			if (sl == null || !sl.isLoaded(pos) || !(sl.getBlockEntity(pos) instanceof Container be)) {
				PERSIST.remove(gp);
				FORGE.remove(gp);
				READY.remove(gp);
				continue;
			}
			processForge(sl, gp, pos, be);
		}
	}

	private static void processForge(ServerLevel sl, GlobalPos gp, BlockPos pos, Container be) {
		if (!be.getItem(2).isEmpty()) {
			READY.add(gp);
			PERSIST.add(gp);
			FORGE.remove(gp);
			return;
		}
		if (READY.remove(gp)) {
			sl.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 0.5F);
			sl.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.7F, 0.5F);
			sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5, 20, 0.3, 0.4, 0.3, 0.02);
		}
		ItemStack res = compute(be.getItem(0), be.getItem(1));
		if (res.isEmpty()) {
			FORGE.remove(gp);
			PERSIST.remove(gp);
			return;
		}
		int prog = FORGE.getOrDefault(gp, 0) + 1;
		FORGE.put(gp, prog);
		PERSIST.add(gp);
		forgeAnim(sl, pos, prog);
		if (prog >= FORGE_TIME) {
			be.removeItem(0, 1);
			be.removeItem(1, 1);
			be.setItem(2, res);
			be.setChanged();
			FORGE.remove(gp);
			READY.add(gp);
			forgeBurst(sl, pos);
		}
	}

	private static void forgeAnim(ServerLevel sl, BlockPos pos, int prog) {
		double x = pos.getX() + 0.5, y = pos.getY() + 1.0, z = pos.getZ() + 0.5;
		float frac = prog / (float) FORGE_TIME;
		double ang = prog * 0.55;
		double rad = 0.75 * (1.0 - frac) + 0.12;
		sl.sendParticles(new DustParticleOptions(0x8A5CFF, 1.2F), x + Math.cos(ang) * rad, y + frac * 0.55, z + Math.sin(ang) * rad, 1, 0.0, 0.0, 0.0, 0.0);
		sl.sendParticles(new DustParticleOptions(0x34E0F0, 1.0F), x + Math.cos(-ang) * rad, y + frac * 0.55, z + Math.sin(-ang) * rad, 1, 0.0, 0.0, 0.0, 0.0);
		sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y + 0.1, z, 1, 0.18, 0.2, 0.18, 0.01);
		if (prog % 6 == 0)
			sl.sendParticles(ParticleTypes.ENCHANT, x, y + 0.8, z, 5, 0.4, 0.4, 0.4, 0.7);
		if (prog % 12 == 0)
			sl.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.35F, 0.6F + frac * 0.9F);
	}

	private static void forgeBurst(ServerLevel sl, BlockPos pos) {
		double x = pos.getX() + 0.5, y = pos.getY() + 1.0, z = pos.getZ() + 0.5;
		sl.sendParticles(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
		sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, x, y, z, 70, 0.55, 0.6, 0.55, 0.09);
		sl.sendParticles(ParticleTypes.END_ROD, x, y, z, 45, 0.45, 0.6, 0.45, 0.13);
		sl.sendParticles(new DustParticleOptions(0xFFD700, 1.7F), x, y, z, 55, 0.6, 0.7, 0.6, 0.06);
		sl.sendParticles(new DustParticleOptions(0x9BF6FF, 1.4F), x, y, z, 40, 0.7, 0.5, 0.7, 0.05);
		sl.playSound(null, pos, SoundEvents.BEACON_POWER_SELECT, SoundSource.BLOCKS, 1.3F, 1.2F);
		sl.playSound(null, pos, SoundEvents.TRIDENT_THUNDER.value(), SoundSource.BLOCKS, 0.8F, 1.5F);
		sl.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 1.0F, 0.9F);
	}

	@SubscribeEvent
	public static void onTooltip(ItemTooltipEvent event) {
		ItemStack s = event.getItemStack();
		if (!isPortadorWeapon(s))
			return;
		int lvl = seal(s);
		if (lvl <= 0)
			return;
		String roman = lvl == 3 ? "III" : lvl == 2 ? "II" : "I";
		event.getToolTip().add(Component.literal("§b✦ Reverberation Seal " + roman + " ✦"));
		event.getToolTip().add(Component.literal("§7• Summon lasts 25s"));
		if (lvl >= 2)
			event.getToolTip().add(Component.literal("§7• Charges 40% faster"));
		if (lvl >= 3)
			event.getToolTip().add(Component.literal("§7• Two Portadores at once"));
	}

}
