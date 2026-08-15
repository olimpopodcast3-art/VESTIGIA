package net.mcreator.vestigia;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import net.minecraft.core.Direction;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

import net.mcreator.vestigia.init.VestigiaModSounds;
import net.mcreator.vestigia.item.BellRingersMaceItem;

@EventBusSubscriber(modid = "vestigia")
public class VestigiaMace {
	@SubscribeEvent
	public static void onMaceSmash(LivingIncomingDamageEvent event) {
		if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || attacker.level().isClientSide())
			return;
		if (event.getSource().getDirectEntity() != attacker)
			return;
		if (!(attacker.getMainHandItem().getItem() instanceof BellRingersMaceItem))
			return;
		if (!(attacker.fallDistance > 1.5) || attacker.isFallFlying())
			return;
		double fd = attacker.fallDistance;
		double bonus;
		if (fd <= 3.0)
			bonus = 4.0 * fd;
		else if (fd <= 8.0)
			bonus = 12.0 + 2.0 * (fd - 3.0);
		else
			bonus = 22.0 + (fd - 8.0);
		event.setAmount(event.getAmount() + (float) bonus);
		LivingEntity victim = event.getEntity();
		if (attacker.level() instanceof ServerLevel sl) {
			sl.levelEvent(2013, victim.getOnPos(), 750);
			SoundEvent bell = attacker.getRandom().nextBoolean() ? SoundEvents.BELL_BLOCK : VestigiaModSounds.BELL_BONG.get();
			sl.playSound(null, victim.getX(), victim.getY(), victim.getZ(), bell, SoundSource.PLAYERS, 1.3F, 1.0F);
			knockback(sl, attacker, victim);
		}
		attacker.setDeltaMovement(attacker.getDeltaMovement().with(Direction.Axis.Y, 0.01));
		attacker.setIgnoreFallDamageFromCurrentImpulse(true, attacker.position());
		attacker.resetFallDistance();
		if (attacker instanceof ServerPlayer player)
			player.connection.send(new ClientboundSetEntityMotionPacket(player));
	}

	private static void knockback(ServerLevel level, LivingEntity attacker, LivingEntity victim) {
		double mult = attacker.fallDistance > 5.0 ? 2.0 : 1.0;
		for (LivingEntity nearby : level.getEntitiesOfClass(LivingEntity.class, victim.getBoundingBox().inflate(3.5))) {
			if (nearby == attacker || nearby == victim || nearby.isSpectator() || attacker.isAlliedTo(nearby))
				continue;
			Vec3 dir = nearby.position().subtract(victim.position());
			double dist = dir.length();
			if (dist <= 0.0)
				continue;
			double power = (3.5 - dist) * 0.7 * mult * (1.0 - nearby.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE));
			if (power > 0.0) {
				Vec3 kb = dir.normalize().scale(power);
				nearby.push(kb.x, 0.7, kb.z);
			}
		}
	}
}
