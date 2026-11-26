package dev.voxira.client.mixin;

import dev.voxira.client.VoxiraClient;
import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Zoom: divides the computed field of view by the current zoom factor. */
@Mixin(Camera.class)
public abstract class CameraMixin {
    @Inject(method = "calculateFov", at = @At("RETURN"), cancellable = true)
    private void voxira$applyZoom(float partialTick, CallbackInfoReturnable<Float> cir) {
        float divisor = VoxiraClient.modules().zoom.fovDivisor(partialTick);
        if (divisor > 1f) {
            cir.setReturnValue(cir.getReturnValueF() / divisor);
        }
    }
}
