package dev.voxira.client.module.visual;

import dev.voxira.client.module.Category;
import dev.voxira.client.module.Module;
import dev.voxira.client.module.setting.BoolSetting;
import dev.voxira.client.module.setting.ModeSetting;
import dev.voxira.client.module.setting.NumberSetting;
import net.minecraft.client.gui.GuiGraphicsExtractor;

/** Replaces the vanilla crosshair with a configurable one. */
public class CrosshairModule extends Module {
    private final ModeSetting style = setting(new ModeSetting("style", "Style", "Cross", "Cross", "Dot", "Circle", "Cross + Dot"));
    private final ModeSetting color = setting(new ModeSetting("color", "Color", "White", "White", "Violet", "Green", "Red", "Cyan", "Yellow"));
    private final NumberSetting size = setting(new NumberSetting("size", "Size", 5, 2, 15, 1, "px"));
    private final NumberSetting gap = setting(new NumberSetting("gap", "Gap", 2, 0, 8, 1, "px"));
    private final NumberSetting thickness = setting(new NumberSetting("thickness", "Thickness", 1, 1, 4, 1, "px"));
    private final BoolSetting outline = setting(new BoolSetting("outline", "Outline", true));

    public CrosshairModule() {
        super("crosshair", "Custom Crosshair", "Your own crosshair: style, color, size and gap.", Category.VISUAL, false);
    }

    private int argb() {
        return switch (color.get()) {
            case "Violet" -> 0xFF8B6CFF;
            case "Green" -> 0xFF4ADE80;
            case "Red" -> 0xFFFF5555;
            case "Cyan" -> 0xFF55FFFF;
            case "Yellow" -> 0xFFFFDD55;
            default -> 0xFFFFFFFF;
        };
    }

    public void draw(GuiGraphicsExtractor ctx) {
        int cx = ctx.guiWidth() / 2;
        int cy = ctx.guiHeight() / 2;
        int c = argb();
        int s = size.intValue();
        int g = gap.intValue();
        int t = thickness.intValue();
        boolean o = outline.get();

        if (style.is("Cross") || style.is("Cross + Dot")) {
            int h = t / 2;
            rect(ctx, cx - g - s, cy - h, cx - g, cy - h + t, c, o);
            rect(ctx, cx + g + 1, cy - h, cx + g + 1 + s, cy - h + t, c, o);
            rect(ctx, cx - h, cy - g - s, cx - h + t, cy - g, c, o);
            rect(ctx, cx - h, cy + g + 1, cx - h + t, cy + g + 1 + s, c, o);
        }
        if (style.is("Dot") || style.is("Cross + Dot")) {
            rect(ctx, cx - t / 2, cy - t / 2, cx - t / 2 + Math.max(t, 2), cy - t / 2 + Math.max(t, 2), c, o);
        }
        if (style.is("Circle")) {
            int r = s + g;
            for (int a = 0; a < 360; a += 6) {
                double rad = Math.toRadians(a);
                int x = cx + (int) Math.round(Math.cos(rad) * r);
                int y = cy + (int) Math.round(Math.sin(rad) * r);
                rect(ctx, x, y, x + t, y + t, c, false);
            }
        }
    }

    private static void rect(GuiGraphicsExtractor ctx, int x1, int y1, int x2, int y2, int color, boolean outline) {
        if (outline) {
            ctx.fill(x1 - 1, y1 - 1, x2 + 1, y2 + 1, 0xC0000000);
        }
        ctx.fill(x1, y1, x2, y2, color);
    }
}
