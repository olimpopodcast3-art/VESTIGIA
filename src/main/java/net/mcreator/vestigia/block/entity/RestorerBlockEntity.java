package net.mcreator.vestigia.block.entity;

import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import net.mcreator.vestigia.world.inventory.RestorerGUIMenu;
import net.mcreator.vestigia.init.VestigiaModBlockEntities;

import javax.annotation.Nullable;

import java.util.stream.IntStream;

import io.netty.buffer.Unpooled;

public class RestorerBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
	public static final int PROGRESS_MAX = 200;
	private static final String CHISEL = "vestigia:chisel";

	private record Recipe(String a, String b, String out) {
	}

	private static final Recipe[] RECIPES = {new Recipe("vestigia:broken_metal_hoe", "vestigia:metal_splinter", "vestigia:metal_hoe"),
			new Recipe("vestigia:broken_ash_pickaxe", "vestigia:ash_splinter", "vestigia:ash_pickaxe"), new Recipe("vestigia:broken_ash_sword", "vestigia:ash_splinter", "vestigia:ash_sword"),
			new Recipe("minecraft:flint", "vestigia:flint_splinter", "vestigia:cracked_flint"), new Recipe("vestigia:repaired_flint", "", "vestigia:worked_flint"),
			new Recipe("minecraft:diamond_axe", "vestigia:worked_flint", "vestigia:flint_axe"), new Recipe("vestigia:iron_shard", "", "vestigia:iron_dagger"),
			new Recipe("vestigia:coal_hoe_broken", "vestigia:coal_splinter", "vestigia:coal_hoe"), new Recipe("vestigia:aurora_knife_broken", "vestigia:aurora_splinter", "vestigia:aurora_knife"),
			new Recipe("minecraft:totem_of_undying", "vestigia:mythic_splinter", "vestigia:capibara_of_wisdom"), new Recipe("vestigia:uncharged_golden_staff", "vestigia:mythic_splinter", "vestigia:golden_staff"),
			new Recipe("vestigia:hook_broken", "vestigia:oxidized_bronze_splinter", "vestigia:hook"),
			new Recipe("vestigia:percussion_hammer_broken", "vestigia:oxidized_bronze_splinter", "vestigia:percussion_hammer"),
			new Recipe("vestigia:stone_mason_pick_broken", "vestigia:oxidized_bronze_splinter", "vestigia:stone_mason_pickaxe"),
			new Recipe("minecraft:golden_sword", "vestigia:gold_splinter", "vestigia:golden_sword"),
			new Recipe("vestigia:uncharged_ruby_blade_broken", "vestigia:ruby", "vestigia:uncharged_ruby_blade"),
			new Recipe("vestigia:uncharged_ruby_blade", "vestigia:ruby_splinter", "vestigia:ruby_blade"),
			new Recipe("minecraft:ender_eye", "vestigia:ender_splinter", "vestigia:echo"),
			new Recipe("vestigia:empty_water_sword", "vestigia:water_splinter", "vestigia:water_element_sword"),
			new Recipe("vestigia:refugebellbroken", "vestigia:oxidized_bronze_splinter", "vestigia:refuge_bell")};

	private NonNullList<ItemStack> stacks = NonNullList.withSize(4, ItemStack.EMPTY);
	private int progress = 0;

	private final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int id) {
			return id == 0 ? RestorerBlockEntity.this.progress : 0;
		}

		@Override
		public void set(int id, int value) {
			if (id == 0)
				RestorerBlockEntity.this.progress = value;
		}

		@Override
		public int getCount() {
			return 1;
		}
	};

	public RestorerBlockEntity(BlockPos position, BlockState state) {
		super(VestigiaModBlockEntities.RESTORER.get(), position, state);
	}

	public ContainerData getDataAccess() {
		return this.dataAccess;
	}

	public NonNullList<ItemStack> getContents() {
		return this.stacks;
	}

	@Override
	public void loadAdditional(ValueInput valueInput) {
		super.loadAdditional(valueInput);
		if (!this.tryLoadLootTable(valueInput))
			this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
		ContainerHelper.loadAllItems(valueInput, this.stacks);
		this.progress = valueInput.getIntOr("Progress", 0);
	}

	@Override
	public void saveAdditional(ValueOutput valueOutput) {
		super.saveAdditional(valueOutput);
		if (!this.trySaveLootTable(valueOutput))
			ContainerHelper.saveAllItems(valueOutput, this.stacks);
		valueOutput.putInt("Progress", this.progress);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, RestorerBlockEntity be) {
		Recipe recipe = be.currentRecipe();
		if (recipe == null || !be.stacks.get(3).isEmpty()) {
			if (be.progress != 0) {
				be.progress = 0;
				be.setChanged();
			}
			return;
		}
		be.progress++;
		if (level instanceof ServerLevel sl) {
			if (level.getGameTime() % 30 == 0)
				sl.playSound(null, pos, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.7F, 1.0F);
			if (level.getGameTime() % 15 == 0)
				sl.sendParticles(ParticleTypes.CRIT, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 3, 0.2, 0.05, 0.2, 0.02);
		}
		if (be.progress >= PROGRESS_MAX) {
			be.progress = 0;
			if (!be.stacks.get(0).isEmpty())
				be.stacks.get(0).shrink(1);
			if (!be.stacks.get(1).isEmpty())
				be.stacks.get(1).shrink(1);
			damageChisel(be.stacks.get(2), be);
			be.stacks.set(3, new ItemStack(itemOf(recipe.out)));
			be.setChanged();
			if (level instanceof ServerLevel sl) {
				sl.playSound(null, pos, SoundEvents.ANVIL_LAND, SoundSource.BLOCKS, 0.8F, 1.2F);
				sl.sendParticles(ParticleTypes.GLOW, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 20, 0.3, 0.2, 0.3, 0.05);
				Player p = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 32.0, false);
				if (p != null)
					p.sendSystemMessage(Component.literal("The restoration was completed"));
			}
		}
	}

	private static void damageChisel(ItemStack chisel, RestorerBlockEntity be) {
		if (chisel.isEmpty() || !chisel.isDamageableItem())
			return;
		int dmg = chisel.getDamageValue() + 1;
		if (dmg >= chisel.getMaxDamage())
			be.stacks.set(2, ItemStack.EMPTY);
		else
			chisel.setDamageValue(dmg);
	}

	@Nullable
	private Recipe currentRecipe() {
		if (!CHISEL.equals(idOf(this.stacks.get(2))))
			return null;
		String a = idOf(this.stacks.get(0));
		String b = idOf(this.stacks.get(1));
		for (Recipe r : RECIPES) {
			if (r.b.isEmpty()) {
				if ((r.a.equals(a) && b.isEmpty()) || (r.a.equals(b) && a.isEmpty()))
					return r;
			} else if ((r.a.equals(a) && r.b.equals(b)) || (r.a.equals(b) && r.b.equals(a))) {
				return r;
			}
		}
		return null;
	}

	private static String idOf(ItemStack stack) {
		return stack.isEmpty() ? "" : BuiltInRegistries.ITEM.getKey(stack.getItem()).toString();
	}

	private static Item itemOf(String id) {
		return BuiltInRegistries.ITEM.getValue(Identifier.parse(id));
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider lookupProvider) {
		return this.saveWithFullMetadata(lookupProvider);
	}

	@Override
	public int getContainerSize() {
		return stacks.size();
	}

	@Override
	public boolean isEmpty() {
		for (ItemStack itemstack : this.stacks)
			if (!itemstack.isEmpty())
				return false;
		return true;
	}

	@Override
	public Component getDefaultName() {
		return Component.literal("restorer");
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory) {
		return new RestorerGUIMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("Restorer");
	}

	@Override
	protected NonNullList<ItemStack> getItems() {
		return this.stacks;
	}

	@Override
	protected void setItems(NonNullList<ItemStack> stacks) {
		this.stacks = stacks;
	}

	@Override
	public boolean canPlaceItem(int index, ItemStack stack) {
		return index <= 2;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return side == Direction.UP ? new int[]{0, 1, 2} : new int[]{3};
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack itemstack, @Nullable Direction direction) {
		return this.canPlaceItem(index, itemstack);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack itemstack, Direction direction) {
		return index == 3;
	}
}
