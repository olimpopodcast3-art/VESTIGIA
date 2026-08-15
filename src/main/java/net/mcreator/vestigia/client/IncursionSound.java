package net.mcreator.vestigia.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class IncursionSound extends AbstractTickableSoundInstance {
	private static final int FADE = 40;
	private static final float TARGET = 1.0F;
	private int age = 0;

	public IncursionSound() {
		super(SoundEvent.createVariableRangeEvent(Identifier.parse("vestigia:incursion")), SoundSource.RECORDS, RandomSource.create());
		this.looping = true;
		this.relative = true;
		this.attenuation = SoundInstance.Attenuation.NONE;
		this.volume = 0.0F;
		this.x = 0.0;
		this.y = 0.0;
		this.z = 0.0;
	}

	@Override
	public boolean canStartSilent() {
		return true;
	}

	@Override
	public void tick() {
		if (!VestigiaIncursionClient.isActive()) {
			this.stop();
			return;
		}
		age++;
		this.volume = age < FADE ? TARGET * (age / (float) FADE) : TARGET;
	}
}
