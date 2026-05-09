package com.example.examplemod;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config {
    public static final int DEFAULT_FULL_SMOKING_DURATION_TICKS = 200;
    public static final int DEFAULT_VARIANT_FULL_SMOKING_DURATION_TICKS = 200;
    public static final int DEFAULT_SMOKING_INTERVAL_TICKS = 20;
    public static final int DEFAULT_CIGARETTE_DURABILITY = 10;
    public static final int DEFAULT_VARIANT_CIGARETTE_DURABILITY = 10;
    public static final int DEFAULT_RICK_V_CIGARETTE_DURABILITY = 10;
    public static final int DEFAULT_LIGHTER_DURABILITY = 64;

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue ENABLE_SMOKING_SYSTEM = BUILDER
            .comment("Master switch for the basic cigarette use loop.")
            .define("enableSmokingSystem", true);

    public static final ModConfigSpec.BooleanValue ENABLE_ADDICTION_SYSTEM = BUILDER
            .comment("Whether fully finished cigarettes increase persistent addiction stage data and long-term penalties.")
            .define("enableAddictionSystem", true);

    public static final ModConfigSpec.BooleanValue ENABLE_SECOND_HAND_SMOKE = BUILDER
            .comment("Whether active smokers affect nearby players.")
            .define("enableSecondHandSmoke", true);

    public static final ModConfigSpec.IntValue ADDICTION_PER_INTERVAL = BUILDER
            .comment("Addiction gained each smoking interval.")
            .defineInRange("addictionPerInterval", 1, 0, 100);

    public static final ModConfigSpec.IntValue LUNG_DAMAGE_PER_INTERVAL = BUILDER
            .comment("Lung damage gained each smoking interval.")
            .defineInRange("lungDamagePerInterval", 1, 0, 100);

    public static final ModConfigSpec.IntValue SMOKING_INTERVAL_TICKS = BUILDER
            .comment("Ticks between smoking durability pulses. Default 20 means one durability point per second.")
            .defineInRange("smokingIntervalTicks", DEFAULT_SMOKING_INTERVAL_TICKS, 20, 12000);

    public static final ModConfigSpec.IntValue FULL_SMOKING_DURATION_TICKS = BUILDER
            .comment("Ticks required to finish one complete cigarette use session. Default 200 is 10 seconds.")
            .defineInRange("fullSmokingDurationTicks", DEFAULT_FULL_SMOKING_DURATION_TICKS, 200, 72000);

    public static final ModConfigSpec.IntValue CIGARETTE_DURABILITY = BUILDER
            .comment("Maximum durability for newly created regular cigarettes. Default 10 matches 10 seconds at one point per second.")
            .defineInRange("cigaretteDurability", DEFAULT_CIGARETTE_DURABILITY, 1, 600);

    public static final ModConfigSpec.IntValue VARIANT_CIGARETTE_DURABILITY = BUILDER
            .comment("Maximum durability for newly created variant cigarettes. Default 10 matches 10 seconds at one point per second.")
            .defineInRange("variantCigaretteDurability", DEFAULT_VARIANT_CIGARETTE_DURABILITY, 1, 600);

    public static final ModConfigSpec.IntValue RICK_V_CIGARETTE_DURABILITY = BUILDER
            .comment("Maximum durability for newly created Rick V cigarettes. Default 10 matches 10 seconds at one point per second.")
            .defineInRange("rickVCigaretteDurability", DEFAULT_RICK_V_CIGARETTE_DURABILITY, 1, 600);

    public static final ModConfigSpec.IntValue LIGHT_ADDICTION_THRESHOLD = BUILDER
            .comment("Fully finished cigarette count required for light addiction.")
            .defineInRange("lightAddictionThreshold", 5, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue MEDIUM_ADDICTION_THRESHOLD = BUILDER
            .comment("Fully finished cigarette count required for medium addiction.")
            .defineInRange("mediumAddictionThreshold", 10, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.IntValue HEAVY_ADDICTION_THRESHOLD = BUILDER
            .comment("Fully finished cigarette count required for heavy addiction.")
            .defineInRange("heavyAddictionThreshold", 20, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue ALLOW_DEATH_RESET_ADDICTION = BUILDER
            .comment("If true, death clears the persistent smoking count and addiction stage.")
            .define("allowDeathResetAddiction", false);

    public static final ModConfigSpec.BooleanValue ENABLE_COUGHING_EFFECT = BUILDER
            .comment("Whether addiction stages apply the custom coughing effect and cough behavior.")
            .define("enableCoughingEffect", true);

    public static final ModConfigSpec.BooleanValue ENABLE_LUNG_CANCER_EFFECT = BUILDER
            .comment("Whether heavy addiction reapplies the custom lung cancer effect.")
            .define("enableLungCancerEffect", true);

    public static final ModConfigSpec.BooleanValue ENABLE_LUNG_CANCER_SMOKING_DAMAGE = BUILDER
            .comment("Whether smoking while already in the lung cancer stage causes extra health damage.")
            .define("enableLungCancerSmokingDamage", true);

    public static final ModConfigSpec.BooleanValue ENABLE_ADDICTION_HUD = BUILDER
            .comment("Whether the client HUD shows synchronized cigarette addiction status.")
            .define("enableAddictionHud", true);

    public static final ModConfigSpec.IntValue ADDICTION_HUD_X = BUILDER
            .comment("Addiction HUD X position in scaled GUI pixels.")
            .defineInRange("addictionHudX", 8, 0, 10000);

    public static final ModConfigSpec.IntValue ADDICTION_HUD_Y = BUILDER
            .comment("Addiction HUD Y position in scaled GUI pixels.")
            .defineInRange("addictionHudY", 8, 0, 10000);

    public static final ModConfigSpec.DoubleValue ADDICTION_HUD_SCALE = BUILDER
            .comment("Addiction HUD scale.")
            .defineInRange("addictionHudScale", 1.0D, 0.5D, 2.0D);

    public static final ModConfigSpec.BooleanValue SHOW_ADDICTION_HUD_ONLY_AFTER_SMOKING = BUILDER
            .comment("If true, the addiction HUD stays hidden until the player has smoked or entered an addiction state.")
            .define("showAddictionHudOnlyAfterSmoking", true);

    public static final ModConfigSpec.BooleanValue SHOW_LUNG_CANCER_WARNING = BUILDER
            .comment("Whether the addiction HUD shows the lung cancer smoking damage warning.")
            .define("showLungCancerWarning", true);

    public static final ModConfigSpec.IntValue LUNG_CANCER_SMOKING_DAMAGE_INTERVAL_TICKS = BUILDER
            .comment("Ticks between health damage pulses while smoking in the lung cancer stage.")
            .defineInRange("lungCancerSmokingDamageIntervalTicks", 40, 1, 72000);

    public static final ModConfigSpec.DoubleValue LUNG_CANCER_SMOKING_DAMAGE_AMOUNT = BUILDER
            .comment("Health points lost per smoking damage pulse in the lung cancer stage.")
            .defineInRange("lungCancerSmokingDamageAmount", 1.0D, 0.0D, 40.0D);

    public static final ModConfigSpec.DoubleValue LUNG_CANCER_FINISH_DAMAGE_NORMAL = BUILDER
            .comment("Extra health points lost after fully smoking a regular cigarette in the lung cancer stage.")
            .defineInRange("lungCancerFinishDamageNormal", 2.0D, 0.0D, 40.0D);

    public static final ModConfigSpec.DoubleValue LUNG_CANCER_FINISH_DAMAGE_VARIANT = BUILDER
            .comment("Extra health points lost after fully smoking a variant cigarette in the lung cancer stage.")
            .defineInRange("lungCancerFinishDamageVariant", 3.0D, 0.0D, 40.0D);

    public static final ModConfigSpec.DoubleValue LUNG_CANCER_FINISH_DAMAGE_SPECIAL = BUILDER
            .comment("Extra health points lost after fully smoking a Huazi or Lotus cigarette in the lung cancer stage.")
            .defineInRange("lungCancerFinishDamageSpecial", 4.0D, 0.0D, 40.0D);

    public static final ModConfigSpec.DoubleValue LUNG_CANCER_FINISH_DAMAGE_RICK_V = BUILDER
            .comment("Extra health points lost after fully smoking Rick V in the lung cancer stage.")
            .defineInRange("lungCancerFinishDamageRickV", 6.0D, 0.0D, 40.0D);

    public static final ModConfigSpec.BooleanValue ENABLE_SEVERE_PUNISHMENTS = BUILDER
            .comment("Whether addiction stages can reduce maximum health with an attribute modifier.")
            .define("enableSeverePunishments", true);

    public static final ModConfigSpec.DoubleValue MEDIUM_MAX_HEALTH_PENALTY = BUILDER
            .comment("Maximum health points removed at medium addiction.")
            .defineInRange("mediumMaxHealthPenalty", 2.0D, 0.0D, 40.0D);

    public static final ModConfigSpec.DoubleValue HEAVY_MAX_HEALTH_PENALTY = BUILDER
            .comment("Maximum health points removed at heavy addiction when severe punishments are disabled.")
            .defineInRange("heavyMaxHealthPenalty", 6.0D, 0.0D, 40.0D);

    public static final ModConfigSpec.DoubleValue SEVERE_HEAVY_MAX_HEALTH_PENALTY = BUILDER
            .comment("Maximum health points removed at heavy addiction when severe punishments are enabled.")
            .defineInRange("severeHeavyMaxHealthPenalty", 8.0D, 0.0D, 40.0D);

    public static final ModConfigSpec.IntValue COUGH_SOUND_INTERVAL_LIGHT = BUILDER
            .comment("Average ticks between cough sounds at light addiction.")
            .defineInRange("coughSoundIntervalLight", 600, 40, 72000);

    public static final ModConfigSpec.IntValue COUGH_SOUND_INTERVAL_MEDIUM = BUILDER
            .comment("Average ticks between cough sounds at medium addiction.")
            .defineInRange("coughSoundIntervalMedium", 300, 40, 72000);

    public static final ModConfigSpec.IntValue COUGH_SOUND_INTERVAL_HEAVY = BUILDER
            .comment("Average ticks between cough sounds at heavy addiction.")
            .defineInRange("coughSoundIntervalHeavy", 120, 40, 72000);

    public static final ModConfigSpec.DoubleValue SECOND_HAND_SMOKE_RADIUS = BUILDER
            .comment("Radius in blocks for secondhand smoke checks.")
            .defineInRange("secondHandSmokeRadius", 8.0D, 1.0D, 32.0D);

    public static final ModConfigSpec.BooleanValue ENABLE_FINISH_DIZZY_VISUAL = BUILDER
            .comment("Whether fully finished cigarettes trigger a client-only dizzy visual effect.")
            .define("enableFinishDizzyVisual", true);

    public static final ModConfigSpec.IntValue FINISH_DIZZY_VISUAL_TICKS = BUILDER
            .comment("Client-only dizzy visual duration after finishing a normal cigarette, in ticks.")
            .defineInRange("finishDizzyVisualTicks", 100, 0, 12000);

    public static final ModConfigSpec.IntValue CIGARETTE_REWARD_DURATION_TICKS = BUILDER
            .comment("Positive reward effect duration after fully finishing a cigarette, in ticks.")
            .defineInRange("cigaretteRewardDurationTicks", 1200, 0, 72000);

    public static final ModConfigSpec.DoubleValue LIGHT_REWARD_DURATION_MULTIPLIER = BUILDER
            .comment("Positive smoking reward duration multiplier at light addiction.")
            .defineInRange("lightRewardDurationMultiplier", 0.7D, 0.0D, 1.0D);

    public static final ModConfigSpec.DoubleValue MEDIUM_REWARD_DURATION_MULTIPLIER = BUILDER
            .comment("Positive smoking reward duration multiplier at medium addiction.")
            .defineInRange("mediumRewardDurationMultiplier", 0.4D, 0.0D, 1.0D);

    public static final ModConfigSpec.DoubleValue HEAVY_REWARD_DURATION_MULTIPLIER = BUILDER
            .comment("Positive smoking reward duration multiplier at heavy addiction or lung cancer stage.")
            .defineInRange("heavyRewardDurationMultiplier", 0.15D, 0.0D, 1.0D);

    public static final ModConfigSpec.IntValue MEDIUM_REWARD_AMPLIFIER_PENALTY = BUILDER
            .comment("Positive smoking reward amplifier levels removed at medium addiction.")
            .defineInRange("mediumRewardAmplifierPenalty", 1, 0, 16);

    public static final ModConfigSpec.IntValue HEAVY_REWARD_AMPLIFIER_PENALTY = BUILDER
            .comment("Positive smoking reward amplifier levels removed at heavy addiction or lung cancer stage.")
            .defineInRange("heavyRewardAmplifierPenalty", 2, 0, 16);

    public static final ModConfigSpec.IntValue RICK_V_DIZZY_VISUAL_TICKS = BUILDER
            .comment("Client-only dizzy visual duration after finishing Rick V, in ticks.")
            .defineInRange("rickVDizzyVisualTicks", 160, 0, 12000);

    public static final ModConfigSpec.BooleanValue ENABLE_RICK_V_EASTER_EGG = BUILDER
            .comment("Whether Rick V uses its special finish effects, sound, and messages.")
            .define("enableRickVEasterEgg", true);

    public static final ModConfigSpec.BooleanValue ENABLE_LUNG_CANCER_TREATMENT = BUILDER
            .comment("Master switch for the lung cancer treatment system.")
            .define("enableLungCancerTreatment", true);

    public static final ModConfigSpec.IntValue CHEMOTHERAPY_PROGRESS = BUILDER
            .comment("Treatment progress added by chemotherapy medicine.")
            .defineInRange("chemotherapyProgress", 10, 1, 100);

    public static final ModConfigSpec.IntValue RADIOTHERAPY_PROGRESS = BUILDER
            .comment("Treatment progress added by radiotherapy core.")
            .defineInRange("radiotherapyProgress", 20, 1, 100);

    public static final ModConfigSpec.IntValue TARGETED_THERAPY_PROGRESS = BUILDER
            .comment("Treatment progress added by targeted therapy medicine.")
            .defineInRange("targetedTherapyProgress", 25, 1, 100);

    public static final ModConfigSpec.IntValue TREATMENT_COOLDOWN_TICKS = BUILDER
            .comment("Cooldown in ticks between major treatment items (6000 ticks = ~5 minutes).")
            .defineInRange("treatmentCooldownTicks", 6000, 0, 720000);

    public static final ModConfigSpec.IntValue REHAB_REQUIRED_SMOKE_FREE_TICKS = BUILDER
            .comment("Ticks the player must stay smoke-free during rehabilitation (12000 ticks = ~10 minutes).")
            .defineInRange("rehabRequiredSmokeFreeTicks", 12000, 6000, 720000);

    public static final ModConfigSpec.IntValue SMOKING_DURING_TREATMENT_PROGRESS_PENALTY = BUILDER
            .comment("Treatment progress lost when smoking during treatment.")
            .defineInRange("smokingDuringTreatmentProgressPenalty", 10, 0, 100);

    public static final ModConfigSpec.IntValue SMOKING_DURING_REHAB_PROGRESS_PENALTY = BUILDER
            .comment("Treatment progress lost when smoking during rehabilitation stage.")
            .defineInRange("smokingDuringRehabProgressPenalty", 20, 0, 100);

    public static final ModConfigSpec.IntValue TREATMENT_SUCCESS_SET_SMOKE_COUNT = BUILDER
            .comment("Smoked cigarette count set after successful treatment completion.")
            .defineInRange("treatmentSuccessSetSmokeCount", 10, 1, Integer.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue ENABLE_VILLAGE_NO_SMOKING_ZONE = BUILDER
            .comment("Whether smoking near villages triggers iron golem aggression.")
            .define("enableVillageNoSmokingZone", true);

    public static final ModConfigSpec.IntValue VILLAGE_NO_SMOKING_CHECK_RADIUS = BUILDER
            .comment("Radius in blocks to check for village features around a smoking player.")
            .defineInRange("villageNoSmokingCheckRadius", 48, 8, 256);

    public static final ModConfigSpec.IntValue VILLAGE_NO_SMOKING_GOLEM_AGGRO_RADIUS = BUILDER
            .comment("Radius in blocks to search for iron golems to aggro on the smoking player.")
            .defineInRange("villageNoSmokingGolemAggroRadius", 32, 4, 128);

    public static final ModConfigSpec.IntValue VILLAGE_NO_SMOKING_MIN_VILLAGERS = BUILDER
            .comment("Minimum villagers within check radius to trigger the no-smoking zone.")
            .defineInRange("villageNoSmokingMinVillagers", 3, 1, 100);

    public static final ModConfigSpec.IntValue VILLAGE_NO_SMOKING_MIN_BEDS = BUILDER
            .comment("Minimum beds within check radius to trigger the no-smoking zone.")
            .defineInRange("villageNoSmokingMinBeds", 3, 1, 100);

    public static final ModConfigSpec.IntValue VILLAGE_NO_SMOKING_CHECK_INTERVAL_TICKS = BUILDER
            .comment("Ticks between village no-smoking zone checks.")
            .defineInRange("villageNoSmokingCheckIntervalTicks", 40, 20, 600);

    public static final ModConfigSpec.IntValue VILLAGE_NO_SMOKING_WARNING_COOLDOWN_TICKS = BUILDER
            .comment("Minimum ticks between no-smoking warning messages.")
            .defineInRange("villageNoSmokingWarningCooldownTicks", 200, 20, 72000);

    public static final ModConfigSpec.BooleanValue IGNORE_CREATIVE_PLAYERS_IN_NO_SMOKING_ZONE = BUILDER
            .comment("Whether creative-mode players are ignored by the village no-smoking zone.")
            .define("ignoreCreativePlayersInNoSmokingZone", true);

    public static final ModConfigSpec.BooleanValue ENABLE_VILLAGE_NO_SMOKING_ENTER_HINT = BUILDER
            .comment("Whether entering a village no-smoking zone shows a warning hint even when not smoking.")
            .define("enableVillageNoSmokingEnterHint", true);

    public static final ModConfigSpec.IntValue VILLAGE_NO_SMOKING_ENTER_HINT_COOLDOWN_TICKS = BUILDER
            .comment("Minimum ticks between enter-zone hints (600 ticks = 30 seconds).")
            .defineInRange("villageNoSmokingEnterHintCooldownTicks", 600, 20, 72000);

    public static final ModConfigSpec.BooleanValue ENABLE_TOBACCO_VILLAGER_GENERATION = BUILDER
            .comment("Whether tobacco villagers and their houses can generate in villages.")
            .define("enableTobaccoVillagerVillageGeneration", true);

    public static final ModConfigSpec.BooleanValue TOBACCO_VILLAGER_DEBUG = BUILDER
            .comment("Enable debug logging and chat messages for tobacco villager generation.")
            .define("tobaccoVillagerGenerationDebug", false);

    public static final ModConfigSpec.IntValue TOBACCO_VILLAGER_MAX_PER_VILLAGE = BUILDER
            .comment("Maximum tobacco villagers per village.")
            .defineInRange("tobaccoVillagerMaxPerVillage", 1, 1, 10);

    static final ModConfigSpec SPEC = BUILDER.build();
}
