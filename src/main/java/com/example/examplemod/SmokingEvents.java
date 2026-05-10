package com.example.examplemod;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

public class SmokingEvents {
    private static final int LUNG_CANCER_SMOKING_MESSAGE_COOLDOWN_TICKS = 120;

    @SubscribeEvent
    public void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        PlayerSmokingData data = PlayerSmokingData.get(player);
        decrementLungCancerSmokingMessageCooldown(data);
        if (Config.ENABLE_SMOKING_SYSTEM.getAsBoolean()) {
            boolean smoking = player.isUsingItem() && CigaretteItem.isCigarette(player.getUseItem());
            if (smoking) {
                boolean wasSmoking = data.smokingTicks() > 0;
                tickSmoking(player, data);
                if (!wasSmoking) {
                    AddictionHudSync.syncTo(player, data);
                }
            } else if (data.smokingTicks() > 0) {
                data.setSmokingTicks(0);
                AddictionHudSync.syncTo(player, data);
            }
        }
        SmokingAddictionManager.tick(player, data);
        LungCancerTreatmentManager.tickRehab(player, data);
        LungCancerTreatmentManager.tickCooldown(data);
        VillageNoSmokingHandler.tickCooldown(data);
        VillageNoSmokingHandler.checkVillageNoSmoking(player, data);
        AddictionHudSync.syncPeriodically(player, data);
        syncTreatmentPeriodically(player);
    }

    @SubscribeEvent
    public void onPlayerClone(PlayerEvent.Clone event) {
        boolean resetAddiction = event.isWasDeath() && Config.ALLOW_DEATH_RESET_ADDICTION.getAsBoolean();
        PlayerSmokingData.copy(event.getOriginal(), event.getEntity(), resetAddiction);
        PlayerSmokingData.get(event.getEntity()).setSmokingTicks(0);
    }

    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AddictionHudSync.syncTo(player);
        }
    }

    @SubscribeEvent
    public void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            AddictionHudSync.syncTo(player);
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SmokingAddictionManager.applyBreakSpeedPenalty(player, PlayerSmokingData.get(player), event);
        }
    }

    public static void stopSmoking(Player player, boolean completed) {
        stopSmoking(player, completed, null);
    }

    public static void stopSmoking(Player player, boolean completed, Component completedMessage) {
        PlayerSmokingData.get(player).setSmokingTicks(0);
        if (completed) {
            player.displayClientMessage(completedMessage == null ? Component.translatable("message.SmokingWarningMod.smoking_completed") : completedMessage, true);
        } else {
            player.displayClientMessage(Component.translatable("message.SmokingWarningMod.smoking_stopped"), true);
        }
        if (player instanceof ServerPlayer serverPlayer) {
            AddictionHudSync.syncTo(serverPlayer);
        }
    }

    public static boolean finishSmokingItem(ServerPlayer player, ItemStack stack) {
        if (player.getAbilities().instabuild) {
            return false;
        }

        InteractionHand hand = player.getUsedItemHand();
        ItemStack held = player.getItemInHand(hand);
        CigaretteItem cigaretteItem = null;
        if (CigaretteItem.isCigarette(held)) {
            if (held.getDamageValue() + 1 < held.getMaxDamage()) {
                return false;
            }
            cigaretteItem = (CigaretteItem) held.getItem();
            held.shrink(1);
            player.setItemInHand(hand, ItemStack.EMPTY);
            giveCigaretteButt(player);
            syncHeldItem(player);
        } else if (CigaretteItem.isCigarette(stack) && !stack.isEmpty()) {
            if (stack.getDamageValue() + 1 < stack.getMaxDamage()) {
                return false;
            }
            cigaretteItem = (CigaretteItem) stack.getItem();
            stack.shrink(1);
            giveCigaretteButt(player);
            syncHeldItem(player);
        } else {
            return false;
        }
        completeSmoking(player, cigaretteItem);
        return true;
    }

    private static void tickSmoking(ServerPlayer player, PlayerSmokingData data) {
        int smokingTicks = data.smokingTicks() + 1;
        data.setSmokingTicks(smokingTicks);

        boolean lungCancerStage = isLungCancerStage(player, data);
        if (lungCancerStage && smokingTicks == 1 && Config.ENABLE_LUNG_CANCER_SMOKING_DAMAGE.getAsBoolean()) {
            player.displayClientMessage(Component.translatable("message.SmokingWarningMod.lung_cancer_smoking_started"), true);
        }

        int interval = Config.SMOKING_INTERVAL_TICKS.getAsInt();
        if (smokingTicks % interval != 0) {
            tickLungCancerSmokingDamage(player, data, smokingTicks, lungCancerStage);
            return;
        }

        InteractionHand hand = player.getUsedItemHand();
        ItemStack heldCigarette = player.getItemInHand(hand);
        ItemStack activeCigarette = player.getUseItem();
        ItemStack cigarette = CigaretteItem.isCigarette(heldCigarette) ? heldCigarette : activeCigarette;
        if (!CigaretteItem.isCigarette(cigarette)) {
            stopSmoking(player, false);
            return;
        }

        CigaretteItem cigaretteItem = cigarette.getItem() instanceof CigaretteItem item ? item : null;
        boolean depleted = damageCigarette(player, hand, heldCigarette, activeCigarette);
        spawnSmoke(player.serverLevel(), player);
        playSmokingPulse(player);
        if (!depleted && cigaretteItem != null) {
            cigaretteItem.applyVariantEffects(player, smokingTicks);
        }

        if (depleted) {
            completeSmoking(player, cigaretteItem);
            player.stopUsingItem();
            stopSmoking(player, true, cigaretteItem == null ? null : cigaretteItem.finishMessage());
        } else {
            tickLungCancerSmokingDamage(player, data, smokingTicks, lungCancerStage);
        }
    }

    private static boolean damageCigarette(ServerPlayer player, InteractionHand hand, ItemStack heldCigarette, ItemStack activeCigarette) {
        if (player.getAbilities().instabuild) {
            return false;
        }

        ItemStack cigarette = CigaretteItem.isCigarette(heldCigarette) ? heldCigarette : activeCigarette;
        int nextDamage = cigarette.getDamageValue() + 1;
        if (nextDamage >= cigarette.getMaxDamage()) {
            cigarette.shrink(1);
            player.setItemInHand(hand, ItemStack.EMPTY);
            giveCigaretteButt(player);
            syncHeldItem(player);
            return true;
        }

        cigarette.setDamageValue(nextDamage);
        if (activeCigarette != cigarette && CigaretteItem.isCigarette(activeCigarette)) {
            activeCigarette.setDamageValue(nextDamage);
        }
        if (heldCigarette != cigarette && CigaretteItem.isCigarette(heldCigarette)) {
            heldCigarette.setDamageValue(nextDamage);
        }
        syncHeldItem(player);
        return false;
    }

    private static void giveCigaretteButt(ServerPlayer player) {
        ItemStack butt = new ItemStack(SmokingWarningMod.CIGARETTE_BUTT.get());
        if (!player.getInventory().add(butt)) {
            player.drop(butt, false);
        }
    }

    private static void syncHeldItem(ServerPlayer player) {
        player.getInventory().setChanged();
        player.inventoryMenu.broadcastChanges();
        player.containerMenu.broadcastChanges();
    }

    private static void completeSmoking(ServerPlayer player, CigaretteItem cigaretteItem) {
        if (cigaretteItem == null) {
            return;
        }

        PlayerSmokingData data = PlayerSmokingData.get(player);
        boolean lungCancerStageBeforeFinish = isLungCancerStage(player, data);
        cigaretteItem.applyFinishedEffects(player);
        SmokingAddictionManager.onCigaretteFinished(player, cigaretteItem);
        applyLungCancerFinishDamage(player, cigaretteItem, lungCancerStageBeforeFinish);
        LungCancerTreatmentManager.onSmokeDuringTreatment(player);
        int dizzyTicks = cigaretteItem.finishDizzyTicks();
        if (dizzyTicks > 0) {
            PacketDistributor.sendToPlayer(player, new FinishDizzyVisualPayload(dizzyTicks));
        }
    }

    private static void syncTreatmentPeriodically(ServerPlayer player) {
        if (player.tickCount % 100 == 0) {
            TreatmentHudSyncPayload.syncTo(player);
        }
    }

    private static void tickLungCancerSmokingDamage(ServerPlayer player, PlayerSmokingData data, int smokingTicks, boolean lungCancerStage) {
        if (!Config.ENABLE_LUNG_CANCER_SMOKING_DAMAGE.getAsBoolean() || !lungCancerStage || player.isCreative() || player.isSpectator()) {
            return;
        }

        int interval = Math.max(1, Config.LUNG_CANCER_SMOKING_DAMAGE_INTERVAL_TICKS.getAsInt());
        if (smokingTicks % interval != 0) {
            return;
        }

        if (hurtForLungCancerSmoking(player, Config.LUNG_CANCER_SMOKING_DAMAGE_AMOUNT.getAsDouble())) {
            playLungCancerSmokingDamageSound(player);
            if (data.lungCancerSmokingMessageCooldown() <= 0) {
                player.displayClientMessage(Component.translatable("message.SmokingWarningMod.lung_cancer_smoking_pain"), true);
                data.setLungCancerSmokingMessageCooldown(LUNG_CANCER_SMOKING_MESSAGE_COOLDOWN_TICKS);
            }
        }
    }

    private static void applyLungCancerFinishDamage(ServerPlayer player, CigaretteItem cigaretteItem, boolean lungCancerStageBeforeFinish) {
        if (!Config.ENABLE_LUNG_CANCER_SMOKING_DAMAGE.getAsBoolean()
                || !lungCancerStageBeforeFinish
                || player.isCreative()
                || player.isSpectator()) {
            return;
        }

        double damage = switch (cigaretteItem.variant()) {
            case REGULAR -> Config.LUNG_CANCER_FINISH_DAMAGE_NORMAL.getAsDouble();
            case HUAZI, LOTUS -> Config.LUNG_CANCER_FINISH_DAMAGE_SPECIAL.getAsDouble();
            case RICK_V -> Config.LUNG_CANCER_FINISH_DAMAGE_RICK_V.getAsDouble();
            default -> Config.LUNG_CANCER_FINISH_DAMAGE_VARIANT.getAsDouble();
        };
        damage += finalSmokingPulseDamage(PlayerSmokingData.get(player));

        if (hurtForLungCancerSmoking(player, damage)) {
            playLungCancerSmokingDamageSound(player);
            player.displayClientMessage(Component.translatable("message.SmokingWarningMod.lung_cancer_finish_damage"), true);
        }
    }

    private static boolean hurtForLungCancerSmoking(ServerPlayer player, double damage) {
        if (damage <= 0.0D) {
            return false;
        }
        return player.hurt(player.damageSources().magic(), (float) damage);
    }

    private static double finalSmokingPulseDamage(PlayerSmokingData data) {
        int smokingTicks = data.smokingTicks();
        int interval = Math.max(1, Config.LUNG_CANCER_SMOKING_DAMAGE_INTERVAL_TICKS.getAsInt());
        if (smokingTicks > 0 && smokingTicks % interval == 0) {
            return Config.LUNG_CANCER_SMOKING_DAMAGE_AMOUNT.getAsDouble();
        }
        return 0.0D;
    }

    private static boolean isLungCancerStage(ServerPlayer player, PlayerSmokingData data) {
        return data.addictionStage() >= SmokingAddictionManager.STAGE_HEAVY
                || player.hasEffect(SmokingWarningMod.LUNG_CANCER)
                || data.smokedCigaretteCount() >= Config.HEAVY_ADDICTION_THRESHOLD.getAsInt();
    }

    private static void decrementLungCancerSmokingMessageCooldown(PlayerSmokingData data) {
        int cooldown = data.lungCancerSmokingMessageCooldown();
        if (cooldown > 0) {
            data.setLungCancerSmokingMessageCooldown(cooldown - 1);
        }
    }

    private static void playLungCancerSmokingDamageSound(ServerPlayer player) {
        try {
            player.serverLevel().playSound(null, player.blockPosition(), SmokingWarningMod.LUNG_CANCER_SOUND.get(), SoundSource.PLAYERS, 0.85F, 0.75F + player.getRandom().nextFloat() * 0.1F);
        } catch (RuntimeException exception) {
            player.serverLevel().playSound(null, player.blockPosition(), SmokingWarningMod.COUGHING_SOUND.get(), SoundSource.PLAYERS, 0.75F, 0.9F + player.getRandom().nextFloat() * 0.15F);
        }
    }

    private static void playSmokingPulse(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.blockPosition(), SmokingWarningMod.SMOKING_INHALE.get(), SoundSource.PLAYERS, 0.35F, 0.95F + player.getRandom().nextFloat() * 0.1F);
        level.playSound(null, player.blockPosition(), SmokingWarningMod.SMOKING_EXHALE.get(), SoundSource.PLAYERS, 0.28F, 0.95F + player.getRandom().nextFloat() * 0.1F);
    }

    private static void spawnSmoke(ServerLevel level, ServerPlayer player) {
        Vec3 look = player.getLookAngle();
        Vec3 pos = player.getEyePosition().add(look.scale(0.35D)).add(0.0D, -0.08D, 0.0D);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x, pos.y, pos.z, 2, 0.08D, 0.05D, 0.08D, 0.005D);
    }
}
