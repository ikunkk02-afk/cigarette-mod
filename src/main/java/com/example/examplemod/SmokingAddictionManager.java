package com.example.examplemod;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.phys.Vec3;

public final class SmokingAddictionManager {
    public static final int STAGE_NONE = 0;
    public static final int STAGE_LIGHT = 1;
    public static final int STAGE_MEDIUM = 2;
    public static final int STAGE_HEAVY = 3;

    private static final ResourceLocation MAX_HEALTH_PENALTY_ID = SmokingWarningMod.id("addiction_max_health_penalty");
    private static final int EFFECT_REFRESH_TICKS = 40;
    private static final int EFFECT_DURATION_TICKS = 260;

    private SmokingAddictionManager() {
    }

    public static void onCigaretteFinished(ServerPlayer player, CigaretteItem cigaretteItem) {
        if (!Config.ENABLE_ADDICTION_SYSTEM.getAsBoolean()) {
            return;
        }

        PlayerSmokingData data = PlayerSmokingData.get(player);
        int weight = cigaretteItem.getAddictionWeight();
        int count = data.addSmokedCigarettes(weight);
        data.setLastSmokeTime(player.serverLevel().getGameTime());
        data.addAddiction(Math.max(0, cigaretteItem.addictionGain()));
        data.addLungDamage(Math.max(0, cigaretteItem.lungDamageGain()));

        player.displayClientMessage(Component.translatable("message.SmokingWarningMod.addiction.count", count), false);
        int oldStage = data.addictionStage();
        int newStage = updateStage(player, data, true);
        if (newStage > STAGE_NONE && data.coughCooldown() <= 0) {
            data.setCoughCooldown(initialCoughCooldown(player, newStage));
        }
        if (newStage == STAGE_NONE && oldStage == STAGE_NONE && player.getRandom().nextFloat() < 0.25F) {
            player.displayClientMessage(Component.translatable("message.SmokingWarningMod.throat_irritated"), true);
        }
        tick(player, data);
    }

    public static void tick(ServerPlayer player, PlayerSmokingData data) {
        if (!Config.ENABLE_ADDICTION_SYSTEM.getAsBoolean()) {
            data.setCoughCooldown(0);
            data.setLungCancerActive(false);
            removeMaxHealthPenalty(player);
            return;
        }

        int stage = updateStage(player, data, false);
        applyMaxHealthPenalty(player, stage);

        if (stage <= STAGE_NONE || player.isCreative() || player.isSpectator()) {
            data.setCoughCooldown(0);
            data.setLungCancerActive(false);
            return;
        }

        // Show treatment guide hint even if player loaded into HEAVY stage
        if (stage >= STAGE_HEAVY && !data.hasShownTreatmentGuideHint()) {
            data.setHasShownTreatmentGuideHint(true);
            player.displayClientMessage(Component.translatable("message.SmokingWarningMod.lung_cancer.guide_hint"), false);
        }

        if (player.tickCount % EFFECT_REFRESH_TICKS == 0) {
            applyStageEffects(player, data, stage);
        }

        tickCoughing(player, data, stage);
    }

    public static void applyBreakSpeedPenalty(ServerPlayer player, PlayerSmokingData data, net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed event) {
        if (!Config.ENABLE_ADDICTION_SYSTEM.getAsBoolean() || data.addictionStage() < STAGE_MEDIUM) {
            return;
        }
        if (!player.hasEffect(SmokingWarningMod.COUGHING)) {
            return;
        }

        float multiplier = data.addictionStage() >= STAGE_HEAVY ? 0.45F : 0.75F;
        event.setNewSpeed(event.getNewSpeed() * multiplier);
    }

    private static int updateStage(ServerPlayer player, PlayerSmokingData data, boolean notify) {
        int oldStage = data.addictionStage();
        int newStage = stageForCount(data.smokedCigaretteCount());
        if (oldStage != newStage) {
            data.setAddictionStage(newStage);
            if (newStage < STAGE_HEAVY) {
                data.setLungCancerActive(false);
                data.setHasShownTreatmentGuideHint(false);
            }
            if (notify && newStage > oldStage) {
                sendStageMessages(player, newStage);
            }
        }
        return newStage;
    }

    private static int stageForCount(int count) {
        if (count >= Config.HEAVY_ADDICTION_THRESHOLD.getAsInt()) {
            return STAGE_HEAVY;
        }
        if (count >= Config.MEDIUM_ADDICTION_THRESHOLD.getAsInt()) {
            return STAGE_MEDIUM;
        }
        if (count >= Config.LIGHT_ADDICTION_THRESHOLD.getAsInt()) {
            return STAGE_LIGHT;
        }
        return STAGE_NONE;
    }

