package dev.voxira.client.gui;

import dev.voxira.client.VoxiraClient;
import dev.voxira.client.config.VoxiraConfig;
import dev.voxira.client.module.Category;
import dev.voxira.client.module.Module;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/** Mod-Menü (Right Shift): Module nach Kategorie filtern und per Klick umschalten. */
public class ModMenuScreen extends Screen {
    private static final int COLUMNS = 3;
    private static final int CARD_HEIGHT = 46;
    private static final int GAP = 6;
    private static final int HEADER = 28;
    private static final int TABS = 20;
    private static final int FOOTER = 32;

    private final @Nullable Screen parent;
    private @Nullable Category filter;
    private double scroll;

    private int panelX;
    private int panelY;
    private int panelW;
    private int panelH;

    public ModMenuScreen(@Nullable Screen parent) {
        super(Component.literal("Voxira Mods"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        panelW = Math.min(width - 32, 440);
        panelH = Math.min(height - 32, 300);
        panelX = (width - panelW) / 2;
        panelY = (height - panelH) / 2;

        int buttonY = panelY + panelH - FOOTER + 6;
        int buttonW = 110;
        addRenderableWidget(new VoxiraButton(panelX + 10, buttonY, buttonW, 20,
                Component.literal("HUD bearbeiten"),
                () -> minecraft.gui.setScreen(new HudEditorScreen(this))));
        addRenderableWidget(new VoxiraButton(panelX + panelW - 10 - buttonW, buttonY, buttonW, 20,
                Component.translatable("gui.done"), this::onClose));
    }

    // ---------------------------------------------------------------- Layout

    private List<Module> visibleModules() {
        List<Module> list = new ArrayList<>();
        for (Module m : VoxiraClient.modules().all()) {
            if (filter == null || m.category() == filter) {
                list.add(m);
            }
        }
        return list;
    }

    private int gridTop() {
        return panelY + HEADER + TABS + 6;
    }

    private int gridBottom() {
        return panelY + panelH - FOOTER;
    }

    private int cardWidth() {
        return (panelW - 20 - GAP * (COLUMNS - 1)) / COLUMNS;
    }

    private int cardX(int index) {
        return panelX + 10 + (index % COLUMNS) * (cardWidth() + GAP);
    }

    private int cardY(int index) {
        return gridTop() + (index / COLUMNS) * (CARD_HEIGHT + GAP) - (int) scroll;
    }

    private int maxScroll() {
        int rows = (visibleModules().size() + COLUMNS - 1) / COLUMNS;
        int content = rows * (CARD_HEIGHT + GAP) - GAP;
        return Math.max(0, content - (gridBottom() - gridTop()));
    }

    private static final Category[] TAB_ORDER = {null, Category.HUD, Category.VISUAL, Category.UTILITY};

    private String tabLabel(@Nullable Category c) {
        return c == null ? "Alle" : c.displayName();
    }

    private int tabX(int index) {
        int x = panelX + 10;
        for (int i = 0; i < index; i++) {
            x += font.width(tabLabel(TAB_ORDER[i])) + 16 + 4;
        }
        return x;
    }

    // ---------------------------------------------------------------- Render

    @Override
    public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(ctx, mouseX, mouseY, partialTick);

        ctx.fill(panelX, panelY, panelX + panelW, panelY + panelH, Theme.PANEL);
        ctx.outline(panelX, panelY, panelW, panelH, Theme.BORDER);
        ctx.fill(panelX, panelY, panelX + panelW, panelY + 2, Theme.ACCENT);

        ctx.text(font, "VOXIRA", panelX + 10, panelY + 10, Theme.TEXT, true);
        ctx.text(font, "Mods", panelX + 14 + font.width("VOXIRA"), panelY + 10, Theme.ACCENT, true);
        String hint = "Klick = an/aus";
        ctx.text(font, hint, panelX + panelW - 10 - font.width(hint), panelY + 10, Theme.TEXT_DIM, false);

        // Tabs
        int tabY = panelY + HEADER;
        for (int i = 0; i < TAB_ORDER.length; i++) {
            String label = tabLabel(TAB_ORDER[i]);
            int x = tabX(i);
            int w = font.width(label) + 16;
            boolean selected = TAB_ORDER[i] == filter;
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= tabY && mouseY < tabY + TABS - 4;
            ctx.fill(x, tabY, x + w, tabY + TABS - 4, selected ? Theme.ACCENT_DARK : hover ? Theme.CARD_HOVER : Theme.CARD);
            ctx.text(font, label, x + 8, tabY + 4, selected ? Theme.TEXT : Theme.TEXT_DIM, false);
        }

        // Karten
        ctx.enableScissor(panelX, gridTop(), panelX + panelW, gridBottom());
        List<Module> modules = visibleModules();
        int cw = cardWidth();
        for (int i = 0; i < modules.size(); i++) {
            Module m = modules.get(i);
            int x = cardX(i);
            int y = cardY(i);
            boolean hover = mouseY >= gridTop() && mouseY < gridBottom()
                    && mouseX >= x && mouseX < x + cw && mouseY >= y && mouseY < y + CARD_HEIGHT;

            ctx.fill(x, y, x + cw, y + CARD_HEIGHT, hover ? Theme.CARD_HOVER : Theme.CARD);
            ctx.outline(x, y, cw, CARD_HEIGHT, m.isEnabled() ? Theme.ACCENT : Theme.BORDER);

            ctx.text(font, m.name(), x + 6, y + 6, Theme.TEXT, false);
            ctx.text(font, m.category().displayName(), x + 6, y + 17, Theme.ACCENT, false);

            // Schalter
            int sw = 20;
            int sx = x + cw - sw - 6;
            int sy = y + 6;
            ctx.fill(sx, sy, sx + sw, sy + 9, m.isEnabled() ? Theme.ON : Theme.OFF);
            int knob = m.isEnabled() ? sx + sw - 8 : sx + 1;
            ctx.fill(knob, sy + 1, knob + 7, sy + 8, Theme.TEXT);

            String desc = font.plainSubstrByWidth(m.description(), cw - 12);
            ctx.text(font, desc, x + 6, y + 31, Theme.TEXT_DIM, false);

            if (hover && !desc.equals(m.description())) {
                ctx.setTooltipForNextFrame(font, Component.literal(m.description()), mouseX, mouseY);
            }
        }
        ctx.disableScissor();
    }

    // ---------------------------------------------------------------- Input

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (super.mouseClicked(event, doubleClick)) {
            return true;
        }
        if (event.button() != 0) {
            return false;
        }
        double mx = event.x();
        double my = event.y();

        int tabY = panelY + HEADER;
        for (int i = 0; i < TAB_ORDER.length; i++) {
            int x = tabX(i);
            int w = font.width(tabLabel(TAB_ORDER[i])) + 16;
            if (mx >= x && mx < x + w && my >= tabY && my < tabY + TABS - 4) {
                filter = TAB_ORDER[i];
                scroll = 0;
                return true;
            }
        }

        if (my < gridTop() || my >= gridBottom()) {
            return false;
        }
        List<Module> modules = visibleModules();
        int cw = cardWidth();
        for (int i = 0; i < modules.size(); i++) {
            int x = cardX(i);
            int y = cardY(i);
            if (mx >= x && mx < x + cw && my >= y && my < y + CARD_HEIGHT) {
                modules.get(i).toggle();
                return true;
            }
        }
        return false;
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
