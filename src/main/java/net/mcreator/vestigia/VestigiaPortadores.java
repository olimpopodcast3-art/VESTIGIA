package net.mcreator.vestigia;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.phys.Vec3;

import net.mcreator.vestigia.entity.GladiatorEntity;
import net.mcreator.vestigia.entity.PortadorEntity;
import net.mcreator.vestigia.entity.TheZagalEntity;
import net.mcreator.vestigia.init.VestigiaModEntities;
import net.mcreator.vestigia.item.EchoItem;
import net.mcreator.vestigia.item.PhantomSpearItem;
import net.mcreator.vestigia.item.SlingshotItem;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@EventBusSubscriber(modid = "vestigia")
public class VestigiaPortadores {
	private static final int SUMMON_LIFETIME = 300;

	private static boolean isArtifactWeapon(ItemStack stack) {
		return stack.getItem() instanceof EchoItem || stack.getItem() instanceof PhantomSpearItem;
	}

	private static int threshold(ItemStack stack) {
		int base = stack.getItem() instanceof PhantomSpearItem ? 6 : 5;
		return Math.max(1, (int) Math.ceil(base * VestigiaSellos.chargeFactor(stack)));
	}

	private static int getCharge(ItemStack stack) {
		CustomData cd = stack.get(DataComponents.CUSTOM_DATA);
		return cd == null ? 0 : cd.copyTag().getIntOr("echo_charge", 0);
	}

	private static void setCharge(ItemStack stack, int value) {
		CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
		tag.putInt("echo_charge", value);
		stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
	}

	@SubscribeEvent
	public static void onWeaponHit(LivingIncomingDamageEvent event) {
		if (!(event.getSource().getEntity() instanceof Player p) || p.level().isClientSide())
			return;
		ItemStack weapon = p.getMainHandItem();
		if (!isArtifactWeapon(weapon) || event.getAmount() <= 0.0F)
			return;
		int max = threshold(weapon);
		int c = getCharge(weapon);
		if (c >= max)
			return;
		c = Math.min(max, c + 1);
		setCharge(weapon, c);
		if (c >= max) {
			weapon.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
			if (p.level() instanceof ServerLevel sl) {
				sl.playSound(null, p.blockPosition(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.7F, 1.6F);
				sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, p.getX(), p.getY() + 1.0, p.getZ(), 30, 0.4, 0.6, 0.4, 0.02);
			}
		}
	}

