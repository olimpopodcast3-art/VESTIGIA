package net.mcreator.vestigia.entity;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
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
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;

import java.util.List;

public class BronzeAxeEntity extends Projectile implements ItemSupplier {
	private static final EntityDataAccessor<ItemStack> DATA_ITEM = SynchedEntityData.defineId(BronzeAxeEntity.class, EntityDataSerializers.ITEM_STACK);
	private static final EntityDataAccessor<Boolean> DATA_STUCK = SynchedEntityData.defineId(BronzeAxeEntity.class, EntityDataSerializers.BOOLEAN);
	private static final float DAMAGE = 16.0F;
	private static final double RANGE = 5.0;
	private int age;
	private boolean returning;
	private Vec3 origin = Vec3.ZERO;

	public BronzeAxeEntity(EntityType<? extends BronzeAxeEntity> type, Level level) {
		super(type, level);
	}

	public BronzeAxeEntity(Level level, LivingEntity owner, ItemStack stack) {
		this(VestigiaEntities.BRONZE_AXE, level);
		this.setOwner(owner);
		this.setPos(owner.getX(), owner.getEyeY() - 0.1, owner.getZ());
		this.origin = this.position();
		this.setItem(stack);
		this.setYRot(owner.getYRot());
		this.setXRot(owner.getXRot());
		this.setDeltaMovement(owner.getLookAngle().scale(1.0));
	}

	@Override
	protected void defineSynchedData(SynchedEntityData.Builder builder) {
		builder.define(DATA_ITEM, new ItemStack(Items.AIR));
		builder.define(DATA_STUCK, false);
	}

	public void setItem(ItemStack stack) {
		this.entityData.set(DATA_ITEM, stack.copy());
	}

	@Override
	public ItemStack getItem() {
		return this.entityData.get(DATA_ITEM);
	}

	public boolean isStuck() {
		return this.entityData.get(DATA_STUCK);
	}

	private void setStuck(boolean value) {
		this.entityData.set(DATA_STUCK, value);
	}

	@Override
	public void tick() {
		super.tick();
		if (this.isStuck()) {
			this.setDeltaMovement(Vec3.ZERO);
			if (!this.level().isClientSide()) {
				List<Player> nearby = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(0.7));
				if (!nearby.isEmpty()) {
					Player picker = nearby.get(0);
					ItemStack back = this.getItem().copy();
					if (!picker.addItem(back))
						picker.drop(back, false);
					this.level().playSound(null, this.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
					this.discard();
				}
			}
			return;
		}
		this.age++;
		Entity owner = this.getOwner();
		if (!this.level().isClientSide() && this.age % 5 == 0)
			this.level().playSound(null, this.blockPosition(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.4F, 1.5F);
		if (!this.returning) {
			Vec3 v = this.getDeltaMovement();
			Vec3 perp = new Vec3(-v.z, 0.0, v.x);
			if (perp.lengthSqr() > 1.0E-4)
				this.setDeltaMovement(v.add(perp.normalize().scale(0.07)));
			if (this.position().distanceTo(this.origin) >= RANGE)
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
					this.level().playSound(null, this.blockPosition(), SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
					this.discard();
				}
				return;
			}
			this.setDeltaMovement(this.getDeltaMovement().scale(0.86).add(toOwner.normalize().scale(0.4)));
		}
		HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
		if (hit.getType() == HitResult.Type.ENTITY) {
			this.onHitEntity((EntityHitResult) hit);
		} else if (hit.getType() == HitResult.Type.BLOCK && !this.returning) {
			BlockHitResult bhr = (BlockHitResult) hit;
			if (this.level().getBlockState(bhr.getBlockPos()).is(BlockTags.LOGS)) {
				this.stickInLog(bhr);
				return;
			} else {
				this.returning = true;
			}
		}
		Vec3 mv = this.getDeltaMovement();
		this.setPos(this.getX() + mv.x, this.getY() + mv.y, this.getZ() + mv.z);
	}

	private void stickInLog(BlockHitResult bhr) {
		Vec3 loc = bhr.getLocation();
		this.setPos(loc.x, loc.y, loc.z);
		this.setDeltaMovement(Vec3.ZERO);
		this.setNoGravity(true);
		this.setStuck(true);
		if (!this.level().isClientSide())
			this.level().playSound(null, this.blockPosition(), SoundEvents.AXE_STRIP, SoundSource.PLAYERS, 0.9F, 0.8F);
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		Entity target = result.getEntity();
		if (target == this.getOwner() || this.level().isClientSide())
			return;
		if (target instanceof LivingEntity) {
			target.hurt(this.damageSources().thrown(this, this.getOwner()), DAMAGE);
			this.returning = true;
		}
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) && entity != this.getOwner();
	}

	@Override
	protected void readAdditionalSaveData(ValueInput input) {
		if (input.getBooleanOr("Stuck", false)) {
			this.setStuck(true);
			this.setNoGravity(true);
			this.setDeltaMovement(Vec3.ZERO);
		}
	}

	@Override
	protected void addAdditionalSaveData(ValueOutput output) {
		output.putBoolean("Stuck", this.isStuck());
	}
}
