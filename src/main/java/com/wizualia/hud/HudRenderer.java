package com.wizualia.hud;

import com.wizualia.module.Module;
import com.wizualia.module.Modules;
import com.wizualia.util.Gfx;
import com.wizualia.util.Theme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.effect.StatusEffectCategory;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Deque;
import java.util.List;
import java.util.Locale;

public final class HudRenderer {
    private HudRenderer() {}

    private static final int PILL = 0xB00B1217;
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm:ss");

    // CPS / prędkość
    private static final Deque<Long> LMB = new ArrayDeque<>(), RMB = new ArrayDeque<>();
    private static boolean lastL, lastR, hasLast;
    private static double bps, lastX, lastZ;

    // animacje
    private static final float[] KEY_ANIM = new float[7];
    private static float targetShown;
    private static int targetId = -1;

    // ---------------------------------------------------------------- tick
    public static void tick(MinecraftClient mc) {
        if (mc.player == null) { hasLast = false; return; }
        long now = System.currentTimeMillis();
        boolean l = mc.options.attackKey.isPressed(), r = mc.options.useKey.isPressed();
        if (l && !lastL) LMB.addLast(now);
        if (r && !lastR) RMB.addLast(now);
        lastL = l; lastR = r;
        while (!LMB.isEmpty() && now - LMB.peekFirst() > 1000) LMB.pollFirst();
        while (!RMB.isEmpty() && now - RMB.peekFirst() > 1000) RMB.pollFirst();

        double x = mc.player.getX(), z = mc.player.getZ();
        if (hasLast) {
            double v = Math.sqrt((x - lastX) * (x - lastX) + (z - lastZ) * (z - lastZ)) * 20.0;
            bps = bps * 0.7 + v * 0.3;
        }
        lastX = x; lastZ = z; hasLast = true;
    }

    // ---------------------------------------------------------------- helpers
    private static void pill(DrawContext ctx, int x, int y, int w, int h, int accent) {
        Gfx.roundRect(ctx, x, y, w, h, 3, PILL);
        ctx.fill(x, y + 3, x + 2, y + h - 3, accent);
    }

