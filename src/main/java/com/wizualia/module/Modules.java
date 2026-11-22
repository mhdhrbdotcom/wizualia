package com.wizualia.module;

import com.wizualia.util.Theme;

import java.util.ArrayList;
import java.util.List;

public final class Modules {
    private Modules() {}

    // ----- WIDOK -----
    public static final Module FULLBRIGHT = new Module("fullbright", "Rozjaśnienie", Category.VIEW, true,
            Setting.slider("level", "Jasność", 2, 16, 12, true));

    public static final Module ZOOM = new Module("zoom", "Zoom (klawisz C)", Category.VIEW, true,
            Setting.slider("factor", "Przybliżenie", 1.5, 10, 4, false),
            Setting.toggle("smooth", "Płynne przybliżanie", true));

    public static final Module FOV = new Module("fov", "Własne FOV", Category.VIEW, true,
            Setting.slider("fov", "Pole widzenia", 30, 130, 100, true));

    public static final Module VIEWMODEL = new Module("viewmodel", "Model ręki", Category.VIEW, true,
            Setting.slider("x", "Przesunięcie X", -1, 1, 0, false),
            Setting.slider("y", "Przesunięcie Y", -1, 1, 0, false),
            Setting.slider("z", "Przesunięcie Z", -1, 1, 0, false),
            Setting.slider("scale", "Rozmiar", 0.3, 1.5, 1, false));

    public static final Module CROSSHAIR = new Module("crosshair", "Własny celownik", Category.VIEW, true,
            Setting.mode("style", "Styl", 0, "Krzyż", "Kropka", "Kółko"),
            Setting.slider("size", "Rozmiar", 2, 14, 5, true),
            Setting.slider("gap", "Odstęp", 0, 8, 2, true),
            Setting.slider("thick", "Grubość", 1, 4, 1, true),
            Setting.toggle("outline", "Czarny obrys", true),
            Setting.toggle("themed", "Kolor motywu", true));

    // ----- HUD -----
    public static final Module WATERMARK = new Module("watermark", "Znak wodny", Category.HUD, true);

    public static final Module ARRAYLIST = new Module("arraylist", "Lista modułów", Category.HUD, true);

    public static final Module INFO = new Module("info", "Panel informacji", Category.HUD, true,
            Setting.toggle("fps", "FPS", true),
            Setting.toggle("ping", "Ping", true),
            Setting.toggle("xyz", "Koordynaty", true),
            Setting.toggle("dir", "Kierunek", true),
            Setting.toggle("bps", "Prędkość", false),
            Setting.toggle("cps", "CPS", true),
            Setting.toggle("clock", "Zegar", false));

    public static final Module KEYS = new Module("keys", "Klawisze (WASD)", Category.HUD, true,
            Setting.toggle("space", "Spacja", true),
            Setting.toggle("mouse", "Przyciski myszy", true));

    public static final Module ARMOR = new Module("armor", "Pancerz", Category.HUD, true,
            Setting.toggle("dur", "Wytrzymałość %", true));

    public static final Module EFFECTS = new Module("effects", "Efekty mikstur", Category.HUD, true);

    public static final Module TARGET = new Module("target", "Wskaźnik celu", Category.HUD, true);

    public static final Module TOTEMS = new Module("totems", "Licznik totemów", Category.HUD, true);

    // ----- KLIENT -----
    public static final Module THEME = new Module("theme", "Motyw kolorystyczny", Category.CLIENT, false,
            Setting.mode("preset", "Kolor", 0, Theme.NAMES));

    public static final Module UI = new Module("ui", "Wygląd menu", Category.CLIENT, false,
            Setting.slider("dim", "Przyciemnienie tła", 0, 80, 40, true),
            Setting.slider("radius", "Zaokrąglenie", 0, 8, 5, true),
            Setting.toggle("anim", "Animacje", true));

    public static final List<Module> ALL = List.of(
            FULLBRIGHT, ZOOM, FOV, VIEWMODEL, CROSSHAIR,
            WATERMARK, ARRAYLIST, INFO, KEYS, ARMOR, EFFECTS, TARGET, TOTEMS,
            THEME, UI);

    static {
        WATERMARK.enabled = true;
        INFO.enabled = true;
        KEYS.enabled = true;
        ARMOR.enabled = true;
    }

    public static List<Module> of(Category c) {
        List<Module> out = new ArrayList<>();
        for (Module m : ALL) if (m.category == c) out.add(m);
        return out;
    }
}
