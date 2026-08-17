package net.mcreator.vestigia;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.blocks.BlockStateParser;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.IntTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.storage.LevelResource;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

@EventBusSubscriber(modid = "vestigia")
public class VestigiaSchematic {
	private static final Map<UUID, BlockPos[]> SEL = new HashMap<>();
	private static final Map<UUID, StructureTemplate> CLIP = new HashMap<>();
	private static final int MAX_AXIS = 384;
	private static final long MAX_VOLUME = 6_000_000L;

	@SubscribeEvent
	public static void onRegisterCommands(RegisterCommandsEvent event) {
		var dispatcher = event.getDispatcher();
		for (String name : new String[]{"position1", "pos1"})
			dispatcher.register(Commands.literal(name).requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(c -> setPos(c, 0, null))
					.then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(c -> setPos(c, 0, BlockPosArgument.getBlockPos(c, "pos")))));
		for (String name : new String[]{"position2", "pos2"})
			dispatcher.register(Commands.literal(name).requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS)).executes(c -> setPos(c, 1, null))
					.then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(c -> setPos(c, 1, BlockPosArgument.getBlockPos(c, "pos")))));
		dispatcher.register(Commands.literal("save").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("nbt").then(Commands.argument("name", StringArgumentType.word()).executes(c -> save(c, "nbt"))))
				.then(Commands.literal("schematic").then(Commands.argument("name", StringArgumentType.word()).executes(c -> save(c, "schematic"))))
				.then(Commands.literal("schem").then(Commands.argument("name", StringArgumentType.word()).executes(c -> save(c, "schem")))));
		dispatcher.register(Commands.literal("load").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("schematic").then(Commands.argument("name", StringArgumentType.word())
						.suggests((c, b) -> SharedSuggestionProvider.suggest(listNames(c.getSource()), b)).executes(VestigiaSchematic::load))));
		dispatcher.register(Commands.literal("list").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("schematic").executes(VestigiaSchematic::list)));
		dispatcher.register(Commands.literal("paste").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
				.then(Commands.literal("schematic").then(Commands.literal("loaded").executes(VestigiaSchematic::paste))));
	}

	private static Path structuresDir(CommandSourceStack src) {
		return src.getServer().getWorldPath(LevelResource.ROOT).resolve("Structures");
	}

	private static List<String> listNames(CommandSourceStack src) {
		List<String> out = new ArrayList<>();
		Path dir = structuresDir(src);
		if (!Files.isDirectory(dir))
			return out;
		try (Stream<Path> s = Files.list(dir)) {
			s.forEach(p -> {
				String n = p.getFileName().toString();
				if (n.endsWith(".nbt") || n.endsWith(".schem") || n.endsWith(".schematic"))
					out.add(n.substring(0, n.lastIndexOf('.')));
			});
		} catch (Exception ignored) {
		}
		return out;
	}

	private static int list(CommandContext<CommandSourceStack> c) {
		List<String> names = listNames(c.getSource());
		if (names.isEmpty()) {
			c.getSource().sendSuccess(() -> Component.literal("§7No schematics in world §f/Structures§7."), false);
			return 0;
		}
		c.getSource().sendSuccess(() -> Component.literal("§6Schematics (" + names.size() + "): §f" + String.join("§7, §f", names)), false);
		return names.size();
	}

	private static int load(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
		CommandSourceStack src = c.getSource();
		ServerPlayer p = src.getPlayerOrException();
		ServerLevel level = src.getLevel();
		String name = StringArgumentType.getString(c, "name");
		Path dir = structuresDir(src);
		Path file = null;
		for (String ext : new String[]{"", ".nbt", ".schem", ".schematic"}) {
			Path cand = dir.resolve(name + ext);
			if (Files.isRegularFile(cand)) {
				file = cand;
				break;
			}
		}
		if (file == null) {
			src.sendFailure(Component.literal("§cNo schematic named §f" + name + "§c. Try §f/list schematic§c."));
			return 0;
		}
		try {
			CompoundTag raw;
			try (InputStream is = Files.newInputStream(file)) {
				raw = NbtIo.readCompressed(is, NbtAccounter.unlimitedHeap());
			}
			HolderLookup.RegistryLookup<Block> blocks = level.registryAccess().lookupOrThrow(Registries.BLOCK);
			CompoundTag spongeRoot = spongeRoot(raw);
			CompoundTag structureTag = spongeRoot != null ? spongeToStructure(spongeRoot, blocks) : raw;
			StructureTemplate template = new StructureTemplate();
			template.load(blocks, structureTag);
			CLIP.put(p.getUUID(), template);
			Vec3i sz = template.getSize();
			final Path f = file;
			src.sendSuccess(() -> Component.literal("§a✔ Loaded §e" + f.getFileName() + " §a(" + sz.getX() + "×" + sz.getY() + "×" + sz.getZ() + "). Use §f/paste schematic loaded"), false);
			return 1;
		} catch (Exception e) {
			src.sendFailure(Component.literal("§cLoad failed: " + e.getMessage()));
			return 0;
		}
	}

	private static int paste(CommandContext<CommandSourceStack> c) throws CommandSyntaxException {
		CommandSourceStack src = c.getSource();
		ServerPlayer p = src.getPlayerOrException();
		StructureTemplate template = CLIP.get(p.getUUID());
		if (template == null) {
			src.sendFailure(Component.literal("§cNothing loaded. Use §f/load schematic <name>§c first."));
			return 0;
		}
		ServerLevel level = src.getLevel();
		BlockPos origin = p.blockPosition();
		try {
			template.placeInWorld(level, origin, origin, new StructurePlaceSettings(), level.getRandom(), Block.UPDATE_CLIENTS);
			Vec3i sz = template.getSize();
			src.sendSuccess(() -> Component.literal("§a✔ Pasted (" + sz.getX() + "×" + sz.getY() + "×" + sz.getZ() + ") at your position."), true);
			return 1;
		} catch (Exception e) {
			src.sendFailure(Component.literal("§cPaste failed: " + e.getMessage()));
			return 0;
		}
	}

	private static CompoundTag spongeRoot(CompoundTag raw) {
		if (raw.contains("BlockData") && raw.contains("Palette"))
			return raw;
		CompoundTag inner = raw.getCompound("Schematic").orElse(null);
		if (inner != null && inner.contains("BlockData") && inner.contains("Palette"))
			return inner;
		return null;
	}

	private static CompoundTag spongeToStructure(CompoundTag s, HolderLookup.RegistryLookup<Block> blocks) throws CommandSyntaxException {
		int w = s.getShortOr("Width", (short) 0) & 0xFFFF;
		int h = s.getShortOr("Height", (short) 0) & 0xFFFF;
		int l = s.getShortOr("Length", (short) 0) & 0xFFFF;
		CompoundTag palTag = s.getCompound("Palette").orElseGet(CompoundTag::new);
		int paletteSize = palTag.size();
		String[] byIndex = new String[paletteSize];
		for (String key : palTag.keySet())
			byIndex[palTag.getIntOr(key, 0)] = key;
		ListTag paletteList = new ListTag();
		for (int i = 0; i < paletteSize; i++) {
			BlockState state = BlockStateParser.parseForBlock(blocks, byIndex[i] == null ? "minecraft:air" : byIndex[i], false).blockState();
			paletteList.add(NbtUtils.writeBlockState(state));
		}
		Map<Long, CompoundTag> beByPos = new HashMap<>();
		ListTag beList = s.getList("BlockEntities").orElseGet(ListTag::new);
		for (int i = 0; i < beList.size(); i++) {
			CompoundTag be = beList.getCompound(i).orElseGet(CompoundTag::new);
			int[] pos = be.getIntArray("Pos").orElse(new int[]{0, 0, 0});
			CompoundTag data = be.copy();
			data.remove("Pos");
			String id = data.getStringOr("Id", "");
			data.remove("Id");
			if (!id.isEmpty())
				data.putString("id", id);
			beByPos.put(packLocal(pos[0], pos[1], pos[2]), data);
		}
		byte[] blockData = s.getByteArray("BlockData").orElse(new byte[0]);
		int[] cursor = {0};
		ListTag blocksList = new ListTag();
		int total = w * h * l;
		for (int i = 0; i < total; i++) {
			int stateIdx = readVarInt(blockData, cursor);
			int x = i % w, z = (i / w) % l, y = i / (w * l);
			CompoundTag block = new CompoundTag();
			ListTag posList = new ListTag();
			posList.add(IntTag.valueOf(x));
			posList.add(IntTag.valueOf(y));
			posList.add(IntTag.valueOf(z));
			block.put("pos", posList);
			block.putInt("state", Math.max(0, Math.min(paletteSize - 1, stateIdx)));
			CompoundTag be = beByPos.get(packLocal(x, y, z));
			if (be != null)
				block.put("nbt", be);
			blocksList.add(block);
		}
		CompoundTag out = new CompoundTag();
		ListTag sizeList = new ListTag();
		sizeList.add(IntTag.valueOf(w));
		sizeList.add(IntTag.valueOf(h));
		sizeList.add(IntTag.valueOf(l));
		out.put("size", sizeList);
		out.put("palette", paletteList);
		out.put("blocks", blocksList);
		out.put("entities", new ListTag());
		NbtUtils.addCurrentDataVersion(out);
		return out;
	}

	private static long packLocal(int x, int y, int z) {
		return ((long) (x & 0x3FFFFF) << 42) | ((long) (z & 0x3FFFFF) << 20) | (y & 0xFFFFF);
	}

	private static int readVarInt(byte[] data, int[] cursor) {
		int value = 0, shift = 0, b;
		do {
			if (cursor[0] >= data.length)
				return 0;
			b = data[cursor[0]++] & 0xFF;
			value |= (b & 0x7F) << shift;
			shift += 7;
		} while ((b & 0x80) != 0);
		return value;
	}

	private static int setPos(CommandContext<CommandSourceStack> c, int idx, BlockPos pos) throws CommandSyntaxException {
		ServerPlayer p = c.getSource().getPlayerOrException();
		BlockPos bp = pos != null ? pos : p.blockPosition();
		SEL.computeIfAbsent(p.getUUID(), k -> new BlockPos[2])[idx] = bp;
		c.getSource().sendSuccess(() -> Component.literal("§aPosition " + (idx + 1) + " §7set to §f" + bp.getX() + ", " + bp.getY() + ", " + bp.getZ()), false);
		return 1;
	}

	private static int save(CommandContext<CommandSourceStack> c, String fmt) throws CommandSyntaxException {
		CommandSourceStack src = c.getSource();
		ServerPlayer p = src.getPlayerOrException();
		BlockPos[] sel = SEL.get(p.getUUID());
		if (sel == null || sel[0] == null || sel[1] == null) {
			src.sendFailure(Component.literal("§cSet §fposition1§c and §fposition2§c first."));
			return 0;
		}
		String name = StringArgumentType.getString(c, "name");
		ServerLevel level = src.getLevel();
		BlockPos a = sel[0], b = sel[1];
		BlockPos min = new BlockPos(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
		BlockPos max = new BlockPos(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
		int w = max.getX() - min.getX() + 1, h = max.getY() - min.getY() + 1, l = max.getZ() - min.getZ() + 1;
		if (w > MAX_AXIS || h > MAX_AXIS || l > MAX_AXIS || (long) w * h * l > MAX_VOLUME) {
			src.sendFailure(Component.literal("§cSelection too large (max " + MAX_AXIS + " per axis, " + (MAX_VOLUME / 1_000_000) + "M blocks)."));
			return 0;
		}
		try {
			Path dir = src.getServer().getWorldPath(LevelResource.ROOT).resolve("Structures");
			Files.createDirectories(dir);
			String safe = name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
			Path file;
			if (fmt.equals("schem")) {
				file = dir.resolve(safe + ".schem");
				saveSchem(level, min, w, h, l, file);
			} else if (fmt.equals("schematic")) {
				file = dir.resolve(safe + ".schematic");
				saveNbt(level, min, w, h, l, file);
			} else {
				file = dir.resolve(safe + ".nbt");
				saveNbt(level, min, w, h, l, file);
			}
			final Path saved = file;
			src.sendSuccess(() -> Component.literal("§a✔ Saved §e" + saved.getFileName() + " §a(" + w + "×" + h + "×" + l + ") to world §f/Structures"), true);
			return 1;
		} catch (Exception e) {
			src.sendFailure(Component.literal("§cSave failed: " + e.getMessage()));
			return 0;
		}
	}

	private static void saveNbt(ServerLevel level, BlockPos min, int w, int h, int l, Path file) throws Exception {
		StructureTemplate template = new StructureTemplate();
		template.fillFromWorld(level, min, new Vec3i(w, h, l), true, List.<Block>of());
		CompoundTag tag = template.save(new CompoundTag());
		NbtUtils.addCurrentDataVersion(tag);
		try (OutputStream os = Files.newOutputStream(file)) {
			NbtIo.writeCompressed(tag, os);
		}
	}

	private static void saveSchem(ServerLevel level, BlockPos min, int w, int h, int l, Path file) throws Exception {
		LinkedHashMap<String, Integer> palette = new LinkedHashMap<>();
		ByteArrayOutputStream blockData = new ByteArrayOutputStream();
		ListTag blockEntities = new ListTag();
		BlockPos.MutableBlockPos m = new BlockPos.MutableBlockPos();
		for (int y = 0; y < h; y++)
			for (int z = 0; z < l; z++)
				for (int x = 0; x < w; x++) {
					m.set(min.getX() + x, min.getY() + y, min.getZ() + z);
					BlockState state = level.getBlockState(m);
					String key = stateString(state);
					Integer id = palette.get(key);
					if (id == null) {
						id = palette.size();
						palette.put(key, id);
					}
					writeVarInt(blockData, id);
					BlockEntity be = level.getBlockEntity(m);
					if (be != null) {
						CompoundTag beTag = be.saveWithFullMetadata(level.registryAccess());
						beTag.remove("x");
						beTag.remove("y");
						beTag.remove("z");
						String beId = beTag.getStringOr("id", "");
						beTag.remove("id");
						beTag.putString("Id", beId);
						beTag.putIntArray("Pos", new int[]{x, y, z});
						blockEntities.add(beTag);
					}
				}
		CompoundTag root = new CompoundTag();
		root.putInt("Version", 2);
		root.putInt("DataVersion", currentDataVersion());
		root.putShort("Width", (short) w);
		root.putShort("Height", (short) h);
		root.putShort("Length", (short) l);
		root.putIntArray("Offset", new int[]{0, 0, 0});
		root.putInt("PaletteMax", palette.size());
		CompoundTag pal = new CompoundTag();
		for (Map.Entry<String, Integer> e : palette.entrySet())
			pal.putInt(e.getKey(), e.getValue());
		root.put("Palette", pal);
		root.putByteArray("BlockData", blockData.toByteArray());
		root.put("BlockEntities", blockEntities);
		try (OutputStream os = Files.newOutputStream(file)) {
			NbtIo.writeCompressed(root, os);
		}
	}

	private static String stateString(BlockState state) {
		Identifier id = BuiltInRegistries.BLOCK.getKey(state.getBlock());
		StringBuilder sb = new StringBuilder(id.toString());
		if (!state.getProperties().isEmpty()) {
			sb.append('[');
			boolean first = true;
			for (Property<?> prop : state.getProperties()) {
				if (!first)
					sb.append(',');
				first = false;
				sb.append(prop.getName()).append('=').append(propValue(state, prop));
			}
			sb.append(']');
		}
		return sb.toString();
	}

	private static <T extends Comparable<T>> String propValue(BlockState state, Property<T> prop) {
		return prop.getName(state.getValue(prop));
	}

	private static void writeVarInt(ByteArrayOutputStream out, int value) {
		while ((value & -128) != 0) {
			out.write(value & 127 | 128);
			value >>>= 7;
		}
		out.write(value);
	}

	private static int currentDataVersion() {
		CompoundTag dv = new CompoundTag();
		NbtUtils.addCurrentDataVersion(dv);
		return dv.getIntOr("DataVersion", 0);
	}
}
