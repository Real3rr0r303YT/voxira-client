package dev.voxira.client.module.hud;

import dev.voxira.client.util.CpsTracker;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class CpsModule extends HudModule {
    public CpsModule() {
        super("cps", "CPS", "Klicks pro Sekunde (links | rechts).", true, 4, 22);
    }

    @Override
    public int width() {
        return labelWidth("CPS: 00 | 00");
    }

    @Override
    public int height() {
        return 14;
    }

    @Override
    protected void draw(GuiGraphicsExtractor ctx) {
        drawLabel(ctx, "CPS: " + CpsTracker.left() + " | " + CpsTracker.right());
    }
}
