package com.wizualia.util;

import com.wizualia.WizualiaMod;
import com.wizualia.module.Modules;
import net.minecraft.client.MinecraftClient;

public final class ZoomState {
    private ZoomState() {}
    private static float prev = 1f, cur = 1f;

    public static void tick(MinecraftClient mc) {
        prev = cur;
        boolean active = Modules.ZOOM.enabled && mc.currentScreen == null && WizualiaMod.ZOOM_KEY.isPressed();
        float target = active ? (float) Modules.ZOOM.num("factor") : 1f;
        if (Modules.ZOOM.bool("smooth")) cur += (target - cur) * 0.35f;
        else cur = target;
        if (Math.abs(cur - target) < 0.005f) cur = target;
    }

    public static float factor(float tickDelta) {
        return prev + (cur - prev) * tickDelta;
    }
}
