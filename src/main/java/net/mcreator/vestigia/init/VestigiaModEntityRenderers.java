/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.vestigia.init;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;

import net.mcreator.vestigia.client.renderer.*;

@EventBusSubscriber(Dist.CLIENT)
public class VestigiaModEntityRenderers {
	@SubscribeEvent
	public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerEntityRenderer(VestigiaModEntities.PORTADOR.get(), PortadorRenderer::new);
		event.registerEntityRenderer(VestigiaModEntities.GLADIATOR.get(), GladiatorRenderer::new);
		event.registerEntityRenderer(VestigiaModEntities.THE_ZAGAL.get(), TheZagalRenderer::new);
		event.registerEntityRenderer(VestigiaModEntities.GOLDEN_KNIGHT.get(), GoldenKnightRenderer::new);
		event.registerEntityRenderer(VestigiaModEntities.TLALOC.get(), TlalocRenderer::new);
		event.registerEntityRenderer(VestigiaModEntities.MEXICA.get(), MexicaRenderer::new);
		event.registerEntityRenderer(VestigiaModEntities.CAVE_MAN.get(), CaveManRenderer::new);
		event.registerEntityRenderer(VestigiaModEntities.MUMMY.get(), MummyRenderer::new);
		event.registerEntityRenderer(VestigiaModEntities.PHARAOH.get(), PharaohRenderer::new);
		event.registerEntityRenderer(VestigiaModEntities.ROMAN.get(), RomanRenderer::new);
	}
}