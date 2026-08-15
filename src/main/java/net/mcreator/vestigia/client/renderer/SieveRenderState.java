package net.mcreator.vestigia.client.renderer;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.item.ItemStackRenderState;

import java.util.Collections;
import java.util.List;

public class SieveRenderState extends BlockEntityRenderState {
	public List<ItemStackRenderState> items = Collections.emptyList();
}
