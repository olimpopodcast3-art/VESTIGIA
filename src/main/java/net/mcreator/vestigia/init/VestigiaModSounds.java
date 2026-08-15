/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.vestigia.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.Identifier;
import net.minecraft.core.registries.Registries;

import net.mcreator.vestigia.VestigiaMod;

public class VestigiaModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(Registries.SOUND_EVENT, VestigiaMod.MODID);
	public static final DeferredHolder<SoundEvent, SoundEvent> BELL_BONG = REGISTRY.register("bell_bong", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("vestigia", "bell_bong")));
	public static final DeferredHolder<SoundEvent, SoundEvent> POWER_UP = REGISTRY.register("power_up", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("vestigia", "power_up")));
	public static final DeferredHolder<SoundEvent, SoundEvent> INCURSION = REGISTRY.register("incursion", () -> SoundEvent.createVariableRangeEvent(Identifier.fromNamespaceAndPath("vestigia", "incursion")));
}