    // ---------------------------------------------------------------- render
    public static void render(DrawContext ctx, RenderTickCounter tick) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || mc.options.hudHidden) return;
        TextRenderer tr = mc.textRenderer;
        int W = ctx.getScaledWindowWidth(), H = ctx.getScaledWindowHeight();
        int accent = Theme.accent();
        int y = 6;

        if (Modules.WATERMARK.enabled) {
            String a = "Wizualia", b = " 1.0";
            int w = tr.getWidth(a + b) + 16;
            pill(ctx, 6, y, w, 16, accent);
            ctx.drawText(tr, a, 13, y + 4, accent, true);
            ctx.drawText(tr, b, 13 + tr.getWidth(a), y + 4, 0xFF8A9A9C, true);
            y += 20;
        }
        if (Modules.INFO.enabled) y = info(ctx, tr, mc, accent, y);
        if (Modules.ARRAYLIST.enabled) arrayList(ctx, tr, W, accent);
        if (Modules.KEYS.enabled) keys(ctx, tr, mc, H, accent);
        if (Modules.ARMOR.enabled) armor(ctx, tr, mc, W, H);
        if (Modules.TOTEMS.enabled) totems(ctx, tr, mc, W, H, accent);
        if (Modules.EFFECTS.enabled) effects(ctx, tr, mc, W, H, accent);
        if (Modules.TARGET.enabled) target(ctx, tr, mc, W, H, accent);
        if (Modules.CROSSHAIR.enabled) crosshair(ctx, W, H, accent, mc);
    }

    // ---------------------------------------------------------------- info
    private static int info(DrawContext ctx, TextRenderer tr, MinecraftClient mc, int accent, int y) {
        Module m = Modules.INFO;
        List<String[]> lines = new ArrayList<>();
        if (m.bool("fps")) lines.add(new String[]{"FPS", String.valueOf(mc.getCurrentFps())});
        if (m.bool("ping")) {
            PlayerListEntry e = mc.getNetworkHandler() != null ? mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid()) : null;
            lines.add(new String[]{"Ping", e != null ? e.getLatency() + " ms" : "-"});
        }
        if (m.bool("xyz")) lines.add(new String[]{"XYZ", String.format(Locale.ROOT, "%.1f  %.1f  %.1f",
                mc.player.getX(), mc.player.getY(), mc.player.getZ())});
        if (m.bool("dir")) {
            String d = switch (mc.player.getHorizontalFacing()) {
                case NORTH -> "Północ";
                case SOUTH -> "Południe";
                case EAST -> "Wschód";
                case WEST -> "Zachód";
                default -> "-";
            };
            lines.add(new String[]{"Kierunek", d});
        }
        if (m.bool("bps")) lines.add(new String[]{"Prędkość", String.format(Locale.ROOT, "%.1f b/s", bps)});
        if (m.bool("cps")) lines.add(new String[]{"CPS", LMB.size() + " | " + RMB.size()});
        if (m.bool("clock")) lines.add(new String[]{"Czas", LocalTime.now().format(CLOCK)});
        if (lines.isEmpty()) return y;

        int w = 0;
        for (String[] l : lines) w = Math.max(w, tr.getWidth(l[0] + ": " + l[1]));
        w += 16;
        int h = lines.size() * 11 + 6;
        pill(ctx, 6, y, w, h, accent);
        int ty = y + 4;
        for (String[] l : lines) {
            ctx.drawText(tr, l[0] + ":", 13, ty, accent, true);
            ctx.drawText(tr, l[1], 13 + tr.getWidth(l[0] + ": "), ty, 0xFFFFFFFF, true);
            ty += 11;
        }
        return y + h + 4;
    }

    // ---------------------------------------------------------------- lista modułów
    private static void arrayList(DrawContext ctx, TextRenderer tr, int W, int accent) {
        List<Module> on = new ArrayList<>();
        for (Module m : Modules.ALL) if (m.toggleable && m.enabled && m != Modules.ARRAYLIST) on.add(m);
        on.sort(Comparator.comparingInt((Module m) -> tr.getWidth(m.name)).reversed());
        int y = 6;
        for (Module m : on) {
            int w = tr.getWidth(m.name) + 12;
            int x = W - 6 - w;
            Gfx.roundRect(ctx, x, y, w, 13, 3, PILL);
            ctx.fill(x + w - 2, y + 3, x + w, y + 10, accent);
            ctx.drawText(tr, m.name, x + 5, y + 3, 0xFFFFFFFF, true);
            y += 15;
        }
    }

    // ---------------------------------------------------------------- klawisze
    private static void keyBox(DrawContext ctx, TextRenderer tr, int i, boolean down, int x, int y, int w, int h, String label, int accent) {
        KEY_ANIM[i] += ((down ? 1f : 0f) - KEY_ANIM[i]) * 0.3f;
        int bg = Gfx.lerp(PILL, Gfx.a(accent, 0.85f), KEY_ANIM[i]);
        Gfx.roundRect(ctx, x, y, w, h, 3, bg);
        if (!label.isEmpty()) {
            int col = Gfx.lerp(0xFFD0DCDF, 0xFF101820, KEY_ANIM[i]);
            ctx.drawText(tr, label, x + (w - tr.getWidth(label)) / 2, y + (h - 8) / 2, col, false);
        }
    }

    private static void keys(DrawContext ctx, TextRenderer tr, MinecraftClient mc, int H, int accent) {
        Module m = Modules.KEYS;
        int total = 42 + (m.bool("space") ? 14 : 0) + (m.bool("mouse") ? 22 : 0);
        int x = 6, y = H / 2 + 10;
        keyBox(ctx, tr, 0, mc.options.forwardKey.isPressed(), x + 22, y, 20, 20, "W", accent);
        keyBox(ctx, tr, 1, mc.options.leftKey.isPressed(), x, y + 22, 20, 20, "A", accent);
        keyBox(ctx, tr, 2, mc.options.backKey.isPressed(), x + 22, y + 22, 20, 20, "S", accent);
        keyBox(ctx, tr, 3, mc.options.rightKey.isPressed(), x + 44, y + 22, 20, 20, "D", accent);
        int cy = y + 44;
        if (m.bool("space")) {
            keyBox(ctx, tr, 4, mc.options.jumpKey.isPressed(), x, cy, 64, 12, "", accent);
            cy += 14;
        }
        if (m.bool("mouse")) {
            keyBox(ctx, tr, 5, mc.options.attackKey.isPressed(), x, cy, 31, 18, "LPM", accent);
            keyBox(ctx, tr, 6, mc.options.useKey.isPressed(), x + 33, cy, 31, 18, "PPM", accent);
        }
    }

    // ---------------------------------------------------------------- pancerz
    private static void armor(DrawContext ctx, TextRenderer tr, MinecraftClient mc, int W, int H) {
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        int x0 = W / 2 + 98, y = H - 22;
        boolean dur = Modules.ARMOR.bool("dur");
        for (int i = 0; i < 4; i++) {
            ItemStack st = mc.player.getEquippedStack(slots[i]);
            if (st.isEmpty()) continue;
            int x = x0 + i * 18;
            ctx.drawItem(st, x, y);
            if (dur && st.isDamageable()) {
                int max = st.getMaxDamage();
                int pct = Math.round(100f * (max - st.getDamage()) / max);
                int col = Gfx.lerp(0xFFFF5555, 0xFF55FF77, pct / 100f);
                String t = pct + "%";
                ctx.getMatrices().push();
                ctx.getMatrices().translate(x + 8, y - 7, 0);
                ctx.getMatrices().scale(0.6f, 0.6f, 1f);
                ctx.drawText(tr, t, -tr.getWidth(t) / 2, 0, col, true);
                ctx.getMatrices().pop();
            }
        }
    }

    // ---------------------------------------------------------------- totemy
    private static void totems(DrawContext ctx, TextRenderer tr, MinecraftClient mc, int W, int H, int accent) {
        int count = 0;
        for (int i = 0; i < mc.player.getInventory().size(); i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.TOTEM_OF_UNDYING)) count += s.getCount();
        }
        int x = W / 2 - 91 - 70, y = H - 24;
        Gfx.roundRect(ctx, x, y, 38, 20, 4, PILL);
        ctx.drawItem(new ItemStack(Items.TOTEM_OF_UNDYING), x + 2, y + 2);
        ctx.drawText(tr, "x" + count, x + 21, y + 6, count > 0 ? 0xFFFFFFFF : 0xFFFF5555, true);
    }

    // ---------------------------------------------------------------- efekty
    private static void effects(DrawContext ctx, TextRenderer tr, MinecraftClient mc, int W, int H, int accent) {
        int y = H - 6 - 14;
        for (StatusEffectInstance e : mc.player.getStatusEffects()) {
            String name = e.getEffectType().value().getName().getString();
            if (e.getAmplifier() > 0) name += " " + (e.getAmplifier() + 1);
            int sec = e.getDuration() / 20;
            String time = e.isInfinite() ? "inf" : String.format(Locale.ROOT, "%d:%02d", sec / 60, sec % 60);
            String line = name + "  " + time;
            int w = tr.getWidth(line) + 12;
            int x = W - 6 - w;
            boolean bad = e.getEffectType().value().getCategory() == StatusEffectCategory.HARMFUL;
            Gfx.roundRect(ctx, x, y, w, 14, 3, PILL);
            ctx.fill(x + w - 2, y + 3, x + w, y + 11, bad ? 0xFFFF5555 : accent);
            ctx.drawText(tr, name, x + 5, y + 3, bad ? 0xFFFF9090 : 0xFFFFFFFF, true);
            ctx.drawText(tr, time, x + 5 + tr.getWidth(name + "  "), y + 3, 0xFF9AA8AB, true);
            y -= 16;
        }
    }

    // ---------------------------------------------------------------- cel
    private static void target(DrawContext ctx, TextRenderer tr, MinecraftClient mc, int W, int H, int accent) {
        Entity e = mc.targetedEntity;
        if (!(e instanceof LivingEntity le) || !le.isAlive()) return;
        float hp = le.getHealth(), max = Math.max(1f, le.getMaxHealth());
        if (targetId != le.getId()) { targetId = le.getId(); targetShown = hp; }
        targetShown += (hp - targetShown) * 0.15f;

        int w = 136, h = 32, x = W / 2 - w / 2, y = H / 2 + 28;
        pill(ctx, x, y, w, h, accent);
        ctx.drawText(tr, le.getName().getString(), x + 8, y + 5, 0xFFFFFFFF, true);
        String t = String.format(Locale.ROOT, "%.1f", hp);
        ctx.drawText(tr, t, x + w - 8 - tr.getWidth(t), y + 5, 0xFFB4C2C6, true);
        int bx = x + 8, bw = w - 16;
        Gfx.roundRect(ctx, bx, y + 19, bw, 6, 3, 0xFF1B272D);
        float ratio = Math.max(0f, Math.min(1f, targetShown / max));
        int fw = Math.round(bw * ratio);
        if (fw > 0) Gfx.roundRect(ctx, bx, y + 19, Math.max(fw, 4), 6, 3, Gfx.lerp(0xFFFF5555, 0xFF55FF88, ratio));
    }

    // ---------------------------------------------------------------- celownik
    private static void crosshair(DrawContext ctx, int W, int H, int accent, MinecraftClient mc) {
        if (!mc.options.getPerspective().isFirstPerson()) return;
        Module m = Modules.CROSSHAIR;
        int cx = W / 2, cy = H / 2;
        int size = (int) m.num("size"), gap = (int) m.num("gap"), t = (int) m.num("thick");
        int lo = t / 2, hi = t - lo;
        int col = m.bool("themed") ? accent : 0xFFFFFFFF;
        boolean ol = m.bool("outline");
        switch (m.mode("style")) {
            case 0 -> {
                int[][] r = {
                        {cx - gap - size, cy - lo, cx - gap, cy + hi},
                        {cx + gap, cy - lo, cx + gap + size, cy + hi},
                        {cx - lo, cy - gap - size, cx + hi, cy - gap},
                        {cx - lo, cy + gap, cx + hi, cy + gap + size}};
                if (ol) for (int[] q : r) ctx.fill(q[0] - 1, q[1] - 1, q[2] + 1, q[3] + 1, 0xCC000000);
                for (int[] q : r) ctx.fill(q[0], q[1], q[2], q[3], col);
            }
            case 1 -> {
                int s = Math.max(1, size / 2);
                if (ol) ctx.fill(cx - s - 1, cy - s - 1, cx + s + 1, cy + s + 1, 0xCC000000);
                ctx.fill(cx - s, cy - s, cx + s, cy + s, col);
            }
            default -> {
                int r = size + gap + 2;
                int n = Math.max(20, r * 6);
                if (ol) for (int i = 0; i < n; i++) {
                    double a = i * Math.PI * 2 / n;
                    int px = cx + (int) Math.round(Math.cos(a) * r), py = cy + (int) Math.round(Math.sin(a) * r);
                    ctx.fill(px - 1, py - 1, px + t + 1, py + t + 1, 0xCC000000);
                }
                for (int i = 0; i < n; i++) {
                    double a = i * Math.PI * 2 / n;
                    int px = cx + (int) Math.round(Math.cos(a) * r), py = cy + (int) Math.round(Math.sin(a) * r);
                    ctx.fill(px, py, px + t, py + t, col);
                }
            }
        }
    }
}
