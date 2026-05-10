package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import java.util.List;

public final class VillageNoSmokingHandler {
    private VillageNoSmokingHandler() {
    }

    public static void checkVillageNoSmoking(ServerPlayer player, PlayerSmokingData data) {
        if (!Config.ENABLE_VILLAGE_NO_SMOKING_ZONE.getAsBoolean()) {
            return;
        }

        if (player.isSpectator()) {
            return;
        }

        boolean creativeIgnored = player.isCreative()
                && Config.IGNORE_CREATIVE_PLAYERS_IN_NO_SMOKING_ZONE.getAsBoolean();

        int interval = Config.VILLAGE_NO_SMOKING_CHECK_INTERVAL_TICKS.getAsInt();
        if (player.tickCount % interval != 0) {
            return;
        }

        int radius = Config.VILLAGE_NO_SMOKING_CHECK_RADIUS.getAsInt();
        Level level = player.level();
        BlockPos center = player.blockPosition();

        boolean isVillage = isVillageArea(level, center, radius);

        // Enter-zone hint: runs for ALL players (including creative)
        if (Config.ENABLE_VILLAGE_NO_SMOKING_ENTER_HINT.getAsBoolean()) {
            if (isVillage && !data.wasInVillageZone() && data.villageEnterHintCooldown() <= 0) {
                player.displayClientMessage(Component.translatable("message.SmokingWarningMod.village.no_smoking_enter"), true);
                data.setVillageEnterHintCooldown(Config.VILLAGE_NO_SMOKING_ENTER_HINT_COOLDOWN_TICKS.getAsInt());
            }
            data.setWasInVillageZone(isVillage);
        }

        // Creative players are exempt from smoking penalties
        if (creativeIgnored) {
            return;
        }

        if (!isVillage) {
            return;
        }

        // Only trigger smoking penalties if the player is actually smoking
        if (!(player.isUsingItem() && CigaretteItem.isCigarette(player.getUseItem()))) {
            return;
        }

        // Warning cooldown
        if (data.noSmokingWarningCooldown() > 0) {
            triggerIronGolemAggro(player, level, center);
            return;
        }

        player.displayClientMessage(Component.translatable("message.SmokingWarningMod.village.no_smoking"), true);
        data.setNoSmokingWarningCooldown(Config.VILLAGE_NO_SMOKING_WARNING_COOLDOWN_TICKS.getAsInt());

        // Sound
        level.playSound(null, center, SoundEvents.IRON_GOLEM_HURT, SoundSource.HOSTILE, 0.6F, 0.8F);

        // Particles
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.ANGRY_VILLAGER,
                    player.getX(), player.getY() + 1.0D, player.getZ(),
                    5, 0.3D, 0.3D, 0.3D, 0.05D);
        }

        triggerIronGolemAggro(player, level, center);
    }

    static boolean isVillageArea(Level level, BlockPos center, int radius) {
        AABB area = new AABB(center).inflate(radius);

        int minVillagers = Config.VILLAGE_NO_SMOKING_MIN_VILLAGERS.getAsInt();
        int minBeds = Config.VILLAGE_NO_SMOKING_MIN_BEDS.getAsInt();

        List<Villager> villagers = level.getEntitiesOfClass(Villager.class, area, v -> true);
        if (villagers.size() >= minVillagers) {
            return true;
        }

        long bedCount = BlockPos.betweenClosedStream(area)
                .filter(pos -> level.getBlockState(pos).is(BlockTags.BEDS))
                .limit(minBeds)
                .count();
        if (bedCount >= minBeds) {
            return true;
        }

        boolean hasBell = BlockPos.betweenClosedStream(area)
                .anyMatch(pos -> level.getBlockState(pos).is(Blocks.BELL));
        if (hasBell && !villagers.isEmpty()) {
            return true;
        }

        List<IronGolem> golems = level.getEntitiesOfClass(IronGolem.class, area, g -> true);
        if (!golems.isEmpty() && !villagers.isEmpty()) {
            return true;
        }

        return false;
    }

    private static void triggerIronGolemAggro(ServerPlayer player, Level level, BlockPos center) {
        int aggroRadius = Config.VILLAGE_NO_SMOKING_GOLEM_AGGRO_RADIUS.getAsInt();
        AABB area = new AABB(center).inflate(aggroRadius);

        List<IronGolem> golems = level.getEntitiesOfClass(IronGolem.class, area, g -> true);
        for (IronGolem golem : golems) {
            if (golem.getTarget() == null || golem.getTarget() == player) {
                golem.setTarget(player);
            }
        }
    }

    public static void tickCooldown(PlayerSmokingData data) {
        if (data.noSmokingWarningCooldown() > 0) {
            data.setNoSmokingWarningCooldown(data.noSmokingWarningCooldown() - 1);
        }
        if (data.villageEnterHintCooldown() > 0) {
            data.setVillageEnterHintCooldown(data.villageEnterHintCooldown() - 1);
        }
    }
}
