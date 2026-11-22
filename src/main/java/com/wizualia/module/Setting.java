package com.wizualia.module;

import java.util.Locale;

public class Setting {
    public enum Type { SLIDER, BOOL, MODE }

    public final String key, name;
    public final Type type;
    public final double min, max;
    public final boolean integer;
    public final String[] modes;
    public double value;

    private Setting(String key, String name, Type type, double min, double max, double def, boolean integer, String[] modes) {
        this.key = key; this.name = name; this.type = type;
        this.min = min; this.max = max; this.value = def; this.integer = integer; this.modes = modes;
    }

    public static Setting slider(String key, String name, double min, double max, double def, boolean integer) {
        return new Setting(key, name, Type.SLIDER, min, max, def, integer, null);
    }

    public static Setting toggle(String key, String name, boolean def) {
        return new Setting(key, name, Type.BOOL, 0, 1, def ? 1 : 0, true, null);
    }

    public static Setting mode(String key, String name, int def, String... modes) {
        return new Setting(key, name, Type.MODE, 0, modes.length - 1, def, true, modes);
    }

    public boolean bool() { return value > 0.5; }
    public int index() { return (int) Math.round(value); }

    public void flip() { value = bool() ? 0 : 1; }

    public void cycle(int dir) {
        int n = modes.length;
        value = ((index() + dir) % n + n) % n;
    }

    public double ratio() { return max == min ? 0 : (value - min) / (max - min); }

    public void setFromRatio(double r) {
        r = Math.max(0, Math.min(1, r));
        double v = min + (max - min) * r;
        value = integer ? Math.round(v) : Math.round(v * 10.0) / 10.0;
    }

    public void setClamped(double v) {
        value = Math.max(min, Math.min(max, v));
    }

    public String display() {
        return switch (type) {
            case BOOL -> bool() ? "Wł." : "Wył.";
            case MODE -> modes[Math.max(0, Math.min(modes.length - 1, index()))];
            case SLIDER -> integer ? String.valueOf((int) value) : String.format(Locale.ROOT, "%.1f", value);
        };
    }
}
