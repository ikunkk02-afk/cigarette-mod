package com.example.examplemod;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

public final class CigaretteDizzyVisuals {
    private static int clientDizzyTicks;
    private static int clientDizzyTotalTicks;

    private CigaretteDizzyVisuals() {
    }

    public static void start(int ticks) {
        if (ticks <= 0) {
            return;
        }
        if (ticks >= clientDizzyTicks) {
            clientDizzyTicks = ticks;
            clientDizzyTotalTicks = ticks;
        }
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        if (clientDizzyTicks > 0) {
            clientDizzyTicks--;
            if (clientDizzyTicks == 0) {
                clientDizzyTotalTicks = 0;
            }
        }
    }

    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        float intensity = intensity((float) event.getPartialTick());
        if (intensity <= 0.0F) {
            return;
        }

        double time = renderTime(event.getPartialTick());
        double fovPulse = Math.sin(time * 0.34D) * 2.4D * intensity;
        event.setFOV(event.getFOV() + fovPulse);
    }

    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        float intensity = intensity((float) event.getPartialTick());
        if (intensity <= 0.0F) {
            return;
        }

        double time = renderTime(event.getPartialTick());
        event.setRoll(event.getRoll() + (float) (Math.sin(time * 0.42D) * 1.15D * intensity));
        event.setPitch(event.getPitch() + (float) (Math.sin(time * 0.23D + 1.4D) * 0.18D * intensity));
    }

    public static void onRenderGui(RenderGuiEvent.Post event) {
        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        float intensity = intensity(partialTick);
        if (intensity <= 0.0F) {
            return;
        }

        GuiGraphics graphics = event.getGuiGraphics();
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        int edge = Math.max(10, Math.min(width, height) / 12);
        int alpha = Math.clamp((int) (70.0F * intensity), 0, 95);
        int cyan = argb(alpha, 40, 210, 220);
        int purple = argb(alpha, 145, 70, 210);

        graphics.fill(0, 0, width, edge, cyan);
        graphics.fill(0, height - edge, width, height, purple);
        graphics.fill(0, 0, edge, height, argb(alpha / 2, 110, 80, 190));
        graphics.fill(width - edge, 0, width, height, argb(alpha / 2, 25, 190, 210));

        double time = renderTime(partialTick);
        for (int i = 0; i < 6; i++) {
            double wave = Math.sin(time * 0.28D + i * 1.45D);
            int y = (int) (height * 0.5D + wave * height * 0.35D);
            int lineAlpha = Math.clamp((int) ((18 - i * 2) * intensity), 0, 28);
            graphics.fill(0, Math.clamp(y, 0, height - 1), width, Math.clamp(y + 2, 1, height), argb(lineAlpha, 210, 80, 220));
        }
    }

    private static float intensity(float partialTick) {
        if (clientDizzyTicks <= 0 || clientDizzyTotalTicks <= 0) {
            return 0.0F;
        }
        float remaining = Math.clamp(clientDizzyTicks + partialTick, 0.0F, clientDizzyTotalTicks);
        float fade = remaining / clientDizzyTotalTicks;
        return fade * fade;
    }

    private static double renderTime(double partialTick) {
        Minecraft minecraft = Minecraft.getInstance();
        int tickCount = minecraft.player == null ? 0 : minecraft.player.tickCount;
        return tickCount + partialTick;
    }

    private static int argb(int alpha, int red, int green, int blue) {
        return (Math.clamp(alpha, 0, 255) << 24)
                | (Math.clamp(red, 0, 255) << 16)
                | (Math.clamp(green, 0, 255) << 8)
                | Math.clamp(blue, 0, 255);
    }
}
