package dev.voxira.client.mixin;

import dev.voxira.client.VoxiraClient;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fullbright: maximum night vision intensity in the lightmap. */
@Mixin(LightmapRenderStateExtractor.class)
public abstract class LightmapRenderStateExtractorMixin {
    @Inject(method = "extract", at = @At("TAIL"))
    private void voxira$fullbright(LightmapRenderState state, float partialTick, CallbackInfo ci) {
        if (VoxiraClient.modules().fullbright.isEnabled()) {
            state.nightVisionEffectIntensity = 1.0f;
        }
    }
}
