package net.mcreator.vestigia.client;

import net.minecraft.client.Minecraft;

public class VestigiaClientPayloads {
	public static void playPowerUp() {
		Minecraft mc = Minecraft.getInstance();
		if (mc.player != null)
			mc.getSoundManager().play(new PowerUpSound());
	}
}
