package dev.voxira.client.module.visual;

import dev.voxira.client.module.Category;
import dev.voxira.client.module.Module;
import dev.voxira.client.module.setting.BoolSetting;
import dev.voxira.client.module.setting.NumberSetting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

/** Pulsing red screen edge when your health gets low. */
public class LowHealthModule extends Module {
    private final NumberSetting threshold = setting(new NumberSetting("threshold", "Below", 4, 1, 10, 0.5, "hearts"));
    private final BoolSetting pulse = setting(new BoolSetting("pulse", "Pulse", true));

    public LowHealthModule() {
        super("low_health", "Low Health Warning", "Red screen edge when your health is low.", Category.VISUAL, true);
    }

    public void draw(GuiGraphicsExtractor ctx) {
        LocalPlayer player = MC.player;
        if (player == null || !player.isAlive() || player.isCreative() || player.isSpectator()) {
            return;
        }
        float hearts = player.getHealth() / 2f;
        float limit = threshold.floatValue();
        if (hearts > limit) {
            return;
        }
        float strength = 1f - hearts / limit * 0.6f;
        if (pulse.get()) {
            strength *= 0.65f + 0.35f * (float) Math.sin(System.currentTimeMillis() / 180.0);
        }
        int alpha = Math.max(0, Math.min(200, (int) (strength * 170)));
        int edge = (alpha << 24) | 0xC01010;
        int w = ctx.guiWidth();
        int h = ctx.guiHeight();
        int band = Math.max(24, h / 6);
        ctx.fillGradient(0, 0, w, band, edge, 0x00C01010);
        ctx.fillGradient(0, h - band, w, h, 0x00C01010, edge);
        // Side edges as thin strips with fading opacity
        for (int i = 0; i < band; i += 2) {
            int a = (int) (alpha * (1f - i / (float) band));
            int col = (a << 24) | 0xC01010;
            ctx.fill(i, 0, i + 2, h, col);
            ctx.fill(w - i - 2, 0, w - i, h, col);
        }
    }
}
