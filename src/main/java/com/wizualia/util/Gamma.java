package com.wizualia.util;

import com.wizualia.mixin.SimpleOptionAccessor;
import com.wizualia.module.Modules;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.SimpleOption;

public final class Gamma {
    private Gamma() {}
    private static double saved = -1;

    @SuppressWarnings("unchecked")
    private static void set(SimpleOption<Double> g, double v) {
        ((SimpleOptionAccessor<Double>) (Object) g).wizualia$setValue(v);
    }

    public static void tick(MinecraftClient mc) {
        if (mc.options == null) return;
        SimpleOption<Double> g = mc.options.getGamma();
        if (Modules.FULLBRIGHT.enabled) {
            if (saved < 0) saved = Math.min(g.getValue(), 1.0);
            set(g, Modules.FULLBRIGHT.num("level"));
        } else if (saved >= 0) {
            set(g, saved);
            saved = -1;
        }
    }

    public static void restore(MinecraftClient mc) {
        if (saved >= 0 && mc.options != null) {
            set(mc.options.getGamma(), saved);
            saved = -1;
        }
    }
}
