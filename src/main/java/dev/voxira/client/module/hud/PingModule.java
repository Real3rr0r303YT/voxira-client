package dev.voxira.client.module.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;

public class PingModule extends HudModule {
    public PingModule() {
        super("ping", "Ping", "Your latency to the server.", false, 4, 200);
    }

    @Override
    public int width() {
        return labelWidth("0000 ms");
    }

    @Override
    public int height() {
        return 14;
    }

    @Override
    protected void draw(GuiGraphicsExtractor ctx) {
        String text = "-- ms";
        if (MC.player != null && MC.getConnection() != null && !MC.isLocalServer()) {
            PlayerInfo info = MC.getConnection().getPlayerInfo(MC.player.getUUID());
            if (info != null) {
                text = info.getLatency() + " ms";
            }
        } else if (MC.isLocalServer()) {
            text = "0 ms";
        }
        drawLabel(ctx, text);
    }
}
