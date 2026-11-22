package com.wizualia.module;

import java.util.List;

public class Module {
    public final String id, name;
    public final Category category;
    public final boolean toggleable;
    public final List<Setting> settings;
    public boolean enabled;
    public boolean expanded;
    public float anim, expandAnim;   // animacje GUI

    public Module(String id, String name, Category category, boolean toggleable, Setting... settings) {
        this.id = id; this.name = name; this.category = category;
        this.toggleable = toggleable; this.settings = List.of(settings);
    }

    public Setting get(String key) {
        for (Setting s : settings) if (s.key.equals(key)) return s;
        throw new IllegalArgumentException("Brak ustawienia: " + key);
    }

    public boolean bool(String key) { return get(key).bool(); }
    public double num(String key) { return get(key).value; }
    public int mode(String key) { return get(key).index(); }

    public void toggle() { enabled = !enabled; }
}
