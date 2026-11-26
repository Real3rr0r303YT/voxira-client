package dev.voxira.client.module.visual;

import dev.voxira.client.module.Category;
import dev.voxira.client.module.Module;

/**
 * Zoom per Taste (Standard: C). Mausrad passt die Stärke an, solange gezoomt wird.
 * Der eigentliche FOV-Eingriff passiert in {@code CameraMixin}.
 */
public class ZoomModule extends Module {
    private static final float MIN_ZOOM = 1.5f;
    private static final float MAX_ZOOM = 50f;
    private static final float DEFAULT_ZOOM = 4f;

    private boolean active;
    private float target = DEFAULT_ZOOM;
    private float current = 1f;
    private float previous = 1f;
    private boolean savedSmoothCamera;

    public ZoomModule() {
        super("zoom", "Zoom", "Halte die Zoom-Taste (C). Mausrad ändert die Stärke.", Category.VISUAL, true);
    }

    /** Wird jeden Tick vom Keybind-Handler aufgerufen. */
    public void setKeyHeld(boolean held) {
        boolean shouldZoom = held && isEnabled() && MC.gui.screen() == null;
        if (shouldZoom == active) {
            return;
        }
        active = shouldZoom;
        if (active) {
            savedSmoothCamera = MC.options.smoothCamera;
            MC.options.smoothCamera = true;
        } else {
            MC.options.smoothCamera = savedSmoothCamera;
            target = DEFAULT_ZOOM;
        }
    }

    @Override
    public void onTick() {
        previous = current;
        float goal = active ? target : 1f;
        current += (goal - current) * 0.5f;
        if (Math.abs(goal - current) < 0.01f) {
            current = goal;
        }
    }

    @Override
    protected void onDisable() {
        setKeyHeld(false);
        current = previous = 1f;
    }

    /** @return true, wenn das Scroll-Event verbraucht wurde. */
    public boolean onScroll(double amount) {
        if (!active || amount == 0) {
            return false;
        }
        target = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, target * (amount > 0 ? 1.25f : 0.8f)));
        return true;
    }

    /** Interpolierter Divisor für das FOV (1 = kein Zoom). */
    public float fovDivisor(float partialTick) {
        return previous + (current - previous) * partialTick;
    }
}
