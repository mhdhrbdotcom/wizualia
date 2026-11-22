package com.wizualia;

import com.wizualia.gui.ClickGuiScreen;
import com.wizualia.hud.HudRenderer;
import com.wizualia.module.Config;
import com.wizualia.util.Gamma;
import com.wizualia.util.ZoomState;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import org.lwjgl.glfw.GLFW;

public class WizualiaMod implements ClientModInitializer {
    public static KeyBinding OPEN_GUI;
    public static KeyBinding ZOOM_KEY;

    @Override
    public void onInitializeClient() {
        Config.load();

        OPEN_GUI = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.wizualia.open", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, "category.wizualia"));
        ZOOM_KEY = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.wizualia.zoom", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_C, "category.wizualia"));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (OPEN_GUI.wasPressed()) {
                if (mc.currentScreen == null) mc.setScreen(new ClickGuiScreen());
            }
            ZoomState.tick(mc);
            Gamma.tick(mc);
            HudRenderer.tick(mc);
        });

        ClientLifecycleEvents.CLIENT_STOPPING.register(mc -> {
            Gamma.restore(mc);
            Config.save();
        });

        HudRenderCallback.EVENT.register(HudRenderer::render);
    }
}
