package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public final class AddictionHudRenderer {
    private static final ResourceLocation ADDICTION_ICON = SmokingWarningMod.id("textures/gui/addiction_icon.png");
    private static final ResourceLocation COUGHING_ICON = SmokingWarningMod.id("textures/gui/coughing_icon.png");
    private static final ResourceLocation LUNG_CANCER_ICON = SmokingWarningMod.id("textures/gui/lung_cancer_icon.png");
    static final int DEFAULT_HUD_X = 8;
    static final int DEFAULT_HUD_Y = 8;
    static final float DEFAULT_HUD_SCALE = 1.0F;
    static final float MIN_HUD_SCALE = 0.5F;
    static final float MAX_HUD_SCALE = 2.0F;
    private static final int PANEL_WIDTH = 156;
    private static final int BAR_WIDTH = 118;
    private static final int BAR_HEIGHT = 5;
    private static final int HOTBAR_RESERVED_HEIGHT = 24;

    private AddictionHudRenderer() {
    }

    public static void onRenderGui(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.options.hideGui || !Config.ENABLE_ADDICTION_HUD.getAsBoolean()) {
            return;
        }

        ClientAddictionHudData.Snapshot snapshot = ClientAddictionHudData.snapshot();
        if (!shouldRender(snapshot)) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        Font font = minecraft.font;
        float scale = currentScale();
        int x = clampHudX(Config.ADDICTION_HUD_X.getAsInt(), graphics.guiWidth(), scale);
        int y = clampHudY(Config.ADDICTION_HUD_Y.getAsInt(), graphics.guiHeight(), snapshot, scale);
        renderHud(graphics, font, snapshot, x, y, scale, false, false);
    }

    static void renderEditorPreview(GuiGraphics graphics, Font font, int x, int y, float scale, boolean hovered, boolean dragging) {
        ClientAddictionHudData.Snapshot snapshot = ClientAddictionHudData.snapshot();
        renderEditorBacking(graphics, snapshot, x, y, scale);
        renderHud(graphics, font, snapshot, x, y, scale, false, false);
        renderEditorFrame(graphics, snapshot, x, y, scale, hovered, dragging);
    }

    static float currentScale() {
        return Math.clamp((float) Config.ADDICTION_HUD_SCALE.getAsDouble(), MIN_HUD_SCALE, MAX_HUD_SCALE);
    }

    static int currentX(int screenWidth, float scale) {
        return clampHudX(Config.ADDICTION_HUD_X.getAsInt(), screenWidth, scale);
    }

    static int currentY(int screenHeight, ClientAddictionHudData.Snapshot snapshot, float scale) {
        return clampHudY(Config.ADDICTION_HUD_Y.getAsInt(), screenHeight, snapshot, scale);
    }

    static int clampHudX(int x, int screenWidth, float scale) {
        return Math.clamp(x, 0, Math.max(0, screenWidth - scaledPanelWidth(scale)));
    }

    static int clampHudY(int y, int screenHeight, ClientAddictionHudData.Snapshot snapshot, float scale) {
        int maxY = Math.max(0, screenHeight - scaledPanelHeight(snapshot, scale) - HOTBAR_RESERVED_HEIGHT);
        return Math.clamp(y, 0, maxY);
    }

    static int scaledPanelWidth(float scale) {
        return (int) Math.ceil(PANEL_WIDTH * scale);
    }

    static int scaledPanelHeight(ClientAddictionHudData.Snapshot snapshot, float scale) {
        return (int) Math.ceil(panelHeight(snapshot) * scale);
    }

    static void saveHudConfig(int x, int y, float scale) {
        Config.ADDICTION_HUD_X.set(Math.clamp(x, 0, 10000));
        Config.ADDICTION_HUD_Y.set(Math.clamp(y, 0, 10000));
        Config.ADDICTION_HUD_SCALE.set((double) Math.clamp(scale, MIN_HUD_SCALE, MAX_HUD_SCALE));
        Config.SPEC.save();
    }

    static void resetHudConfig() {
        saveHudConfig(DEFAULT_HUD_X, DEFAULT_HUD_Y, DEFAULT_HUD_SCALE);
    }

    private static void renderEditorBacking(GuiGraphics graphics, ClientAddictionHudData.Snapshot snapshot, int x, int y, float scale) {
        int width = scaledPanelWidth(scale);
        int height = scaledPanelHeight(snapshot, scale);
        graphics.fill(x - 4, y - 4, x + width + 4, y + height + 4, 0x44000000);
    }

    private static void renderEditorFrame(GuiGraphics graphics, ClientAddictionHudData.Snapshot snapshot, int x, int y, float scale, boolean hovered, boolean dragging) {
        int width = scaledPanelWidth(scale);
        int height = scaledPanelHeight(snapshot, scale);
        int thickness = dragging ? 3 : 2;
        int color = dragging ? 0xFFFFFF66 : hovered ? 0xFFFFE082 : 0xE8FFFFFF;
        int outerX = x - thickness;
        int outerY = y - thickness;
        int outerRight = x + width + thickness;
        int outerBottom = y + height + thickness;

        graphics.fill(outerX, outerY, outerRight, y, color);
        graphics.fill(outerX, y + height, outerRight, outerBottom, color);
        graphics.fill(outerX, y, x, y + height, color);
        graphics.fill(x + width, y, outerRight, y + height, color);

        int handleColor = dragging ? 0xFFFFFFFF : 0xFFFFF4BF;
        int handle = dragging ? 5 : 4;
        graphics.fill(outerX, outerY, outerX + handle, outerY + handle, handleColor);
        graphics.fill(outerRight - handle, outerY, outerRight, outerY + handle, handleColor);
        graphics.fill(outerX, outerBottom - handle, outerX + handle, outerBottom, handleColor);
        graphics.fill(outerRight - handle, outerBottom - handle, outerRight, outerBottom, handleColor);
    }

    private static void renderHud(GuiGraphics graphics, Font font, ClientAddictionHudData.Snapshot snapshot, int x, int y, float scale, boolean editorBorder, boolean hoveredInEditor) {
        int panelHeight = panelHeight(snapshot);
        int accent = accentColor(snapshot);

        graphics.pose().pushPose();
        graphics.pose().translate(x, y, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);

        graphics.fill(0, 0, PANEL_WIDTH, panelHeight, 0x99000000);
        graphics.fill(0, 0, 2, panelHeight, accent);
        renderIcons(graphics, snapshot, 0, 0);
        if (editorBorder) {
            graphics.renderOutline(-1, -1, PANEL_WIDTH + 2, panelHeight + 2, hoveredInEditor ? 0xFFFFD54F : 0x88FFFFFF);
        }

        int textX = 26;
        int lineY = 6;
        graphics.drawString(font, Component.literal(Component.translatable("hud.SmokingWarningMod.addiction").getString() + ": " + addictionProgressText(snapshot)), textX, lineY, 0xFFFFFFFF);
        lineY += 10;
        graphics.drawString(font, Component.literal(Component.translatable("hud.SmokingWarningMod.stage").getString() + ": ").append(stageText(snapshot)), textX, lineY, 0xFFE8E8E8);

        int barX = 26;
        int barY = 29;
        graphics.fill(barX - 1, barY - 1, barX + BAR_WIDTH + 1, barY + BAR_HEIGHT + 1, 0xCC111111);
        graphics.fill(barX, barY, barX + BAR_WIDTH, barY + BAR_HEIGHT, 0xFF2D2D2D);
        int fillWidth = Math.clamp((int) (BAR_WIDTH * progress(snapshot)), 0, BAR_WIDTH);
        graphics.fill(barX, barY, barX + fillWidth, barY + BAR_HEIGHT, accent);

        lineY = 39;
        if (snapshot.addictionStage() < SmokingAddictionManager.STAGE_HEAVY) {
            graphics.drawString(font, Component.translatable("hud.SmokingWarningMod.next_stage", nextStageRemaining(snapshot)), textX, lineY, 0xFFDCDCDC);
            lineY += 10;
        }
        if (snapshot.hasCoughing()) {
            graphics.drawString(font, Component.literal(Component.translatable("hud.SmokingWarningMod.status").getString() + ": ").append(Component.translatable("hud.SmokingWarningMod.coughing")), textX, lineY, 0xFFFFE082);
            lineY += 10;
        }
        if (snapshot.hasLungCancer()) {
            graphics.drawString(font, Component.literal(Component.translatable("hud.SmokingWarningMod.status").getString() + ": ").append(Component.translatable("hud.SmokingWarningMod.lung_cancer")), textX, lineY, 0xFFFF8A80);
            lineY += 10;
        }
        if (snapshot.hasLungCancer() && Config.SHOW_LUNG_CANCER_WARNING.getAsBoolean()) {
            Minecraft minecraft = Minecraft.getInstance();
            int tickCount = minecraft.player == null ? 0 : minecraft.player.tickCount;
            int warningColor = snapshot.isSmoking() && (tickCount / 10) % 2 == 0 ? 0xFFFF5555 : 0xFFFFB0B0;
            graphics.drawString(font, Component.translatable("hud.SmokingWarningMod.danger_smoking"), textX, lineY, warningColor);
            lineY += 12;
        }

        ClientTreatmentData.Snapshot treatment = ClientTreatmentData.snapshot();
        if (treatment.diagnosedLungCancer()) {
            lineY = renderTreatmentHudLines(graphics, font, treatment, textX, lineY);
        }

        graphics.pose().popPose();
    }

    private static boolean shouldRender(ClientAddictionHudData.Snapshot snapshot) {
        if (!snapshot.hasServerData()) {
            return false;
        }
        if (!Config.SHOW_ADDICTION_HUD_ONLY_AFTER_SMOKING.getAsBoolean()) {
            return true;
        }
        return snapshot.smokedCigaretteCount() > 0
                || snapshot.addictionStage() > SmokingAddictionManager.STAGE_NONE
                || snapshot.hasCoughing()
                || snapshot.hasLungCancer();
    }

    private static int panelHeight(ClientAddictionHudData.Snapshot snapshot) {
        int height = 46;
        if (snapshot.addictionStage() < SmokingAddictionManager.STAGE_HEAVY) {
            height += 10;
        }
        if (snapshot.hasCoughing()) {
            height += 10;
        }
        if (snapshot.hasLungCancer()) {
            height += 10;
        }
        if (snapshot.hasLungCancer() && Config.SHOW_LUNG_CANCER_WARNING.getAsBoolean()) {
            height += 12;
        }
        ClientTreatmentData.Snapshot treatment = ClientTreatmentData.snapshot();
        if (treatment.diagnosedLungCancer()) {
            height += 20;
            if (treatment.treatmentStage() == LungCancerTreatmentManager.STAGE_REHAB && treatment.smokeFreeTicks() > 0) {
                height += 10;
            }
            if (treatment.treatmentCooldown() > 0) {
                height += 10;
            }
        }
        return height;
    }

    private static void renderIcons(GuiGraphics graphics, ClientAddictionHudData.Snapshot snapshot, int x, int y) {
        graphics.blit(ADDICTION_ICON, x + 6, y + 6, 0.0F, 0.0F, 16, 16, 16, 16);
        int iconX = x + PANEL_WIDTH - 22;
        if (snapshot.hasLungCancer()) {
            graphics.blit(LUNG_CANCER_ICON, iconX, y + 6, 0.0F, 0.0F, 16, 16, 16, 16);
            iconX -= 18;
        }
        if (snapshot.hasCoughing()) {
            graphics.blit(COUGHING_ICON, iconX, y + 6, 0.0F, 0.0F, 16, 16, 16, 16);
        }
    }

    private static Component stageText(ClientAddictionHudData.Snapshot snapshot) {
        return switch (snapshot.addictionStage()) {
            case SmokingAddictionManager.STAGE_LIGHT -> Component.translatable("hud.SmokingWarningMod.stage.light");
            case SmokingAddictionManager.STAGE_MEDIUM -> Component.translatable("hud.SmokingWarningMod.stage.medium");
            case SmokingAddictionManager.STAGE_HEAVY -> Component.translatable("hud.SmokingWarningMod.stage.heavy");
            default -> Component.translatable("hud.SmokingWarningMod.stage.none");
        };
    }

    private static String addictionProgressText(ClientAddictionHudData.Snapshot snapshot) {
        int count = snapshot.smokedCigaretteCount();
        if (snapshot.addictionStage() >= SmokingAddictionManager.STAGE_HEAVY) {
            return Config.HEAVY_ADDICTION_THRESHOLD.getAsInt() + "+";
        }
        return count + "/" + nextTarget(snapshot);
    }

    private static int nextStageRemaining(ClientAddictionHudData.Snapshot snapshot) {
        return Math.max(0, nextTarget(snapshot) - snapshot.smokedCigaretteCount());
    }

    private static int nextTarget(ClientAddictionHudData.Snapshot snapshot) {
        return switch (snapshot.addictionStage()) {
            case SmokingAddictionManager.STAGE_LIGHT -> Config.MEDIUM_ADDICTION_THRESHOLD.getAsInt();
            case SmokingAddictionManager.STAGE_MEDIUM -> Config.HEAVY_ADDICTION_THRESHOLD.getAsInt();
            default -> Config.LIGHT_ADDICTION_THRESHOLD.getAsInt();
        };
    }

    private static float progress(ClientAddictionHudData.Snapshot snapshot) {
        if (snapshot.addictionStage() >= SmokingAddictionManager.STAGE_HEAVY) {
            return 1.0F;
        }
        int target = Math.max(1, nextTarget(snapshot));
        return Math.clamp(snapshot.smokedCigaretteCount() / (float) target, 0.0F, 1.0F);
    }

    private static int renderTreatmentHudLines(GuiGraphics graphics, Font font, ClientTreatmentData.Snapshot treatment, int textX, int lineY) {
        graphics.drawString(font, Component.literal(Component.translatable("hud.SmokingWarningMod.treatment.progress").getString() + ": " + treatment.treatmentProgress() + "%"), textX, lineY, 0xFF64B5F6);
        lineY += 10;

        String stageKey = switch (treatment.treatmentStage()) {
            case LungCancerTreatmentManager.STAGE_DIAGNOSED -> "hud.SmokingWarningMod.treatment.stage.diagnosed";
            case LungCancerTreatmentManager.STAGE_CONTROL -> "hud.SmokingWarningMod.treatment.stage.control";
            case LungCancerTreatmentManager.STAGE_TREATING -> "hud.SmokingWarningMod.treatment.stage.treating";
            case LungCancerTreatmentManager.STAGE_REHAB -> "hud.SmokingWarningMod.treatment.stage.rehab";
            case LungCancerTreatmentManager.STAGE_REMISSION -> "hud.SmokingWarningMod.treatment.stage.remission";
            default -> "hud.SmokingWarningMod.treatment.stage.none";
        };
        graphics.drawString(font, Component.literal(Component.translatable("hud.SmokingWarningMod.treatment.stage").getString() + ": ").append(Component.translatable(stageKey)), textX, lineY, 0xFFB0BEC5);
        lineY += 10;

        if (treatment.treatmentStage() == LungCancerTreatmentManager.STAGE_REHAB && treatment.smokeFreeTicks() > 0) {
            int requiredTicks = Config.REHAB_REQUIRED_SMOKE_FREE_TICKS.getAsInt();
            int percent = Math.clamp((int) (treatment.smokeFreeTicks() * 100L / requiredTicks), 0, 100);
            graphics.drawString(font, Component.literal(Component.translatable("hud.SmokingWarningMod.treatment.smoke_free").getString() + ": " + percent + "%"), textX, lineY, 0xFF81C784);
            lineY += 10;
        }

        if (treatment.treatmentCooldown() > 0) {
            int seconds = treatment.treatmentCooldown() / 20;
            graphics.drawString(font, Component.literal(Component.translatable("hud.SmokingWarningMod.treatment.cooldown").getString() + ": " + seconds + "s"), textX, lineY, 0xFFFFCC80);
            lineY += 10;
        }

        return lineY;
    }

    private static int accentColor(ClientAddictionHudData.Snapshot snapshot) {
        if (snapshot.hasLungCancer()) {
            return 0xFF7A1010;
        }
        return switch (snapshot.addictionStage()) {
            case SmokingAddictionManager.STAGE_LIGHT -> 0xFFFFD54F;
            case SmokingAddictionManager.STAGE_MEDIUM -> 0xFFFF8F00;
            case SmokingAddictionManager.STAGE_HEAVY -> 0xFFE53935;
            default -> 0xFF66BB6A;
        };
    }
}
