package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class TobaccoVillagerSpawner {
    private static final String DATA_KEY = "smokingwarningmod_tobacco_villages";
    private static final int BELL_SEARCH_RADIUS = 128;
    private static final int HOUSE_PLACEMENT_RADIUS = 24;
    // Only process each bell once per world
    private static final int RESCAN_TICKS = 600;

    private TobaccoVillagerSpawner() {
    }

    public static void buildHouseAndSpawn(ServerLevel level, BlockPos pos) {
        buildTobaccoHouse(level, pos);
        Villager villager = EntityType.VILLAGER.create(level);
        if (villager != null) {
            villager.moveTo(pos.getX() + 2.5D, pos.getY(), pos.getZ() + 2.5D,
                    level.getRandom().nextFloat() * 360.0F, 0.0F);
            villager.setVillagerData(villager.getVillagerData()
                    .setProfession(SmokingWarningMod.TOBACCO_VILLAGER.get()));
            villager.finalizeSpawn(level, level.getCurrentDifficultyAt(pos),
                    MobSpawnType.COMMAND, null);
            level.addFreshEntity(villager);
        }
    }

    public static void onServerTick(ServerLevel level) {
        if (!Config.ENABLE_TOBACCO_VILLAGER_GENERATION.getAsBoolean()) {
            return;
        }
        if (level.getGameTime() % RESCAN_TICKS != 0) {
            return;
        }
        if (level.dimension() != net.minecraft.world.level.Level.OVERWORLD) {
            return;
        }

        TobaccoVillageData data = TobaccoVillageData.get(level);

        for (var player : level.players()) {
            BlockPos playerPos = player.blockPosition();

            // Find nearby bells as village markers
            AABB area = new AABB(playerPos).inflate(BELL_SEARCH_RADIUS);
            List<BlockPos> bells = BlockPos.betweenClosedStream(area)
                    .filter(pos -> level.getBlockState(pos).is(Blocks.BELL))
                    .map(BlockPos::immutable)
                    .limit(5)
                    .toList();

            for (BlockPos bellPos : bells) {
                long bellKey = posKey(bellPos);
                if (data.isBellProcessed(bellKey)) {
                    continue;
                }
                data.markBellProcessed(bellKey);

                // Find a suitable spot near the bell for the house
                BlockPos housePos = findHouseSpot(level, bellPos, HOUSE_PLACEMENT_RADIUS);
                if (housePos == null) {
                    if (Config.TOBACCO_VILLAGER_DEBUG.getAsBoolean()) {
                        SmokingWarningMod.LOGGER.info("TobaccoVillager: No suitable spot near bell at {}", bellPos);
                    }
                    continue;
                }

                // Check no existing tobacco villager nearby
                AABB checkArea = new AABB(housePos).inflate(64);
                List<Villager> existing = level.getEntitiesOfClass(Villager.class, checkArea,
                        v -> v.getVillagerData().getProfession() == SmokingWarningMod.TOBACCO_VILLAGER.get());
                if (!existing.isEmpty()) {
                    if (Config.TOBACCO_VILLAGER_DEBUG.getAsBoolean()) {
                        SmokingWarningMod.LOGGER.info("TobaccoVillager: Tobacco villager already exists near bell at {}", bellPos);
                    }
                    continue;
                }

                // Build the house
                buildTobaccoHouse(level, housePos);

                // Spawn tobacco villager inside
                BlockPos villagerPos = housePos.offset(2, 1, 2);
                Villager villager = EntityType.VILLAGER.create(level);
                if (villager != null) {
                    villager.moveTo(villagerPos.getX() + 0.5D, villagerPos.getY(), villagerPos.getZ() + 0.5D,
                            level.getRandom().nextFloat() * 360.0F, 0.0F);
                    villager.setVillagerData(villager.getVillagerData()
                            .setProfession(SmokingWarningMod.TOBACCO_VILLAGER.get()));
                    villager.finalizeSpawn(level, level.getCurrentDifficultyAt(housePos),
                            MobSpawnType.NATURAL, null);
                    level.addFreshEntity(villager);
                }

                SmokingWarningMod.LOGGER.info("TobaccoVillager: Built house and spawned tobacco villager near bell at {} (house at {})", bellPos, housePos);
                if (Config.TOBACCO_VILLAGER_DEBUG.getAsBoolean()) {
                    for (var p : level.players()) {
                        p.displayClientMessage(Component.literal("[DEBUG] Tobacco villager house built at " + housePos.getX() + ", " + housePos.getZ()), false);
                    }
                }
            }
        }
    }

    private static BlockPos findHouseSpot(ServerLevel level, BlockPos bell, int radius) {
        // Search around the bell for a suitable spot, from near to far
        for (int dist = 8; dist <= radius; dist += 4) {
            for (int attempt = 0; attempt < 30; attempt++) {
                int dx = level.getRandom().nextInt(dist * 2) - dist;
                int dz = level.getRandom().nextInt(dist * 2) - dist;
                // Use bell's Y as reference, search for surface nearby
                BlockPos base = bell.offset(dx, 0, dz);
                BlockPos ground = findGround(level, base, bell.getY());
                if (ground != null && canPlaceHouse(level, ground)) {
                    return ground;
                }
            }
        }
        return null;
    }

    private static BlockPos findGround(ServerLevel level, BlockPos pos, int refY) {
        // Search around refY level for a solid block with air above
        for (int dy = -8; dy <= 15; dy++) {
            BlockPos check = pos.atY(refY + dy);
            BlockPos above = check.above();
            BlockState below = level.getBlockState(check);
            BlockState airState = level.getBlockState(above);
            if (!below.liquid() && !below.isAir()
                    && below.blocksMotion()
                    && (airState.isAir() || airState.canBeReplaced())) {
                return above;
            }
        }
        return null;
    }

    private static boolean canPlaceHouse(ServerLevel level, BlockPos origin) {
        int solidGround = 0;
        for (int dx = 0; dx < 5; dx++) {
            for (int dz = 0; dz < 5; dz++) {
                BlockPos ground = origin.offset(dx, -1, dz);
                BlockState gs = level.getBlockState(ground);
                if (!gs.liquid() && !gs.isAir()) solidGround++;

                // Check no hard blocks (bedrock, obsidian) blocking above
                BlockPos above = origin.offset(dx, 0, dz);
                BlockState as = level.getBlockState(above);
                if (as.getDestroySpeed(level, above) < 0 && !as.isAir() && !as.canBeReplaced()) {
                    return false;
                }
                BlockPos above2 = origin.offset(dx, 1, dz);
                BlockState as2 = level.getBlockState(above2);
                if (as2.getDestroySpeed(level, above2) < 0 && !as2.isAir() && !as2.canBeReplaced()) {
                    return false;
                }
            }
        }
        return solidGround >= 18;
    }

    private static void buildTobaccoHouse(ServerLevel level, BlockPos origin) {
        String villageType = detectVillageType(level, origin);
        ResourceLocation structureId = getVanillaHouseStructure(villageType);

        if (Config.TOBACCO_VILLAGER_DEBUG.getAsBoolean()) {
            SmokingWarningMod.LOGGER.info("TobaccoVillager: Loading {} style house from {} at {}", villageType, structureId, origin);
        }

        // Try to load and place the vanilla structure
        var templateOpt = level.getStructureManager().get(structureId);
        if (templateOpt.isPresent()) {
            var template = templateOpt.get();
            var settings = new net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings();
            template.placeInWorld(level, origin, origin, settings, level.getRandom(), 3);

            // Replace jigsaw blocks with surrounding block material
            var size = template.getSize();
            for (int dx = 0; dx < size.getX(); dx++) {
                for (int dy = 0; dy < size.getY(); dy++) {
                    for (int dz = 0; dz < size.getZ(); dz++) {
                        BlockPos check = origin.offset(dx, dy, dz);
                        if (level.getBlockState(check).is(Blocks.JIGSAW)) {
                            Block replacement = guessBlockForPosition(level, check);
                            level.setBlock(check, replacement.defaultBlockState(), 3);
                        }
                    }
                }
            }

            if (Config.TOBACCO_VILLAGER_DEBUG.getAsBoolean()) {
                SmokingWarningMod.LOGGER.info("TobaccoVillager: Successfully placed vanilla house {} at {}", structureId, origin);
            }

            // Place tobacco workbench near the center of the house
            BlockPos benchPos = origin.offset(2, 1, 2);
            if (level.getBlockState(benchPos).isAir()) {
                level.setBlock(benchPos, SmokingWarningMod.TOBACCO_WORKBENCH.get().defaultBlockState(), 3);
            } else {
                // Try an alternate spot
                BlockPos altPos = origin.offset(2, 1, 3);
                if (level.getBlockState(altPos).isAir() || level.getBlockState(altPos).canBeReplaced()) {
                    level.setBlock(altPos, SmokingWarningMod.TOBACCO_WORKBENCH.get().defaultBlockState(), 3);
                }
            }
        } else {
            // Fallback: build a very simple 5x5 house
            if (Config.TOBACCO_VILLAGER_DEBUG.getAsBoolean()) {
                SmokingWarningMod.LOGGER.warn("TobaccoVillager: Structure {} not found, using fallback house", structureId);
            }
            buildFallbackHouse(level, origin);
        }
    }

    private static ResourceLocation getVanillaHouseStructure(String villageType) {
        // Try the most common small house variants
        return switch (villageType) {
            case "desert" -> ResourceLocation.withDefaultNamespace("village/desert/houses/desert_small_house_1");
            case "taiga" -> ResourceLocation.withDefaultNamespace("village/taiga/houses/taiga_small_house_1");
            case "savanna" -> ResourceLocation.withDefaultNamespace("village/savanna/houses/savanna_small_house_1");
            case "snowy" -> ResourceLocation.withDefaultNamespace("village/snowy/houses/snowy_small_house_2");
            default -> ResourceLocation.withDefaultNamespace("village/plains/houses/plains_small_house_4");
        };
    }

    private static Block guessBlockForPosition(ServerLevel level, BlockPos pos) {
        // Check adjacent blocks for the most common structural block
        java.util.Map<Block, Integer> counts = new java.util.HashMap<>();
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir);
            BlockState state = level.getBlockState(neighbor);
            Block block = state.getBlock();
            if (block != Blocks.AIR && block != Blocks.JIGSAW && block != Blocks.CAVE_AIR && block != Blocks.VOID_AIR) {
                counts.merge(block, 1, Integer::sum);
            }
        }
        // Also check 2 blocks away
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.relative(dir).relative(dir);
            BlockState state = level.getBlockState(neighbor);
            Block block = state.getBlock();
            if (block != Blocks.AIR && block != Blocks.JIGSAW && block != Blocks.CAVE_AIR && block != Blocks.VOID_AIR) {
                counts.merge(block, 1, Integer::sum);
            }
        }
        // Return most common block, or cobblestone as default
        return counts.entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse(Blocks.COBBLESTONE);
    }

    private static String detectVillageType(ServerLevel level, BlockPos near) {
        AABB area = new AABB(near).inflate(32);
        java.util.Set<Block> set;
        set = java.util.Set.of(Blocks.SANDSTONE, Blocks.SMOOTH_SANDSTONE, Blocks.CUT_SANDSTONE);
        if (countBlocks(level, area, set) >= 3) return "desert";
        set = java.util.Set.of(Blocks.SNOW, Blocks.SNOW_BLOCK);
        if (countBlocks(level, area, set) >= 3) return "snowy";
        set = java.util.Set.of(Blocks.ACACIA_PLANKS, Blocks.ACACIA_LOG, Blocks.STRIPPED_ACACIA_LOG);
        if (countBlocks(level, area, set) >= 3) return "savanna";
        set = java.util.Set.of(Blocks.SPRUCE_PLANKS, Blocks.SPRUCE_LOG, Blocks.STRIPPED_SPRUCE_LOG);
        if (countBlocks(level, area, set) >= 3) return "taiga";
        return "plains";
    }

    private static int countBlocks(ServerLevel level, AABB area, java.util.Set<Block> target) {
        return (int) BlockPos.betweenClosedStream(area)
                .filter(pos -> target.contains(level.getBlockState(pos).getBlock()))
                .limit(20)
                .count();
    }

    private static void buildFallbackHouse(ServerLevel level, BlockPos origin) {
        // Minimal 5x5 oak house as fallback
        for (int dx = -1; dx <= 5; dx++)
            for (int dz = -1; dz <= 5; dz++)
                for (int dy = 0; dy < 6; dy++)
                    level.setBlock(origin.offset(dx, dy, dz), Blocks.AIR.defaultBlockState(), 3);

        for (int dx = 0; dx < 5; dx++)
            for (int dz = 0; dz < 5; dz++)
                level.setBlock(origin.below().offset(dx, 0, dz), Blocks.OAK_PLANKS.defaultBlockState(), 3);

        for (int y = 0; y < 3; y++)
            for (int dx = 0; dx < 5; dx++)
                for (int dz = 0; dz < 5; dz++) {
                    if (dx != 0 && dx != 4 && dz != 0 && dz != 4) continue;
                    if (y <= 1 && dx == 2 && dz == 0) continue;
                    boolean corner = (dx == 0 || dx == 4) && (dz == 0 || dz == 4);
                    level.setBlock(origin.offset(dx, y, dz),
                            (corner ? Blocks.OAK_LOG : Blocks.OAK_PLANKS).defaultBlockState(), 3);
                }

        for (int dx = -1; dx <= 5; dx++)
            for (int dz = -1; dz <= 5; dz++)
                level.setBlock(origin.offset(dx, 3, dz), Blocks.OAK_STAIRS.defaultBlockState(), 3);

        level.setBlock(origin.offset(2, 0, 0), Blocks.OAK_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.SOUTH)
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.LOWER), 3);
        level.setBlock(origin.offset(2, 1, 0), Blocks.OAK_DOOR.defaultBlockState()
                .setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.SOUTH)
                .setValue(net.minecraft.world.level.block.DoorBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), 3);

        level.setBlock(origin.offset(2, 1, 3), SmokingWarningMod.TOBACCO_WORKBENCH.get().defaultBlockState(), 3);
        level.setBlock(origin.offset(4, 2, 4), Blocks.TORCH.defaultBlockState(), 3);
    }

    private static long posKey(BlockPos pos) {
        return ((long) pos.getX() << 32) | (pos.getZ() & 0xFFFFFFFFL);
    }

    public static class TobaccoVillageData extends SavedData {
        private final Set<Long> processedBells = new HashSet<>();

        public static TobaccoVillageData get(ServerLevel level) {
            return level.getDataStorage().computeIfAbsent(
                    new Factory<>(TobaccoVillageData::new, TobaccoVillageData::load),
                    DATA_KEY);
        }

        public boolean isBellProcessed(long bellKey) {
            return processedBells.contains(bellKey);
        }

        public void markBellProcessed(long bellKey) {
            processedBells.add(bellKey);
            if (processedBells.size() > 500) {
                var it = processedBells.iterator();
                for (int i = 0; i < 50 && it.hasNext(); i++) {
                    it.next();
                    it.remove();
                }
            }
            setDirty();
        }

        @Override
        public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
            ListTag list = new ListTag();
            for (long key : processedBells) {
                list.add(StringTag.valueOf(Long.toHexString(key)));
            }
            tag.put("processedBells", list);
            return tag;
        }

        public static TobaccoVillageData load(CompoundTag tag, HolderLookup.Provider registries) {
            TobaccoVillageData data = new TobaccoVillageData();
            ListTag list = tag.getList("processedBells", Tag.TAG_STRING);
            for (int i = 0; i < list.size(); i++) {
                try {
                    data.processedBells.add(Long.parseLong(list.getString(i), 16));
                } catch (NumberFormatException ignored) {
                }
            }
            return data;
        }
    }
}
