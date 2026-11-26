package dev.voxira.client.gui;

import dev.voxira.client.VoxiraClient;
import dev.voxira.client.config.VoxiraConfig;
import dev.voxira.client.module.Module;
import dev.voxira.client.module.setting.BoolSetting;
import dev.voxira.client.module.setting.ModeSetting;
import dev.voxira.client.module.setting.NumberSetting;
import dev.voxira.client.module.setting.Setting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.List;

/** Settings of one module: toggles, sliders and option cycles. */
public class SettingsScreen extends Screen {
    private static final int ROW = 22;
    private static final int HEADER = 44;
    private static final int FOOTER = 32;
    private static final int CONTROL_W = 110;

    private final @Nullable Screen parent;
    private final Module module;
    private @Nullable NumberSetting dragging;
    private double scroll;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    public SettingsScreen(@Nullable Screen parent, Module module) {
        super(Component.literal(module.name()));
        this.parent = parent;
        this.module = module;
    }

    @Override
    protected void init() {
        panelW = Math.min(width - 32, 360);
        int content = HEADER + module.settings().size() * ROW + FOOTER + 8;
        panelH = Math.min(height - 32, Math.max(140, content));
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        int by = panelY + panelH - FOOTER + 6;
        addRenderableWidget(new VoxiraButton(panelX + 10, by, 90, 20, Component.literal("Reset"), () -> {
            module.settings().forEach(Setting::reset);
        }));
        addRenderableWidget(new VoxiraButton(panelX + panelW - 100, by, 90, 20,
                Component.translatable("gui.done"), this::onClose));
    }

    private int listTop() {
        return panelY + HEADER;
    }

    private int listBottom() {
        return panelY + panelH - FOOTER;
    }

    private int rowY(int index) {
        return listTop() + index * ROW - (int) scroll;
    }

    private int controlX() {
        return panelX + panelW - 10 - CONTROL_W;
    }

    private int maxScroll() {
        return Math.max(0, module.settings().size() * ROW - (listBottom() - listTop()));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(ctx, mouseX, mouseY, partialTick);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, Theme.PANEL);
        ctx.outline(panelX, panelY, panelW, panelH, Theme.BORDER);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 2, Theme.ACCENT);

        ctx.text(font, module.name(), panelX + 10, panelY + 10, Theme.TEXT, true);
        String state = module.isEnabled() ? "ON" : "OFF";
        ctx.text(font, state, panelX + panelW - 10 - font.width(state), panelY + 10,
                module.isEnabled() ? Theme.ON : Theme.TEXT_DIM, true);
        ctx.text(font, font.plainSubstrByWidth(module.description(), panelW - 20), panelX + 10, panelY + 24, Theme.TEXT_DIM, false);

        List<Setting<?>> settings = module.settings();
        if (settings.isEmpty()) {
            ctx.centeredText(font, "This module has no settings.", panelX + panelW / 2, listTop() + 12, Theme.TEXT_DIM);
            return;
        }

        ctx.enableScissor(panelX, listTop(), panelX + panelW, listBottom());
        for (int i = 0; i < settings.size(); i++) {
            Setting<?> s = settings.get(i);
            int y = rowY(i);
            boolean hover = mouseY >= Math.max(y, listTop()) && mouseY < Math.min(y + ROW, listBottom())
                    && mouseX >= panelX && mouseX < panelX + panelW;
            if (hover) {
                ctx.fill(panelX + 4, y, panelX + panelW - 4, y + ROW - 2, Theme.CARD);
            }
            ctx.text(font, s.name(), panelX + 10, y + 6, Theme.TEXT, false);
            drawControl(ctx, s, controlX(), y + 3);
        }
        ctx.disableScissor();
    }

    private void drawControl(GuiGraphicsExtractor ctx, Setting<?> s, int x, int y) {
        int h = ROW - 8;
        if (s instanceof BoolSetting b) {
            int sw = 24;
            int sx = x + CONTROL_W - sw;
            ctx.fill(sx, y + 2, sx + sw, y + h - 2, b.get() ? Theme.ON : Theme.OFF);
            int knob = b.get() ? sx + sw - 9 : sx + 1;
            ctx.fill(knob, y + 3, knob + 8, y + h - 3, Theme.TEXT);
        } else if (s instanceof NumberSetting n) {
            ctx.fill(x, y, x + CONTROL_W, y + h, Theme.CARD);
            int fillW = (int) Math.round(CONTROL_W * n.progress());
            ctx.fill(x, y, x + fillW, y + h, Theme.ACCENT_DARK);
            ctx.outline(x, y, CONTROL_W, h, n == dragging ? Theme.ACCENT : Theme.BORDER);
            ctx.centeredText(font, n.display(), x + CONTROL_W / 2, y + (h - font.lineHeight) / 2 + 1, Theme.TEXT);
        } else if (s instanceof ModeSetting m) {
            ctx.fill(x, y, x + CONTROL_W, y + h, Theme.CARD);
            ctx.outline(x, y, CONTROL_W, h, Theme.BORDER);
            ctx.text(font, "<", x + 4, y + (h - font.lineHeight) / 2 + 1, Theme.ACCENT, false);
            ctx.text(font, ">", x + CONTROL_W - 9, y + (h - font.lineHeight) / 2 + 1, Theme.ACCENT, false);
            ctx.centeredText(font, m.display(), x + CONTROL_W / 2, y + (h - font.lineHeight) / 2 + 1, Theme.TEXT);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        double mx = event.x();
        double my = event.y();
        if (my < listTop() || my >= listBottom()) {
            return false;
        }
        List<Setting<?>> settings = module.settings();
        for (int i = 0; i < settings.size(); i++) {
            int y = rowY(i);
            if (my < y || my >= y + ROW) {
                continue;
            }
            Setting<?> s = settings.get(i);
            int cx = controlX();
            if (s instanceof BoolSetting b) {
                b.toggle();
                return true;
            }
            if (s instanceof ModeSetting m) {
                // Left half of the box goes back, everything else (and left click on the label) goes forward.
                boolean back = event.button() != 0 || (mx >= cx && mx < cx + CONTROL_W / 2.0);
                m.cycle(!back);
                return true;
            }
            if (s instanceof NumberSetting n && mx >= cx && mx < cx + CONTROL_W) {
                dragging = n;
                n.setFromProgress((mx - cx) / CONTROL_W);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (dragging != null) {
            dragging.setFromProgress((event.x() - controlX()) / CONTROL_W);
            return true;
        }
        return super.mouseDragged(event, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (dragging != null) {
            dragging = null;
            return true;
        }
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        scroll = Math.max(0, Math.min(maxScroll(), scroll - scrollY * 16));
        return true;
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
