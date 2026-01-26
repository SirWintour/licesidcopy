package com.lices.idcopy;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderTooltipEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import org.jetbrains.annotations.Nullable;

public class JeiIntegration {
    private static Object jeiRuntime = null;
    private static boolean jeiLoaded = false;

    public static void init() {
        try {
            Class.forName("mezz.jei.api.JeiPlugin");
            jeiLoaded = true;
            LicesIDCopy.LOGGER.info("JEI detectado e integração ativada");
        } catch (ClassNotFoundException e) {
            jeiLoaded = false;
            LicesIDCopy.LOGGER.info("JEI não encontrado, continuando sem suporte JEI");
        }
    }

    public static void setRuntime(Object runtime) {
        jeiRuntime = runtime;
    }

    @Nullable
    public static Object getRuntime() {
        return jeiRuntime;
    }

    public static boolean isJeiLoaded() {
        return jeiLoaded;
    }
}

@EventBusSubscriber(modid = LicesIDCopy.MODID, value = Dist.CLIENT)
class ItemIdCopyHandler {

    private static ItemStack lastHoveredItem = ItemStack.EMPTY;
    private static long lastTooltipTime = 0;
    private static final long TOOLTIP_TIMEOUT = 200;

    private static String lastCopiedMessage = null;
    private static long lastCopyTime = 0;
    private static final long MESSAGE_DURATION = 3000;

    @SubscribeEvent
    static void onRenderTooltip(RenderTooltipEvent.Pre event) {
        ItemStack itemStack = event.getItemStack();
        if (itemStack != null && !itemStack.isEmpty()) {
            lastHoveredItem = itemStack;
            lastTooltipTime = System.currentTimeMillis();
        }
    }

    @SubscribeEvent
    static void onScreenRender(ScreenEvent.Render.Post event) {
        if (lastCopiedMessage != null && (System.currentTimeMillis() - lastCopyTime) < MESSAGE_DURATION) {
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Minecraft minecraft = Minecraft.getInstance();

            Component message = Component.translatable("licesidcopy.message.id_copied")
                    .append(Component.literal("§e" + lastCopiedMessage));

            int centerX = minecraft.screen.width / 2;
            int y = minecraft.screen.height - 30;

            guiGraphics.drawCenteredString(minecraft.font, message, centerX, y, 0x00AA00);
        } else if (lastCopiedMessage != null && (System.currentTimeMillis() - lastCopyTime) >= MESSAGE_DURATION) {
            lastCopiedMessage = null;
        }
    }

    @SubscribeEvent
    static void onKeyPressed(ScreenEvent.KeyPressed.Pre event) {
        if (!Screen.hasControlDown() || event.getKeyCode() != com.mojang.blaze3d.platform.InputConstants.KEY_C) {
            return;
        }

        Screen screen = event.getScreen();
        if (screen == null) {
            return;
        }

        String id = getIdToCopy();

        if (id != null) {
            copyToClipboard(id);
            event.setCanceled(true);
        } else {
            LicesIDCopy.LOGGER.warn("Nenhum ID encontrado para copiar");
        }
    }

    private static String getIdToCopy() {
        if (JeiIntegration.isJeiLoaded()) {
            try {
                String jeiId = JeiIdProvider.getIdFromJeiOverlay();
                if (jeiId != null) {
                    LicesIDCopy.LOGGER.info("ID obtido do JEI overlay");
                    return jeiId;
                }
            } catch (Exception e) {
                LicesIDCopy.LOGGER.debug("Erro ao buscar ID do JEI", e);
            }
        }

        boolean hasRecentTooltip = (System.currentTimeMillis() - lastTooltipTime) < TOOLTIP_TIMEOUT;
        if (hasRecentTooltip && !lastHoveredItem.isEmpty()) {
            LicesIDCopy.LOGGER.info("ID obtido do item tooltip");
            return getItemId(lastHoveredItem);
        }

        return null;
    }

    private static String getItemId(ItemStack itemStack) {
        return itemStack.getItem().builtInRegistryHolder().key().location().toString();
    }

    private static void copyToClipboard(String text) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.keyboardHandler.setClipboard(text);

        lastCopiedMessage = text;
        lastCopyTime = System.currentTimeMillis();

        LicesIDCopy.LOGGER.info("ID copiado: {}", text);
    }
}
