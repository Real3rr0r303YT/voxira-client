package dev.voxira.client.mixin;

import dev.voxira.client.VoxiraClient;
import dev.voxira.client.util.CpsTracker;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public abstract class MouseHandlerMixin {
    /** CPS-Zählung: nur echte Klicks im Spiel, nicht in Menüs. */
    @Inject(method = "onButton", at = @At("HEAD"))
    private void voxira$countClick(long window, MouseButtonInfo info, int action, CallbackInfo ci) {
        if (action == InputConstants.PRESS && Minecraft.getInstance().gui.screen() == null) {
            CpsTracker.click(info.button());
        }
    }

    /** Mausrad ändert die Zoom-Stärke, solange gezoomt wird. */
    @Inject(method = "onScroll", at = @At("HEAD"), cancellable = true)
    private void voxira$zoomScroll(long window, double xOffset, double yOffset, CallbackInfo ci) {
        if (VoxiraClient.modules().zoom.onScroll(yOffset)) {
            ci.cancel();
        }
    }
}
