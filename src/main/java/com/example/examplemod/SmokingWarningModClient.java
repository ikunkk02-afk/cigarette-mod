package com.example.examplemod;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = SmokingWarningMod.MODID, dist = Dist.CLIENT)
public class SmokingWarningModClient {
    private static KeyMapping editAddictionHudKey;

    public SmokingWarningModClient(IEventBus modEventBus, ModContainer container) {
        modEventBus.addListener(SmokingWarningModClient::registerClientExtensions);
        modEventBus.addListener(SmokingWarningModClient::registerKeyMappings);
        NeoForge.EVENT_BUS.addListener(CigaretteDizzyVisuals::onClientTick);
        NeoForge.EVENT_BUS.addListener(SmokingWarningModClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(CigaretteDizzyVisuals::onComputeFov);
        NeoForge.EVENT_BUS.addListener(CigaretteDizzyVisuals::onComputeCameraAngles);
        NeoForge.EVENT_BUS.addListener(CigaretteDizzyVisuals::onRenderGui);
        NeoForge.EVENT_BUS.addListener(AddictionHudRenderer::onRenderGui);
        NeoForge.EVENT_BUS.addListener(SmokingWarningModClient::onClientLogout);
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    private static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientAddictionHudData.clear();
        ClientTreatmentData.clear();
    }

    private static void registerKeyMappings(RegisterKeyMappingsEvent event) {
        editAddictionHudKey = new KeyMapping(
                "key.smokingwarningmod.edit_addiction_hud",
                InputConstants.Type.KEYSYM,
                InputConstants.KEY_H,
                "key.categories.smokingwarningmod");
        event.register(editAddictionHudKey);
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        if (editAddictionHudKey == null) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        while (editAddictionHudKey.consumeClick()) {
            if (minecraft.player == null) {
                continue;
            }
            if (minecraft.screen instanceof AddictionHudEditorScreen) {
                minecraft.screen.onClose();
            } else if (minecraft.screen == null) {
                minecraft.setScreen(new AddictionHudEditorScreen());
            }
        }
    }

    static boolean matchesEditHudKey(int keyCode, int scanCode) {
        return editAddictionHudKey != null && editAddictionHudKey.matches(keyCode, scanCode);
    }

    static void registerClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerItem(new IClientItemExtensions() {
            @Override
            public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack) {
                if (entityLiving.isUsingItem() && entityLiving.getUsedItemHand() == hand) {
                    return HumanoidModel.ArmPose.SPYGLASS;
                }
                return HumanoidModel.ArmPose.ITEM;
            }

            @Override
            public boolean applyForgeHandTransform(PoseStack poseStack, LocalPlayer player, HumanoidArm arm, ItemStack itemInHand, float partialTick, float equipProgress, float swingProgress) {
                if (!player.isUsingItem() || !CigaretteItem.isCigarette(player.getUseItem())) {
                    return false;
                }

                int side = arm == HumanoidArm.RIGHT ? 1 : -1;
                float pulse = (float) Math.sin((player.getTicksUsingItem() + partialTick) * 0.22F) * 0.035F;
                poseStack.translate(side * 0.08F, -0.04F + pulse, -0.06F);
                poseStack.mulPose(Axis.XP.rotationDegrees(-8.0F + pulse * 80.0F));
                poseStack.mulPose(Axis.YP.rotationDegrees(side * 9.0F));
                return false;
            }
        }, SmokingWarningMod.allCigaretteItems());
    }
}
