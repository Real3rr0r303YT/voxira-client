package dev.voxira.client.module.visual;

import dev.voxira.client.module.Category;
import dev.voxira.client.module.Module;

/**
 * Full brightness without a gamma hack: sets the lightmap's night vision
 * intensity on the client (see {@code LightmapRenderStateExtractorMixin}).
 */
public class FullbrightModule extends Module {
    public FullbrightModule() {
        super("fullbright", "Fullbright", "Everything bright, caves without torches.", Category.VISUAL, false);
    }
}
