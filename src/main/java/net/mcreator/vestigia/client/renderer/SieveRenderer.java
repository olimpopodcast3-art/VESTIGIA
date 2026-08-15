package net.mcreator.vestigia.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import org.jspecify.annotations.Nullable;

import net.mcreator.vestigia.block.entity.SieveBlockEntity;

import java.util.ArrayList;
import java.util.List;

public class SieveRenderer implements BlockEntityRenderer<SieveBlockEntity, SieveRenderState> {
	private static final float[] XS = {0.30F, 0.50F, 0.70F};
	private final ItemModelResolver itemModelResolver;

	public SieveRenderer(BlockEntityRendererProvider.Context context) {
		this.itemModelResolver = context.itemModelResolver();
	}

	@Override
	public SieveRenderState createRenderState() {
		return new SieveRenderState();
	}

	@Override
	public void extractRenderState(SieveBlockEntity blockEntity, SieveRenderState state, float partialTicks, Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
		BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
		int seed = (int) blockEntity.getBlockPos().asLong();
		state.items = new ArrayList<>();
		NonNullList<ItemStack> contents = blockEntity.getContents();
		for (int i = 0; i < contents.size(); i++) {
			ItemStackRenderState itemState = new ItemStackRenderState();
			this.itemModelResolver.updateForTopItem(itemState, contents.get(i), ItemDisplayContext.FIXED, blockEntity.getLevel(), null, seed + i);
			state.items.add(itemState);
		}
	}

	@Override
	public void submit(SieveRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		List<ItemStackRenderState> items = state.items;
		for (int i = 0; i < items.size(); i++) {
			ItemStackRenderState itemState = items.get(i);
			if (itemState.isEmpty())
				continue;
			float x = XS[i % 3];
			float z = i < 3 ? 0.35F : 0.65F;
			poseStack.pushPose();
			poseStack.translate(x, 1.03F, z);
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			poseStack.scale(0.30F, 0.30F, 0.30F);
			itemState.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
			poseStack.popPose();
		}
	}
}
