package dev.voxira.client.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dev.voxira.client.VoxiraClient;
import dev.voxira.client.module.Module;
import dev.voxira.client.module.ModuleManager;
import dev.voxira.client.module.hud.HudModule;
import dev.voxira.client.module.setting.Setting;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/** Stores module state, settings and HUD positions in config/voxira.json. */
public final class VoxiraConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("voxira.json");

    private VoxiraConfig() {
    }

    public static void load(ModuleManager modules) {
        if (!Files.exists(FILE)) {
            save(modules);
            return;
        }
        try {
            JsonObject root = JsonParser.parseString(Files.readString(FILE, StandardCharsets.UTF_8)).getAsJsonObject();
            JsonObject mods = root.has("modules") ? root.getAsJsonObject("modules") : new JsonObject();
            for (Module module : modules.all()) {
                if (!mods.has(module.id())) {
                    continue;
                }
                JsonObject entry = mods.getAsJsonObject(module.id());
                if (entry.has("enabled")) {
                    module.setEnabled(entry.get("enabled").getAsBoolean());
                }
                if (entry.has("settings")) {
                    JsonObject values = entry.getAsJsonObject("settings");
                    for (Setting<?> setting : module.settings()) {
                        if (values.has(setting.id())) {
                            try {
                                setting.fromJson(values.get(setting.id()));
                            } catch (RuntimeException e) {
                                setting.reset();
                            }
                        }
                    }
                }
                if (module instanceof HudModule hud && entry.has("x") && entry.has("y")) {
                    hud.setPosition(entry.get("x").getAsInt(), entry.get("y").getAsInt());
                }
            }
        } catch (IOException | RuntimeException e) {
            VoxiraClient.LOGGER.warn("Could not read {}, using defaults", FILE, e);
        }
    }

    public static void save(ModuleManager modules) {
        JsonObject mods = new JsonObject();
        for (Module module : modules.all()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("enabled", module.isEnabled());
            if (!module.settings().isEmpty()) {
                JsonObject values = new JsonObject();
                for (Setting<?> setting : module.settings()) {
                    values.add(setting.id(), setting.toJson());
                }
                entry.add("settings", values);
            }
            if (module instanceof HudModule hud) {
                entry.addProperty("x", hud.x());
                entry.addProperty("y", hud.y());
            }
            mods.add(module.id(), entry);
        }
        JsonObject root = new JsonObject();
        root.addProperty("version", 1);
        root.add("modules", mods);
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException e) {
            VoxiraClient.LOGGER.warn("Could not save {}", FILE, e);
        }
    }
}
