package com.wizualia.util;

import com.wizualia.module.Modules;

import java.awt.Color;

public final class Theme {
    private Theme() {}

    public static final String[] NAMES = {"Turkus", "Fiolet", "Róż", "Pomarańcz", "Niebieski", "Limonka", "Tęcza"};
    private static final int[] COLORS = {
            0xFF2EC4B6, 0xFFA77BFF, 0xFFFF6FA8, 0xFFFF9F43, 0xFF4DA3FF, 0xFF9BE564
    };

    public static int accent() {
        int i = Modules.THEME.mode("preset");
        if (i >= 0 && i < COLORS.length) return COLORS[i];
        float h = (System.currentTimeMillis() % 6000L) / 6000f;
        return 0xFF000000 | (Color.HSBtoRGB(h, 0.55f, 1f) & 0xFFFFFF);
    }
}
