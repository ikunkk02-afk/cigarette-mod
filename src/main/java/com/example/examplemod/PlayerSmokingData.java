package com.example.examplemod;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.entity.player.Player;

public class PlayerSmokingData {
    private static final String ROOT = "cigarettemod_player_data";
    private static final String ADDICTION = "addiction";
    private static final String LUNG_DAMAGE = "lungDamage";
    private static final String WITHDRAWAL_TICKS = "withdrawalTicks";
    private static final String SMOKING_TICKS = "smokingTicks";
    private static final String LAST_PENALTY_STAGE = "lastPenaltyStage";
    private static final String SMOKED_CIGARETTE_COUNT = "smokedCigaretteCount";
    private static final String ADDICTION_STAGE = "addictionStage";
    private static final String LAST_SMOKE_TIME = "lastSmokeTime";
    private static final String COUGH_COOLDOWN = "coughCooldown";
    private static final String LUNG_CANCER_ACTIVE = "lungCancerActive";
    private static final String LUNG_CANCER_SMOKING_MESSAGE_COOLDOWN = "lungCancerSmokingMessageCooldown";
    private static final String DIAGNOSED_LUNG_CANCER = "diagnosedLungCancer";
    private static final String TREATMENT_STAGE = "treatmentStage";
    private static final String TREATMENT_PROGRESS = "treatmentProgress";
    private static final String TREATMENT_COOLDOWN = "treatmentCooldown";
    private static final String SMOKE_FREE_TICKS = "smokeFreeTicks";
    private static final String TREATMENT_FAILED_RECENTLY = "treatmentFailedRecently";
    private static final String HAS_SHOWN_TREATMENT_GUIDE_HINT = "hasShownTreatmentGuideHint";
    private static final String NO_SMOKING_WARNING_COOLDOWN = "noSmokingWarningCooldown";
    private static final String VILLAGE_ENTER_HINT_COOLDOWN = "villageEnterHintCooldown";
    private static final String WAS_IN_VILLAGE_ZONE = "wasInVillageZone";

    private final CompoundTag tag;

    private PlayerSmokingData(CompoundTag tag) {
        this.tag = tag;
    }

    public static PlayerSmokingData get(Player player) {
        CompoundTag persistentData = player.getPersistentData();
        if (!persistentData.contains(ROOT, Tag.TAG_COMPOUND)) {
            persistentData.put(ROOT, new CompoundTag());
        }
        return new PlayerSmokingData(persistentData.getCompound(ROOT));
    }

    public static void copy(Player original, Player target) {
        copy(original, target, false);
    }

    public static void copy(Player original, Player target, boolean resetCoreAddiction) {
        CompoundTag source = original.getPersistentData();
        if (source.contains(ROOT, Tag.TAG_COMPOUND)) {
            CompoundTag copied = source.getCompound(ROOT).copy();
            if (resetCoreAddiction) {
                resetCoreAddiction(copied);
            }
            target.getPersistentData().put(ROOT, copied);
        }
    }

    public int addiction() {
        return tag.getInt(ADDICTION);
    }

    public void setAddiction(int value) {
        tag.putInt(ADDICTION, clamp(value));
    }

    public void addAddiction(int value) {
        setAddiction(addiction() + value);
    }

    public int lungDamage() {
        return tag.getInt(LUNG_DAMAGE);
    }

    public void setLungDamage(int value) {
        tag.putInt(LUNG_DAMAGE, clamp(value));
    }

    public void addLungDamage(int value) {
        setLungDamage(lungDamage() + value);
    }

    public int withdrawalTicks() {
        return tag.getInt(WITHDRAWAL_TICKS);
    }

    public void setWithdrawalTicks(int value) {
        tag.putInt(WITHDRAWAL_TICKS, Math.max(0, value));
    }

    public int smokingTicks() {
        return tag.getInt(SMOKING_TICKS);
    }

    public void setSmokingTicks(int value) {
        tag.putInt(SMOKING_TICKS, Math.max(0, value));
    }

    public int lastPenaltyStage() {
        return tag.getInt(LAST_PENALTY_STAGE);
    }

    public void setLastPenaltyStage(int value) {
        tag.putInt(LAST_PENALTY_STAGE, value);
    }

    public int smokedCigaretteCount() {
        return tag.getInt(SMOKED_CIGARETTE_COUNT);
    }

    public void setSmokedCigaretteCount(int value) {
        tag.putInt(SMOKED_CIGARETTE_COUNT, Math.max(0, value));
    }

