package com.example.examplemod;

import com.mojang.brigadier.CommandDispatcher;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class TobaccoLocateCommand {
    private static final int SEARCH_RADIUS = 1000;

    private TobaccoLocateCommand() {
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("tobacco")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("find")
                        .executes(ctx -> findTobacco(ctx.getSource()))
                )
                .then(Commands.literal("spawn")
                        .executes(ctx -> spawnTobacco(ctx.getSource()))
                )
                .then(Commands.literal("debug")
                        .then(Commands.literal("spawn_villager_house")
                                .executes(ctx -> spawnDebugHouse(ctx.getSource()))
                        )
                )
        );
    }

    private static int findTobacco(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        ServerLevel level = source.getLevel();
        BlockPos center = player.blockPosition();

        AABB area = new AABB(center).inflate(SEARCH_RADIUS);
        List<Villager> villagers = level.getEntitiesOfClass(Villager.class, area,
                v -> v.getVillagerData().getProfession() == cigaretteMod.TOBACCO_VILLAGER.get());

        if (!villagers.isEmpty()) {
            BlockPos pos = villagers.get(0).blockPosition();
            teleportPlayer(player, pos);
            source.sendSuccess(() -> Component.translatable("command.cigarettemod.tobacco.found_villager",
                    pos.getX(), pos.getY(), pos.getZ()), true);
            return 1;
        }

        BlockPos workbenchPos = BlockPos.findClosestMatch(center, SEARCH_RADIUS / 2, 128,
                pos -> level.getBlockState(pos).is(cigaretteMod.TOBACCO_WORKBENCH.get())).orElse(null);

        if (workbenchPos != null) {
            teleportPlayer(player, workbenchPos.above());
            source.sendSuccess(() -> Component.translatable("command.cigarettemod.tobacco.found_workbench",
                    workbenchPos.getX(), workbenchPos.getY(), workbenchPos.getZ()), true);
            return 1;
        }

        source.sendFailure(Component.translatable("command.cigarettemod.tobacco.not_found"));
        return 0;
    }

    private static int spawnTobacco(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        ServerLevel level = source.getLevel();
        BlockPos spawnPos = player.blockPosition();

        BlockState existing = level.getBlockState(spawnPos);
        if (existing.isAir() || existing.canBeReplaced()) {
            level.setBlock(spawnPos, cigaretteMod.TOBACCO_WORKBENCH.get().defaultBlockState(), 3);
        }

        Villager villager = EntityType.VILLAGER.create(level);
        if (villager != null) {
            villager.moveTo(spawnPos.getX() + 0.5D, spawnPos.getY() + 1.0D, spawnPos.getZ() + 0.5D,
                    level.getRandom().nextFloat() * 360.0F, 0.0F);
            villager.setVillagerData(villager.getVillagerData()
                    .setProfession(cigaretteMod.TOBACCO_VILLAGER.get()));
            villager.finalizeSpawn(level, level.getCurrentDifficultyAt(spawnPos), MobSpawnType.COMMAND, null);
            level.addFreshEntity(villager);
        }

        source.sendSuccess(() -> Component.translatable("command.cigarettemod.tobacco.spawned"), true);
        return 1;
    }

    private static int spawnDebugHouse(CommandSourceStack source) {
        ServerPlayer player = source.getPlayer();
        if (player == null) return 0;
        ServerLevel level = source.getLevel();

        BlockPos housePos = player.blockPosition();
        for (int y = 0; y < 20; y++) {
            BlockPos check = housePos.offset(0, y, 0);
            if (!level.getBlockState(check).isAir() && level.getBlockState(check).blocksMotion()
                    && level.getBlockState(check.above()).isAir()) {
                housePos = check.above();
                break;
            }
        }

        final BlockPos builtPos = housePos;
        TobaccoVillagerSpawner.buildHouseAndSpawn(level, builtPos);

        final int hx = builtPos.getX(), hy = builtPos.getY(), hz = builtPos.getZ();
        source.sendSuccess(() -> Component.literal("Test tobacco villager house built at " + hx + ", " + hy + ", " + hz), true);
        return 1;
    }

    private static void teleportPlayer(ServerPlayer player, BlockPos pos) {
        player.teleportTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D);
    }
}