    private static void sendStageMessages(ServerPlayer player, int stage) {
        switch (stage) {
            case STAGE_LIGHT -> {
                player.displayClientMessage(Component.translatable("message.SmokingWarningMod.addiction.light"), false);
                player.displayClientMessage(Component.translatable("message.SmokingWarningMod.penalty.light"), true);
            }
            case STAGE_MEDIUM -> {
                player.displayClientMessage(Component.translatable("message.SmokingWarningMod.addiction.medium"), false);
                player.displayClientMessage(Component.translatable("message.SmokingWarningMod.penalty.medium"), true);
            }
            case STAGE_HEAVY -> {
                player.displayClientMessage(Component.translatable("message.SmokingWarningMod.addiction.heavy"), false);
                player.displayClientMessage(Component.translatable("message.SmokingWarningMod.penalty.heavy"), true);
                PlayerSmokingData stageData = PlayerSmokingData.get(player);
                if (!stageData.hasShownTreatmentGuideHint()) {
                    stageData.setHasShownTreatmentGuideHint(true);
                    player.displayClientMessage(Component.translatable("message.SmokingWarningMod.lung_cancer.guide_hint"), false);
                }
            }
            default -> {
            }
        }
    }

    private static void applyStageEffects(ServerPlayer player, PlayerSmokingData data, int stage) {
        removeConflictingPositiveEffects(player, stage);

        if (Config.ENABLE_COUGHING_EFFECT.getAsBoolean()) {
            int coughingAmplifier = switch (stage) {
                case STAGE_LIGHT -> 0;
                case STAGE_MEDIUM -> 1;
                case STAGE_HEAVY -> 2;
                default -> -1;
            };
            if (coughingAmplifier >= 0) {
                addEffect(player, SmokingWarningMod.COUGHING, EFFECT_DURATION_TICKS, coughingAmplifier);
            }
        }

        if (stage >= STAGE_MEDIUM) {
            addEffect(player, MobEffects.WEAKNESS, EFFECT_DURATION_TICKS, stage >= STAGE_HEAVY ? 1 : 0);
            addEffect(player, MobEffects.DIG_SLOWDOWN, EFFECT_DURATION_TICKS, stage >= STAGE_HEAVY ? 2 : 0);
            addEffect(player, MobEffects.HUNGER, EFFECT_DURATION_TICKS, stage >= STAGE_HEAVY ? 1 : 0);
        }

        if (stage >= STAGE_HEAVY) {
            if (Config.ENABLE_LUNG_CANCER_EFFECT.getAsBoolean()) {
                addEffect(player, SmokingWarningMod.LUNG_CANCER, EFFECT_DURATION_TICKS, 0);
                if (!data.lungCancerActive()) {
                    data.setLungCancerActive(true);
                    playLungCancerSound(player, 0.85F);
                }
            }
            addEffect(player, MobEffects.MOVEMENT_SLOWDOWN, EFFECT_DURATION_TICKS, 1);
        }
    }

    private static void removeConflictingPositiveEffects(ServerPlayer player, int stage) {
        if (stage >= STAGE_MEDIUM) {
            player.removeEffect(MobEffects.MOVEMENT_SPEED);
            player.removeEffect(MobEffects.DIG_SPEED);
            player.removeEffect(MobEffects.DAMAGE_BOOST);
            player.removeEffect(MobEffects.SATURATION);
        }

        if (stage >= STAGE_HEAVY) {
            player.removeEffect(MobEffects.REGENERATION);
        }
    }

    private static void tickCoughing(ServerPlayer player, PlayerSmokingData data, int stage) {
        if (!Config.ENABLE_COUGHING_EFFECT.getAsBoolean()) {
            data.setCoughCooldown(0);
            return;
        }

        int cooldown = data.coughCooldown();
        int decrement = stage >= STAGE_MEDIUM && player.isSprinting() ? 2 : 1;
        cooldown = Math.max(0, cooldown - decrement);
        if (cooldown > 0) {
            data.setCoughCooldown(cooldown);
            return;
        }

        triggerCough(player, stage);
        data.setCoughCooldown(nextCoughCooldown(player, stage));
    }

