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

import net.mcreator.vestigia.entity.PhantomSpearProjectile;

public class PhantomSpearRenderer extends EntityRenderer<PhantomSpearProjectile, PhantomSpearRenderState> {
	private static final float TEXTURE_ANGLE = 45.0F;
	private final ItemModelResolver itemModelResolver;

	public PhantomSpearRenderer(EntityRendererProvider.Context context) {
		super(context);
		this.itemModelResolver = context.getItemModelResolver();
	}

	@Override
	public PhantomSpearRenderState createRenderState() {
		return new PhantomSpearRenderState();
	}

	@Override
	public void extractRenderState(PhantomSpearProjectile entity, PhantomSpearRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
		this.itemModelResolver.updateForNonLiving(state.item, entity.getItem(), ItemDisplayContext.GROUND, entity);
		state.yRot = entity.getYRot(partialTicks);
		state.xRot = entity.getXRot(partialTicks);
	}

	@Override
	public void submit(PhantomSpearRenderState state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera) {
		poseStack.pushPose();
		poseStack.mulPose(Axis.YP.rotationDegrees(state.yRot - 90.0F));
		poseStack.mulPose(Axis.ZP.rotationDegrees(state.xRot - 90.0F + TEXTURE_ANGLE));
		poseStack.scale(1.6F, 1.6F, 1.6F);
		state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.outlineColor);
		poseStack.popPose();
		super.submit(state, poseStack, submitNodeCollector, camera);
	}
}
