package net.mcreator.vestigia.client;

import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

public class PowerUpSound extends AbstractTickableSoundInstance {
	private static final int MAX = 2300;
	private static final int FADE_IN = 50;
	private static final int FADE_OUT = 60;
	private static final float TARGET = 0.9F;
	private int age = 0;

	public PowerUpSound() {
		super(SoundEvent.createVariableRangeEvent(Identifier.parse("vestigia:power_up")), SoundSource.RECORDS, RandomSource.create());
		this.looping = false;
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
		age++;
		if (age >= MAX) {
			this.stop();
			return;
		}
		if (age < FADE_IN)
			this.volume = TARGET * (age / (float) FADE_IN);
		else if (age > MAX - FADE_OUT)
			this.volume = TARGET * ((MAX - age) / (float) FADE_OUT);
		else
			this.volume = TARGET;
	}
}
