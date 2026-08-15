package net.mcreator.vestigia.client.renderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class BronzeAxeRenderState extends EntityRenderState {
	public final ItemStackRenderState item = new ItemStackRenderState();
	public float spin;
	public float yaw;
	public float pitch;
	public boolean stuck;
}
