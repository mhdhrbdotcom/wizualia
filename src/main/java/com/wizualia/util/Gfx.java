package com.wizualia.util;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

public final class Gfx {
    private Gfx() {}

    /** Mnoży kanał alfa koloru przez m (0..1). */
    public static int a(int col, float m) {
        int al = (int) (((col >>> 24) & 0xFF) * Math.max(0f, Math.min(1f, m)));
        return (al << 24) | (col & 0xFFFFFF);
    }

    public static int lerp(int a, int b, float t) {
        t = Math.max(0f, Math.min(1f, t));
        int aa = (int) (((a >>> 24) & 255) * (1 - t) + ((b >>> 24) & 255) * t);
        int rr = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int gg = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bb = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return (aa << 24) | (rr << 16) | (gg << 8) | bb;
    }

    /** Wypełniony prostokąt z zaokrąglonymi rogami. */
    public static void roundRect(DrawContext c, int x, int y, int w, int h, int r, int col) {
        if (((col >>> 24) & 0xFF) == 0 || w <= 0 || h <= 0) return;
        r = Math.max(0, Math.min(r, Math.min(w, h) / 2));
        if (r == 0) { c.fill(x, y, x + w, y + h, col); return; }
        c.fill(x, y + r, x + w, y + h - r, col);
        for (int i = 0; i < r; i++) {
            double d = r - i - 0.5;
            int inset = (int) Math.round(r - Math.sqrt(r * (double) r - d * d));
            c.fill(x + inset, y + i, x + w - inset, y + i + 1, col);
            c.fill(x + inset, y + h - i - 1, x + w - inset, y + h - i, col);
        }
    }

    /** Miękki cień pod panelem. */
    public static void shadow(DrawContext c, int x, int y, int w, int h, int r, float ga) {
        for (int i = 1; i <= 4; i++) {
            roundRect(c, x - i, y - i + 2, w + i * 2, h + i * 2, r + i, ((int) (14 * ga)) << 24);
        }
    }

    /** Tekst z zabezpieczeniem: MC traktuje bardzo małą alfę jak pełną nieprzezroczystość. */
    public static void text(DrawContext c, TextRenderer tr, String s, int x, int y, int col, boolean shadow) {
        if (((col >>> 24) & 0xFF) < 8) return;
        c.drawText(tr, s, x, y, col, shadow);
    }
}
