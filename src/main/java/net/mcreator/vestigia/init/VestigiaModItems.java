/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.vestigia.init;

import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.mcreator.vestigia.item.inventory.VestigiaBookInventoryCapability;
import net.mcreator.vestigia.item.*;
import net.mcreator.vestigia.VestigiaMod;

import java.util.function.Function;

@EventBusSubscriber
public class VestigiaModItems {
	public static final DeferredRegister.Items REGISTRY = DeferredRegister.createItems(VestigiaMod.MODID);
	public static final DeferredItem<Item> ASH_DEPOSIT;
	public static final DeferredItem<Item> BRONZE_DEPOSIT;
	public static final DeferredItem<Item> MUD_DEPOSIT;
	public static final DeferredItem<Item> COLDHORSEHAIRBRUSH;
	public static final DeferredItem<Item> MUD_SEDIMENT;
	public static final DeferredItem<Item> BRONZE_SEDIMENT;
	public static final DeferredItem<Item> ASH_SEDIMENT;
	public static final DeferredItem<Item> SIEVE;
	public static final DeferredItem<Item> AURORA_SPLINTER;
	public static final DeferredItem<Item> DIAMOND_SPLINTER;
	public static final DeferredItem<Item> METAL_SPLINTER;
	public static final DeferredItem<Item> WATER_SPLINTER;
	public static final DeferredItem<Item> BRONCE_COIN;
	public static final DeferredItem<Item> OXIDIZED_BRONZE_SPLINTER;
	public static final DeferredItem<Item> BRONZE;
	public static final DeferredItem<Item> CERAMIC_FRAGMENT;
	public static final DeferredItem<Item> AMBER;
	public static final DeferredItem<Item> AMBERPEARL;
	public static final DeferredItem<Item> STRATIGRAPHIC_SHOVEL;
	public static final DeferredItem<Item> RESTORER;
	public static final DeferredItem<Item> ASH_SPLINTER;
	public static final DeferredItem<Item> CHISEL;
	public static final DeferredItem<Item> OBSIDIAN_SPLINTER;
	public static final DeferredItem<Item> RUBY_SPLINTER;
	public static final DeferredItem<Item> FLINT_SPLINTER;
	public static final DeferredItem<Item> IRON_SHARD;
	public static final DeferredItem<Item> GOLD_SPLINTER;
	public static final DeferredItem<Item> COAL_SPLINTER;
	public static final DeferredItem<Item> MYTHIC_SPLINTER;
	public static final DeferredItem<Item> ENDER_SPLINTER;
	public static final DeferredItem<Item> AURORA_KNIFE_BROKEN;
	public static final DeferredItem<Item> AURORA_KNIFE;
	public static final DeferredItem<Item> HOOK_BROKEN;
	public static final DeferredItem<Item> HOOK;
	public static final DeferredItem<Item> PERCUSSION_HAMMER_BROKEN;
	public static final DeferredItem<Item> PERCUSSION_HAMMER;
	public static final DeferredItem<Item> STONE_MASON_PICK_BROKEN;
	public static final DeferredItem<Item> STONE_MASON_PICKAXE;
	public static final DeferredItem<Item> RUBY;
	public static final DeferredItem<Item> RUBY_BLADE;
	public static final DeferredItem<Item> ASH_SWORD;
	public static final DeferredItem<Item> ASH_PICKAXE;
	public static final DeferredItem<Item> CAPIBARA_OF_WISDOM;
	public static final DeferredItem<Item> BOOMERANG;
	public static final DeferredItem<Item> REFUGEBELLBROKEN;
	public static final DeferredItem<Item> REFUGE_BELL;
	public static final DeferredItem<Item> UNCHARGED_RUBY_BLADE;
	public static final DeferredItem<Item> SLINGSHOT;
	public static final DeferredItem<Item> GOLDEN_STAFF;
	public static final DeferredItem<Item> UNCHARGED_GOLDEN_STAFF;
	public static final DeferredItem<Item> COAL_HOE_BROKEN;
	public static final DeferredItem<Item> COAL_HOE;
	public static final DeferredItem<Item> BROKEN_METAL_HOE;
	public static final DeferredItem<Item> METAL_HOE;
	public static final DeferredItem<Item> IRON_DAGGER;
	public static final DeferredItem<Item> CRACKED_FLINT;
	public static final DeferredItem<Item> REPAIRED_FLINT;
	public static final DeferredItem<Item> WORKED_FLINT;
	public static final DeferredItem<Item> FLINT_AXE;
	public static final DeferredItem<Item> BROKEN_ASH_PICKAXE;
	public static final DeferredItem<Item> BROKEN_ASH_SWORD;
	public static final DeferredItem<Item> GOLDEN_SWORD;
	public static final DeferredItem<Item> PORTADOR_SPAWN_EGG;
	public static final DeferredItem<Item> ECHO;
	public static final DeferredItem<Item> PHANTOM_SPEAR;
	public static final DeferredItem<Item> OFFERING_KNIFE;
	public static final DeferredItem<Item> BELL_RINGERS_MACE;
	public static final DeferredItem<Item> BRONZE_VOLTIVE_AXE;
	public static final DeferredItem<Item> WATER_ELEMENT_SWORD;
	public static final DeferredItem<Item> UNCHARGED_RUBY_BLADE_BROKEN;
	public static final DeferredItem<Item> EMPTY_WATER_SWORD;
	public static final DeferredItem<Item> REVERBERATION_ANVIL;
	public static final DeferredItem<Item> SEAL_1;
	public static final DeferredItem<Item> SEAL_1_OFF;
	public static final DeferredItem<Item> SEAL_2;
	public static final DeferredItem<Item> SEAL_2_OFF;
	public static final DeferredItem<Item> SEAL_3;
	public static final DeferredItem<Item> SEAL_3_OFF;
	public static final DeferredItem<Item> SEAL_Z;
	public static final DeferredItem<Item> SEAL_Z_OFF;
	public static final DeferredItem<Item> STELLAR_SEAL;
	public static final DeferredItem<Item> STELLAR_SEAL_OFF;
	public static final DeferredItem<Item> FIRE_EMBLEM;
	public static final DeferredItem<Item> ALTAR_DE_INVOCACION_NIVEL_1;
	public static final DeferredItem<Item> ALTAR_DE_INVOCACION_NIVEL_2;
	public static final DeferredItem<Item> TWISTED_EMBLEM;
	public static final DeferredItem<Item> AZTEC_STATUE;
	public static final DeferredItem<Item> AZTEC_EMBLEM;
	public static final DeferredItem<Item> ALTAR;
	public static final DeferredItem<Item> JADE_EMBLEM;
	public static final DeferredItem<Item> RUBY_EMBLEM;
	public static final DeferredItem<Item> GOLDEN_EMBLEM;
	public static final DeferredItem<Item> BLACK_JADE_EMBLEM;
	public static final DeferredItem<Item> SEAL_EMBLEM;
	public static final DeferredItem<Item> AURORA_EMBLEM;
	public static final DeferredItem<Item> MACAHUITL;
	public static final DeferredItem<Item> PEPPER;
	public static final DeferredItem<Item> CORN;
	public static final DeferredItem<Item> ANCIENT_FUIR;
	public static final DeferredItem<Item> VESTIGIA_BOOK;
	static {
		ASH_DEPOSIT = block(VestigiaModBlocks.ASH_DEPOSIT, new Item.Properties().rarity(Rarity.UNCOMMON));
		BRONZE_DEPOSIT = block(VestigiaModBlocks.BRONZE_DEPOSIT, new Item.Properties().rarity(Rarity.RARE));
		MUD_DEPOSIT = block(VestigiaModBlocks.MUD_DEPOSIT);
		COLDHORSEHAIRBRUSH = register("coldhorsehairbrush", ColdhorsehairbrushItem::new);
		MUD_SEDIMENT = register("mud_sediment", MudSedimentItem::new);
		BRONZE_SEDIMENT = register("bronze_sediment", BronzeSedimentItem::new);
		ASH_SEDIMENT = register("ash_sediment", AshSedimentItem::new);
		SIEVE = block(VestigiaModBlocks.SIEVE, new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON));
		AURORA_SPLINTER = register("aurora_splinter", AuroraSplinterItem::new);
		DIAMOND_SPLINTER = register("diamond_splinter", DiamondSplinterItem::new);
		METAL_SPLINTER = register("metal_splinter", IronSplinterItem::new);
		WATER_SPLINTER = register("water_splinter", WaterSplinterItem::new);
		BRONCE_COIN = register("bronce_coin", BronceCoinItem::new);
		OXIDIZED_BRONZE_SPLINTER = register("oxidized_bronze_splinter", OxidizedBronzeSplinterItem::new);
		BRONZE = register("bronze", BronzeItem::new);
		CERAMIC_FRAGMENT = register("ceramic_fragment", CeramicFragmentItem::new);
		AMBER = register("amber", AmberItem::new);
		AMBERPEARL = register("amberpearl", AmberpearlItem::new);
		STRATIGRAPHIC_SHOVEL = register("stratigraphic_shovel", StratigraphicShovelItem::new);
		RESTORER = block(VestigiaModBlocks.RESTORER, new Item.Properties().rarity(Rarity.RARE));
		ASH_SPLINTER = register("ash_splinter", AshSplinterItem::new);
		CHISEL = register("chisel", ChiselItem::new);
		OBSIDIAN_SPLINTER = register("obsidian_splinter", ObsidianSplinterItem::new);
		RUBY_SPLINTER = register("ruby_splinter", RubySplinterItem::new);
		FLINT_SPLINTER = register("flint_splinter", FlintSplinterItem::new);
		IRON_SHARD = register("iron_shard", IronShardItem::new);
		GOLD_SPLINTER = register("gold_splinter", GoldSplinterItem::new);
		COAL_SPLINTER = register("coal_splinter", CoalSplinterItem::new);
		MYTHIC_SPLINTER = register("mythic_splinter", MythicSplinterItem::new);
		ENDER_SPLINTER = register("ender_splinter", EnderSplinterItem::new);
		AURORA_KNIFE_BROKEN = register("aurora_knife_broken", AuroraKnifeBrokenItem::new);
		AURORA_KNIFE = register("aurora_knife", AuroraKnifeItem::new);
		HOOK_BROKEN = register("hook_broken", HookBrokenItem::new);
		HOOK = register("hook", HookItem::new);
		PERCUSSION_HAMMER_BROKEN = register("percussion_hammer_broken", PercussionHammerBrokenItem::new);
		PERCUSSION_HAMMER = register("percussion_hammer", PercussionHammerItem::new);
		STONE_MASON_PICK_BROKEN = register("stone_mason_pick_broken", StoneMasonPickBrokenItem::new);
		STONE_MASON_PICKAXE = register("stone_mason_pickaxe", StoneMasonPickaxeItem::new);
		RUBY = register("ruby", RubyItem::new);
		RUBY_BLADE = register("ruby_blade", RubyBladeItem::new);
		ASH_SWORD = register("ash_sword", AshSwordItem::new);
		ASH_PICKAXE = register("ash_pickaxe", AshPickaxeItem::new);
		CAPIBARA_OF_WISDOM = register("capibara_of_wisdom", CapibaraOfWisdomItem::new);
		BOOMERANG = register("boomerang", BoomerangItem::new);
		REFUGEBELLBROKEN = register("refugebellbroken", RefugebellbrokenItem::new);
		REFUGE_BELL = register("refuge_bell", RefugeBellItem::new);
		UNCHARGED_RUBY_BLADE = register("uncharged_ruby_blade", UnchargedRubyBladeItem::new);
		SLINGSHOT = register("slingshot", SlingshotItem::new);
		GOLDEN_STAFF = register("golden_staff", GoldenStaffItem::new);
		UNCHARGED_GOLDEN_STAFF = register("uncharged_golden_staff", UnchargedGoldenStaffItem::new);
		COAL_HOE_BROKEN = register("coal_hoe_broken", CoalHoeBrokenItem::new);
		COAL_HOE = register("coal_hoe", CoalHoeItem::new);
		BROKEN_METAL_HOE = register("broken_metal_hoe", BrokenMetalHoeItem::new);
		METAL_HOE = register("metal_hoe", MetalHoeItem::new);
		IRON_DAGGER = register("iron_dagger", IronDaggerItem::new);
		CRACKED_FLINT = register("cracked_flint", CrackedFlintItem::new);
		REPAIRED_FLINT = register("repaired_flint", RepairedFlintItem::new);
		WORKED_FLINT = register("worked_flint", WorkedFlintItem::new);
		FLINT_AXE = register("flint_axe", FlintAxeItem::new);
		BROKEN_ASH_PICKAXE = register("broken_ash_pickaxe", BrokenAshPickaxeItem::new);
		BROKEN_ASH_SWORD = register("broken_ash_sword", BrokenAshSwordItem::new);
		GOLDEN_SWORD = register("golden_sword", GoldenSwordItem::new);
		PORTADOR_SPAWN_EGG = register("portador_spawn_egg", properties -> new SpawnEggItem(properties.spawnEgg(VestigiaModEntities.PORTADOR.get())));
		ECHO = register("echo", EchoItem::new);
		PHANTOM_SPEAR = register("phantom_spear", PhantomSpearItem::new);
		OFFERING_KNIFE = register("offering_knife", OfferingKnifeItem::new);
		BELL_RINGERS_MACE = register("bell_ringers_mace", BellRingersMaceItem::new);
		BRONZE_VOLTIVE_AXE = register("bronze_voltive_axe", BronzeVoltiveAxeItem::new);
		WATER_ELEMENT_SWORD = register("water_element_sword", WaterElementSwordItem::new);
		UNCHARGED_RUBY_BLADE_BROKEN = register("uncharged_ruby_blade_broken", UnchargedRubyBladeBrokenItem::new);
		EMPTY_WATER_SWORD = register("empty_water_sword", EmptyWaterSwordItem::new);
		REVERBERATION_ANVIL = block(VestigiaModBlocks.REVERBERATION_ANVIL);
		SEAL_1 = register("seal_1", Seal1Item::new);
		SEAL_1_OFF = register("seal_1_off", Seal1OffItem::new);
		SEAL_2 = register("seal_2", Seal2Item::new);
		SEAL_2_OFF = register("seal_2_off", Seal2OffItem::new);
		SEAL_3 = register("seal_3", Seal3Item::new);
		SEAL_3_OFF = register("seal_3_off", Seal3OffItem::new);
		SEAL_Z = register("seal_z", SealZItem::new);
		SEAL_Z_OFF = register("seal_z_off", SealZOffItem::new);
		STELLAR_SEAL = register("stellar_seal", StellarSealItem::new);
		STELLAR_SEAL_OFF = register("stellar_seal_off", StellarSealOffItem::new);
		FIRE_EMBLEM = register("fire_emblem", FireEmblemItem::new);
		ALTAR_DE_INVOCACION_NIVEL_1 = block(VestigiaModBlocks.ALTAR_DE_INVOCACION_NIVEL_1);
		ALTAR_DE_INVOCACION_NIVEL_2 = block(VestigiaModBlocks.ALTAR_DE_INVOCACION_NIVEL_2);
		TWISTED_EMBLEM = register("twisted_emblem", TwistedEmblemItem::new);
		AZTEC_STATUE = register("aztec_statue", AztecStatueItem::new);
		AZTEC_EMBLEM = register("aztec_emblem", AztecEmblemItem::new);
		ALTAR = block(VestigiaModBlocks.ALTAR);
		JADE_EMBLEM = register("jade_emblem", JadeEmblemItem::new);
		RUBY_EMBLEM = register("ruby_emblem", RubyEmblemItem::new);
		GOLDEN_EMBLEM = register("golden_emblem", GoldenEmblemItem::new);
		BLACK_JADE_EMBLEM = register("black_jade_emblem", BlackJadeEmblemItem::new);
		SEAL_EMBLEM = register("seal_emblem", SealEmblemItem::new);
		AURORA_EMBLEM = register("aurora_emblem", AuroraEmblemItem::new);
		MACAHUITL = register("macahuitl", MacahuitlItem::new);
		PEPPER = register("pepper", PepperItem::new);
		CORN = register("corn", CornItem::new);
		ANCIENT_FUIR = register("ancient_fuir", AncientFuirItem::new);
		VESTIGIA_BOOK = register("vestigia_book", VestigiaBookItem::new);
	}

	// Start of user code block custom items
	// End of user code block custom items
	private static <I extends Item> DeferredItem<I> register(String name, Function<Item.Properties, ? extends I> supplier) {
		return REGISTRY.registerItem(name, supplier, Item.Properties::new);
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block) {
		return block(block, new Item.Properties());
	}

	private static DeferredItem<Item> block(DeferredHolder<Block, Block> block, Item.Properties properties) {
		return REGISTRY.registerItem(block.getId().getPath(), prop -> new BlockItem(block.get(), prop), () -> properties);
	}

	@SubscribeEvent
	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.registerItem(Capabilities.Item.ITEM, (stack, access) -> new VestigiaBookInventoryCapability(access), VESTIGIA_BOOK.get());
	}
}