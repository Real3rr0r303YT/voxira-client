package dev.voxira.client.gui.menu;

import dev.voxira.client.gui.Theme;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

/** Rounded title-screen button with hover glow and a staggered intro. */
public class PillButton extends AbstractButton {
    private static final int FILL = 0xD0120C26;
    private static final int FILL_HOVER = 0xF08B6CFF;
    private static final int OUTLINE = 0x908B6CFF;
    private static final int OUTLINE_HOVER = 0xFFE1D2FF;

    private final Runnable action;
    private final long introStart;
    private final float introDelay;
    private float hover;
    private long lastFrame;

    public PillButton(int x, int y, int width, int height, Component message, Runnable action,
                      long introStart, float introDelay) {
        super(x, y, width, height, message);
        this.action = action;
        this.introStart = introStart;
        this.introDelay = introDelay;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        action.run();
    }

    /** 0..1 progress of this button's intro animation. */
    protected float intro() {
        float t = (System.currentTimeMillis() - introStart) / 1000f - introDelay;
        return MenuArt.ease(t / 0.45f);
    }

    /** Smoothly follows the hover state, independent of frame rate. */
    protected float hoverProgress() {
        long now = System.currentTimeMillis();
        float dt = lastFrame == 0 ? 0 : Math.min(0.1f, (now - lastFrame) / 1000f);
        lastFrame = now;
        float target = isHoveredOrFocused() && active ? 1f : 0f;
        hover += (target - hover) * Math.min(1f, dt * 12f);
        return hover;
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float partialTick) {
        float in = intro();
        float h = hoverProgress();
        int x = getX();
        int y = getY() + Math.round((1f - in) * 10f);
        int w = getWidth();
        int ht = getHeight();

        if (h > 0.01f) {
            int gw = w + 70;
            int gh = ht + 44;
            MenuArt.image(ctx, MenuArt.GLOW, 128, 128, x - 35, y - 22, gw, gh,
                    MenuArt.alpha(Theme.ACCENT, 0.55f * h * in));
        }
        MenuArt.pill(ctx, MenuArt.PILL, x, y, w, ht, MenuArt.alpha(MenuArt.mix(FILL, FILL_HOVER, h), in));
        MenuArt.pill(ctx, MenuArt.PILL_OUTLINE, x, y, w, ht, MenuArt.alpha(MenuArt.mix(OUTLINE, OUTLINE_HOVER, h), in));

        Minecraft mc = Minecraft.getInstance();
        int text = MenuArt.alpha(active ? Theme.TEXT : Theme.TEXT_DIM, in);
        if ((text >>> 24) > 8) {
            ctx.centeredText(mc.font, getMessage(), x + w / 2, y + (ht - mc.font.lineHeight) / 2 + 1, text);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
