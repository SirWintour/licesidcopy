package com.lices.idcopy;

import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static final String CATEGORY = "key.categories.licesidcopy";
    
    public static final KeyMapping COPY_ITEM_ID = createCtrlKeyBinding(
            "key.licesidcopy.copy_item_id",
            GLFW.GLFW_KEY_C,
            CATEGORY
    );
    
    private static KeyMapping createCtrlKeyBinding(String name, int keyCode, String category) {
        return new KeyMapping(name, InputConstants.Type.KEYSYM, keyCode, category) {
            @Override
            public Component getTranslatedKeyMessage() {
                return Component.literal("Ctrl+").append(super.getTranslatedKeyMessage());
            }
        };
    }

    @EventBusSubscriber(modid = LicesIDCopy.MODID, value = Dist.CLIENT)
    public static class KeyEventHandler {
        @SubscribeEvent
        static void onKeyRegister(RegisterKeyMappingsEvent event) {
            event.register(COPY_ITEM_ID);
            LicesIDCopy.LOGGER.info("KeyBinding 'Copy Item ID' registrado com sucesso!");
        }
    }
}
