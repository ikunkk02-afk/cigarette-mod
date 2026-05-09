package com.example.examplemod;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;

public final class AddictionHudSync {
    private static final int PERIODIC_SYNC_TICKS = 100;

    private AddictionHudSync() {
    }

    public static void syncTo(ServerPlayer player) {
        syncTo(player, PlayerSmokingData.get(player));
    }

    public static void syncTo(ServerPlayer player, PlayerSmokingData data) {
        PacketDistributor.sendToPlayer(player, snapshot(player, data));
    }

    public static void syncPeriodically(ServerPlayer player, PlayerSmokingData data) {
        if (player.tickCount % PERIODIC_SYNC_TICKS == 0) {
            syncTo(player, data);
        }
    }

    private static AddictionHudSyncPayload snapshot(ServerPlayer player, PlayerSmokingData data) {
        int stage = data.addictionStage();
        boolean isSmoking = player.isUsingItem() && CigaretteItem.isCigarette(player.getUseItem());
        boolean hasCoughing = Config.ENABLE_COUGHING_EFFECT.getAsBoolean()
                && (stage >= SmokingAddictionManager.STAGE_LIGHT || player.hasEffect(cigaretteMod.COUGHING));
        boolean hasLungCancer = stage >= SmokingAddictionManager.STAGE_HEAVY
                || data.lungCancerActive()
                || player.hasEffect(cigaretteMod.LUNG_CANCER);
        return new AddictionHudSyncPayload(
                data.smokedCigaretteCount(),
                stage,
                hasCoughing,
                hasLungCancer,
                isSmoking);
    }
}
