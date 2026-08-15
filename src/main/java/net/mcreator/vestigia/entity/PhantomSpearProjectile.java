package net.mcreator.vestigia.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.throwableitemprojectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.Mth;

import net.mcreator.vestigia.init.VestigiaModItems;

public class PhantomSpearProjectile extends ThrowableItemProjectile {
	private float damage = 6.0F;

	public PhantomSpearProjectile(EntityType<? extends PhantomSpearProjectile> type, Level level) {
		super(type, level);
	}

	public PhantomSpearProjectile(Level level, LivingEntity owner, ItemStack stack) {
		super(VestigiaEntities.PHANTOM_SPEAR, owner, level, stack);
	}

	public void setDamage(float value) {
		this.damage = value;
	}

	@Override
	protected Item getDefaultItem() {
		return VestigiaModItems.PHANTOM_SPEAR.get();
	}

	@Override
	protected double getDefaultGravity() {
		return 0.02;
	}

	@Override
	public void tick() {
		super.tick();
		if (this.isRemoved())
			return;
		Vec3 v = this.getDeltaMovement();
		if (v.lengthSqr() > 1.0e-6) {
			double horiz = Math.sqrt(v.x * v.x + v.z * v.z);
			float yaw = (float) (Mth.atan2(v.x, v.z) * (180.0 / Math.PI));
			float pitch = (float) (Mth.atan2(v.y, horiz) * (180.0 / Math.PI));
			this.setYRot(yaw);
			this.setXRot(pitch);
			this.yRotO = yaw;
			this.xRotO = pitch;
		}
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		Entity target = result.getEntity();
		target.hurt(this.damageSources().thrown(this, this.getOwner()), this.damage);
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		if (!this.level().isClientSide())
			this.discard();
	}
}
