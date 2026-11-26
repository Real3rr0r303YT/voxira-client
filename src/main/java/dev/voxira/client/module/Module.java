package dev.voxira.client.module;

import dev.voxira.client.module.setting.Setting;
import net.minecraft.client.Minecraft;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base of every Voxira module: a fixed id (used in the config), a display name,
 * a category, an on/off state and optional settings.
 */
public abstract class Module {
    protected static final Minecraft MC = Minecraft.getInstance();

    private final String id;
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;
    private final List<Setting<?>> settings = new ArrayList<>();

    protected Module(String id, String name, String description, Category category, boolean enabledByDefault) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.category = category;
        this.enabled = enabledByDefault;
    }

    public final String id() {
        return id;
    }

    public final String name() {
        return name;
    }

    public final String description() {
        return description;
    }

    public final Category category() {
        return category;
    }

    /** Registers a setting; call from field initializers in subclasses. */
    protected final <S extends Setting<?>> S setting(S setting) {
        settings.add(setting);
        return setting;
    }

    public final List<Setting<?>> settings() {
        return Collections.unmodifiableList(settings);
    }

    public final boolean isEnabled() {
        return enabled;
    }

    public final void setEnabled(boolean enabled) {
        if (this.enabled == enabled) {
            return;
        }
        this.enabled = enabled;
        if (enabled) {
            onEnable();
        } else {
            onDisable();
        }
    }

    public final void toggle() {
        setEnabled(!enabled);
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    /** Called every client tick while the module is enabled. */
    public void onTick() {
    }
}
