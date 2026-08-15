package net.mcreator.vestigia.client.renderer;

import net.minecraft.resources.Identifier;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ArmorModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;

import net.mcreator.vestigia.entity.GladiatorEntity;

public class GladiatorRenderer extends HumanoidMobRenderer<GladiatorEntity, HumanoidRenderState, HumanoidModel<HumanoidRenderState>> {
	private final Identifier entityTexture = Identifier.parse("vestigia:textures/entities/gladiator.png");

	public GladiatorRenderer(EntityRendererProvider.Context context) {
		super(context, new HumanoidModel<HumanoidRenderState>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
		this.addLayer(new HumanoidArmorLayer(this, ArmorModelSet.bake(ModelLayers.PLAYER_ARMOR, context.getModelSet(), HumanoidModel::new), context.getEquipmentRenderer()));
	}

	@Override
	public HumanoidRenderState createRenderState() {
		return new HumanoidRenderState();
	}

	@Override
	public void extractRenderState(GladiatorEntity entity, HumanoidRenderState state, float partialTicks) {
		super.extractRenderState(entity, state, partialTicks);
	}

	@Override
	public Identifier getTextureLocation(HumanoidRenderState state) {
		return entityTexture;
	}

	@Override
	protected RenderType getRenderType(HumanoidRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
		return RenderTypes.entityTranslucent(getTextureLocation(state));
	}

	@Override
	protected int getModelTint(HumanoidRenderState state) {
		return 0xB0CFEFFF;
	}
}