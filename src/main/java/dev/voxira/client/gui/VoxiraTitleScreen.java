package dev.voxira.client.gui;

import dev.voxira.client.VoxiraClient;
import dev.voxira.client.gui.menu.IconButton;
import dev.voxira.client.gui.menu.MenuArt;
import dev.voxira.client.gui.menu.PillButton;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ConfirmLinkScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;

import java.net.URI;

/**
 * Voxira title screen: a violet dusk scene with parallax layers (sky, eclipse,
 * crystal mountains, voxel hills), a glowing emblem, a spaced title, pill buttons
 * and a row of icon buttons. Everything fades in with a short staggered intro.
 */
public class VoxiraTitleScreen extends Screen {
    private static final URI WEBSITE = URI.create("https://real3rr0r303yt.github.io/voxira-client/");
    private static final String TITLE = spaced("VOXIRA") + "   " + spaced("CLIENT");

    private static final int BUTTON_W = 180;
    private static final int BUTTON_H = 20;
    private static final int BUTTON_GAP = 6;
    private static final int ICON = 26;
    private static final int ICON_GAP = 12;

    private final long openedAt = System.currentTimeMillis();
    private final String footerLeft;

    // Layout, computed in init()
    private int emblemSize;
    private int emblemY;
    private int titleY;
    private float titleScale;

    // Smoothed mouse position for parallax, -0.5..0.5
    private float parallaxX;
    private float parallaxY;
    private long lastFrame;

    public VoxiraTitleScreen() {
        super(Component.literal(VoxiraClient.NAME));
        this.footerLeft = "Voxira " + versionOf(VoxiraClient.MOD_ID) + "  •  Minecraft " + versionOf("minecraft");
    }

    private static String versionOf(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("?");
    }

    private static String spaced(String word) {
        return String.join(" ", word.split(""));
    }

    private float seconds() {
        return (System.currentTimeMillis() - openedAt) / 1000f;
    }

    @Override
    protected void init() {
        titleScale = height >= 300 ? 2f : 1.5f;
        int titleH = Math.round(font.lineHeight * titleScale);
        int buttonsH = 3 * BUTTON_H + 2 * BUTTON_GAP;
        int iconsY = height - ICON - 22;

        // Fit the emblem into whatever space is left above the title.
        int fixed = 8 + titleH + 18 + buttonsH + 14;
        int top = Math.round(height * 0.06f);
        emblemSize = Math.max(32, Math.min(Math.round(height * 0.24f), iconsY - fixed - top));
        emblemY = top;
        titleY = emblemY + emblemSize + 8;
        int buttonsY = titleY + titleH + 18;

        long start = openedAt;
        int bx = width / 2 - BUTTON_W / 2;
        addRenderableWidget(new PillButton(bx, buttonsY, BUTTON_W, BUTTON_H,
                Component.translatable("menu.singleplayer"),
                () -> minecraft.gui.setScreen(new SelectWorldScreen(this)), start, 0.50f));
        addRenderableWidget(new PillButton(bx, buttonsY + (BUTTON_H + BUTTON_GAP), BUTTON_W, BUTTON_H,
                Component.translatable("menu.multiplayer"),
                () -> minecraft.gui.setScreen(new JoinMultiplayerScreen(this)), start, 0.58f));
        addRenderableWidget(new PillButton(bx, buttonsY + 2 * (BUTTON_H + BUTTON_GAP), BUTTON_W, BUTTON_H,
                Component.literal("Voxira Mods"),
                () -> minecraft.gui.setScreen(new ModMenuScreen(this)), start, 0.66f));

        int rowW = 3 * ICON + 2 * ICON_GAP;
        int ix = width / 2 - rowW / 2;
        addRenderableWidget(new IconButton(ix, iconsY, ICON, MenuArt.ICON_OPTIONS,
                Component.translatable("menu.options"),
                () -> minecraft.gui.setScreen(new OptionsScreen(this, minecraft.options)), start, 0.85f));
        addRenderableWidget(new IconButton(ix + ICON + ICON_GAP, iconsY, ICON, MenuArt.ICON_WEB,
                Component.literal("Website"),
                () -> ConfirmLinkScreen.confirmLinkNow(this, WEBSITE), start, 0.92f));
        addRenderableWidget(new IconButton(ix + 2 * (ICON + ICON_GAP), iconsY, ICON, MenuArt.ICON_QUIT,
                Component.translatable("menu.quit"),
                () -> minecraft.stop(), start, 0.99f));
    }

    // ---------------------------------------------------------------- Background

