package com.atom.grabvillager;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(Constants.MOD_ID)
public class GrabVillagerNeoForge {

    public GrabVillagerNeoForge(IEventBus eventBus) {
        Constants.LOG.info("Chargement de Grab Villager sur NeoForge !");

        // On appelle le code commun
        GrabVillager.init();
    }
}