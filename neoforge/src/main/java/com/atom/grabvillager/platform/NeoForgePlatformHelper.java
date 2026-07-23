package com.atom.grabvillager.platform;

import com.atom.grabvillager.platform.services.IPlatformHelper;
import net.neoforged.fml.ModList;
// L'import reste le même, seule la méthode finale change !
import net.neoforged.fml.loading.FMLEnvironment;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        // La nouvelle méthode NeoForge 1.21.9
        return !FMLEnvironment.isProduction();
    }
}