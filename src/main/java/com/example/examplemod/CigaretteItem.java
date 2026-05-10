package com.example.examplemod;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CigaretteItem extends Item {
    private static final int VARIANT_EFFECT_INTERVAL_TICKS = 100;
    private static final int SHORT_EFFECT_DURATION_TICKS = 120;
    private static final int REWARD_NOTICE_COOLDOWN_TICKS = 200;
    private static final Map<UUID, Long> REWARD_DECAY_NOTICE_TIMES = new HashMap<>();
    private static final Map<UUID, Long> REWARD_CONFLICT_NOTICE_TIMES = new HashMap<>();

    private final CigaretteVariant variant;

    public CigaretteItem(Properties properties) {
        this(properties, CigaretteVariant.REGULAR);
    }

    public CigaretteItem(Properties properties, CigaretteVariant variant) {
        super(properties);
        this.variant = variant;
    }

    public CigaretteVariant variant() {
        return this.variant;
    }

    public int addictionGain() {
        return this.variant.addictionGain();
    }

    public int lungDamageGain() {
        return this.variant.lungDamageGain();
    }

    public int withdrawalRisk() {
        return this.variant.withdrawalRisk();
    }

    public int effectStrength() {
        return this.variant.effectStrength();
    }

    public int getAddictionWeight() {
        return switch (this.variant) {
            case BLAZE, PHANTOM, ENDER, REDSTONE, NETHERITE, HUAZI, LOTUS -> 2;
            case RICK_V -> 3;
            case REGULAR, MENTHOL, HONEY, GLOW -> 1;
        };
    }

    public static boolean isCigarette(ItemStack stack) {
        return stack.getItem() instanceof CigaretteItem;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!Config.ENABLE_SMOKING_SYSTEM.getAsBoolean()) {
            return InteractionResultHolder.fail(stack);
        }

        ItemStack lighter = findLighter(player);
        if (lighter.isEmpty()) {
            if (!level.isClientSide()) {
                player.displayClientMessage(Component.translatable("message.smokingwarningmod.need_lighter"), true);
            }
            return InteractionResultHolder.fail(stack);
        }

        player.startUsingItem(hand);
        if (!level.isClientSide()) {
            PlayerSmokingData.get(player).setSmokingTicks(0);
            if (level instanceof ServerLevel serverLevel) {
                lighter.hurtAndBreak(1, serverLevel, player, item -> {
                });
            }
            if (this.variant == CigaretteVariant.RICK_V && Config.ENABLE_RICK_V_EASTER_EGG.getAsBoolean()) {
                level.playSound(null, player.blockPosition(), SmokingWarningMod.RICK_V_EASTER_EGG.get(), SoundSource.PLAYERS, 0.75F, 1.0F);
            } else {
                level.playSound(null, player.blockPosition(), SmokingWarningMod.LIGHTER_USE.get(), SoundSource.PLAYERS, 0.65F, 1.0F);
            }
            player.displayClientMessage(Component.translatable(this.variant.startMessageKey()), true);
        }
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.CUSTOM;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return Config.FULL_SMOKING_DURATION_TICKS.getAsInt();
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        if (!level.isClientSide() && livingEntity instanceof ServerPlayer player) {
            if (!player.getAbilities().instabuild) {
                stack.shrink(1);
            }
            SmokingEvents.finishSmokingItem(player, stack);
            SmokingEvents.stopSmoking(player, true, this.finishMessage());
            SmokingEvents.giveCigaretteButt(player);
        }
        return stack;
    }

    @Override
    public void releaseUsing(ItemStack stack, Level level, LivingEntity livingEntity, int timeLeft) {
        if (!level.isClientSide() && !stack.isEmpty() && livingEntity instanceof Player player) {
            SmokingEvents.stopSmoking(player, false);
        }
    }

    private static ItemStack findLighter(Player player) {
        ItemStack offhand = player.getOffhandItem();
        if (offhand.is(SmokingWarningMod.LIGHTER.get())) {
            return offhand;
        }

        for (ItemStack inventoryStack : player.getInventory().items) {
            if (inventoryStack.is(SmokingWarningMod.LIGHTER.get())) {
                return inventoryStack;
            }
        }

        return ItemStack.EMPTY;
    }

    public void applyVariantEffects(ServerPlayer player, int smokingTicks) {
        if (this.variant == CigaretteVariant.REGULAR || smokingTicks % VARIANT_EFFECT_INTERVAL_TICKS != 0) {
            return;
        }

        int amplifier = Math.max(0, this.variant.effectStrength() - 1);
        switch (this.variant) {
            case MENTHOL -> {
                applyRewardWithAddictionDecay(player, MobEffects.MOVEMENT_SPEED, SHORT_EFFECT_DURATION_TICKS, amplifier);
                applyRewardWithAddictionDecay(player, MobEffects.WATER_BREATHING, SHORT_EFFECT_DURATION_TICKS, 0);
                if (smokingTicks % 200 == 0) {
                    addEffect(player, MobEffects.MOVEMENT_SLOWDOWN, 40, 0);
                }
            }
            case HONEY -> {
                applyRewardWithAddictionDecay(player, MobEffects.REGENERATION, 60, 0);
                if (smokingTicks % 200 == 0) {
                    addEffect(player, MobEffects.HUNGER, 80, 0);
                }
            }
            case BLAZE -> {
                applyRewardWithAddictionDecay(player, MobEffects.FIRE_RESISTANCE, SHORT_EFFECT_DURATION_TICKS, 0);
                applyRewardWithAddictionDecay(player, MobEffects.DAMAGE_BOOST, SHORT_EFFECT_DURATION_TICKS, 0);
                if (smokingTicks % 600 == 0 && player.getHealth() > 2.0F) {
                    player.hurt(player.damageSources().magic(), 1.0F);
                    player.displayClientMessage(Component.translatable("message.smokingwarningmod.blaze_burn"), true);
                }
            }
            case PHANTOM -> {
                applyRewardWithAddictionDecay(player, MobEffects.SLOW_FALLING, SHORT_EFFECT_DURATION_TICKS, 0);
                applyRewardWithAddictionDecay(player, MobEffects.NIGHT_VISION, SHORT_EFFECT_DURATION_TICKS, 0);
                if (smokingTicks % 300 == 0) {
                    addEffect(player, MobEffects.CONFUSION, 80, 0);
                }
            }
            case ENDER -> {
                applyRewardWithAddictionDecay(player, MobEffects.MOVEMENT_SPEED, 80, 0);
                if (player.getRandom().nextFloat() < 0.08F) {
                    double x = player.getX() + (player.getRandom().nextDouble() - 0.5D) * 8.0D;
                    double y = player.getY() + player.getRandom().nextInt(3) - 1;
                    double z = player.getZ() + (player.getRandom().nextDouble() - 0.5D) * 8.0D;
                    if (player.randomTeleport(x, y, z, true)) {
                        player.hurt(player.damageSources().magic(), 1.0F);
                        addEffect(player, MobEffects.WEAKNESS, 80, 0);
                    }
                }
            }
            case GLOW -> {
                applyRewardWithAddictionDecay(player, MobEffects.NIGHT_VISION, SHORT_EFFECT_DURATION_TICKS, 0);
                applyRewardWithAddictionDecay(player, MobEffects.GLOWING, SHORT_EFFECT_DURATION_TICKS, 0);
            }
            case REDSTONE -> {
                applyRewardWithAddictionDecay(player, MobEffects.DIG_SPEED, SHORT_EFFECT_DURATION_TICKS, 0);
                applyRewardWithAddictionDecay(player, MobEffects.MOVEMENT_SPEED, SHORT_EFFECT_DURATION_TICKS, 0);
                addEffect(player, MobEffects.HUNGER, SHORT_EFFECT_DURATION_TICKS, 0);
            }
            case NETHERITE -> {
                applyRewardWithAddictionDecay(player, MobEffects.DAMAGE_RESISTANCE, SHORT_EFFECT_DURATION_TICKS, 0);
                applyRewardWithAddictionDecay(player, MobEffects.FIRE_RESISTANCE, SHORT_EFFECT_DURATION_TICKS, 0);
                addEffect(player, MobEffects.MOVEMENT_SLOWDOWN, SHORT_EFFECT_DURATION_TICKS, 0);
            }
            case HUAZI, LOTUS -> {
            }
            case REGULAR -> {
            }
        }
    }

    public void applyFinishedEffects(ServerPlayer player) {
        CigaretteVariant effectiveVariant = this.variant;
        if (effectiveVariant == CigaretteVariant.RICK_V && !Config.ENABLE_RICK_V_EASTER_EGG.getAsBoolean()) {
            effectiveVariant = CigaretteVariant.REGULAR;
        }

        applySmokingRewardEffects(player, effectiveVariant);
    }

    private void applySmokingRewardEffects(ServerPlayer player, CigaretteVariant cigaretteType) {
        int duration = Config.CIGARETTE_REWARD_DURATION_TICKS.getAsInt();
        int projectedAddictionWeight = getAddictionWeight();
        switch (cigaretteType) {
            case REGULAR -> {
                applyRewardWithAddictionDecay(player, MobEffects.MOVEMENT_SPEED, duration, 0, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.DIG_SPEED, duration, 0, projectedAddictionWeight);
            }
            case MENTHOL -> {
                applyRewardWithAddictionDecay(player, MobEffects.MOVEMENT_SPEED, duration, 1, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.WATER_BREATHING, duration, 0, projectedAddictionWeight);
            }
            case HONEY -> {
                applyRewardWithAddictionDecay(player, MobEffects.REGENERATION, Math.min(400, duration), 0, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.SATURATION, duration, 0, projectedAddictionWeight);
            }
            case BLAZE -> {
                applyRewardWithAddictionDecay(player, MobEffects.FIRE_RESISTANCE, duration, 0, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.DAMAGE_BOOST, duration, 0, projectedAddictionWeight);
            }
            case PHANTOM -> {
                applyRewardWithAddictionDecay(player, MobEffects.SLOW_FALLING, duration, 0, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.NIGHT_VISION, duration, 0, projectedAddictionWeight);
            }
            case ENDER -> {
                applyRewardWithAddictionDecay(player, MobEffects.MOVEMENT_SPEED, duration, 1, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.DAMAGE_RESISTANCE, Math.min(600, duration), 0, projectedAddictionWeight);
                trySafeEnderTeleport(player);
            }
            case GLOW -> {
                applyRewardWithAddictionDecay(player, MobEffects.NIGHT_VISION, duration, 0, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.GLOWING, duration, 0, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.DIG_SPEED, duration, 0, projectedAddictionWeight);
            }
            case REDSTONE -> {
                applyRewardWithAddictionDecay(player, MobEffects.MOVEMENT_SPEED, duration, 1, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.DIG_SPEED, duration, 1, projectedAddictionWeight);
            }
            case NETHERITE -> {
                applyRewardWithAddictionDecay(player, MobEffects.DAMAGE_RESISTANCE, duration, 1, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.FIRE_RESISTANCE, duration, 0, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.DAMAGE_BOOST, duration, 0, projectedAddictionWeight);
            }
            case RICK_V -> {
                applyRewardWithAddictionDecay(player, MobEffects.MOVEMENT_SPEED, duration, 2, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.DIG_SPEED, duration, 2, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.NIGHT_VISION, duration, 0, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.DAMAGE_RESISTANCE, duration, 1, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.SLOW_FALLING, duration, 0, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.LUCK, duration, 0, projectedAddictionWeight);
                playRickVFinish(player);
            }
            case HUAZI -> {
                applyRewardWithAddictionDecay(player, MobEffects.DAMAGE_BOOST, duration, 0, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.DAMAGE_RESISTANCE, duration, 0, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.DIG_SPEED, duration, 0, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.LUCK, duration, 0, projectedAddictionWeight);
                notifyNearby(player, "message.smokingwarningmod.huazi.nearby");
            }
            case LOTUS -> {
                applyRewardWithAddictionDecay(player, MobEffects.MOVEMENT_SPEED, duration, 1, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.NIGHT_VISION, duration, 0, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.JUMP, duration, 0, projectedAddictionWeight);
                applyRewardWithAddictionDecay(player, MobEffects.SLOW_FALLING, duration, 0, projectedAddictionWeight);
                notifyNearby(player, "message.smokingwarningmod.lotus.nearby");
            }
        }
    }

    public Component finishMessage() {
        if (this.variant == CigaretteVariant.RICK_V && !Config.ENABLE_RICK_V_EASTER_EGG.getAsBoolean()) {
            return Component.translatable(CigaretteVariant.REGULAR.finishMessageKey());
        }
        return Component.translatable(this.variant.finishMessageKey());
    }

    public int finishDizzyTicks() {
        if (!Config.ENABLE_FINISH_DIZZY_VISUAL.getAsBoolean()) {
            return 0;
        }
        if (this.variant == CigaretteVariant.RICK_V && Config.ENABLE_RICK_V_EASTER_EGG.getAsBoolean()) {
            return Config.RICK_V_DIZZY_VISUAL_TICKS.getAsInt();
        }
        return Config.FINISH_DIZZY_VISUAL_TICKS.getAsInt();
    }

    private static void addEffect(ServerPlayer player, net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int duration, int amplifier) {
        if (duration <= 0) {
            return;
        }
        player.addEffect(new MobEffectInstance(effect, duration, amplifier, false, true, true));
    }

    private static boolean applyRewardWithAddictionDecay(ServerPlayer player, Holder<MobEffect> effect, int duration, int amplifier) {
        return applyRewardWithAddictionDecay(player, effect, duration, amplifier, 0);
    }

    private static boolean applyRewardWithAddictionDecay(ServerPlayer player, Holder<MobEffect> effect, int duration, int amplifier, int projectedAddictionWeight) {
        if (duration <= 0) {
            return false;
        }

        int stage = rewardAddictionStage(player, projectedAddictionWeight);
        if (isRewardBlockedByAddiction(player, effect, stage)) {
            player.removeEffect(effect);
            sendRewardConflictMessage(player);
            return false;
        }

        int adjustedDuration = adjustRewardDuration(duration, stage);
        int adjustedAmplifier = adjustRewardAmplifier(amplifier, stage);
        if (adjustedDuration <= 0) {
            return false;
        }

        if (stage > SmokingAddictionManager.STAGE_NONE
                && (adjustedDuration < duration || adjustedAmplifier < amplifier)) {
            sendRewardDecayMessage(player, stage);
        }

        player.addEffect(new MobEffectInstance(effect, adjustedDuration, adjustedAmplifier, false, true, true));
        return true;
    }

    private static int rewardAddictionStage(ServerPlayer player, int projectedAddictionWeight) {
        if (!Config.ENABLE_ADDICTION_SYSTEM.getAsBoolean()) {
            return SmokingAddictionManager.STAGE_NONE;
        }

        PlayerSmokingData data = PlayerSmokingData.get(player);
        int stage = Math.clamp(data.addictionStage(), SmokingAddictionManager.STAGE_NONE, SmokingAddictionManager.STAGE_HEAVY);
        int projectedCount = data.smokedCigaretteCount();
        if (projectedAddictionWeight > 0 && projectedCount <= Integer.MAX_VALUE - projectedAddictionWeight) {
            projectedCount += projectedAddictionWeight;
        } else if (projectedAddictionWeight > 0) {
            projectedCount = Integer.MAX_VALUE;
        }

        if (projectedCount >= Config.HEAVY_ADDICTION_THRESHOLD.getAsInt()) {
            stage = Math.max(stage, SmokingAddictionManager.STAGE_HEAVY);
        } else if (projectedCount >= Config.MEDIUM_ADDICTION_THRESHOLD.getAsInt()) {
            stage = Math.max(stage, SmokingAddictionManager.STAGE_MEDIUM);
        } else if (projectedCount >= Config.LIGHT_ADDICTION_THRESHOLD.getAsInt()) {
            stage = Math.max(stage, SmokingAddictionManager.STAGE_LIGHT);
        }

        if (player.hasEffect(SmokingWarningMod.LUNG_CANCER) || data.lungCancerActive()) {
            stage = SmokingAddictionManager.STAGE_HEAVY;
        }
        return stage;
    }

    private static boolean isRewardBlockedByAddiction(ServerPlayer player, Holder<MobEffect> effect, int stage) {
        if (effect == MobEffects.MOVEMENT_SPEED) {
            return player.hasEffect(MobEffects.MOVEMENT_SLOWDOWN) || stage >= SmokingAddictionManager.STAGE_MEDIUM;
        }
        if (effect == MobEffects.DIG_SPEED) {
            return player.hasEffect(MobEffects.DIG_SLOWDOWN) || stage >= SmokingAddictionManager.STAGE_MEDIUM;
        }
        if (effect == MobEffects.DAMAGE_BOOST) {
            return player.hasEffect(MobEffects.WEAKNESS) || stage >= SmokingAddictionManager.STAGE_MEDIUM;
        }
        if (effect == MobEffects.REGENERATION) {
            return player.hasEffect(SmokingWarningMod.LUNG_CANCER) || stage >= SmokingAddictionManager.STAGE_HEAVY;
        }
        if (effect == MobEffects.SATURATION) {
            return player.hasEffect(MobEffects.HUNGER) || stage >= SmokingAddictionManager.STAGE_MEDIUM;
        }
        return false;
    }

    private static int adjustRewardDuration(int duration, int stage) {
        double multiplier = switch (stage) {
            case SmokingAddictionManager.STAGE_LIGHT -> Config.LIGHT_REWARD_DURATION_MULTIPLIER.getAsDouble();
            case SmokingAddictionManager.STAGE_MEDIUM -> Config.MEDIUM_REWARD_DURATION_MULTIPLIER.getAsDouble();
            case SmokingAddictionManager.STAGE_HEAVY -> Config.HEAVY_REWARD_DURATION_MULTIPLIER.getAsDouble();
            default -> 1.0D;
        };
        return Math.max(1, (int) Math.round(duration * multiplier));
    }

    private static int adjustRewardAmplifier(int amplifier, int stage) {
        if (stage >= SmokingAddictionManager.STAGE_HEAVY) {
            int weakened = Math.max(0, amplifier - Config.HEAVY_REWARD_AMPLIFIER_PENALTY.getAsInt());
            return Math.min(weakened, 0);
        }
        if (stage >= SmokingAddictionManager.STAGE_MEDIUM) {
            return Math.max(0, amplifier - Config.MEDIUM_REWARD_AMPLIFIER_PENALTY.getAsInt());
        }
        return Math.max(0, amplifier);
    }

    private static void sendRewardDecayMessage(ServerPlayer player, int stage) {
        String messageKey = switch (stage) {
            case SmokingAddictionManager.STAGE_LIGHT -> "message.smokingwarningmod.reward_decay.light";
            case SmokingAddictionManager.STAGE_MEDIUM -> "message.smokingwarningmod.reward_decay.medium";
            case SmokingAddictionManager.STAGE_HEAVY -> "message.smokingwarningmod.reward_decay.heavy";
            default -> null;
        };
        if (messageKey != null) {
            sendCooldownMessage(player, REWARD_DECAY_NOTICE_TIMES, messageKey);
        }
    }

    private static void sendRewardConflictMessage(ServerPlayer player) {
        sendCooldownMessage(player, REWARD_CONFLICT_NOTICE_TIMES, "message.smokingwarningmod.reward_conflict_blocked");
    }

    private static void sendCooldownMessage(ServerPlayer player, Map<UUID, Long> cooldowns, String messageKey) {
        long now = player.serverLevel().getGameTime();
        UUID uuid = player.getUUID();
        Long lastSent = cooldowns.get(uuid);
        if (lastSent != null && now >= lastSent && now - lastSent < REWARD_NOTICE_COOLDOWN_TICKS) {
            return;
        }
        cooldowns.put(uuid, now);
        player.displayClientMessage(Component.translatable(messageKey), true);
    }

    private static void playRickVFinish(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SmokingWarningMod.RICK_V_EASTER_EGG.get(), SoundSource.PLAYERS, 0.95F, 0.85F + player.getRandom().nextFloat() * 0.2F);
        player.displayClientMessage(Component.translatable("message.smokingwarningmod.rick_v.chat"), false);
        for (ServerPlayer otherPlayer : level.players()) {
            if (otherPlayer != player && otherPlayer.distanceToSqr(player) <= 16.0D * 16.0D) {
                otherPlayer.displayClientMessage(Component.translatable("message.smokingwarningmod.rick_v.nearby"), false);
            }
        }
    }

    private static void notifyNearby(ServerPlayer player, String messageKey) {
        ServerLevel level = player.serverLevel();
        for (ServerPlayer otherPlayer : level.players()) {
            if (otherPlayer != player && otherPlayer.distanceToSqr(player) <= 16.0D * 16.0D) {
                otherPlayer.displayClientMessage(Component.translatable(messageKey), false);
            }
        }
    }

    private static void trySafeEnderTeleport(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        BlockPos origin = player.blockPosition();
        for (int attempt = 0; attempt < 24; attempt++) {
            int dx = player.getRandom().nextInt(17) - 8;
            int dy = player.getRandom().nextInt(9) - 4;
            int dz = player.getRandom().nextInt(17) - 8;
            if (dx * dx + dy * dy + dz * dz > 64) {
                continue;
            }

            BlockPos target = origin.offset(dx, dy, dz);
            if (isSafeTeleportTarget(level, target)
                    && player.randomTeleport(target.getX() + 0.5D, target.getY(), target.getZ() + 0.5D, true)) {
                return;
            }
        }
    }

    private static boolean isSafeTeleportTarget(ServerLevel level, BlockPos target) {
        if (!level.getWorldBorder().isWithinBounds(target)) {
            return false;
        }

        BlockPos belowPos = target.below();
        BlockPos headPos = target.above();
        BlockState below = level.getBlockState(belowPos);
        BlockState feet = level.getBlockState(target);
        BlockState head = level.getBlockState(headPos);
        return below.isFaceSturdy(level, belowPos, Direction.UP)
                && feet.getCollisionShape(level, target).isEmpty()
                && head.getCollisionShape(level, headPos).isEmpty()
                && !below.getFluidState().is(FluidTags.LAVA)
                && !feet.getFluidState().is(FluidTags.LAVA)
                && !head.getFluidState().is(FluidTags.LAVA);
    }

    public enum CigaretteVariant {
        REGULAR("message.smokingwarningmod.smoking_started", "message.smokingwarningmod.finish.regular", Config.DEFAULT_FULL_SMOKING_DURATION_TICKS, Config.DEFAULT_CIGARETTE_DURABILITY, 1, 1, 0, 1),
        MENTHOL("message.smokingwarningmod.smoking_started.menthol", "message.smokingwarningmod.finish.menthol", Config.DEFAULT_VARIANT_FULL_SMOKING_DURATION_TICKS, Config.DEFAULT_VARIANT_CIGARETTE_DURABILITY, 2, 1, 1, 1),
        HONEY("message.smokingwarningmod.smoking_started.honey", "message.smokingwarningmod.finish.honey", Config.DEFAULT_VARIANT_FULL_SMOKING_DURATION_TICKS, Config.DEFAULT_VARIANT_CIGARETTE_DURABILITY, 2, 2, 1, 1),
        BLAZE("message.smokingwarningmod.smoking_started.blaze", "message.smokingwarningmod.finish.blaze", Config.DEFAULT_VARIANT_FULL_SMOKING_DURATION_TICKS, Config.DEFAULT_VARIANT_CIGARETTE_DURABILITY, 3, 3, 2, 1),
        PHANTOM("message.smokingwarningmod.smoking_started.phantom", "message.smokingwarningmod.finish.phantom", Config.DEFAULT_VARIANT_FULL_SMOKING_DURATION_TICKS, Config.DEFAULT_VARIANT_CIGARETTE_DURABILITY, 3, 2, 2, 1),
        ENDER("message.smokingwarningmod.smoking_started.ender", "message.smokingwarningmod.finish.ender", Config.DEFAULT_VARIANT_FULL_SMOKING_DURATION_TICKS, Config.DEFAULT_VARIANT_CIGARETTE_DURABILITY, 4, 3, 3, 1),
        GLOW("message.smokingwarningmod.smoking_started.glow", "message.smokingwarningmod.finish.glow", Config.DEFAULT_VARIANT_FULL_SMOKING_DURATION_TICKS, Config.DEFAULT_VARIANT_CIGARETTE_DURABILITY, 3, 3, 2, 1),
        REDSTONE("message.smokingwarningmod.smoking_started.redstone", "message.smokingwarningmod.finish.redstone", Config.DEFAULT_VARIANT_FULL_SMOKING_DURATION_TICKS, Config.DEFAULT_VARIANT_CIGARETTE_DURABILITY, 4, 4, 3, 1),
        NETHERITE("message.smokingwarningmod.smoking_started.netherite", "message.smokingwarningmod.finish.netherite", Config.DEFAULT_VARIANT_FULL_SMOKING_DURATION_TICKS, Config.DEFAULT_VARIANT_CIGARETTE_DURABILITY, 6, 5, 4, 1),
        RICK_V("message.smokingwarningmod.smoking_started.rick_v", "message.smokingwarningmod.rick_v.finish", Config.DEFAULT_VARIANT_FULL_SMOKING_DURATION_TICKS, Config.DEFAULT_RICK_V_CIGARETTE_DURABILITY, 0, 0, 0, 3),
        HUAZI("message.smokingwarningmod.smoking_started", "message.smokingwarningmod.huazi.finish", Config.DEFAULT_VARIANT_FULL_SMOKING_DURATION_TICKS, Config.DEFAULT_VARIANT_CIGARETTE_DURABILITY, 3, 2, 1, 1),
        LOTUS("message.smokingwarningmod.smoking_started", "message.smokingwarningmod.lotus.finish", Config.DEFAULT_VARIANT_FULL_SMOKING_DURATION_TICKS, Config.DEFAULT_VARIANT_CIGARETTE_DURABILITY, 2, 1, 1, 1);

        private final String startMessageKey;
        private final String finishMessageKey;
        private final int fullSmokingDurationTicks;
        private final int maxDurability;
        private final int addictionGain;
        private final int lungDamageGain;
        private final int withdrawalRisk;
        private final int effectStrength;

        CigaretteVariant(String startMessageKey, String finishMessageKey, int fullSmokingDurationTicks, int maxDurability, int addictionGain, int lungDamageGain, int withdrawalRisk, int effectStrength) {
            this.startMessageKey = startMessageKey;
            this.finishMessageKey = finishMessageKey;
            this.fullSmokingDurationTicks = fullSmokingDurationTicks;
            this.maxDurability = maxDurability;
            this.addictionGain = addictionGain;
            this.lungDamageGain = lungDamageGain;
            this.withdrawalRisk = withdrawalRisk;
            this.effectStrength = effectStrength;
        }

        public String startMessageKey() {
            return this.startMessageKey;
        }

        public String finishMessageKey() {
            return this.finishMessageKey;
        }

        public int fullSmokingDurationTicks() {
            return this.fullSmokingDurationTicks;
        }

        public int maxDurability() {
            return this.maxDurability;
        }

        public int addictionGain() {
            return this.addictionGain;
        }

        public int lungDamageGain() {
            return this.lungDamageGain;
        }

        public int withdrawalRisk() {
            return this.withdrawalRisk;
        }

        public int effectStrength() {
            return this.effectStrength;
        }
    }
}