	@SubscribeEvent
	public static void onWeaponUse(PlayerInteractEvent.RightClickItem event) {
		ItemStack weapon = event.getItemStack();
		if (!isArtifactWeapon(weapon) || getCharge(weapon) < threshold(weapon))
			return;
		event.setCanceled(true);
		event.setCancellationResult(InteractionResult.SUCCESS);
		Player p = event.getEntity();
		if (p.level().isClientSide())
			return;
		if (!VestigiaSellos.canSummon(p, weapon))
			return;
		setCharge(weapon, 0);
		weapon.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, false);
		summonFor(p, weapon);
	}

	public static void summonFor(Player p, ItemStack weapon) {
		if (!(p.level() instanceof ServerLevel sl))
			return;
		Item it = weapon.getItem();
		Mob ally;
		if (it instanceof PhantomSpearItem)
			ally = new GladiatorEntity(VestigiaModEntities.GLADIATOR.get(), sl);
		else if (it instanceof SlingshotItem)
			ally = new TheZagalEntity(VestigiaModEntities.THE_ZAGAL.get(), sl);
		else
			ally = new PortadorEntity(VestigiaModEntities.PORTADOR.get(), sl);
		Vec3 look = p.getLookAngle();
		Vec3 spot = p.position().add(look.x * 1.5, 0.0, look.z * 1.5);
		ally.snapTo(spot.x, p.getY(), spot.z, p.getYRot(), 0.0F);
		markAlly(ally, p);
		ally.getPersistentData().putInt("vestigia_life", VestigiaSellos.lifetimeFor(weapon, SUMMON_LIFETIME));
		VestigiaSellos.tagAlly(ally, VestigiaSellos.weaponKey(weapon));
		if (it instanceof SlingshotItem)
			configureZagal(ally, look);
		else
			configureCombatAlly(ally, it instanceof PhantomSpearItem);
		sl.addFreshEntity(ally);
		sl.sendParticles(ParticleTypes.SOUL, spot.x, p.getY() + 1.0, spot.z, 45, 0.3, 0.7, 0.3, 0.03);
		sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, spot.x, p.getY() + 1.0, spot.z, 25, 0.3, 0.7, 0.3, 0.02);
		sl.playSound(null, ally.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.2F, 0.7F);
	}

	private static void markAlly(Mob ally, Player owner) {
		CompoundTag pd = ally.getPersistentData();
		pd.putString("vestigia_owner", owner.getUUID().toString());
		pd.putInt("vestigia_life", SUMMON_LIFETIME);
		ally.setInvulnerable(true);
		ally.setPersistenceRequired();
	}

	public static void makeCombatAlly(Mob ally, Player owner, int lifetime, boolean follow) {
		CompoundTag pd = ally.getPersistentData();
		pd.putString("vestigia_owner", owner.getUUID().toString());
		pd.putInt("vestigia_life", lifetime);
		ally.setInvulnerable(true);
		ally.setPersistenceRequired();
		ally.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(ally, LivingEntity.class, true,
				(target, level) -> target instanceof Enemy && target.getPersistentData().getIntOr("vestigia_life", -1) < 0));
		if (follow)
			ally.goalSelector.addGoal(1, new FollowOwnerAllyGoal(ally, 1.15, 6.0, 3.0));
	}

	private static void configureCombatAlly(Mob ally, boolean stationary) {
		ally.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(ally, LivingEntity.class, true,
				(target, level) -> target instanceof Enemy && target.getPersistentData().getIntOr("vestigia_life", -1) < 0));
		if (stationary) {
			AttributeInstance speed = ally.getAttribute(Attributes.MOVEMENT_SPEED);
			if (speed != null)
				speed.setBaseValue(0.0);
		} else {
			ally.goalSelector.addGoal(1, new FollowOwnerAllyGoal(ally, 1.15, 6.0, 3.0));
		}
	}

	private static void configureZagal(Mob ally, Vec3 look) {
		AttributeInstance speed = ally.getAttribute(Attributes.MOVEMENT_SPEED);
		if (speed != null)
			speed.setBaseValue(0.29);
		double lx = look.x, lz = look.z;
		double len = Math.sqrt(lx * lx + lz * lz);
		if (len < 1.0e-4) {
			lx = 0.0;
			lz = 1.0;
			len = 1.0;
		}
		ally.goalSelector.addGoal(1, new RunInDirectionGoal(ally, lx / len, lz / len, 1.15));
	}

	@SubscribeEvent
	public static void onAllyTick(EntityTickEvent.Post event) {
		if (!(event.getEntity() instanceof Mob ally) || ally.level().isClientSide())
			return;
		CompoundTag pd = ally.getPersistentData();
		int stored = pd.getIntOr("vestigia_life", -1);
		if (stored < 0)
			return;
		int life = stored - 1;
		if (life <= 0) {
			if (ally.level() instanceof ServerLevel sl) {
				sl.sendParticles(ParticleTypes.SOUL, ally.getX(), ally.getY() + 1.0, ally.getZ(), 50, 0.3, 0.7, 0.3, 0.04);
				sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, ally.getX(), ally.getY() + 1.0, ally.getZ(), 25, 0.3, 0.7, 0.3, 0.03);
				sl.playSound(null, ally.blockPosition(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.9F, 1.2F);
			}
			ally.discard();
			return;
		}
		pd.putInt("vestigia_life", life);
		if (ally instanceof TheZagalEntity && ally.level() instanceof ServerLevel sl) {
			List<Mob> hostiles = sl.getEntitiesOfClass(Mob.class, ally.getBoundingBox().inflate(64.0),
					m -> m instanceof Enemy && m != ally && m.getPersistentData().getIntOr("vestigia_life", -1) < 0);
			for (Mob h : hostiles) {
				h.setTarget(ally);
				h.setLastHurtByMob(ally);
				h.addEffect(new MobEffectInstance(MobEffects.SPEED, 40, 2, false, false, false));
				if (h.tickCount % 5 == 0)
					h.getNavigation().moveTo(ally, 1.3);
			}
		}
		if (ally.tickCount % 20 == 0)
			applyResonance(ally);
	}

	private static void applyResonance(Mob ally) {
		if (!(ally.level() instanceof ServerLevel sl))
			return;
		String ownerId = ally.getPersistentData().getStringOr("vestigia_owner", "");
		if (ownerId.isEmpty())
			return;
		Player owner;
		try {
			owner = sl.getPlayerByUUID(UUID.fromString(ownerId));
		} catch (IllegalArgumentException e) {
			return;
		}
		if (owner == null)
			return;
		List<Mob> allies = sl.getEntitiesOfClass(Mob.class, owner.getBoundingBox().inflate(96.0),
				m -> ownerId.equals(m.getPersistentData().getStringOr("vestigia_owner", "")) && m.getPersistentData().getIntOr("vestigia_life", -1) > 0);
		Set<String> weapons = new HashSet<>();
		for (Mob m : allies)
			weapons.add(m.getPersistentData().getStringOr("vestigia_from", ""));
		if (allies.size() < 2 || weapons.size() < 2)
			return;
		ally.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 45, 1, false, false));
		ally.addEffect(new MobEffectInstance(MobEffects.SPEED, 45, 1, false, false));
		ally.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 45, 0, false, false));
		ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 45, 0, false, false));
		owner.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 45, 0, false, true));
		sl.sendParticles(ParticleTypes.END_ROD, ally.getX(), ally.getY() + 1.2, ally.getZ(), 3, 0.2, 0.3, 0.2, 0.01);
		sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, ally.getX(), ally.getY() + 1.2, ally.getZ(), 2, 0.2, 0.3, 0.2, 0.01);
	}

	private static class RunInDirectionGoal extends Goal {
		private final Mob mob;
		private final double dx;
		private final double dz;
		private final double speed;
		private int recalc;

		RunInDirectionGoal(Mob mob, double dx, double dz, double speed) {
			this.mob = mob;
			this.dx = dx;
			this.dz = dz;
			this.speed = speed;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		@Override
		public boolean canUse() {
			return true;
		}

		@Override
		public boolean canContinueToUse() {
			return true;
		}

		@Override
		public void tick() {
			if (--this.recalc <= 0) {
				this.recalc = 8;
				this.mob.getNavigation().moveTo(this.mob.getX() + this.dx * 12.0, this.mob.getY(), this.mob.getZ() + this.dz * 12.0, this.speed);
			}
		}
	}

	private static class FollowOwnerAllyGoal extends Goal {
		private final Mob mob;
		private final double speed;
		private final double startDistSqr;
		private final double stopDistSqr;
		private Player owner;
		private int recalc;

		FollowOwnerAllyGoal(Mob mob, double speed, double startDist, double stopDist) {
			this.mob = mob;
			this.speed = speed;
			this.startDistSqr = startDist * startDist;
			this.stopDistSqr = stopDist * stopDist;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE));
		}

		private Player findOwner() {
			String id = this.mob.getPersistentData().getStringOr("vestigia_owner", "");
			if (id.isEmpty())
				return null;
			try {
				return this.mob.level().getPlayerByUUID(UUID.fromString(id));
			} catch (IllegalArgumentException e) {
				return null;
			}
		}

		@Override
		public boolean canUse() {
			this.owner = findOwner();
			return this.owner != null && this.mob.getTarget() == null && this.mob.distanceToSqr(this.owner) > this.startDistSqr;
		}

		@Override
		public boolean canContinueToUse() {
			return this.owner != null && this.owner.isAlive() && this.mob.getTarget() == null && this.mob.distanceToSqr(this.owner) > this.stopDistSqr;
		}

		@Override
		public void stop() {
			this.owner = null;
			this.mob.getNavigation().stop();
		}

		@Override
		public void tick() {
			if (this.owner == null)
				return;
			this.mob.getLookControl().setLookAt(this.owner, 10.0F, this.mob.getMaxHeadXRot());
			if (--this.recalc <= 0) {
				this.recalc = 10;
				if (this.mob.distanceToSqr(this.owner) > 400.0)
					this.mob.snapTo(this.owner.getX(), this.owner.getY(), this.owner.getZ(), this.mob.getYRot(), this.mob.getXRot());
				else
					this.mob.getNavigation().moveTo(this.owner, this.speed);
			}
		}
	}
}
