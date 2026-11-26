package dev.voxira.client;

import com.mojang.blaze3d.platform.InputConstants;
import dev.voxira.client.config.VoxiraConfig;
import dev.voxira.client.gui.HudEditorScreen;
import dev.voxira.client.gui.ModMenuScreen;
import dev.voxira.client.module.ModuleManager;
import dev.voxira.client.module.hud.HudModule;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import dev.voxira.client.gui.VoxiraButton;
import net.fabricmc.fabric.api.client.rendering.v1.hud.VanillaHudElements;
import net.fabricmc.fabric.api.client.screen.v1.ScreenEvents;
import net.fabricmc.fabric.api.client.screen.v1.Screens;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.DisconnectedScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VoxiraClient implements ClientModInitializer {
    public static final String MOD_ID = "voxira";
    public static final String NAME = "Voxira Client";
    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    private static ModuleManager modules;

    private static KeyMapping menuKey;
    private static KeyMapping zoomKey;

    public static Identifier id(String path) {
        return Identifier.fromNamespaceAndPath(MOD_ID, path);
    }

    public static ModuleManager modules() {
        return modules;
    }

    @Override
    public void onInitializeClient() {
        modules = new ModuleManager();
        VoxiraConfig.load(modules);

        KeyMapping.Category category = KeyMapping.Category.register(id("main"));
        menuKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.voxira.menu", InputConstants.KEY_RSHIFT, category));
        zoomKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.voxira.zoom", InputConstants.KEY_C, category));

        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> VoxiraConfig.save(modules));

        HudElementRegistry.replaceElement(VanillaHudElements.CROSSHAIR, vanilla -> (ctx, deltaTracker) -> {
            if (modules.crosshair.isEnabled()) {
                Minecraft mc = Minecraft.getInstance();
                if (mc.options.getCameraType().isFirstPerson()) {
                    modules.crosshair.draw(ctx);
                }
            } else {
                vanilla.extractRenderState(ctx, deltaTracker);
            }
        });
        HudElementRegistry.attachElementBefore(VanillaHudElements.MISC_OVERLAYS, id("low_health"), (ctx, deltaTracker) -> {
            if (modules.lowHealth.isEnabled()) {
                modules.lowHealth.draw(ctx);
            }
        });
        ScreenEvents.AFTER_INIT.register(this::onScreenInit);

        HudElementRegistry.addLast(id("hud"), (ctx, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui.screen() instanceof HudEditorScreen) {
                return; // The editor draws the modules itself.
            }
            for (HudModule hud : modules.hudModules()) {
                if (hud.isEnabled()) {
                    hud.render(ctx);
                }
            }
        });

        LOGGER.info("{} loaded with {} modules", NAME, modules.all().size());
    }

    private void onTick(Minecraft mc) {
        while (menuKey.consumeClick()) {
            if (mc.gui.screen() == null) {
                mc.gui.setScreen(new ModMenuScreen(null));
            }
        }
        modules.zoom.setKeyHeld(zoomKey.isDown());
        modules.autoReconnect.trackServer();
        modules.tick();
    }

    /** Adds the Auto Reconnect button with countdown to the vanilla disconnect screen. */
    private void onScreenInit(Minecraft mc, Screen screen, int width, int height) {
        if (!(screen instanceof DisconnectedScreen)
                || !modules.autoReconnect.isEnabled()
                || modules.autoReconnect.lastServer() == null
                || modules.autoLog.leftOnPurpose()) {
            return;
        }
        int[] ticksLeft = {modules.autoReconnect.delayTicks()};
        boolean[] cancelled = {false};

        VoxiraButton reconnect = new VoxiraButton(width / 2 - 100, height - 52, 200, 20,
                Component.literal("Reconnect"), modules.autoReconnect::reconnect);
        VoxiraButton cancel = new VoxiraButton(width / 2 - 100, height - 28, 200, 20,
                Component.literal("Cancel auto reconnect"), () -> cancelled[0] = true);
        Screens.getWidgets(screen).add(reconnect);
        Screens.getWidgets(screen).add(cancel);

        ScreenEvents.afterTick(screen).register(s -> {
            if (cancelled[0]) {
                reconnect.setMessage(Component.literal("Reconnect"));
                cancel.active = false;
                return;
            }
            ticksLeft[0]--;
            int seconds = Math.max(0, (ticksLeft[0] + 19) / 20);
            reconnect.setMessage(Component.literal("Reconnecting in " + seconds + "s"));
            if (ticksLeft[0] <= 0) {
                cancelled[0] = true;
                modules.autoReconnect.reconnect();
            }
        });
    }
}
