package net.mcreator.vestigia.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

public class VestigiaEntities {
	public static final ResourceKey<EntityType<?>> BOOMERANG_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.parse("vestigia:boomerang"));
	public static final EntityType<BoomerangEntity> BOOMERANG = EntityType.Builder.<BoomerangEntity>of(BoomerangEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(6).updateInterval(1)
			.build(BOOMERANG_KEY);

	public static final ResourceKey<EntityType<?>> PEBBLE_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.parse("vestigia:pebble"));
	public static final EntityType<PebbleEntity> PEBBLE = EntityType.Builder.<PebbleEntity>of(PebbleEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(4).updateInterval(1).build(PEBBLE_KEY);

	public static final ResourceKey<EntityType<?>> PHANTOM_SPEAR_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.parse("vestigia:phantom_spear_projectile"));
	public static final EntityType<PhantomSpearProjectile> PHANTOM_SPEAR = EntityType.Builder.<PhantomSpearProjectile>of(PhantomSpearProjectile::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(8).updateInterval(1).build(PHANTOM_SPEAR_KEY);

	public static final ResourceKey<EntityType<?>> BRONZE_AXE_KEY = ResourceKey.create(Registries.ENTITY_TYPE, Identifier.parse("vestigia:bronze_axe"));
	public static final EntityType<BronzeAxeEntity> BRONZE_AXE = EntityType.Builder.<BronzeAxeEntity>of(BronzeAxeEntity::new, MobCategory.MISC).sized(0.5F, 0.5F).clientTrackingRange(6).updateInterval(1).build(BRONZE_AXE_KEY);
}
