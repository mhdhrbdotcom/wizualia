package com.wizualia.mixin;

import com.wizualia.module.Modules;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Arm;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Inject(method = "renderFirstPersonItem", at = @At("HEAD"))
    private void wizualia$head(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                               float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                               VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        matrices.push();
        if (Modules.VIEWMODEL.enabled) {
            boolean right = (hand == Hand.MAIN_HAND) == (player.getMainArm() == Arm.RIGHT);
            float side = right ? 1f : -1f;
            matrices.translate(Modules.VIEWMODEL.num("x") * side, Modules.VIEWMODEL.num("y"), Modules.VIEWMODEL.num("z"));
            float s = (float) Modules.VIEWMODEL.num("scale");
            matrices.scale(s, s, s);
        }
    }

    @Inject(method = "renderFirstPersonItem", at = @At("RETURN"))
    private void wizualia$ret(AbstractClientPlayerEntity player, float tickDelta, float pitch, Hand hand,
                              float swingProgress, ItemStack item, float equipProgress, MatrixStack matrices,
                              VertexConsumerProvider vertexConsumers, int light, CallbackInfo ci) {
        matrices.pop();
    }
}
