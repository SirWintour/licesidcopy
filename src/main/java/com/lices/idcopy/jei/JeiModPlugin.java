package com.lices.idcopy.jei;

import com.lices.idcopy.JeiIntegration;
import com.lices.idcopy.LicesIDCopy;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JeiModPlugin implements IModPlugin {

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(LicesIDCopy.MODID, "jei_integration");
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        JeiIntegration.setRuntime(runtime);
        LicesIDCopy.LOGGER.info("JEI Runtime registrado com sucesso!");
    }
}
