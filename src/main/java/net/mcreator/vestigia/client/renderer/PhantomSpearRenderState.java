package net.mcreator.vestigia.client.renderer;

import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

public class PhantomSpearRenderState extends EntityRenderState {
	public final ItemStackRenderState item = new ItemStackRenderState();
	public float yRot;
	public float xRot;
}
