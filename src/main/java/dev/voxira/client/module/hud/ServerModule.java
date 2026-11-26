package dev.voxira.client.module.hud;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ServerData;

public class ServerModule extends HudModule {
    public ServerModule() {
        super("server", "Server Address", "Shows which server you are on.", false, 4, 254);
    }

    private String text() {
        if (MC.isLocalServer()) {
            return "Singleplayer";
        }
        ServerData data = MC.getCurrentServer();
        return data != null ? data.ip : "play.example.net";
    }

    @Override
    public int width() {
        return labelWidth(text());
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
