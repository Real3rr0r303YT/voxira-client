package dev.voxira.client.module.hud;

import dev.voxira.client.module.setting.BoolSetting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class ClockModule extends HudModule {
    private static final DateTimeFormatter H24 = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter H12 = DateTimeFormatter.ofPattern("h:mm a");

    private final BoolSetting twelveHour = setting(new BoolSetting("twelve_hour", "12-hour format", false));

    public ClockModule() {
        super("clock", "Clock", "Real-world time.", false, 4, 218);
    }

    @Override
    public int width() {
        return labelWidth("00:00 PM");
    }

    @Override
    public int height() {
        return 14;
    }

    @Override
    protected void draw(GuiGraphicsExtractor ctx) {
        drawLabel(ctx, LocalTime.now().format(twelveHour.get() ? H12 : H24));
    }
}
