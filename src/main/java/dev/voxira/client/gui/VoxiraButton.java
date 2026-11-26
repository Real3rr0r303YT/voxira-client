package dev.voxira.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;

/** Flat Voxira-style button with an accent bar on hover. */
public class VoxiraButton extends AbstractButton {
    private final Runnable action;

    public VoxiraButton(int x, int y, int width, int height, Component message, Runnable action) {
        super(x, y, width, height, message);
        this.action = action;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        action.run();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float partialTick) {
        boolean hovered = isHoveredOrFocused();
        int x = getX();
        int y = getY();
        int w = getWidth();
        int h = getHeight();

        ctx.fill(x, y, x + w, y + h, hovered ? Theme.CARD_HOVER : Theme.CARD);
        ctx.outline(x, y, w, h, hovered ? Theme.ACCENT : Theme.BORDER);
        if (hovered) {
            ctx.fill(x, y, x + 3, y + h, Theme.ACCENT);
        }

        Minecraft mc = Minecraft.getInstance();
        int color = active ? Theme.TEXT : Theme.TEXT_DIM;
        ctx.centeredText(mc.font, getMessage(), x + w / 2, y + (h - mc.font.lineHeight) / 2 + 1, color);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
