package net.mcreator.vestigia.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;

import net.mcreator.vestigia.entity.BoomerangEntity;

public class BoomerangRenderer extends EntityRenderer<BoomerangEntity, BoomerangRenderState> {
	private final ItemModelResolver itemModelResolver;

	public BoomerangRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemModelResolver = context.getItemModelResolver();
	}

	@Override
	public BoomerangRenderState createRenderState() {
		return new BoomerangRenderState();
	}

	@Override
	public void extractRenderState(BoomerangEntity entity, BoomerangRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		this.itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.GROUND, entity);
		state.spin = (entity.tickCount + partialTicks) * 45.0F;
	}

	@Override
	public void submit(BoomerangRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.translate(0.0F, 0.15F, 0.0F);
		poseStack.mulPose(Axis.YP.rotationDegrees(state.spin));
		poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
		poseStack.scale(1.1F, 1.1F, 1.1F);
		state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
		super.submit(state, poseStack, submitNodeCollector, camera);
	}
}
