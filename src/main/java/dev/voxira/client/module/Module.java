package dev.voxira.client.module;

import net.minecraft.client.Minecraft;

/**
 * Basis aller Voxira-Module. Ein Modul hat eine feste ID (für die Config),
 * einen Anzeigenamen, eine Kategorie und kann an-/ausgeschaltet werden.
 */
public abstract class Module {
    protected static final Minecraft MC = Minecraft.getInstance();

    private final String id;
    private final String name;
    private final String description;
    private final Category category;
    private boolean enabled;

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

    /** Wird jeden Client-Tick aufgerufen, solange das Modul aktiv ist. */
    public void onTick() {
    }
}
