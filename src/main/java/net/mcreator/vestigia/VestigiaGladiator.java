package net.mcreator.vestigia;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.mcreator.vestigia.entity.GladiatorEntity;
import net.mcreator.vestigia.entity.PhantomSpearProjectile;
import net.mcreator.vestigia.init.VestigiaModItems;

import java.util.EnumSet;

@EventBusSubscriber(modid = "vestigia")
public class VestigiaGladiator {
	@SubscribeEvent
	public static void onGladiatorSpawn(EntityJoinLevelEvent event) {
		if (event.getLevel().isClientSide() || !(event.getEntity() instanceof GladiatorEntity gladiator))
			return;
		gladiator.goalSelector.addGoal(2, new ThrowSpearGoal(gladiator));
		boolean ally = gladiator.getPersistentData().getIntOr("vestigia_life", -1) >= 0;
		if (!ally)
			gladiator.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(gladiator, Player.class, true));
	}

	private static class ThrowSpearGoal extends Goal {
		private final GladiatorEntity mob;
		private int cooldown;

		ThrowSpearGoal(GladiatorEntity mob) {
			this.mob = mob;
			this.setFlags(EnumSet.noneOf(Goal.Flag.class));
		}

		@Override
		public boolean canUse() {
			LivingEntity t = this.mob.getTarget();
			if (t == null || !t.isAlive())
				return false;
			double d = this.mob.distanceToSqr(t);
			return d > 4.0 && d < 576.0 && this.mob.getSensing().hasLineOfSight(t);
		}

		@Override
		public boolean canContinueToUse() {
			return canUse();
		}

		@Override
		public void start() {
			this.cooldown = 25;
		}

		@Override
		public void tick() {
			LivingEntity t = this.mob.getTarget();
			if (t == null)
				return;
			this.mob.getLookControl().setLookAt(t, 30.0F, 30.0F);
			if (--this.cooldown <= 0) {
				this.cooldown = 45;
				this.mob.swing(InteractionHand.MAIN_HAND);
				throwSpear(t);
			}
		}

		private void throwSpear(LivingEntity target) {
			Level lvl = this.mob.level();
			PhantomSpearProjectile spear = new PhantomSpearProjectile(lvl, this.mob, new ItemStack(VestigiaModItems.PHANTOM_SPEAR.get()));
			spear.setDamage(6.0F);
			double dx = target.getX() - this.mob.getX();
			double dy = (target.getY() + target.getBbHeight() * 0.5) - spear.getY();
			double dz = target.getZ() - this.mob.getZ();
			double horiz = Math.sqrt(dx * dx + dz * dz);
			spear.shoot(dx, dy + horiz * 0.12, dz, 1.5F, 4.0F);
			this.mob.playSound(SoundEvents.TRIDENT_THROW.value(), 1.0F, 0.9F);
			lvl.addFreshEntity(spear);
		}
	}
}
