package dev.voxira.client.module.visual;

import dev.voxira.client.module.Category;
import dev.voxira.client.module.Module;

/**
 * Volle Helligkeit ohne Gamma-Hack: setzt clientseitig die Nachtsicht-Intensität
 * der Lightmap (siehe {@code LightmapRenderStateExtractorMixin}).
 */
public class FullbrightModule extends Module {
    public FullbrightModule() {
        super("fullbright", "Fullbright", "Alles hell – Höhlen ohne Fackeln.", Category.VISUAL, false);
    }
}
