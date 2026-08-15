package net.mcreator.vestigia.entity;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ItemSupplier;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

public class BoomerangEntity extends Projectile implements ItemSupplier {
	private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(BoomerangEntity.class, EntityDataSerializers.ITEM_STACK);
	private int age;
	private boolean returning;

	public BoomerangEntity(EntityType<? extends BoomerangEntity> type, Level level) {
		super(type, level);
	}

	public BoomerangEntity(Level level, LivingEntity owner, ItemStack stack) {
		this(VestigiaEntities.BOOMERANG, level);
		this.setOwner(owner);
		this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
		this.setItem(stack);
		this.setDeltaMovement(owner.getLookAngle().scale(1.4));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_ITEM, new ItemStack(Items.AIR));
	}

	public void setItem(ItemStack stack) {
		this.entityData.set(DATA_ITEM, stack.copy());
	}

	@Override
	public ItemStack getItem() {
		return this.entityData.get(DATA_ITEM);
	}

	@Override
	public void tick() {
		super.tick();
		this.age++;
		Entity owner = this.getOwner();
		if (!this.returning) {
			Vec3 v = this.getDeltaMovement();
			Vec3 perp = new Vec3(-v.z, 0.0, v.x);
			if (perp.lengthSqr() > 1.0E-4)
				this.setDeltaMovement(v.add(perp.normalize().scale(0.07)));
			if (this.age > 25 || this.horizontalCollision)
				this.returning = true;
		}
		if (this.returning) {
			if (owner == null) {
				if (!this.level().isClientSide())
					this.discard();
				return;
			}
			Vec3 toOwner = owner.position().add(0, owner.getBbHeight() * 0.5, 0).subtract(this.position());
			if (toOwner.length() < 1.6) {
				if (!this.level().isClientSide()) {
					if (owner instanceof Player player) {
						ItemStack back = this.getItem().copy();
						if (!player.addItem(back))
							player.drop(back, false);
					}
					this.discard();
				}
				return;
			}
			this.setDeltaMovement(this.getDeltaMovement().scale(0.86).add(toOwner.normalize().scale(0.4)));
		}
		HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
		if (hit.getType() == HitResult.Type.ENTITY)
			this.onHitEntity((EntityHitResult) hit);
		Vec3 mv = this.getDeltaMovement();
		this.setPos(this.getX() + mv.x, this.getY() + mv.y, this.getZ() + mv.z);
		this.setYRot(this.getYRot() + 45.0F);
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		Entity target = result.getEntity();
		if (target == this.getOwner() || this.level().isClientSide())
			return;
		if (target instanceof LivingEntity) {
			target.hurt(this.damageSources().thrown(this, this.getOwner()), 5.0F);
			this.returning = true;
		}
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) && entity != this.getOwner();
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
	}
}
