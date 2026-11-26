package dev.voxira.client.mixin;

import dev.voxira.client.VoxiraClient;
import dev.voxira.client.gui.VoxiraTitleScreen;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/** Swaps the vanilla title screen for the Voxira one. */
@Mixin(Gui.class)
public abstract class GuiMixin {
    @ModifyVariable(method = "setScreen", at = @At("HEAD"), argsOnly = true)
    private Screen voxira$replaceTitleScreen(Screen screen) {
        if (screen != null && screen.getClass() == TitleScreen.class
                && VoxiraClient.modules() != null
                && VoxiraClient.modules().customMenu.isEnabled()) {
            return new VoxiraTitleScreen();
        }
        return screen;
    }
}
