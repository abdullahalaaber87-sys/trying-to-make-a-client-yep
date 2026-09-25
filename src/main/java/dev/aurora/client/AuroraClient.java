package dev.aurora.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.LinkedHashMap;
import java.util.Map;

public final class AuroraClient implements ClientModInitializer {
    private static boolean wasDown;
    private static final Map<String, Module> MODULES = new LinkedHashMap<>();
    static {
        MODULES.put("COMBAT", new Module("Auto Totem"));
        MODULES.put("MISC", new Module("Auto Walk"));
        MODULES.put("RENDER", new Module("Fullbright"));
        MODULES.put("VISUALS", new Module("Custom Crosshair"));
        MODULES.put("CLIENT", new Module("HUD"));
    }
    private record Module(String name, boolean enabled) {
        Module(String name) { this(name, false); }
        Module toggle() { return new Module(name, !enabled); }
    }
    @Override public void onInitializeClient() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.getWindow() == null) return;
            boolean down = GLFW.glfwGetKey(client.getWindow().getHandle(), GLFW.GLFW_KEY_RIGHT_SHIFT) == GLFW.GLFW_PRESS;
            if (down && !wasDown) client.setScreen(client.currentScreen instanceof Menu ? null : new Menu());
            wasDown = down;
            if (client.player != null && MODULES.get("MISC").enabled && client.currentScreen == null)
                client.options.forwardKey.setPressed(true);
        });
    }
    private static final class Menu extends Screen {
        private final String[] sections = MODULES.keySet().toArray(String[]::new);
        private static final int CYAN = 0xff55dbee, BG = 0xe818202a, WHITE = 0xffe4eff2;
        Menu() { super(Text.literal("Aurora Client")); }
        @Override public void render(DrawContext context, int mouseX, int mouseY, float delta) {
            super.render(context, mouseX, mouseY, delta);
            int width = Math.min(125, (this.width - 20) / 5);
            int gap = 7, start = Math.max(8, (this.width - width * 5 - gap * 4) / 2);
            for (int i = 0; i < 5; i++) {
                int x = start + i * (width + gap);
                context.fill(x, 35, x + width, 102, BG);
                context.fill(x, 35, x + width, 37, CYAN);
                context.drawTextWithShadow(textRenderer, sections[i], x + 8, 45, WHITE);
                Module module = MODULES.get(sections[i]);
                context.drawTextWithShadow(textRenderer, module.name, x + 8, 76, module.enabled ? CYAN : WHITE);
                context.fill(x + width - 14, 78, x + width - 8, 84, module.enabled ? CYAN : 0xff56616c);
            }
            context.drawCenteredTextWithShadow(textRenderer, "AURORA  /  RIGHT SHIFT", this.width / 2, 13, CYAN);
        }
        @Override public boolean mouseClicked(net.minecraft.client.gui.Click click, boolean doubled) {
            double mouseX = click.x(), mouseY = click.y();
            int width = Math.min(125, (this.width - 20) / 5);
            int gap = 7, start = Math.max(8, (this.width - width * 5 - gap * 4) / 2);
            for (int i = 0; i < 5; i++) {
                int x = start + i * (width + gap);
                if (mouseX >= x && mouseX <= x + width && mouseY >= 65 && mouseY <= 99) {
                    String key = sections[i];
                    MODULES.put(key, MODULES.get(key).toggle());
                    return true;
                }
            }
            return super.mouseClicked(click, doubled);
        }
        @Override public boolean shouldPause() { return false; }
    }
}
