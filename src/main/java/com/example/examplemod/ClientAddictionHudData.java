package com.example.examplemod;

public final class ClientAddictionHudData {
    private static Snapshot snapshot = Snapshot.empty();

    private ClientAddictionHudData() {
    }

    public static void apply(AddictionHudSyncPayload payload) {
        snapshot = new Snapshot(
                true,
                Math.max(0, payload.smokedCigaretteCount()),
                Math.clamp(payload.addictionStage(), SmokingAddictionManager.STAGE_NONE, SmokingAddictionManager.STAGE_HEAVY),
                payload.hasCoughing(),
                payload.hasLungCancer(),
                payload.isSmoking());
    }

    public static void clear() {
        snapshot = Snapshot.empty();
    }

    public static Snapshot snapshot() {
        return snapshot;
    }

    public record Snapshot(
            boolean hasServerData,
            int smokedCigaretteCount,
            int addictionStage,
            boolean hasCoughing,
            boolean hasLungCancer,
            boolean isSmoking) {
        static Snapshot empty() {
            return new Snapshot(false, 0, SmokingAddictionManager.STAGE_NONE, false, false, false);
        }
    }
}
