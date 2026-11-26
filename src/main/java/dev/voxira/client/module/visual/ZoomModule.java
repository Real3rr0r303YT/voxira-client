package dev.voxira.client.module.visual;

import dev.voxira.client.module.Category;
import dev.voxira.client.module.Module;

/**
 * Zoom while a key is held (default: C). Scrolling adjusts the level while zooming.
 * The actual FOV change happens in {@code CameraMixin}.
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
        super("zoom", "Zoom", "Hold the zoom key (C). Scroll to change the zoom level.", Category.VISUAL, true);
    }

    /** Called every tick by the keybind handler. */
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

    /** @return true if the scroll event was consumed. */
    public boolean onScroll(double amount) {
        if (!active || amount == 0) {
            return false;
        }
        target = Math.max(MIN_ZOOM, Math.min(MAX_ZOOM, target * (amount > 0 ? 1.25f : 0.8f)));
        return true;
    }

    /** Interpolated FOV divisor (1 = no zoom). */
    public float fovDivisor(float partialTick) {
        return previous + (current - previous) * partialTick;
    }
}
