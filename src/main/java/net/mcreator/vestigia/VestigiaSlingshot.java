package net.mcreator.vestigia;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingEntityUseItemEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomData;

import net.mcreator.vestigia.entity.PebbleEntity;
import net.mcreator.vestigia.item.SlingshotItem;

@EventBusSubscriber(modid = "vestigia")
public class VestigiaSlingshot {
	private static final int CHARGE_TICKS = 160;
	private static final int COOLDOWN = 1200;

	private static boolean isReady(ItemStack stack) {
		CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
		return cd != null && cd.copyTag().getIntOr("zagal_ready", 0) == 1;
	}

	private static void setReady(ItemStack stack, boolean value) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putInt("zagal_ready", value ? 1 : 0);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
		stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, value);
	}

	@SubscribeEvent
	public static void onChargeTick(PlayerTickEvent.Post event) {
		Player p = event.getEntity();
		if (p.level().isClientSide() || !p.isUsingItem())
			return;
		ItemStack use = p.getUseItem();
		if (!(use.getItem() instanceof SlingshotItem) || p.getCooldowns().isOnCooldown(use))
			return;
		if (p.getTicksUsingItem() < (int) Math.round(CHARGE_TICKS * VestigiaSellos.chargeFactor(use)))
			return;
		setReady(use, true);
		if (p.level() instanceof ServerLevel sl) {
			sl.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.7F, 1.5F);
			sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.getX(), p.getY() + 1.0, p.getZ(), 25, 0.4, 0.6, 0.4, 0.02);
		}
		p.stopUsingItem();
	}

	private static boolean sneakSummon(Player p, ItemStack weapon) {
		if (!(weapon.getItem() instanceof SlingshotItem) || !p.isShiftKeyDown())
			return false;
		if (!p.level().isClientSide() && isReady(weapon) && !p.getCooldowns().isOnCooldown(weapon)) {
			if (!VestigiaSellos.canSummon(p, weapon))
				return true;
			setReady(weapon, false);
			p.getCooldowns().addCooldown(weapon, COOLDOWN);
			VestigiaPortadores.summonFor(p, weapon);
		}
		return true;
	}

	@SubscribeEvent
	public static void onSlingRelease(LivingEntityUseItemEvent.Stop event) {
		if (!(event.getEntity() instanceof Player p) || !(event.getItem().getItem() instanceof SlingshotItem))
			return;
		event.setCanceled(true);
		if (p.level().isClientSide())
			return;
		int held = 72000 - event.getDuration();
		if (held < 3 || !consumeAmmo(p))
			return;
		float frac = Math.min(held / (float) Math.max(1, (int) Math.round(CHARGE_TICKS * VestigiaSellos.chargeFactor(event.getItem()))), 1.0F);
		float power = 1.5F + frac * 2.5F;
		PebbleEntity pebble = new PebbleEntity(p.level(), p, new ItemStack(Items.COBBLESTONE));
		pebble.shootFromRotation(p, p.getXRot(), p.getYRot(), 0.0F, power, 1.0F);
		p.level().addFreshEntity(pebble);
		p.level().playSound(null, p.getX(), p.getY(), p.getZ(), SoundEvents.SNOWBALL_THROW, SoundSource.PLAYERS, 0.9F, 0.8F + frac * 0.7F);
	}

	private static boolean consumeAmmo(Player p) {
		Inventory inv = p.getInventory();
		for (int i = 0; i < inv.getContainerSize(); i++) {
			ItemStack s = inv.getItem(i);
			if (s.is(Items.COBBLESTONE) || s.is(Items.STONE)) {
				s.shrink(1);
				return true;
			}
		}
		return false;
	}

	@SubscribeEvent
	public static void onSneakSummonItem(PlayerInteractEvent.RightClickItem event) {
		if (sneakSummon(event.getEntity(), event.getItemStack())) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}

	@SubscribeEvent
	public static void onSneakSummonBlock(PlayerInteractEvent.RightClickBlock event) {
		if (sneakSummon(event.getEntity(), event.getItemStack())) {
			event.setCanceled(true);
			event.setCancellationResult(InteractionResult.SUCCESS);
		}
	}
}
