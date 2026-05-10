package com.example.examplemod;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class AddictionHudEditorScreen extends Screen {
    private static final float SCALE_STEP = 0.1F;
    private static final int RESET_BUTTON_WIDTH = 110;
    private static final int RESET_BUTTON_HEIGHT = 20;
    private static final int EDGE_MARGIN = 12;
    private static final int HOTBAR_CLEARANCE = 28;

    private int hudX;
    private int hudY;
    private float hudScale;
    private boolean dragging;
    private double dragOffsetX;
    private double dragOffsetY;
    private Button resetButton;

    public AddictionHudEditorScreen() {
        super(Component.translatable("screen.SmokingWarningMod.hud_editor.title"));
    }

    @Override
    protected void init() {
        this.hudScale = AddictionHudRenderer.currentScale();
        this.hudX = AddictionHudRenderer.currentX(this.width, this.hudScale);
        this.hudY = AddictionHudRenderer.currentY(this.height, ClientAddictionHudData.snapshot(), this.hudScale);

        this.resetButton = Button.builder(Component.translatable("screen.SmokingWarningMod.hud_editor.reset"), button -> resetHud())
                .bounds(0, 0, RESET_BUTTON_WIDTH, RESET_BUTTON_HEIGHT)
                .build();
        this.addRenderableWidget(this.resetButton);
        positionResetButton();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, this.width, this.height, 0x18000000);
        clampHudToScreen();
        positionResetButton();

        boolean hoveringHud = isMouseOverHud(mouseX, mouseY);
        AddictionHudRenderer.renderEditorPreview(graphics, this.font, this.hudX, this.hudY, this.hudScale, hoveringHud, this.dragging);

        renderHelpText(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == InputConstants.MOUSE_BUTTON_LEFT && isMouseOverHud(mouseX, mouseY)) {
            this.dragging = true;
            this.dragOffsetX = mouseX - this.hudX;
            this.dragOffsetY = mouseY - this.hudY;
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (this.dragging && button == InputConstants.MOUSE_BUTTON_LEFT) {
            this.hudX = (int) Math.round(mouseX - this.dragOffsetX);
            this.hudY = (int) Math.round(mouseY - this.dragOffsetY);
            clampHudToScreen();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (this.dragging && button == InputConstants.MOUSE_BUTTON_LEFT) {
            this.dragging = false;
            clampHudToScreen();
            saveHud();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (scrollY != 0.0D) {
            float oldScale = this.hudScale;
            boolean anchorToMouse = isMouseOverHud(mouseX, mouseY);
            double anchorX = anchorToMouse ? (mouseX - this.hudX) / oldScale : 0.0D;
            double anchorY = anchorToMouse ? (mouseY - this.hudY) / oldScale : 0.0D;
            float direction = scrollY > 0.0D ? 1.0F : -1.0F;
            this.hudScale = Math.clamp(this.hudScale + direction * SCALE_STEP, AddictionHudRenderer.MIN_HUD_SCALE, AddictionHudRenderer.MAX_HUD_SCALE);
            if (anchorToMouse && this.hudScale != oldScale) {
                this.hudX = (int) Math.round(mouseX - anchorX * this.hudScale);
                this.hudY = (int) Math.round(mouseY - anchorY * this.hudScale);
            }
            clampHudToScreen();
            saveHud();
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == InputConstants.KEY_ESCAPE || SmokingWarningModClient.matchesEditHudKey(keyCode, scanCode)) {
            saveHud();
            this.onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        saveHud();
        super.onClose();
    }

    @Override
    public void removed() {
        saveHud();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    @Override
    protected void renderBlurredBackground(float partialTick) {
    }

    @Override
    public void renderTransparentBackground(GuiGraphics graphics) {
    }

    private void renderHelpText(GuiGraphics graphics) {
        Component help = Component.translatable("screen.SmokingWarningMod.hud_editor.help");
        int textWidth = this.font.width(help);
        int x = Math.max(EDGE_MARGIN, (this.width - textWidth) / 2);
        int y = 10;
        int right = Math.min(this.width - EDGE_MARGIN, x + textWidth);
        graphics.fill(x - 6, y - 4, right + 6, y + 13, 0x88000000);
        graphics.drawString(this.font, help, x, y, 0xFFFFFFFF);
    }

    private boolean isMouseOverHud(double mouseX, double mouseY) {
        ClientAddictionHudData.Snapshot snapshot = ClientAddictionHudData.snapshot();
        int scaledWidth = AddictionHudRenderer.scaledPanelWidth(this.hudScale);
        int scaledHeight = AddictionHudRenderer.scaledPanelHeight(snapshot, this.hudScale);
        return mouseX >= this.hudX
                && mouseX <= this.hudX + scaledWidth
                && mouseY >= this.hudY
                && mouseY <= this.hudY + scaledHeight;
    }

    private void resetHud() {
        this.hudScale = AddictionHudRenderer.DEFAULT_HUD_SCALE;
        this.hudX = AddictionHudRenderer.DEFAULT_HUD_X;
        this.hudY = AddictionHudRenderer.DEFAULT_HUD_Y;
        clampHudToScreen();
        saveHud();
    }

    private void positionResetButton() {
        if (this.resetButton == null) {
            return;
        }

        ClientAddictionHudData.Snapshot snapshot = ClientAddictionHudData.snapshot();
        int hudWidth = AddictionHudRenderer.scaledPanelWidth(this.hudScale);
        int hudHeight = AddictionHudRenderer.scaledPanelHeight(snapshot, this.hudScale);
        int x = Math.max(EDGE_MARGIN, this.width - RESET_BUTTON_WIDTH - EDGE_MARGIN);
        int y = Math.max(30, this.height - RESET_BUTTON_HEIGHT - HOTBAR_CLEARANCE);

        if (intersects(x, y, RESET_BUTTON_WIDTH, RESET_BUTTON_HEIGHT, this.hudX, this.hudY, hudWidth, hudHeight)) {
            int aboveHudY = this.hudY - RESET_BUTTON_HEIGHT - 8;
            if (aboveHudY >= 30) {
                y = aboveHudY;
            } else {
                x = EDGE_MARGIN;
            }
        }

        this.resetButton.setX(x);
        this.resetButton.setY(y);
    }

    private static boolean intersects(int ax, int ay, int aw, int ah, int bx, int by, int bw, int bh) {
        return ax < bx + bw && ax + aw > bx && ay < by + bh && ay + ah > by;
    }

    private void clampHudToScreen() {
        ClientAddictionHudData.Snapshot snapshot = ClientAddictionHudData.snapshot();
        this.hudScale = Math.clamp(this.hudScale, AddictionHudRenderer.MIN_HUD_SCALE, AddictionHudRenderer.MAX_HUD_SCALE);
        this.hudX = AddictionHudRenderer.clampHudX(this.hudX, this.width, this.hudScale);
        this.hudY = AddictionHudRenderer.clampHudY(this.hudY, this.height, snapshot, this.hudScale);
    }

    private void saveHud() {
        clampHudToScreen();
        AddictionHudRenderer.saveHudConfig(this.hudX, this.hudY, this.hudScale);
    }
}
