package dev.voxira.client.module.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

import java.util.Locale;

/** Horizontal movement speed in blocks per second. */
public class SpeedModule extends HudModule {
    public SpeedModule() {
        super("speed", "Speed", "Your speed in blocks per second.", false, 4, 236);
    }

    @Override
    public int width() {
        return labelWidth("00.00 b/s");
    }

    @Override
    public int height() {
        return 14;
    }

    @Override
    protected void draw(GuiGraphicsExtractor ctx) {
        LocalPlayer p = MC.player;
        double bps = 0;
        if (p != null) {
            double dx = p.getX() - p.xo;
            double dz = p.getZ() - p.zo;
            bps = Math.sqrt(dx * dx + dz * dz) * 20.0;
        }
        drawLabel(ctx, String.format(Locale.ROOT, "%.2f b/s", bps));
    }
}
