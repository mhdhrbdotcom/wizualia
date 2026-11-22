package com.wizualia.mixin;

import com.wizualia.module.Modules;
import com.wizualia.util.ZoomState;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GameRenderer.class)
public class GameRendererMixin {
    @Inject(method = "getFov", at = @At("RETURN"), cancellable = true)
    private void wizualia$fov(Camera camera, float tickDelta, boolean changingFov, CallbackInfoReturnable<Float> cir) {
        float fov = cir.getReturnValueF();
        if (Modules.FOV.enabled) fov = (float) Modules.FOV.num("fov");
        float z = ZoomState.factor(tickDelta);
        if (z > 1.001f) fov /= z;
        cir.setReturnValue(fov);
    }
}
