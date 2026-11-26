package dev.voxira.client.gui.menu;

import dev.voxira.client.gui.Theme;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/** Round icon button for the bottom row of the title screen. */
public class IconButton extends PillButton {
    private final Identifier icon;

    public IconButton(int x, int y, int size, Identifier icon, Component label, Runnable action,
                      long introStart, float introDelay) {
        super(x, y, size, size, label, action, introStart, introDelay);
        this.icon = icon;
        setTooltip(Tooltip.create(label));
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float partialTick) {
        float in = intro();
        float h = hoverProgress();
        int s = getWidth();
        int x = getX();
        int y = getY() + Math.round((1f - in) * 8f);

        if (h > 0.01f) {
            MenuArt.image(ctx, MenuArt.GLOW, 128, 128, x - s / 2, y - s / 2, s * 2, s * 2,
                    MenuArt.alpha(Theme.ACCENT, 0.6f * h * in));
        }
        MenuArt.pill(ctx, MenuArt.PILL, x, y, s, s, MenuArt.alpha(MenuArt.mix(0xD8120C26, 0xF08B6CFF, h), in));
        MenuArt.pill(ctx, MenuArt.PILL_OUTLINE, x, y, s, s, MenuArt.alpha(MenuArt.mix(0x908B6CFF, 0xFFE1D2FF, h), in));

        int pad = Math.round(s * 0.24f);
        MenuArt.image(ctx, icon, 64, 64, x + pad, y + pad, s - pad * 2, s - pad * 2, MenuArt.alpha(0xFFFFFFFF, in));
    }
}
