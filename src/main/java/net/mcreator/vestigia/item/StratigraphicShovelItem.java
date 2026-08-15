package net.mcreator.vestigia.item;

import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.TagKey;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.phys.Vec3;

import net.mcreator.vestigia.block.AshDepositBlock;
import net.mcreator.vestigia.block.BronzeDepositBlock;
import net.mcreator.vestigia.block.MudDepositBlock;

import java.util.List;

public class StratigraphicShovelItem extends ShovelItem {
	private static final ToolMaterial TOOL_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_DIAMOND_TOOL, 500, 7.5f, 0, 22, TagKey.create(Registries.ITEM, Identifier.parse("vestigia:stratigraphic_shovel_repair_items")));

	public StratigraphicShovelItem(Item.Properties properties) {
		super(TOOL_MATERIAL, 4f, -2.5f, properties.rarity(Rarity.RARE));
	}

	private static boolean isDeposit(Block block) {
		return block instanceof AshDepositBlock || block instanceof BronzeDepositBlock || block instanceof MudDepositBlock;
	}

	private static ItemStack contentOf(ServerLevel level, BlockPos pos, ItemStack tool, LivingEntity user) {
		Block block = level.getBlockState(pos).getBlock();
		Identifier id = BuiltInRegistries.BLOCK.getKey(block);
		ResourceKey<LootTable> key = ResourceKey.create(Registries.LOOT_TABLE, Identifier.parse(id.getNamespace() + ":brushing/" + id.getPath()));
		LootTable table = level.getServer().reloadableRegistries().getLootTable(key);
		LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.ORIGIN, Vec3.atCenterOf(pos)).withLuck(user.getLuck()).withParameter(LootContextParams.THIS_ENTITY, user)
				.withParameter(LootContextParams.TOOL, tool).create(LootContextParamSets.ARCHAEOLOGY);
		List<ItemStack> loot = table.getRandomItems(params, pos.asLong());
		return loot.isEmpty() ? ItemStack.EMPTY : loot.get(0);
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		Level level = context.getLevel();
		BlockPos pos = context.getClickedPos();
		if (!isDeposit(level.getBlockState(pos).getBlock()))
			return super.useOn(context);
		Player player = context.getPlayer();
		if (player != null && level instanceof ServerLevel server) {
			ItemStack content = contentOf(server, pos, context.getItemInHand(), player);
			if (!content.isEmpty())
				player.sendOverlayMessage(Component.literal("Inside: ").append(content.getHoverName()));
		}
		return InteractionResult.SUCCESS;
	}
}
