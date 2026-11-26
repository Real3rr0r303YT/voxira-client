package dev.voxira.client.module.hud;

import dev.voxira.client.module.Category;
import dev.voxira.client.module.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/**
 * Ein verschiebbares HUD-Element. Position wird in GUI-Pixeln gespeichert
 * und beim Rendern an die aktuelle Fenstergröße angepasst.
 */
public abstract class HudModule extends Module {
    public static final int BACKGROUND = 0x90101018;
    public static final int TEXT = 0xFFFFFFFF;
    public static final int ACCENT = 0xFF8B6CFF;

    private int x;
    private int y;

    protected HudModule(String id, String name, String description, boolean enabledByDefault, int defaultX, int defaultY) {
        super(id, name, description, Category.HUD, enabledByDefault);
        this.x = defaultX;
        this.y = defaultY;
    }

    public abstract int width();

    public abstract int height();

    /** Zeichnet das Modul mit linker oberer Ecke bei (0, 0). */
    protected abstract void draw(GuiGraphicsExtractor ctx);

    public final void render(GuiGraphicsExtractor ctx) {
        clampToScreen(ctx.guiWidth(), ctx.guiHeight());
        ctx.pose().pushMatrix();
        ctx.pose().translate(x, y);
        draw(ctx);
        ctx.pose().popMatrix();
    }

    public void clampToScreen(int screenWidth, int screenHeight) {
        x = Math.max(0, Math.min(x, screenWidth - width()));
        y = Math.max(0, Math.min(y, screenHeight - height()));
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX < x + width() && mouseY >= y && mouseY < y + height();
    }

    public int x() {
        return x;
    }

    public int y() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /** Hilfsfunktion: Textbox im typischen Voxira-Stil. */
    protected void drawLabel(GuiGraphicsExtractor ctx, String text) {
        ctx.fill(0, 0, width(), height(), BACKGROUND);
        ctx.fill(0, 0, 2, height(), ACCENT);
        ctx.text(MC.font, text, 6, (height() - MC.font.lineHeight) / 2 + 1, TEXT, true);
    }

    protected int labelWidth(String text) {
        return MC.font.width(text) + 10;
    }
}
