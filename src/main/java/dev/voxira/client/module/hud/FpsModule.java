package dev.voxira.client.module.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;

public class FpsModule extends HudModule {
    public FpsModule() {
        super("fps", "FPS", "Zeigt deine aktuellen Frames pro Sekunde.", true, 4, 4);
    }

    private String text() {
        return MC.getFps() + " FPS";
    }

    @Override
    public int width() {
        return labelWidth("0000 FPS");
    }

    @Override
    public int height() {
        return 14;
    }

    @Override
    protected void draw(GuiGraphicsExtractor ctx) {
        drawLabel(ctx, text());
    }
}
