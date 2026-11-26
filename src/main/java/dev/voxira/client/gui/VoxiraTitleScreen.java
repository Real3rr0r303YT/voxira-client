package dev.voxira.client.gui;

import dev.voxira.client.VoxiraClient;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.client.gui.screens.options.OptionsScreen;
import net.minecraft.client.gui.screens.worldselection.SelectWorldScreen;
import net.minecraft.network.chat.Component;

/** Voxira title screen: panorama, big logo, slim button column. */
public class VoxiraTitleScreen extends Screen {
    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 22;
    private static final int SPACING = 6;

    private final String footerLeft;

    public VoxiraTitleScreen() {
        super(Component.literal(VoxiraClient.NAME));
        String modVersion = versionOf(VoxiraClient.MOD_ID);
        String mcVersion = versionOf("minecraft");
        this.footerLeft = "Voxira " + modVersion + "  •  Minecraft " + mcVersion;
    }

    private static String versionOf(String modId) {
        return FabricLoader.getInstance().getModContainer(modId)
                .map(c -> c.getMetadata().getVersion().getFriendlyString())
                .orElse("?");
    }

    @Override
    protected void init() {
        int x = width / 2 - BUTTON_WIDTH / 2;
        int y = height / 2 - 10;
        int step = BUTTON_HEIGHT + SPACING;

        addRenderableWidget(new VoxiraButton(x, y, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.translatable("menu.singleplayer"),
                () -> minecraft.gui.setScreen(new SelectWorldScreen(this))));
        addRenderableWidget(new VoxiraButton(x, y + step, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.translatable("menu.multiplayer"),
                () -> minecraft.gui.setScreen(new JoinMultiplayerScreen(this))));
        addRenderableWidget(new VoxiraButton(x, y + step * 2, BUTTON_WIDTH, BUTTON_HEIGHT,
                Component.literal("Voxira Mods"),
                () -> minecraft.gui.setScreen(new ModMenuScreen(this))));

        int half = (BUTTON_WIDTH - SPACING) / 2;
        addRenderableWidget(new VoxiraButton(x, y + step * 3, half, BUTTON_HEIGHT,
                Component.translatable("menu.options"),
                () -> minecraft.gui.setScreen(new OptionsScreen(this, minecraft.options))));
        addRenderableWidget(new VoxiraButton(x + half + SPACING, y + step * 3, BUTTON_WIDTH - half - SPACING, BUTTON_HEIGHT,
                Component.translatable("menu.quit"),
                () -> minecraft.stop()));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor ctx, int mouseX, int mouseY, float partialTick) {
        extractPanorama(ctx, partialTick);
        ctx.fillGradient(0, 0, width, height, 0x60000000, 0xC0080810);

        // Logo
        String logo = "VOXIRA";
        float scale = 4f;
        int logoWidth = (int) (font.width(logo) * scale);
        int logoY = height / 2 - 90;
        ctx.pose().pushMatrix();
        ctx.pose().translate(width / 2f - logoWidth / 2f, logoY);
        ctx.pose().scale(scale, scale);
        ctx.text(font, logo, 0, 0, Theme.TEXT, true);
        ctx.pose().popMatrix();

        int lineY = logoY + (int) (font.lineHeight * scale) + 4;
        ctx.fill(width / 2 - 40, lineY, width / 2 + 40, lineY + 2, Theme.ACCENT);
        ctx.centeredText(font, "C L I E N T", width / 2, lineY + 8, Theme.TEXT_DIM);

        // Footer
        ctx.text(font, footerLeft, 4, height - 12, Theme.TEXT_DIM, true);
        String notice = "Not affiliated with Mojang or Microsoft";
        ctx.text(font, notice, width - font.width(notice) - 4, height - 12, Theme.TEXT_DIM, true);
    }

    @Override
    public boolean shouldCloseOnEsc() {
        return false;
    }
}
