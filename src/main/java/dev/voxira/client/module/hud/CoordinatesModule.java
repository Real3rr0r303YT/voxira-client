package dev.voxira.client.module.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;

import java.util.Locale;

public class CoordinatesModule extends HudModule {
    public CoordinatesModule() {
        super("coordinates", "Coordinates", "Shows your position and facing.", true, 4, 40);
    }

    @Override
    public int width() {
        return labelWidth("XYZ: -00000 / 000 / -00000  (N)");
    }

    @Override
    public int height() {
        return 14;
    }

    @Override
    protected void draw(GuiGraphicsExtractor ctx) {
        LocalPlayer player = MC.player;
        String text;
        if (player == null) {
            text = "XYZ: 0 / 64 / 0  (N)";
        } else {
            String dir = player.getDirection().getName().substring(0, 1).toUpperCase(Locale.ROOT);
            text = String.format(Locale.ROOT, "XYZ: %d / %d / %d  (%s)",
                    player.getBlockX(), player.getBlockY(), player.getBlockZ(), dir);
        }
        drawLabel(ctx, text);
    }
}
