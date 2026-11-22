package com.wizualia.gui;

import com.wizualia.module.Category;
import com.wizualia.module.Config;
import com.wizualia.module.Module;
import com.wizualia.module.Modules;
import com.wizualia.module.Setting;
import com.wizualia.util.Gfx;
import com.wizualia.util.Theme;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ClickGuiScreen extends Screen {
    private static final int PW = 148, HEAD = 30, MROW = 22, GAP = 14;
    private static final int K_SEARCH = 0, K_HEADER = 1, K_MODULE = 2, K_SETTING = 3;

    private static final class Panel {
        final Category cat; int x, y;
        Panel(Category c, int x, int y) { this.cat = c; this.x = x; this.y = y; }
    }

    private record Hit(int kind, Panel p, Module m, Setting s, int x, int y, int w, int h) {
        boolean in(double mx, double my) { return mx >= x && mx < x + w && my >= y && my < y + h; }
    }

    private static List<Panel> panels;
    private final List<Hit> hits = new ArrayList<>();
    private long lastNs;
    private float openT, ga = 1f;
    private int yoff, accent, radius;
    private String query = "";
    private boolean searchFocus;
    private Panel dragPanel;
    private int dragDX, dragDY;
    private Hit activeSlider;

    public ClickGuiScreen() { super(Text.literal("Wizualia")); }

    @Override
    protected void init() {
        if (panels == null) {
            int total = Category.values().length * PW + (Category.values().length - 1) * GAP;
            int sx = Math.max(6, (width - total) / 2), sy = 44;
            panels = new ArrayList<>();
            int i = 0;
            for (Category c : Category.values()) panels.add(new Panel(c, sx + i++ * (PW + GAP), sy));
        }
    }

    private int c(int col) { return Gfx.a(col, ga); }

    private void text(DrawContext ctx, String s, int x, int y, int col) {
        Gfx.text(ctx, textRenderer, s, x, y, col, false);
    }

    private boolean matches(Module m) {
        if (query.isEmpty()) return true;
        String q = query.toLowerCase(Locale.ROOT);
        return m.name.toLowerCase(Locale.ROOT).contains(q) || m.id.contains(q);
    }

    private static int settingH(Setting s) { return s.type == Setting.Type.SLIDER ? 26 : 18; }

    private static int blockH(Module m) {
        if (m.settings.isEmpty()) return 0;
        int h = 6;
        for (Setting s : m.settings) h += settingH(s);
        return h;
    }

    @Override
    public void renderBackground(DrawContext ctx, int mx, int my, float delta) {
        // własne tło rysujemy w render()
    }

    @Override
    public void render(DrawContext ctx, int mx, int my, float delta) {
        long now = System.nanoTime();
        float dt = lastNs == 0 ? 0.016f : Math.min(0.1f, (now - lastNs) / 1e9f);
        lastNs = now;
        boolean animOn = Modules.UI.bool("anim");
        if (animOn) {
            openT += (1f - openT) * Math.min(1f, dt * 12f);
            if (openT > 0.995f) openT = 1f;
        } else openT = 1f;
        ga = openT;
        yoff = animOn ? Math.round((1f - openT) * 14f) : 0;
        accent = Theme.accent();
        radius = (int) Modules.UI.num("radius");

        int dim = (int) (Modules.UI.num("dim") / 100.0 * 255.0 * ga);
        if (dim > 0) ctx.fill(0, 0, width, height, dim << 24);

        hits.clear();

        // pole wyszukiwania
        int sw = 220, sh = 20, sx = width / 2 - sw / 2, sy = 12 + yoff;
        Gfx.shadow(ctx, sx, sy, sw, sh, radius + 1, ga);
        Gfx.roundRect(ctx, sx, sy, sw, sh, radius + 1, c(0xE60B1216));
        if (searchFocus) ctx.fill(sx + 8, sy + sh - 1, sx + sw - 8, sy + sh, c(accent));
        String shown;
        int scol;
        if (query.isEmpty() && !searchFocus) { shown = "Szukaj modułu (Ctrl+F)"; scol = 0xFF6F7F84; }
        else { shown = query + (searchFocus && (System.currentTimeMillis() / 500) % 2 == 0 ? "_" : ""); scol = 0xFFFFFFFF; }
        text(ctx, shown, sx + 10, sy + 6, c(scol));
        hits.add(new Hit(K_SEARCH, null, null, null, sx, sy, sw, sh));

        for (Panel p : panels) drawPanel(ctx, p, mx, my, dt, animOn);

        String hint = "LPM - włącz/wyłącz   PPM - ustawienia   Przeciągnij nagłówek, by przesunąć   B / ESC - zamknij";
        text(ctx, hint, width / 2 - textRenderer.getWidth(hint) / 2, height - 14, c(0xFF8A9A9C));
    }

    private void drawPanel(DrawContext ctx, Panel p, int mx, int my, float dt, boolean animOn) {
        List<Module> mods = new ArrayList<>();
        for (Module m : Modules.of(p.cat)) if (matches(m)) mods.add(m);

        int total = HEAD + 6;
        for (Module m : mods) {
            float target = m.expanded ? 1f : 0f;
            m.expandAnim = animOn ? m.expandAnim + (target - m.expandAnim) * Math.min(1f, dt * 14f) : target;
            if (Math.abs(target - m.expandAnim) < 0.004f) m.expandAnim = target;
            float at = m.toggleable ? (m.enabled ? 1f : 0f) : m.expandAnim;
            m.anim = animOn ? m.anim + (at - m.anim) * Math.min(1f, dt * 14f) : at;
            if (Math.abs(at - m.anim) < 0.004f) m.anim = at;
            total += MROW + Math.round(blockH(m) * m.expandAnim);
        }

        int px = p.x, py = p.y + yoff;
        Gfx.shadow(ctx, px, py, PW, total, radius + 1, ga);
        Gfx.roundRect(ctx, px, py, PW, total, radius, c(0xE60B1216));
        Gfx.roundRect(ctx, px, py, PW, HEAD, radius, c(0xFF121D24));
        ctx.fill(px + 10, py + HEAD - 1, px + PW - 10, py + HEAD, c(Gfx.a(accent, 0.55f)));
        text(ctx, p.cat.title, px + 12, py + 11, c(0xFFFFFFFF));

        int on = 0, all = 0;
        for (Module m : Modules.of(p.cat)) if (m.toggleable) { all++; if (m.enabled) on++; }
        if (all > 0) {
            String cnt = on + "/" + all;
            text(ctx, cnt, px + PW - 12 - textRenderer.getWidth(cnt), py + 11, c(accent));
        }
        hits.add(new Hit(K_HEADER, p, null, null, px, py, PW, HEAD));

        int cy = py + HEAD + 3;
        for (Module m : mods) {
            int rx = px + 4, rw = PW - 8;
            boolean hov = mx >= rx && mx < rx + rw && my >= cy && my < cy + MROW;
            int rr = Math.max(0, radius - 2);
            if (hov) Gfx.roundRect(ctx, rx, cy + 1, rw, MROW - 2, rr, c(0x16FFFFFF));
            if (m.anim > 0.01f) Gfx.roundRect(ctx, rx, cy + 1, rw, MROW - 2, rr, c(Gfx.a(accent, 0.28f * m.anim)));
            text(ctx, m.name, px + 13, cy + 7, c(Gfx.lerp(0xFF8F9FA3, 0xFFFFFFFF, m.anim)));

            if (m.toggleable) {
                int tx = px + PW - 30, ty = cy + 6;
                Gfx.roundRect(ctx, tx, ty, 20, 10, 5, c(Gfx.lerp(0xFF25323A, accent, m.anim)));
                Gfx.roundRect(ctx, tx + 1 + Math.round(10 * m.anim), ty + 1, 8, 8, 4, c(0xFFFFFFFF));
            }
            if (!m.settings.isEmpty()) {
                String ch = m.expanded ? "v" : ">";
                text(ctx, ch, m.toggleable ? px + PW - 40 : px + PW - 16, cy + 7, c(0xFF6F7F84));
            }
            hits.add(new Hit(K_MODULE, p, m, null, rx, cy, rw, MROW));
            cy += MROW;

            int vis = Math.round(blockH(m) * m.expandAnim);
            if (vis > 0) {
                ctx.enableScissor(px, cy, px + PW, cy + vis);
                int sy = cy + 3;
                for (Setting s : m.settings) {
                    drawSetting(ctx, px, sy, s);
                    if (m.expandAnim > 0.98f) hits.add(new Hit(K_SETTING, p, m, s, px + 8, sy, PW - 16, settingH(s)));
                    sy += settingH(s);
                }
                ctx.disableScissor();
                cy += vis;
            }
        }
    }

    private void drawSetting(DrawContext ctx, int px, int sy, Setting s) {
        switch (s.type) {
            case SLIDER -> {
                text(ctx, s.name, px + 12, sy + 2, c(0xFFB4C2C6));
                String v = s.display();
                text(ctx, v, px + PW - 12 - textRenderer.getWidth(v), sy + 2, c(accent));
                int tx = px + 12, tw = PW - 24;
                Gfx.roundRect(ctx, tx, sy + 16, tw, 4, 2, c(0xFF1B272D));
                int fw = Math.round(tw * (float) s.ratio());
                if (fw > 0) Gfx.roundRect(ctx, tx, sy + 16, Math.max(fw, 3), 4, 2, c(accent));
                Gfx.roundRect(ctx, tx + fw - 3, sy + 14, 6, 8, 3, c(0xFFFFFFFF));
            }
            case BOOL -> {
                text(ctx, s.name, px + 12, sy + 5, c(0xFFB4C2C6));
                int tx = px + PW - 12 - 18;
                Gfx.roundRect(ctx, tx, sy + 4, 18, 9, 4, c(s.bool() ? accent : 0xFF25323A));
                Gfx.roundRect(ctx, tx + (s.bool() ? 10 : 1), sy + 5, 7, 7, 3, c(0xFFFFFFFF));
            }
            case MODE -> {
                text(ctx, s.name, px + 12, sy + 5, c(0xFFB4C2C6));
                String v = s.display();
                int vw = textRenderer.getWidth(v) + 12;
                int vx = px + PW - 12 - vw;
                Gfx.roundRect(ctx, vx, sy + 1, vw, 15, 4, c(Gfx.a(accent, 0.22f)));
                text(ctx, v, vx + 6, sy + 5, c(accent));
            }
        }
    }

    // ------------------------------------------------------------ input
    @Override
    public boolean mouseClicked(double mx, double my, int button) {
        searchFocus = false;
        for (int i = hits.size() - 1; i >= 0; i--) {
            Hit h = hits.get(i);
            if (!h.in(mx, my)) continue;
            switch (h.kind) {
                case K_SEARCH -> searchFocus = true;
                case K_HEADER -> {
                    if (button == 0) { dragPanel = h.p; dragDX = (int) mx - h.p.x; dragDY = (int) my - h.p.y; }
                }
                case K_MODULE -> {
                    Module m = h.m;
                    if (button == 0 && m.toggleable) m.toggle();
                    else if (!m.settings.isEmpty()) m.expanded = !m.expanded;
                }
                case K_SETTING -> {
                    Setting s = h.s;
                    switch (s.type) {
                        case SLIDER -> {
                            if (button == 0) {
                                activeSlider = h;
                                s.setFromRatio((mx - (h.x + 4)) / (double) (h.w - 8));
                            }
                        }
                        case BOOL -> s.flip();
                        case MODE -> s.cycle(button == 1 ? -1 : 1);
                    }
                }
                default -> {}
            }
            return true;
        }
        return super.mouseClicked(mx, my, button);
    }

    @Override
    public boolean mouseDragged(double mx, double my, int button, double dx, double dy) {
        if (dragPanel != null) {
            dragPanel.x = (int) mx - dragDX;
            dragPanel.y = (int) my - dragDY;
            return true;
        }
        if (activeSlider != null) {
            activeSlider.s.setFromRatio((mx - (activeSlider.x + 4)) / (double) (activeSlider.w - 8));
            return true;
        }
        return super.mouseDragged(mx, my, button, dx, dy);
    }

    @Override
    public boolean mouseReleased(double mx, double my, int button) {
        dragPanel = null;
        activeSlider = null;
        return super.mouseReleased(mx, my, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_F && (modifiers & GLFW.GLFW_MOD_CONTROL) != 0) {
            searchFocus = true;
            return true;
        }
        if (searchFocus) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                if (query.isEmpty()) searchFocus = false; else query = "";
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (!query.isEmpty()) query = query.substring(0, query.length() - 1);
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER) { searchFocus = false; return true; }
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_B) { close(); return true; }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocus && chr >= 32 && query.length() < 24) {
            query += chr;
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public void removed() { Config.save(); }

    @Override
    public boolean shouldPause() { return false; }
}