    @Override
    public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float partialTick) {
        updateParallax(mouseX, mouseY);
        float t = seconds();

        float s = Math.max(width / MenuArt.CANVAS_W, height / MenuArt.CANVAS_H) * 1.06f;
        float left = (width - MenuArt.CANVAS_W * s) / 2f;
        float top = (height - MenuArt.CANVAS_H * s) / 2f;
        float fade = MenuArt.ease(t / 0.6f);

        layer(ctx, left, top, s, 4f, () ->
                MenuArt.image(ctx, MenuArt.SKY, 1920, 1080, 0, 0, 1920, 1080, MenuArt.alpha(0xFFFFFFFF, fade)));

        float breathe = 0.9f + 0.1f * (float) Math.sin(t * 0.8);
        layer(ctx, left, top, s, 9f, () ->
                MenuArt.image(ctx, MenuArt.ECLIPSE, 1024, 1024, 1072, 360, 620, 620,
                        MenuArt.alpha(0xFFFFFFFF, fade * breathe)));
        layer(ctx, left, top, s, 16f, () ->
                MenuArt.image(ctx, MenuArt.MOUNTAINS, 1920, 620, 0, 310, 1920, 620, MenuArt.alpha(0xFFFFFFFF, fade)));
        layer(ctx, left, top, s, 26f, () ->
                MenuArt.image(ctx, MenuArt.HILLS, 1920, 440, 0, 700, 1920, 440, MenuArt.alpha(0xFFFFFFFF, fade)));

        // Vignette so the UI reads well on top of the scene
        ctx.fillGradient(0, 0, width, height / 4, 0x90040210, 0x00040210);
        ctx.fillGradient(0, height * 3 / 4, width, height, 0x00040210, 0xB0040210);

        drawEmblem(ctx, t);
        drawTitle(ctx, t);
        drawFooter(ctx, t);
    }

    private void updateParallax(int mouseX, int mouseY) {
        long now = System.currentTimeMillis();
        float dt = lastFrame == 0 ? 1f : Math.min(0.1f, (now - lastFrame) / 1000f);
        lastFrame = now;
        float tx = width > 0 ? mouseX / (float) width - 0.5f : 0f;
        float ty = height > 0 ? mouseY / (float) height - 0.5f : 0f;
        float k = Math.min(1f, dt * 4f);
        parallaxX += (tx - parallaxX) * k;
        parallaxY += (ty - parallaxY) * k;
    }

    /** Draws a scene layer in canvas units, shifted by the parallax depth (in GUI pixels). */
    private void layer(GuiGraphicsExtractor ctx, float left, float top, float scale, float depth, Runnable draw) {
        ctx.pose().pushMatrix();
        ctx.pose().translate(left - parallaxX * depth, top - parallaxY * depth * 0.5f);
        ctx.pose().scale(scale, scale);
        draw.run();
        ctx.pose().popMatrix();
    }

    private void drawEmblem(GuiGraphicsExtractor ctx, float t) {
        float in = MenuArt.ease(t / 0.8f);
        if (in <= 0f) {
            return;
        }
        float size = emblemSize * (0.92f + 0.08f * in);
        float cx = width / 2f + parallaxX * -3f;
        float cy = emblemY + emblemSize / 2f + (float) Math.sin(t * 1.3) * 1.5f;

        float pulse = 0.45f + 0.25f * (float) Math.sin(t * 2.0);
        int glowSize = Math.round(size * 1.25f);
        drawCentered(ctx, MenuArt.EMBLEM_GLOW, 512, cx, cy, glowSize, MenuArt.alpha(Theme.ACCENT, pulse * in));
        drawCentered(ctx, MenuArt.EMBLEM, 512, cx, cy, Math.round(size), MenuArt.alpha(0xFFFFFFFF, in));
    }

    private void drawCentered(GuiGraphicsExtractor ctx, net.minecraft.resources.Identifier id, int tex,
                              float cx, float cy, int size, int argb) {
        ctx.pose().pushMatrix();
        ctx.pose().translate(cx - size / 2f, cy - size / 2f);
        MenuArt.image(ctx, id, tex, tex, 0, 0, size, size, argb);
        ctx.pose().popMatrix();
    }

    private void drawTitle(GuiGraphicsExtractor ctx, float t) {
        float in = MenuArt.ease((t - 0.3f) / 0.6f);
        if (in <= 0.03f) {
            return;
        }
        int textW = font.width(TITLE);
        ctx.pose().pushMatrix();
        ctx.pose().translate(width / 2f - textW * titleScale / 2f, titleY + (1f - in) * 6f);
        ctx.pose().scale(titleScale, titleScale);
        ctx.text(font, TITLE, 0, 0, MenuArt.alpha(Theme.TEXT, in), true);
        ctx.pose().popMatrix();

        int barY = titleY + Math.round(font.lineHeight * titleScale) + 5;
        int barW = Math.round(60 * in);
        ctx.fill(width / 2 - barW / 2, barY, width / 2 + barW / 2, barY + 2, MenuArt.alpha(Theme.ACCENT, in));
    }

    private void drawFooter(GuiGraphicsExtractor ctx, float t) {
        float in = MenuArt.ease((t - 1.0f) / 0.6f);
        if (in <= 0.03f) {
            return;
        }
        int color = MenuArt.alpha(Theme.TEXT_DIM, in);
        ctx.text(font, footerLeft, 6, height - 12, color, true);
        String notice = "Not affiliated with Mojang or Microsoft";
        ctx.text(font, notice, width - font.width(notice) - 6, height - 12, color, true);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
