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
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.util.RandomSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.NonNullList;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import net.mcreator.vestigia.world.inventory.SieveGUIMenu;
import net.mcreator.vestigia.init.VestigiaModBlockEntities;

import javax.annotation.Nullable;

import io.netty.buffer.Unpooled;

public class SieveBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
	private static final int SIZE = 6;
	private static final int COST = 3;
	public static final int PROGRESS_MAX = 100;
	private static final int MAX_EXTRA_ROLLS = 4;
	private static final int ROLL_PER_SEDIMENT = 12;

	private record Roll(String id, int weight) {
	}

	private static final Roll[] ASH = {new Roll("vestigia:metal_splinter", 50), new Roll("vestigia:coal_splinter", 20), new Roll("vestigia:iron_shard", 10), new Roll("vestigia:ash_splinter", 10),
			new Roll("vestigia:flint_splinter", 10)};
	private static final Roll[] MUD = {new Roll("vestigia:ceramic_fragment", 50), new Roll("vestigia:diamond_splinter", 15), new Roll("vestigia:obsidian_splinter", 15), new Roll("vestigia:water_splinter", 10),
			new Roll("vestigia:gold_splinter", 10)};
	private static final Roll[] BRONZE = {new Roll("vestigia:oxidized_bronze_splinter", 50), new Roll("vestigia:ender_splinter", 20), new Roll("vestigia:ruby_splinter", 15), new Roll("vestigia:aurora_splinter", 10),
			new Roll("vestigia:mythic_splinter", 5)};

	private NonNullList<ItemStack> stacks = NonNullList.withSize(SIZE, ItemStack.EMPTY);
	private int progress = 0;

	private final ContainerData dataAccess = new ContainerData() {
		@Override
		public int get(int id) {
			return id == 0 ? SieveBlockEntity.this.progress : 0;
		}

		@Override
		public void set(int id, int value) {
			if (id == 0)
				SieveBlockEntity.this.progress = value;
		}

		@Override
		public int getCount() {
			return 1;
		}
	};

	public SieveBlockEntity(BlockPos position, BlockState state) {
		super(VestigiaModBlockEntities.SIEVE.get(), position, state);
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

	public static void serverTick(Level level, BlockPos pos, BlockState state, SieveBlockEntity be) {
		int out = be.firstEmptyOutput();
		int in = be.pickInput(level.getRandom());
		if (out < 0 || in < 0) {
			if (be.progress != 0) {
				be.progress = 0;
				be.setChanged();
			}
			return;
		}
		be.progress++;
		if (level instanceof ServerLevel sl) {
			if (level.getGameTime() % 4 == 0)
				sl.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5, pos.getY() + 1.05, pos.getZ() + 0.5, 2, 0.18, 0.02, 0.18, 0.0);
			if (level.getGameTime() % 10 == 0)
				sl.playSound(null, pos, SoundEvents.BRUSH_GRAVEL, SoundSource.BLOCKS, 0.9F, 1.0F);
		}
		if (be.progress >= PROGRESS_MAX) {
			be.progress = 0;
			ItemStack input = be.stacks.get(in);
			int rolls = 1 + Math.min(MAX_EXTRA_ROLLS, (input.getCount() - COST) / ROLL_PER_SEDIMENT);
			ItemStack result = rollBest(tableFor(input), level.getRandom(), rolls);
			input.shrink(COST);
			be.stacks.set(out, result);
			be.setChanged();
		}
	}

	private int firstEmptyOutput() {
		for (int i = 3; i < SIZE; i++)
			if (this.stacks.get(i).isEmpty())
				return i;
		return -1;
	}

	private int pickInput(RandomSource rand) {
		int[] eligible = new int[3];
		int n = 0;
		for (int i = 0; i < 3; i++) {
			ItemStack s = this.stacks.get(i);
			if (s.getCount() >= COST && tableFor(s) != null)
				eligible[n++] = i;
		}
		return n == 0 ? -1 : eligible[rand.nextInt(n)];
	}

	@Nullable
	private static Roll[] tableFor(ItemStack s) {
		if (s.isEmpty())
			return null;
		Identifier id = BuiltInRegistries.ITEM.getKey(s.getItem());
		return switch (id.toString()) {
			case "vestigia:ash_sediment" -> ASH;
			case "vestigia:mud_sediment" -> MUD;
			case "vestigia:bronze_sediment" -> BRONZE;
			default -> null;
		};
	}

	private static ItemStack rollBest(Roll[] table, RandomSource rand, int rolls) {
		Roll best = null;
		for (int i = 0; i < rolls; i++) {
			Roll r = rollOne(table, rand);
			if (best == null || r.weight() < best.weight())
				best = r;
		}
		Item item = BuiltInRegistries.ITEM.getValue(Identifier.parse(best.id()));
		return new ItemStack(item);
	}

	private static Roll rollOne(Roll[] table, RandomSource rand) {
		int total = 0;
		for (Roll r : table)
			total += r.weight();
		int pick = rand.nextInt(total);
		for (Roll r : table) {
			pick -= r.weight();
			if (pick < 0)
				return r;
		}
		return table[0];
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
		return Component.literal("sieve");
	}

	@Override
	public AbstractContainerMenu createMenu(int id, Inventory inventory) {
		return new SieveGUIMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(this.worldPosition));
	}

	@Override
	public Component getDisplayName() {
		return Component.literal("Sieve");
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
		return index < 3 && tableFor(stack) != null;
	}

	@Override
	public int[] getSlotsForFace(Direction side) {
		return side == Direction.UP ? new int[]{0, 1, 2} : new int[]{3, 4, 5};
	}

	@Override
	public boolean canPlaceItemThroughFace(int index, ItemStack itemstack, @Nullable Direction direction) {
		return this.canPlaceItem(index, itemstack);
	}

	@Override
	public boolean canTakeItemThroughFace(int index, ItemStack itemstack, Direction direction) {
		return index >= 3;
	}
}
