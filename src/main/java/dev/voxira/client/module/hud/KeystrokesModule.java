package dev.voxira.client.module.hud;

import dev.voxira.client.util.CpsTracker;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Classic WASD + mouse + space bar overlay. */
public class KeystrokesModule extends HudModule {
    private static final int KEY = 22;
    private static final int GAP = 2;
    private static final int IDLE = 0x90101018;
    private static final int PRESSED = 0xD08B6CFF;

    public KeystrokesModule() {
        super("keystrokes", "Keystrokes", "Shows pressed movement and mouse keys.", true, 4, 60);
    }

    @Override
    public int width() {
        return KEY * 3 + GAP * 2;
    }

    @Override
    public int height() {
        return KEY * 2 + GAP + 2 + (KEY - 4) + GAP + 12;
    }

    @Override
    protected void draw(GuiGraphicsExtractor ctx) {
        Options o = MC.options;
        int row2 = KEY + GAP;
        drawKey(ctx, KEY + GAP, 0, KEY, KEY, "W", o.keyUp);
        drawKey(ctx, 0, row2, KEY, KEY, "A", o.keyLeft);
        drawKey(ctx, KEY + GAP, row2, KEY, KEY, "S", o.keyDown);
        drawKey(ctx, (KEY + GAP) * 2, row2, KEY, KEY, "D", o.keyRight);

        int row3 = row2 + KEY + GAP;
        int half = (width() - GAP) / 2;
        int mouseH = KEY - 4;
        drawKey(ctx, 0, row3, half, mouseH, "LMB " + CpsTracker.left(), o.keyAttack);
        drawKey(ctx, half + GAP, row3, width() - half - GAP, mouseH, "RMB " + CpsTracker.right(), o.keyUse);

        int row4 = row3 + mouseH + GAP;
        boolean jump = o.keyJump.isDown();
        ctx.fill(0, row4, width(), row4 + 12, jump ? PRESSED : IDLE);
        ctx.fill(width() / 2 - 12, row4 + 5, width() / 2 + 12, row4 + 7, TEXT);
    }

    private void drawKey(GuiGraphicsExtractor ctx, int x, int y, int w, int h, String label, KeyMapping key) {
        ctx.fill(x, y, x + w, y + h, key.isDown() ? PRESSED : IDLE);
        int tx = x + (w - MC.font.width(label)) / 2;
        int ty = y + (h - MC.font.lineHeight) / 2 + 1;
        ctx.text(MC.font, label, tx, ty, TEXT, true);
    }
}
