package com.lices.idcopy;

import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.ModContainer;

@Mod(LicesIDCopy.MODID)
public class LicesIDCopy {
    public static final String MODID = "licesidcopy";
    public static final Logger LOGGER = LogUtils.getLogger();

    public LicesIDCopy(IEventBus modEventBus, ModContainer modContainer) {
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        JeiIntegration.init();
    }
}
