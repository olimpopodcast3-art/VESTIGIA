package net.mcreator.vestigia.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class PebbleEntity extends ThrowableItemProjectile {
	public PebbleEntity(EntityType<? extends PebbleEntity> type, Level level) {
		super(type, level);
	}

	public PebbleEntity(Level level, LivingEntity owner, ItemStack stack) {
		super(VestigiaEntities.PEBBLE, owner, level, stack);
	}

	@Override
	protected Item getDefaultItem() {
		return Items.COBBLESTONE;
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		Entity target = result.getEntity();
		target.hurt(this.damageSources().thrown(this, this.getOwner()), 11.0F);
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		if (!this.level().isClientSide())
			this.discard();
	}
}
