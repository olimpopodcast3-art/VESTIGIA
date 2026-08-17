/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.vestigia.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.event.entity.RegisterSpawnPlacementsEvent;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

import net.mcreator.vestigia.entity.*;
import net.mcreator.vestigia.VestigiaMod;

@EventBusSubscriber
public class VestigiaModEntities {
	public static final DeferredRegister<EntityType<?>> REGISTRY = DeferredRegister.create(Registries.ENTITY_TYPE, VestigiaMod.MODID);
	public static final DeferredHolder<EntityType<?>, EntityType<PortadorEntity>> PORTADOR = register("portador",
			EntityType.Builder.<PortadorEntity>of(PortadorEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.ridingOffset(-0.6f).notInPeaceful().sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<GladiatorEntity>> GLADIATOR = register("gladiator",
			EntityType.Builder.<GladiatorEntity>of(GladiatorEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.ridingOffset(-0.6f).notInPeaceful().sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<TheZagalEntity>> THE_ZAGAL = register("the_zagal",
			EntityType.Builder.<TheZagalEntity>of(TheZagalEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.ridingOffset(-0.6f).notInPeaceful().sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<GoldenKnightEntity>> GOLDEN_KNIGHT = register("golden_knight",
			EntityType.Builder.<GoldenKnightEntity>of(GoldenKnightEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().ridingOffset(-0.6f).notInPeaceful().sized(0.7f, 2f));
	public static final DeferredHolder<EntityType<?>, EntityType<TlalocEntity>> TLALOC = register("tlaloc",
			EntityType.Builder.<TlalocEntity>of(TlalocEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().ridingOffset(-0.6f).notInPeaceful().sized(0.7f, 2f));
	public static final DeferredHolder<EntityType<?>, EntityType<MexicaEntity>> MEXICA = register("mexica",
			EntityType.Builder.<MexicaEntity>of(MexicaEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.ridingOffset(-0.6f).notInPeaceful().sized(0.5f, 2f));
	public static final DeferredHolder<EntityType<?>, EntityType<CaveManEntity>> CAVE_MAN = register("cave_man",
			EntityType.Builder.<CaveManEntity>of(CaveManEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.ridingOffset(-0.6f).notInPeaceful().sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<MummyEntity>> MUMMY = register("mummy",
			EntityType.Builder.<MummyEntity>of(MummyEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.ridingOffset(-0.6f).notInPeaceful().sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<PharaohEntity>> PHARAOH = register("pharaoh",
			EntityType.Builder.<PharaohEntity>of(PharaohEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3).fireImmune().ridingOffset(-0.6f).notInPeaceful().sized(0.6f, 1.8f));
	public static final DeferredHolder<EntityType<?>, EntityType<RomanEntity>> ROMAN = register("roman",
			EntityType.Builder.<RomanEntity>of(RomanEntity::new, MobCategory.MONSTER).setShouldReceiveVelocityUpdates(true).setTrackingRange(64).setUpdateInterval(3)

					.ridingOffset(-0.6f).notInPeaceful().sized(0.6f, 1.8f));

	// Start of user code block custom entities
	// End of user code block custom entities
	private static <T extends Entity> DeferredHolder<EntityType<?>, EntityType<T>> register(String registryname, EntityType.Builder<T> entityTypeBuilder) {
		return REGISTRY.register(registryname, () -> (EntityType<T>) entityTypeBuilder.build(ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(VestigiaMod.MODID, registryname))));
	}

	@SubscribeEvent
	public static void init(RegisterSpawnPlacementsEvent event) {
		PortadorEntity.init(event);
		GladiatorEntity.init(event);
		TheZagalEntity.init(event);
		GoldenKnightEntity.init(event);
		TlalocEntity.init(event);
		MexicaEntity.init(event);
		CaveManEntity.init(event);
		MummyEntity.init(event);
		PharaohEntity.init(event);
		RomanEntity.init(event);
	}

	@SubscribeEvent
	public static void registerAttributes(EntityAttributeCreationEvent event) {
		event.put(PORTADOR.get(), PortadorEntity.createAttributes().build());
		event.put(GLADIATOR.get(), GladiatorEntity.createAttributes().build());
		event.put(THE_ZAGAL.get(), TheZagalEntity.createAttributes().build());
		event.put(GOLDEN_KNIGHT.get(), GoldenKnightEntity.createAttributes().build());
		event.put(TLALOC.get(), TlalocEntity.createAttributes().build());
		event.put(MEXICA.get(), MexicaEntity.createAttributes().build());
		event.put(CAVE_MAN.get(), CaveManEntity.createAttributes().build());
		event.put(MUMMY.get(), MummyEntity.createAttributes().build());
		event.put(PHARAOH.get(), PharaohEntity.createAttributes().build());
		event.put(ROMAN.get(), RomanEntity.createAttributes().build());
	}
}