package net.mcreator.vestigia.client;

import com.mojang.serialization.MapCodec;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterConditionalItemModelPropertyEvent;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

@EventBusSubscriber(modid = "vestigia", value = Dist.CLIENT)
public class VestigiaGoldenSwordClient {
	public record GoldChargedProperty() implements ConditionalItemModelProperty {
		public static final MapCodec<GoldChargedProperty> MAP_CODEC = MapCodec.unit(new GoldChargedProperty());

		@Override
		public boolean get(ItemStack itemStack, ClientLevel level, LivingEntity owner, int seed, ItemDisplayContext displayContext) {
			CustomData cd = itemStack.get(DataComponents.CUSTOM_DATA);
			return cd != null && cd.copyTag().getIntOr("gs_charged", 0) == 1;
		}

		@Override
		public MapCodec<GoldChargedProperty> type() {
			return MAP_CODEC;
		}
	}

	public record GoldSeal2Property() implements ConditionalItemModelProperty {
		public static final MapCodec<GoldSeal2Property> MAP_CODEC = MapCodec.unit(new GoldSeal2Property());

		@Override
		public boolean get(ItemStack itemStack, ClientLevel level, LivingEntity owner, int seed, ItemDisplayContext displayContext) {
			CustomData cd = itemStack.get(DataComponents.CUSTOM_DATA);
			return cd != null && cd.copyTag().getIntOr("vestigia_seal", 0) >= 2;
		}

		@Override
		public MapCodec<GoldSeal2Property> type() {
			return MAP_CODEC;
		}
	}

	@SubscribeEvent
	public static void registerChargedProperty(RegisterConditionalItemModelPropertyEvent event) {
		event.register(Identifier.parse("vestigia:gs_charged"), GoldChargedProperty.MAP_CODEC);
		event.register(Identifier.parse("vestigia:gs_seal2"), GoldSeal2Property.MAP_CODEC);
	}
}
