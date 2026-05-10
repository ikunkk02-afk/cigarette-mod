package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class LungCancerTreatmentManager {
    public static final int STAGE_NONE = 0;
    public static final int STAGE_DIAGNOSED = 1;
    public static final int STAGE_CONTROL = 2;
    public static final int STAGE_TREATING = 3;
    public static final int STAGE_REHAB = 4;
    public static final int STAGE_REMISSION = 5;

    private LungCancerTreatmentManager() {
    }

    public static boolean isHeavyAddictionOrLungCancer(ServerPlayer player) {
        PlayerSmokingData data = PlayerSmokingData.get(player);
        return data.addictionStage() >= SmokingAddictionManager.STAGE_HEAVY
                || player.hasEffect(SmokingWarningMod.LUNG_CANCER)
                || data.lungCancerActive();
    }

    public static boolean tryDiagnose(ServerPlayer player) {
        if (!Config.ENABLE_LUNG_CANCER_TREATMENT.getAsBoolean()) {
            return false;
        }

        PlayerSmokingData data = PlayerSmokingData.get(player);

        if (data.diagnosedLungCancer()) {
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.already_diagnosed"), true);
            return false;
        }

        if (!isHeavyAddictionOrLungCancer(player)) {
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.no_disease"), true);
            return false;
        }

        data.setDiagnosedLungCancer(true);
        data.setTreatmentStage(STAGE_DIAGNOSED);
        data.setTreatmentProgress(0);
        player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.diagnosed"), false);
        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.5F, 1.8F);
        syncToClient(player);
        return true;
    }

    public static boolean canUseTreatmentItem(ServerPlayer player) {
        if (!Config.ENABLE_LUNG_CANCER_TREATMENT.getAsBoolean()) {
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.disabled"), true);
            return false;
        }

        PlayerSmokingData data = PlayerSmokingData.get(player);

        if (!data.diagnosedLungCancer()) {
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.not_diagnosed"), true);
            return false;
        }

        if (data.treatmentStage() >= STAGE_REMISSION) {
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.already_remission"), true);
            return false;
        }

        if (data.treatmentCooldown() > 0) {
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.cooldown"), true);
            return false;
        }

        return true;
    }

    public static boolean applyChemotherapy(ServerPlayer player) {
        if (!canUseTreatmentItem(player)) {
            return false;
        }

        PlayerSmokingData data = PlayerSmokingData.get(player);
        int progress = Config.CHEMOTHERAPY_PROGRESS.getAsInt();
        data.setTreatmentProgress(Math.min(100, data.treatmentProgress() + progress));
        if (data.treatmentStage() < STAGE_CONTROL) {
            data.setTreatmentStage(STAGE_CONTROL);
        }
        applyTreatmentCooldown(data);
        applyChemoSideEffects(player);
        player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.chemo"), false);
        syncToClient(player);
        return true;
    }

    public static boolean applyRadiotherapy(ServerPlayer player) {
        if (!canUseTreatmentItem(player)) {
            return false;
        }

        PlayerSmokingData data = PlayerSmokingData.get(player);

        if (data.treatmentProgress() < 20) {
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.need_more_progress"), true);
            return false;
        }

        int progress = Config.RADIOTHERAPY_PROGRESS.getAsInt();
        data.setTreatmentProgress(Math.min(100, data.treatmentProgress() + progress));
        if (data.treatmentStage() < STAGE_TREATING) {
            data.setTreatmentStage(STAGE_TREATING);
        }
        applyTreatmentCooldown(data);
        applyRadioSideEffects(player);
        player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.radio"), false);
        syncToClient(player);
        return true;
    }

    public static boolean applyTargetedTherapy(ServerPlayer player) {
        if (!canUseTreatmentItem(player)) {
            return false;
        }

        PlayerSmokingData data = PlayerSmokingData.get(player);

        if (data.treatmentProgress() < 40) {
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.need_more_progress"), true);
            return false;
        }

        int progress = Config.TARGETED_THERAPY_PROGRESS.getAsInt();
        data.setTreatmentProgress(Math.min(100, data.treatmentProgress() + progress));
        applyTreatmentCooldown(data);
        applyTargetedTherapySideEffects(player);
        player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.targeted"), false);
        syncToClient(player);
        return true;
    }

    public static boolean applyRehabilitation(ServerPlayer player) {
        if (!Config.ENABLE_LUNG_CANCER_TREATMENT.getAsBoolean()) {
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.disabled"), true);
            return false;
        }

        PlayerSmokingData data = PlayerSmokingData.get(player);

        if (!data.diagnosedLungCancer() || data.treatmentStage() >= STAGE_REMISSION) {
            return false;
        }

        if (data.treatmentProgress() < 80) {
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.need_more_progress"), true);
            return false;
        }

        if (data.treatmentStage() == STAGE_REHAB) {
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.already_rehab"), true);
            return false;
        }

        data.setTreatmentStage(STAGE_REHAB);
        data.setSmokeFreeTicks(0);
        player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.rehab_start"), false);
        syncToClient(player);
        return true;
    }

    public static void tickRehab(ServerPlayer player, PlayerSmokingData data) {
        if (!Config.ENABLE_LUNG_CANCER_TREATMENT.getAsBoolean() || data.treatmentStage() != STAGE_REHAB) {
            return;
        }

        boolean isSmoking = player.isUsingItem() && CigaretteItem.isCigarette(player.getUseItem());

        if (isSmoking) {
            data.setSmokeFreeTicks(0);
            return;
        }

        data.setSmokeFreeTicks(data.smokeFreeTicks() + 1);

        if (data.smokeFreeTicks() >= Config.REHAB_REQUIRED_SMOKE_FREE_TICKS.getAsInt()) {
            completeRehab(player, data);
        }
    }

    private static void completeRehab(ServerPlayer player, PlayerSmokingData data) {
        data.setTreatmentProgress(100);
        data.setTreatmentStage(STAGE_REMISSION);
        data.setSmokeFreeTicks(0);
        data.setDiagnosedLungCancer(false);
        player.removeEffect(SmokingWarningMod.LUNG_CANCER);
        data.setLungCancerActive(false);

        int targetCount = Config.TREATMENT_SUCCESS_SET_SMOKE_COUNT.getAsInt();
        if (targetCount < data.smokedCigaretteCount()) {
            data.setSmokedCigaretteCount(targetCount);
        }

        int newStage = stageForCurrentCount(data.smokedCigaretteCount());
        data.setAddictionStage(newStage);
        SmokingAddictionManager.tick(player, data);

        player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8F, 1.0F);
        player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.rehab_complete"), false);
        syncToClient(player);
    }

    public static void onSmokeDuringTreatment(ServerPlayer player) {
        if (!Config.ENABLE_LUNG_CANCER_TREATMENT.getAsBoolean()) {
            return;
        }

        PlayerSmokingData data = PlayerSmokingData.get(player);
        if (!data.diagnosedLungCancer() || data.treatmentStage() >= STAGE_REMISSION || data.treatmentStage() <= STAGE_NONE) {
            return;
        }

        if (data.treatmentStage() == STAGE_REHAB) {
            data.setTreatmentStage(STAGE_TREATING);
            int penalty = Config.SMOKING_DURING_REHAB_PROGRESS_PENALTY.getAsInt();
            data.setTreatmentProgress(Math.max(0, data.treatmentProgress() - penalty));
            data.setSmokeFreeTicks(0);
            data.setTreatmentFailedRecently(true);
            if (!player.hasEffect(SmokingWarningMod.LUNG_CANCER)) {
                player.addEffect(new MobEffectInstance(SmokingWarningMod.LUNG_CANCER, 1200, 0, false, true, true));
            }
            data.setLungCancerActive(true);
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.rehab_failed"), false);
        } else {
            int penalty = Config.SMOKING_DURING_TREATMENT_PROGRESS_PENALTY.getAsInt();
            data.setTreatmentProgress(Math.max(0, data.treatmentProgress() - penalty));
            data.setSmokeFreeTicks(0);
            data.setTreatmentFailedRecently(true);
            player.displayClientMessage(Component.translatable("message.smokingwarningmod.treatment.smoking_setback"), false);
        }

        syncToClient(player);
    }

    public static String stageName(int stage) {
        return switch (stage) {
            case STAGE_DIAGNOSED -> "diagnosed";
            case STAGE_CONTROL -> "control";
            case STAGE_TREATING -> "treating";
            case STAGE_REHAB -> "rehab";
            case STAGE_REMISSION -> "remission";
            default -> "none";
        };
    }

    private static void applyTreatmentCooldown(PlayerSmokingData data) {
        data.setTreatmentCooldown(Config.TREATMENT_COOLDOWN_TICKS.getAsInt());
    }

    public static void tickCooldown(PlayerSmokingData data) {
        if (data.treatmentCooldown() > 0) {
            data.setTreatmentCooldown(data.treatmentCooldown() - 1);
        }
        if (data.treatmentFailedRecently()) {
            data.setTreatmentFailedRecently(false);
        }
    }

    private static void applyChemoSideEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1200, 1, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.CONFUSION, 300, 0, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 1200, 0, false, true, true));
    }

    private static void applyRadioSideEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 1800, 0, false, true, true));
        player.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 1800, 0, false, true, true));
    }

    private static void applyTargetedTherapySideEffects(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 600, 0, false, true, true));
    }

    private static int stageForCurrentCount(int count) {
        if (count >= Config.HEAVY_ADDICTION_THRESHOLD.getAsInt()) {
            return SmokingAddictionManager.STAGE_HEAVY;
        }
        if (count >= Config.MEDIUM_ADDICTION_THRESHOLD.getAsInt()) {
            return SmokingAddictionManager.STAGE_MEDIUM;
        }
        if (count >= Config.LIGHT_ADDICTION_THRESHOLD.getAsInt()) {
            return SmokingAddictionManager.STAGE_LIGHT;
        }
        return SmokingAddictionManager.STAGE_NONE;
    }

    private static void syncToClient(ServerPlayer player) {
        TreatmentHudSyncPayload.syncTo(player);
    }
}
