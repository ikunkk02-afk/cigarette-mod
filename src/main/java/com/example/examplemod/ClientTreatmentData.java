package com.example.examplemod;

public final class ClientTreatmentData {
    private static Snapshot snapshot = Snapshot.empty();

    private ClientTreatmentData() {
    }

    public static void apply(TreatmentHudSyncPayload payload) {
        snapshot = new Snapshot(
                payload.diagnosedLungCancer(),
                Math.clamp(payload.treatmentStage(), 0, 5),
                Math.clamp(payload.treatmentProgress(), 0, 100),
                Math.max(0, payload.smokeFreeTicks()),
                Math.max(0, payload.treatmentCooldown()));
    }

    public static void clear() {
        snapshot = Snapshot.empty();
    }

    public static Snapshot snapshot() {
        return snapshot;
    }

    public record Snapshot(
            boolean diagnosedLungCancer,
            int treatmentStage,
            int treatmentProgress,
            int smokeFreeTicks,
            int treatmentCooldown) {
        static Snapshot empty() {
            return new Snapshot(false, 0, 0, 0, 0);
        }
    }
}
