package net.mcreator.vestigia.client;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import net.minecraft.client.renderer.entity.ThrownItemRenderer;

import net.mcreator.vestigia.client.renderer.SieveRenderer;
import net.mcreator.vestigia.client.renderer.BoomerangRenderer;
import net.mcreator.vestigia.client.renderer.PhantomSpearRenderer;
import net.mcreator.vestigia.client.renderer.BronzeAxeRenderer;
import net.mcreator.vestigia.entity.VestigiaEntities;
import net.mcreator.vestigia.init.VestigiaModBlockEntities;

@EventBusSubscriber(modid = "vestigia", value = Dist.CLIENT)
public class VestigiaClientEvents {
	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(VestigiaModBlockEntities.SIEVE.get(), SieveRenderer::new);
		event.registerEntityRenderer(VestigiaEntities.BOOMERANG, BoomerangRenderer::new);
		event.registerEntityRenderer(VestigiaEntities.PEBBLE, context -> new ThrownItemRenderer<>(context, 0.9F, false));
		event.registerEntityRenderer(VestigiaEntities.PHANTOM_SPEAR, PhantomSpearRenderer::new);
		event.registerEntityRenderer(VestigiaEntities.BRONZE_AXE, BronzeAxeRenderer::new);
	}
}
