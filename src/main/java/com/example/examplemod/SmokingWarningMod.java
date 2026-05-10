package com.example.examplemod;

import com.google.common.collect.ImmutableSet;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import org.slf4j.Logger;
import com.example.examplemod.compat.CreateCompat;

@Mod(SmokingWarningMod.MODID)
public class SmokingWarningMod {
    public static final String MODID = "smokingwarningmod";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(Registries.BLOCK, MODID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MODID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MODID);
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, MODID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(Registries.MOB_EFFECT, MODID);
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, MODID);
    public static final DeferredRegister<VillagerProfession> VILLAGER_PROFESSIONS = DeferredRegister.create(Registries.VILLAGER_PROFESSION, MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(Registries.RECIPE_SERIALIZER, MODID);

    public static final ResourceKey<PoiType> TOBACCO_WORKBENCH_POI_KEY = ResourceKey.create(
            Registries.POINT_OF_INTEREST_TYPE, id("tobacco_workbench"));

    public static final DeferredHolder<Block, TobaccoCropBlock> TOBACCO_CROP = BLOCKS.register("tobacco_crop", () -> new TobaccoCropBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
                    .noCollission()
                    .randomTicks()
                    .instabreak()
                    .sound(SoundType.CROP)
                    .pushReaction(PushReaction.DESTROY)));
    public static final DeferredHolder<Block, TobaccoWorkbenchBlock> TOBACCO_WORKBENCH = BLOCKS.register("tobacco_workbench", () -> new TobaccoWorkbenchBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)));
    public static final DeferredHolder<Block, DryingRackBlock> DRYING_RACK = BLOCKS.register("drying_rack", () -> new DryingRackBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));
    public static final DeferredHolder<Block, TobaccoGrinderBlock> TOBACCO_GRINDER = BLOCKS.register("tobacco_grinder", () -> new TobaccoGrinderBlock(
            BlockBehaviour.Properties.ofFullCopy(Blocks.CRAFTING_TABLE)
                    .noOcclusion()
                    .sound(SoundType.WOOD)));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>> DRYING_RACK_BLOCK_ENTITY =
            BLOCK_ENTITY_TYPES.register("drying_rack", () -> BlockEntityType.Builder.of(DryingRackBlockEntity::new, DRYING_RACK.get()).build(null));

    public static final DeferredItem<Item> TOBACCO_LEAF = ITEMS.registerSimpleItem("tobacco_leaf", new Item.Properties());
    public static final DeferredItem<Item> DRIED_TOBACCO_LEAF = ITEMS.registerSimpleItem("dried_tobacco_leaf", new Item.Properties());
    public static final DeferredItem<Item> TOBACCO_SHREDS = ITEMS.registerSimpleItem("tobacco_shreds", new Item.Properties());
    public static final DeferredItem<Item> TOBACCO_SEEDS = ITEMS.register("tobacco_seeds", () -> new ItemNameBlockItem(TOBACCO_CROP.get(), new Item.Properties()));
    public static final DeferredItem<Item> CIGARETTE = ITEMS.register("cigarette", () -> new CigaretteItem(new Item.Properties().stacksTo(1).durability(Config.DEFAULT_CIGARETTE_DURABILITY)));
    public static final DeferredItem<Item> MENTHOL_CIGARETTE = registerCigarette("menthol_cigarette", CigaretteItem.CigaretteVariant.MENTHOL);
    public static final DeferredItem<Item> HONEY_CIGARETTE = registerCigarette("honey_cigarette", CigaretteItem.CigaretteVariant.HONEY);
    public static final DeferredItem<Item> BLAZE_CIGARETTE = registerCigarette("blaze_cigarette", CigaretteItem.CigaretteVariant.BLAZE);
    public static final DeferredItem<Item> PHANTOM_CIGARETTE = registerCigarette("phantom_cigarette", CigaretteItem.CigaretteVariant.PHANTOM);
    public static final DeferredItem<Item> ENDER_CIGARETTE = registerCigarette("ender_cigarette", CigaretteItem.CigaretteVariant.ENDER);
    public static final DeferredItem<Item> GLOW_CIGARETTE = registerCigarette("glow_cigarette", CigaretteItem.CigaretteVariant.GLOW);
    public static final DeferredItem<Item> REDSTONE_CIGARETTE = registerCigarette("redstone_cigarette", CigaretteItem.CigaretteVariant.REDSTONE);
    public static final DeferredItem<Item> NETHERITE_CIGARETTE = registerCigarette("netherite_cigarette", CigaretteItem.CigaretteVariant.NETHERITE);
    public static final DeferredItem<Item> RICK_V_CIGARETTE = registerCigarette("rick_v_cigarette", CigaretteItem.CigaretteVariant.RICK_V);
    public static final DeferredItem<Item> HUAZI_CIGARETTE = registerCigarette("huazi_cigarette", CigaretteItem.CigaretteVariant.HUAZI);
    public static final DeferredItem<Item> LOTUS_CIGARETTE = registerCigarette("lotus_cigarette", CigaretteItem.CigaretteVariant.LOTUS);
    public static final DeferredItem<Item> CIGARETTE_BUTT = ITEMS.registerSimpleItem("cigarette_butt", new Item.Properties());
    public static final DeferredItem<Item> LIGHTER = ITEMS.registerSimpleItem("lighter", new Item.Properties().stacksTo(1).durability(Config.DEFAULT_LIGHTER_DURABILITY));
    public static final DeferredItem<BlockItem> DRYING_RACK_ITEM = ITEMS.register("drying_rack", () -> new BlockItem(DRYING_RACK.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> TOBACCO_GRINDER_ITEM = ITEMS.register("tobacco_grinder", () -> new BlockItem(TOBACCO_GRINDER.get(), new Item.Properties()));
    public static final DeferredItem<BlockItem> TOBACCO_WORKBENCH_ITEM = ITEMS.register("tobacco_workbench", () -> new BlockItem(TOBACCO_WORKBENCH.get(), new Item.Properties()));

    public static final DeferredItem<Item> LUNG_SCANNER = ITEMS.register("lung_scanner", () -> new LungScannerItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> CHEMOTHERAPY_MEDICINE = ITEMS.register("chemotherapy_medicine", () -> new TreatmentItem(new Item.Properties().stacksTo(16), TreatmentItem.TreatmentType.CHEMOTHERAPY));
    public static final DeferredItem<Item> RADIOTHERAPY_CORE = ITEMS.register("radiotherapy_core", () -> new TreatmentItem(new Item.Properties().stacksTo(16), TreatmentItem.TreatmentType.RADIOTHERAPY));
    public static final DeferredItem<Item> TARGETED_THERAPY_MEDICINE = ITEMS.register("targeted_therapy_medicine", () -> new TreatmentItem(new Item.Properties().stacksTo(8), TreatmentItem.TreatmentType.TARGETED_THERAPY));
    public static final DeferredItem<Item> REHABILITATION_PLAN = ITEMS.register("rehabilitation_plan", () -> new TreatmentItem(new Item.Properties().stacksTo(1), TreatmentItem.TreatmentType.REHABILITATION));
    public static final DeferredItem<Item> LUNG_CANCER_TREATMENT_GUIDE = ITEMS.register("lung_cancer_treatment_guide", () -> new LungCancerTreatmentGuideItem(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> INCOMPLETE_RICK_V_CIGARETTE = ITEMS.registerSimpleItem("incomplete_rick_v_cigarette", new Item.Properties().stacksTo(1));
    // Create automation intermediate items
    public static final DeferredItem<Item> COARSE_TOBACCO_SHREDS = ITEMS.registerSimpleItem("coarse_tobacco_shreds", new Item.Properties());
    public static final DeferredItem<Item> REFINED_TOBACCO_SHREDS = ITEMS.registerSimpleItem("refined_tobacco_shreds", new Item.Properties());
    public static final DeferredItem<Item> CIGARETTE_FILTER = ITEMS.registerSimpleItem("cigarette_filter", new Item.Properties());
    public static final DeferredItem<Item> CIGARETTE_PAPER = ITEMS.registerSimpleItem("cigarette_paper", new Item.Properties());
    public static final DeferredItem<Item> UNFINISHED_CIGARETTE = ITEMS.registerSimpleItem("unfinished_cigarette", new Item.Properties());
    public static final DeferredItem<Item> PACKAGED_CIGARETTE = ITEMS.registerSimpleItem("packaged_cigarette", new Item.Properties());
    public static final DeferredItem<Item> INCOMPLETE_VARIANT_CIGARETTE = ITEMS.registerSimpleItem("incomplete_variant_cigarette", new Item.Properties().stacksTo(1));

    public static final DeferredHolder<MobEffect, MobEffect> SMOKE_ADDICTION = MOB_EFFECTS.register(
            "smoke_addiction", () -> new SimpleHarmfulEffect(0x8b6f47));
    public static final DeferredHolder<MobEffect, MobEffect> LUNG_DAMAGE = MOB_EFFECTS.register(
            "lung_damage", () -> new SimpleHarmfulEffect(0x55595c));
    public static final DeferredHolder<MobEffect, MobEffect> WITHDRAWAL = MOB_EFFECTS.register(
            "withdrawal", () -> new SimpleHarmfulEffect(0x6f608f));
    public static final DeferredHolder<MobEffect, MobEffect> SECONDHAND_SMOKE = MOB_EFFECTS.register(
            "secondhand_smoke", () -> new SimpleHarmfulEffect(0x9aa0a0));
    public static final DeferredHolder<MobEffect, MobEffect> COUGHING = MOB_EFFECTS.register(
            "coughing", () -> new SimpleHarmfulEffect(0xc8b28a));
    public static final DeferredHolder<MobEffect, MobEffect> LUNG_CANCER = MOB_EFFECTS.register(
            "lung_cancer", () -> new SimpleHarmfulEffect(0x3d3035));

    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTER_CLICK = registerSound("lighter_click");
    public static final DeferredHolder<SoundEvent, SoundEvent> INHALE = registerSound("inhale");
    public static final DeferredHolder<SoundEvent, SoundEvent> EXHALE = registerSound("exhale");
    public static final DeferredHolder<SoundEvent, SoundEvent> COUGH = registerSound("cough");
    public static final DeferredHolder<SoundEvent, SoundEvent> COUGHING_SOUND = registerSound("coughing");
    public static final DeferredHolder<SoundEvent, SoundEvent> LUNG_CANCER_SOUND = registerSound("lung_cancer");
    public static final DeferredHolder<SoundEvent, SoundEvent> LIGHTER_USE = registerSound("lighter_use");
    public static final DeferredHolder<SoundEvent, SoundEvent> SMOKING_INHALE = registerSound("smoking_inhale");
    public static final DeferredHolder<SoundEvent, SoundEvent> SMOKING_EXHALE = registerSound("smoking_exhale");
    public static final DeferredHolder<SoundEvent, SoundEvent> RICK_V_EASTER_EGG = registerSound("rick_v_easter_egg");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRYING_RACK_PLACE_LEAF = registerSound("drying_rack_place_leaf");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRYING_RACK_COLLECT_LEAF = registerSound("drying_rack_collect_leaf");
    public static final DeferredHolder<SoundEvent, SoundEvent> TOBACCO_GRINDER_USE = registerSound("tobacco_grinder_use");

    public static final DeferredHolder<PoiType, PoiType> TOBACCO_WORKBENCH_POI = POI_TYPES.register("tobacco_workbench", () -> new PoiType(
            ImmutableSet.copyOf(TOBACCO_WORKBENCH.get().getStateDefinition().getPossibleStates()), 1, 1));
    public static final DeferredHolder<VillagerProfession, VillagerProfession> TOBACCO_VILLAGER = VILLAGER_PROFESSIONS.register("tobacco_villager", () -> new VillagerProfession(
            "tobacco_villager",
            poi -> poi.is(TOBACCO_WORKBENCH_POI_KEY),
            poi -> poi.is(TOBACCO_WORKBENCH_POI_KEY),
            ImmutableSet.of(TOBACCO_SEEDS.get(), TOBACCO_LEAF.get(), DRIED_TOBACCO_LEAF.get()),
            ImmutableSet.of(TOBACCO_WORKBENCH.get()),
            SoundEvents.VILLAGER_WORK_FARMER));

    public static final DeferredHolder<RecipeType<?>, RecipeType<DryingRackRecipe>> DRYING_RACK_RECIPE_TYPE =
            RECIPE_TYPES.register("drying_rack_recipe", () -> RecipeType.simple(id("drying_rack_recipe")));
    public static final DeferredHolder<RecipeType<?>, RecipeType<TobaccoGrinderRecipe>> TOBACCO_GRINDER_RECIPE_TYPE =
            RECIPE_TYPES.register("tobacco_grinder_recipe", () -> RecipeType.simple(id("tobacco_grinder_recipe")));
    public static final DeferredHolder<RecipeType<?>, RecipeType<TobaccoWorkbenchRecipe>> TOBACCO_WORKBENCH_RECIPE_TYPE =
            RECIPE_TYPES.register("tobacco_workbench_recipe", () -> RecipeType.simple(id("tobacco_workbench_recipe")));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DryingRackRecipe>> DRYING_RACK_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("drying_rack_recipe", DryingRackRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TobaccoGrinderRecipe>> TOBACCO_GRINDER_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("tobacco_grinder_recipe", TobaccoGrinderRecipe.Serializer::new);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<TobaccoWorkbenchRecipe>> TOBACCO_WORKBENCH_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("tobacco_workbench_recipe", TobaccoWorkbenchRecipe.Serializer::new);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CIGARETTE_TAB = CREATIVE_MODE_TABS.register("cigarette_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.smokingwarningmod.cigarette_tab"))
            .withTabsBefore(CreativeModeTabs.FOOD_AND_DRINKS)
            .icon(() -> CIGARETTE.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
                output.accept(TOBACCO_SEEDS.get());
                output.accept(TOBACCO_LEAF.get());
                output.accept(DRIED_TOBACCO_LEAF.get());
                output.accept(TOBACCO_SHREDS.get());
                output.accept(COARSE_TOBACCO_SHREDS.get());
                output.accept(REFINED_TOBACCO_SHREDS.get());
                output.accept(CIGARETTE_PAPER.get());
                output.accept(CIGARETTE_FILTER.get());
                output.accept(UNFINISHED_CIGARETTE.get());
                output.accept(PACKAGED_CIGARETTE.get());
                output.accept(CIGARETTE.get());
                output.accept(MENTHOL_CIGARETTE.get());
                output.accept(HONEY_CIGARETTE.get());
                output.accept(BLAZE_CIGARETTE.get());
                output.accept(PHANTOM_CIGARETTE.get());
                output.accept(ENDER_CIGARETTE.get());
                output.accept(GLOW_CIGARETTE.get());
                output.accept(REDSTONE_CIGARETTE.get());
                output.accept(NETHERITE_CIGARETTE.get());
                output.accept(RICK_V_CIGARETTE.get());
                output.accept(HUAZI_CIGARETTE.get());
                output.accept(LOTUS_CIGARETTE.get());
                output.accept(CIGARETTE_BUTT.get());
                output.accept(LIGHTER.get());
                output.accept(DRYING_RACK_ITEM.get());
                output.accept(TOBACCO_GRINDER_ITEM.get());
                output.accept(TOBACCO_WORKBENCH_ITEM.get());
                output.accept(LUNG_SCANNER.get());
                output.accept(CHEMOTHERAPY_MEDICINE.get());
                output.accept(RADIOTHERAPY_CORE.get());
                output.accept(TARGETED_THERAPY_MEDICINE.get());
                output.accept(REHABILITATION_PLAN.get());
                output.accept(LUNG_CANCER_TREATMENT_GUIDE.get());
            })
            .build());

    public SmokingWarningMod(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayloads);

        BLOCKS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
        ITEMS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        SOUND_EVENTS.register(modEventBus);
        MOB_EFFECTS.register(modEventBus);
        POI_TYPES.register(modEventBus);
        VILLAGER_PROFESSIONS.register(modEventBus);
        RECIPE_TYPES.register(modEventBus);
        RECIPE_SERIALIZERS.register(modEventBus);

        NeoForge.EVENT_BUS.register(new SmokingEvents());
        NeoForge.EVENT_BUS.register(new TobaccoVillagerTrades());
        NeoForge.EVENT_BUS.addListener((ServerTickEvent.Post event) -> TobaccoVillagerSpawner.onServerTick(event.getServer().overworld()));
        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> TobaccoLocateCommand.register(event.getDispatcher()));
        NeoForge.EVENT_BUS.addListener((ServerStartedEvent event) -> {
            var rm = event.getServer().getRecipeManager();
            int wb = 0, dr = 0, tg = 0;
            int milling = 0, crushing = 0, smoking = 0, pressing = 0, mixing = 0, deploying = 0, seq = 0;
            for (var recipe : rm.getRecipes()) {
                var id = recipe.id().toString();
                if (!id.startsWith("smokingwarningmod:")) continue;
                var type = recipe.value().getType().toString();
                if (type.contains("tobacco_workbench")) wb++;
                else if (type.contains("drying_rack")) dr++;
                else if (type.contains("tobacco_grinder")) tg++;
                else if (type.contains("milling")) milling++;
                else if (type.contains("crushing")) crushing++;
                else if (type.contains("smoking")) smoking++;
                else if (type.contains("pressing")) pressing++;
                else if (type.contains("mixing")) mixing++;
                else if (type.contains("deploying")) deploying++;
                else if (type.contains("sequenced_assembly")) seq++;
            }
            LOGGER.info("[DEBUG] Recipes loaded — tobacco_workbench: {}, drying_rack: {}, tobacco_grinder: {}", wb, dr, tg);
            LOGGER.info("[DEBUG] Create recipes — milling: {}, crushing: {}, smoking: {}, pressing: {}, mixing: {}, deploying: {}, sequenced_assembly: {}", milling, crushing, smoking, pressing, mixing, deploying, seq);
            LOGGER.info("[DEBUG] Create mod detected: {}", CreateCompat.isActive());
        });
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    private static DeferredHolder<SoundEvent, SoundEvent> registerSound(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id(name)));
    }

    private static DeferredItem<Item> registerCigarette(String name, CigaretteItem.CigaretteVariant variant) {
        return ITEMS.register(name, () -> new CigaretteItem(new Item.Properties().stacksTo(1).durability(variant.maxDurability()), variant));
    }

    public static Item[] allCigaretteItems() {
        return new Item[] {
                CIGARETTE.get(),
                MENTHOL_CIGARETTE.get(),
                HONEY_CIGARETTE.get(),
                BLAZE_CIGARETTE.get(),
                PHANTOM_CIGARETTE.get(),
                ENDER_CIGARETTE.get(),
                GLOW_CIGARETTE.get(),
                REDSTONE_CIGARETTE.get(),
                NETHERITE_CIGARETTE.get(),
                RICK_V_CIGARETTE.get(),
                HUAZI_CIGARETTE.get(),
                LOTUS_CIGARETTE.get()
        };
    }

    private void registerPayloads(RegisterPayloadHandlersEvent event) {
        event.registrar("1")
                .playToClient(FinishDizzyVisualPayload.TYPE, FinishDizzyVisualPayload.STREAM_CODEC, FinishDizzyVisualPayload::handle)
                .playToClient(AddictionHudSyncPayload.TYPE, AddictionHudSyncPayload.STREAM_CODEC, AddictionHudSyncPayload::handle)
                .playToClient(TreatmentHudSyncPayload.TYPE, TreatmentHudSyncPayload.STREAM_CODEC, TreatmentHudSyncPayload::handle);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Loaded Smoking Warning Mod base systems");
        CreateCompat.init();
    }
}