    public int addSmokedCigarettes(int value) {
        if (value <= 0) {
            return smokedCigaretteCount();
        }
        long next = (long) smokedCigaretteCount() + value;
        setSmokedCigaretteCount(next > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) next);
        return smokedCigaretteCount();
    }

    public int addictionStage() {
        return tag.getInt(ADDICTION_STAGE);
    }

    public void setAddictionStage(int value) {
        tag.putInt(ADDICTION_STAGE, Math.clamp(value, 0, 3));
    }

    public long lastSmokeTime() {
        return tag.getLong(LAST_SMOKE_TIME);
    }

    public void setLastSmokeTime(long value) {
        tag.putLong(LAST_SMOKE_TIME, Math.max(0L, value));
    }

    public int coughCooldown() {
        return tag.getInt(COUGH_COOLDOWN);
    }

    public void setCoughCooldown(int value) {
        tag.putInt(COUGH_COOLDOWN, Math.max(0, value));
    }

    public boolean lungCancerActive() {
        return tag.getBoolean(LUNG_CANCER_ACTIVE);
    }

    public void setLungCancerActive(boolean value) {
        tag.putBoolean(LUNG_CANCER_ACTIVE, value);
    }

    public int lungCancerSmokingMessageCooldown() {
        return tag.getInt(LUNG_CANCER_SMOKING_MESSAGE_COOLDOWN);
    }

    public void setLungCancerSmokingMessageCooldown(int value) {
        tag.putInt(LUNG_CANCER_SMOKING_MESSAGE_COOLDOWN, Math.max(0, value));
    }

    public boolean diagnosedLungCancer() {
        return tag.getBoolean(DIAGNOSED_LUNG_CANCER);
    }

    public void setDiagnosedLungCancer(boolean value) {
        tag.putBoolean(DIAGNOSED_LUNG_CANCER, value);
    }

    public int treatmentStage() {
        return tag.getInt(TREATMENT_STAGE);
    }

    public void setTreatmentStage(int value) {
        tag.putInt(TREATMENT_STAGE, Math.clamp(value, 0, 5));
    }

    public int treatmentProgress() {
        return tag.getInt(TREATMENT_PROGRESS);
    }

    public void setTreatmentProgress(int value) {
        tag.putInt(TREATMENT_PROGRESS, Math.clamp(value, 0, 100));
    }

    public int treatmentCooldown() {
        return tag.getInt(TREATMENT_COOLDOWN);
    }

    public void setTreatmentCooldown(int value) {
        tag.putInt(TREATMENT_COOLDOWN, Math.max(0, value));
    }

    public int smokeFreeTicks() {
        return tag.getInt(SMOKE_FREE_TICKS);
    }

    public void setSmokeFreeTicks(int value) {
        tag.putInt(SMOKE_FREE_TICKS, Math.max(0, value));
    }

    public boolean treatmentFailedRecently() {
        return tag.getBoolean(TREATMENT_FAILED_RECENTLY);
    }

    public void setTreatmentFailedRecently(boolean value) {
        tag.putBoolean(TREATMENT_FAILED_RECENTLY, value);
    }

    public boolean hasShownTreatmentGuideHint() {
        return tag.getBoolean(HAS_SHOWN_TREATMENT_GUIDE_HINT);
    }

    public void setHasShownTreatmentGuideHint(boolean value) {
        tag.putBoolean(HAS_SHOWN_TREATMENT_GUIDE_HINT, value);
    }

    public int noSmokingWarningCooldown() {
        return tag.getInt(NO_SMOKING_WARNING_COOLDOWN);
    }

    public void setNoSmokingWarningCooldown(int value) {
        tag.putInt(NO_SMOKING_WARNING_COOLDOWN, Math.max(0, value));
    }

    public int villageEnterHintCooldown() {
        return tag.getInt(VILLAGE_ENTER_HINT_COOLDOWN);
    }

    public void setVillageEnterHintCooldown(int value) {
        tag.putInt(VILLAGE_ENTER_HINT_COOLDOWN, Math.max(0, value));
    }

    public boolean wasInVillageZone() {
        return tag.getBoolean(WAS_IN_VILLAGE_ZONE);
    }

    public void setWasInVillageZone(boolean value) {
        tag.putBoolean(WAS_IN_VILLAGE_ZONE, value);
    }

    public void resetCoreAddiction() {
        resetCoreAddiction(this.tag);
    }

    private static void resetCoreAddiction(CompoundTag tag) {
        tag.putInt(SMOKED_CIGARETTE_COUNT, 0);
        tag.putInt(ADDICTION_STAGE, 0);
        tag.putLong(LAST_SMOKE_TIME, 0L);
        tag.putInt(COUGH_COOLDOWN, 0);
        tag.putBoolean(LUNG_CANCER_ACTIVE, false);
        tag.putInt(LUNG_CANCER_SMOKING_MESSAGE_COOLDOWN, 0);
        tag.putInt(ADDICTION, 0);
        tag.putInt(LUNG_DAMAGE, 0);
        tag.putInt(WITHDRAWAL_TICKS, 0);
    }

    private static int clamp(int value) {
        return Math.clamp(value, 0, 100);
    }
}
