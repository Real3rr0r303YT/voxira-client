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
import net.minecraft.client.KeyMapping;
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

        HudElementRegistry.addLast(id("hud"), (ctx, deltaTracker) -> {
            Minecraft mc = Minecraft.getInstance();
            if (mc.gui.screen() instanceof HudEditorScreen) {
                return; // Der Editor zeichnet die Module selbst.
            }
            for (HudModule hud : modules.hudModules()) {
                if (hud.isEnabled()) {
                    hud.render(ctx);
                }
            }
        });

        LOGGER.info("{} geladen – {} Module", NAME, modules.all().size());
    }

    private void onTick(Minecraft mc) {
        while (menuKey.consumeClick()) {
            if (mc.gui.screen() == null) {
                mc.gui.setScreen(new ModMenuScreen(null));
            }
        }
        modules.zoom.setKeyHeld(zoomKey.isDown());
        modules.tick();
    }
}
