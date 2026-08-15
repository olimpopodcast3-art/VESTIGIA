package net.mcreator.vestigia.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

import net.mcreator.vestigia.init.VestigiaModSounds;
import net.mcreator.vestigia.VestigiaMod;

public class RefugeBellItem extends Item {
	public RefugeBellItem(Item.Properties properties) {
		super(properties.rarity(Rarity.UNCOMMON).stacksTo(1));
	}

	@Override
	public InteractionResult use(Level world, Player player, InteractionHand hand) {
		if (!world.isClientSide()) {
			world.playSound(null, player.getX(), player.getY(), player.getZ(), VestigiaModSounds.BELL_BONG.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
			for (Villager villager : world.getEntitiesOfClass(Villager.class, player.getBoundingBox().inflate(24.0)))
				follow(villager, player, 600);
			player.getCooldowns().addCooldown(player.getItemInHand(hand), 600);
		}
		return InteractionResult.SUCCESS;
	}

	private static void follow(Villager villager, Player player, int remaining) {
		if (!villager.isAlive())
			return;
		villager.getNavigation().moveTo(player, 0.6);
		if (remaining > 0)
			VestigiaMod.queueServerWork(10, () -> follow(villager, player, remaining - 10));
		else
			flee(villager);
	}

	private static void flee(Villager villager) {
		RandomSource rand = villager.getRandom();
		double ang = rand.nextDouble() * Math.PI * 2.0;
		double tx = villager.getX() + Math.cos(ang) * 14.0;
		double tz = villager.getZ() + Math.sin(ang) * 14.0;
		fleeTo(villager, tx, tz, 120);
	}

	private static void fleeTo(Villager villager, double tx, double tz, int remaining) {
		if (!villager.isAlive())
			return;
		villager.getNavigation().moveTo(tx, villager.getY(), tz, 0.85);
		if (remaining > 0)
			VestigiaMod.queueServerWork(15, () -> fleeTo(villager, tx, tz, remaining - 15));
	}
}
