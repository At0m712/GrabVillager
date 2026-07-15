package com.atom.grabvillager;

import net.fabricmc.api.ModInitializer;

public class GrabVillagerFabric implements ModInitializer {

    @Override
    public void onInitialize() {

        // On s'assure que les variables de base sont bien chargées
        Constants.LOG.info("Chargement de Grab Villager sur Fabric !");

        // On appelle la classe commune
        GrabVillager.init();
    }
}