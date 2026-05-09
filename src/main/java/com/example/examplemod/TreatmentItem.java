package com.example.examplemod;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

public class TreatmentItem extends Item {
    public enum TreatmentType {
        CHEMOTHERAPY,
        RADIOTHERAPY,
        TARGETED_THERAPY,
        REHABILITATION
    }

    private final TreatmentType treatmentType;

    public TreatmentItem(Properties properties, TreatmentType treatmentType) {
        super(properties);
        this.treatmentType = treatmentType;
    }

    public TreatmentType treatmentType() {
        return treatmentType;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
            boolean success = switch (treatmentType) {
                case CHEMOTHERAPY -> LungCancerTreatmentManager.applyChemotherapy(serverPlayer);
                case RADIOTHERAPY -> LungCancerTreatmentManager.applyRadiotherapy(serverPlayer);
                case TARGETED_THERAPY -> LungCancerTreatmentManager.applyTargetedTherapy(serverPlayer);
                case REHABILITATION -> LungCancerTreatmentManager.applyRehabilitation(serverPlayer);
            };

            if (success) {
                playTreatmentSound(level, player);
                stack.consume(1, serverPlayer);
            }
        }
        return InteractionResultHolder.consume(stack);
    }

    private void playTreatmentSound(Level level, Player player) {
        switch (treatmentType) {
            case CHEMOTHERAPY -> level.playSound(null, player.blockPosition(), SoundEvents.GENERIC_DRINK, SoundSource.PLAYERS, 0.7F, 0.8F);
            case RADIOTHERAPY -> level.playSound(null, player.blockPosition(), SoundEvents.BEACON_AMBIENT, SoundSource.PLAYERS, 0.6F, 1.8F);
            case TARGETED_THERAPY -> level.playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.8F, 1.2F);
            case REHABILITATION -> level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.8F, 1.0F);
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return treatmentType == TreatmentType.REHABILITATION ? UseAnim.BOW : UseAnim.EAT;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return switch (treatmentType) {
            case CHEMOTHERAPY -> 80;
            case RADIOTHERAPY -> 100;
            case TARGETED_THERAPY -> 60;
            case REHABILITATION -> 40;
        };
    }
}
