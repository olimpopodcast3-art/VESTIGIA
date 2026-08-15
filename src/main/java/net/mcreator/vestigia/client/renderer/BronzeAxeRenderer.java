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

import net.mcreator.vestigia.entity.BronzeAxeEntity;

public class BronzeAxeRenderer extends EntityRenderer<BronzeAxeEntity, BronzeAxeRenderState> {
	private final ItemModelResolver itemModelResolver;

	public BronzeAxeRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemModelResolver = context.getItemModelResolver();
	}

	@Override
	public BronzeAxeRenderState createRenderState() {
		return new BronzeAxeRenderState();
	}

	@Override
	public void extractRenderState(BronzeAxeEntity entity, BronzeAxeRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		this.itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.GROUND, entity);
		state.stuck = entity.isStuck();
		state.spin = (entity.tickCount + partialTicks) * 45.0F;
		state.yaw = entity.getYRot(partialTicks);
		state.pitch = entity.getXRot(partialTicks);
	}

	@Override
	public void submit(BronzeAxeRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		if (state.stuck) {
			poseStack.translate(0.0F, 0.15F, 0.0F);
			poseStack.mulPose(Axis.YP.rotationDegrees(-state.yaw));
			poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch));
			poseStack.mulPose(Axis.ZP.rotationDegrees(135.0F));
			poseStack.scale(1.3F, 1.3F, 1.3F);
		} else {
			poseStack.translate(0.0F, 0.15F, 0.0F);
			poseStack.mulPose(Axis.YP.rotationDegrees(-state.yaw));
			poseStack.mulPose(Axis.XP.rotationDegrees(state.pitch));
			poseStack.mulPose(Axis.YP.rotationDegrees(-state.spin));
			poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
			poseStack.scale(1.25F, 1.25F, 1.25F);
		}
		state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
		super.submit(state, poseStack, submitNodeCollector, camera);
	}
}
