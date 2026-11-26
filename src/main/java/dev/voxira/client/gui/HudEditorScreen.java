package dev.voxira.client.gui;

import dev.voxira.client.VoxiraClient;
import dev.voxira.client.config.VoxiraConfig;
import dev.voxira.client.module.hud.HudModule;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

/** HUD-Editor: aktive HUD-Module per Drag & Drop verschieben, mit Einrasten an Rändern und Mitte. */
public class HudEditorScreen extends Screen {
    private static final int SNAP = 4;

    private final @Nullable Screen parent;
    private @Nullable HudModule dragging;
    private int offsetX;
    private int offsetY;
    private boolean snapX;
    private boolean snapY;

    public HudEditorScreen(@Nullable Screen parent) {
        super(Component.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float partialTick) {
        if (minecraft.level == null) {
            extractPanorama(ctx, partialTick);
        }
        ctx.fill(0, 0, width, height, 0x50000000);

        if (dragging != null) {
            if (snapX) {
                ctx.verticalLine(width / 2, 0, height, Theme.ACCENT);
            }
            if (snapY) {
                ctx.horizontalLine(0, width, height / 2, Theme.ACCENT);
            }
        }

        for (HudModule hud : VoxiraClient.modules().hudModules()) {
            if (!hud.isEnabled()) {
                continue;
            }
            hud.render(ctx);
            boolean hover = hud == dragging || (dragging == null && hud.isHovered(mouseX, mouseY));
            ctx.outline(hud.x() - 1, hud.y() - 1, hud.width() + 2, hud.height() + 2,
                    hover ? Theme.ACCENT : 0x80FFFFFF);
            if (hover) {
                ctx.text(font, hud.name(), hud.x(), Math.max(0, hud.y() - 10), Theme.ACCENT, true);
            }
        }

        ctx.centeredText(font, "Module ziehen zum Verschieben  •  ESC zum Speichern", width / 2, height - 20, Theme.TEXT);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            var huds = VoxiraClient.modules().hudModules();
            // Von hinten nach vorne, damit das oberste Modul gewinnt.
            for (int i = huds.size() - 1; i >= 0; i--) {
                HudModule hud = huds.get(i);
                if (hud.isEnabled() && hud.isHovered(event.x(), event.y())) {
                    dragging = hud;
                    offsetX = (int) event.x() - hud.x();
                    offsetY = (int) event.y() - hud.y();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging == null) {
            return super.mouseDragged(event, dragX, dragY);
        }
        int x = (int) event.x() - offsetX;
        int y = (int) event.y() - offsetY;
        int w = dragging.width();
        int h = dragging.height();

        // Einrasten an Bildschirmrändern
        if (Math.abs(x) < SNAP) x = 0;
        if (Math.abs(y) < SNAP) y = 0;
        if (Math.abs(x + w - width) < SNAP) x = width - w;
        if (Math.abs(y + h - height) < SNAP) y = height - h;

        // Einrasten an der Bildschirmmitte
        snapX = Math.abs(x + w / 2 - width / 2) < SNAP;
        snapY = Math.abs(y + h / 2 - height / 2) < SNAP;
        if (snapX) x = width / 2 - w / 2;
        if (snapY) y = height / 2 - h / 2;

        dragging.setPosition(x, y);
        dragging.clampToScreen(width, height);
        return true;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging != null) {
            dragging = null;
            snapX = snapY = false;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public void onClose() {
        VoxiraConfig.save(VoxiraClient.modules());
        minecraft.gui.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
