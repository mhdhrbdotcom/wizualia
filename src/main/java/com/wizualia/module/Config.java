package com.wizualia.module;

import net.fabricmc.loader.api.FabricLoader;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public final class Config {
    private Config() {}

    private static Path path() {
        return FabricLoader.getInstance().getConfigDir().resolve("wizualia.properties");
    }

    public static void load() {
        Path p = path();
        if (!Files.exists(p)) return;
        Properties pr = new Properties();
        try (Reader r = new InputStreamReader(Files.newInputStream(p), StandardCharsets.UTF_8)) {
            pr.load(r);
        } catch (Exception e) {
            return;
        }
        for (Module m : Modules.ALL) {
            String en = pr.getProperty(m.id + ".enabled");
            if (en != null && m.toggleable) m.enabled = Boolean.parseBoolean(en);
            for (Setting s : m.settings) {
                String v = pr.getProperty(m.id + "." + s.key);
                if (v == null) continue;
                try { s.setClamped(Double.parseDouble(v)); } catch (NumberFormatException ignored) {}
            }
        }
    }

    public static void save() {
        Properties pr = new Properties();
        for (Module m : Modules.ALL) {
            if (m.toggleable) pr.setProperty(m.id + ".enabled", String.valueOf(m.enabled));
            for (Setting s : m.settings) pr.setProperty(m.id + "." + s.key, String.valueOf(s.value));
        }
        try (Writer w = new OutputStreamWriter(Files.newOutputStream(path()), StandardCharsets.UTF_8)) {
            pr.store(w, "Wizualia");
        } catch (Exception ignored) {}
    }
}
