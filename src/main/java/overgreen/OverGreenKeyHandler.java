package overgreen;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

final class OverGreenKeyHandler {
    private static final KeyMapping.Category CATEGORY = new KeyMapping.Category(Identifier.fromNamespaceAndPath(OverGreen.MOD_ID, "category"));

    private static final KeyMapping TOGGLE_FORCE_REDUCED_INFO_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.overgreen.toggle_force_reduced_info", InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), CATEGORY));

    private static final KeyMapping ZOOM_KEY = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.overgreen.zoom", InputConstants.Type.KEYBOARD, InputConstants.UNKNOWN.getValue(), CATEGORY));

    private static int toggleForceReducedInfoKeyPressedTicks;

    private static boolean zoomKeyPressed;

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(OverGreenKeyHandler::onEndClientTick);
    }

    private static void onEndClientTick(Minecraft minecraft) {
        handleZoomKey();

        handleToggleForceReducedInfoKey(minecraft);
    }

    private static void handleZoomKey() {
        boolean pressed = ZOOM_KEY.isDown();

        if(zoomKeyPressed == pressed)
            return;

        if(pressed)
            OverGreen.enableZoom();
        else
            OverGreen.disableZoom();

        zoomKeyPressed = pressed;
    }

    private static void handleToggleForceReducedInfoKey(Minecraft minecraft) {
        if(!TOGGLE_FORCE_REDUCED_INFO_KEY.isDown()) {
            toggleForceReducedInfoKeyPressedTicks = 0;

            return;
        }

        if(toggleForceReducedInfoKeyPressedTicks < 0)
            return;

        OverGreenConfig config = OverGreen.getConfig();

        if(config.forceReducedInfo.getValue()) {
            if(toggleForceReducedInfoKeyPressedTicks == 0)
                addMessage(minecraft, Component.translatable("overgreen.chat.toggle_force_reduced_info.warning").withStyle(ChatFormatting.RED));

            if(++toggleForceReducedInfoKeyPressedTicks < 40)
                return;
        }

        toggleForceReducedInfoKeyPressedTicks = -1;

        boolean force = !config.forceReducedInfo.getValue();

        config.forceReducedInfo.setValue(force);

        config.flush();

        addMessage(minecraft, Component.translatable("overgreen.chat.toggle_force_reduced_info.message", force
            ? Component.translatable("overgreen.chat.toggle_force_reduced_info.forced").withStyle(ChatFormatting.GREEN)
            : Component.translatable("overgreen.chat.toggle_force_reduced_info.not_forced").withStyle(ChatFormatting.RED)));
    }

    private static void addMessage(Minecraft minecraft, Component component) {
        minecraft.gui.chatListener().handleSystemMessage(Component.empty()
            .append(Component.translatable("overgreen.chat.prefix").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD))
            .append(component), false);
    }
}