    private static int nextCoughCooldown(ServerPlayer player, int stage) {
        int base = switch (stage) {
            case STAGE_LIGHT -> Config.COUGH_SOUND_INTERVAL_LIGHT.getAsInt();
            case STAGE_MEDIUM -> Config.COUGH_SOUND_INTERVAL_MEDIUM.getAsInt();
            case STAGE_HEAVY -> Config.COUGH_SOUND_INTERVAL_HEAVY.getAsInt();
            default -> 1200;
        };
        int jitter = Math.max(20, base / 5);
        return Math.max(40, base - jitter / 2 + player.getRandom().nextInt(jitter + 1));
    }

    private static int initialCoughCooldown(ServerPlayer player, int stage) {
        int firstInterval = Math.max(60, nextCoughCooldown(player, stage) / 2);
        return firstInterval + player.getRandom().nextInt(Math.max(20, firstInterval / 3));
    }

    private static void triggerCough(ServerPlayer player, int stage) {
        ServerLevel level = player.serverLevel();
        boolean severe = stage >= STAGE_HEAVY && player.getRandom().nextFloat() < 0.45F;
        if (severe && Config.ENABLE_LUNG_CANCER_EFFECT.getAsBoolean()) {
            playLungCancerSound(player, 0.95F);
        } else {
            level.playSound(null, player.blockPosition(), SmokingWarningMod.COUGHING_SOUND.get(), SoundSource.PLAYERS, 0.75F, 0.9F + player.getRandom().nextFloat() * 0.15F);
        }

        player.setSprinting(false);
        Vec3 movement = player.getDeltaMovement();
        double horizontalScale = stage >= STAGE_HEAVY ? 0.25D : stage >= STAGE_MEDIUM ? 0.45D : 0.65D;
        player.setDeltaMovement(movement.x * horizontalScale, movement.y, movement.z * horizontalScale);
        player.hurtMarked = true;

        int pauseTicks = stage >= STAGE_HEAVY ? 45 : stage >= STAGE_MEDIUM ? 28 : 12;
        addEffect(player, MobEffects.MOVEMENT_SLOWDOWN, pauseTicks, stage >= STAGE_HEAVY ? 1 : 0);

        if (stage >= STAGE_MEDIUM && player.isUsingItem() && player.getRandom().nextFloat() < (stage >= STAGE_HEAVY ? 0.7F : 0.25F)) {
            player.stopUsingItem();
            player.displayClientMessage(Component.translatable("message.SmokingWarningMod.cough_interrupt"), true);
        } else if (stage == STAGE_LIGHT && player.getRandom().nextFloat() < 0.3F) {
            addEffect(player, MobEffects.HUNGER, 80, 0);
        }

        if (stage >= STAGE_HEAVY && player.getRandom().nextFloat() < 0.25F) {
            addEffect(player, MobEffects.WEAKNESS, 100, 1);
            addEffect(player, MobEffects.MOVEMENT_SLOWDOWN, 80, 1);
            player.displayClientMessage(Component.translatable("message.SmokingWarningMod.chest_tightness"), true);
        }
    }

    private static void playLungCancerSound(ServerPlayer player, float volume) {
        player.serverLevel().playSound(null, player.blockPosition(), SmokingWarningMod.LUNG_CANCER_SOUND.get(), SoundSource.PLAYERS, volume, 0.65F + player.getRandom().nextFloat() * 0.1F);
    }

    private static void applyMaxHealthPenalty(ServerPlayer player, int stage) {
        double penalty = switch (stage) {
            case STAGE_MEDIUM -> Config.MEDIUM_MAX_HEALTH_PENALTY.getAsDouble();
            case STAGE_HEAVY -> Config.ENABLE_SEVERE_PUNISHMENTS.getAsBoolean()
                    ? Config.SEVERE_HEAVY_MAX_HEALTH_PENALTY.getAsDouble()
                    : Config.HEAVY_MAX_HEALTH_PENALTY.getAsDouble();
            default -> 0.0D;
        };

        if (penalty <= 0.0D) {
            removeMaxHealthPenalty(player);
            return;
        }

        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth == null) {
            return;
        }

        maxHealth.addOrUpdateTransientModifier(new AttributeModifier(MAX_HEALTH_PENALTY_ID, -penalty, AttributeModifier.Operation.ADD_VALUE));
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void removeMaxHealthPenalty(ServerPlayer player) {
        AttributeInstance maxHealth = player.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealth != null && maxHealth.hasModifier(MAX_HEALTH_PENALTY_ID)) {
            maxHealth.removeModifier(MAX_HEALTH_PENALTY_ID);
        }
    }

    private static void addEffect(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int duration, int amplifier) {
        if (duration <= 0) {
            return;
        }
        player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, true, true));
    }
}
