package com.atom.grabvillager.fabric.client;

import com.atom.grabvillager.client.GrabVillagerClientLogic;
import com.atom.grabvillager.client.GrabVillagerOverlay;
import com.atom.grabvillager.network.VillagerDropPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;

public class GrabVillagerClientFabric implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        KeyBindingHelper.registerKeyBinding(GrabVillagerClientLogic.DROP_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            GrabVillagerClientLogic.tick((isThrow, charge) -> {
                ClientPlayNetworking.send(new VillagerDropPayload(isThrow, charge));
            });
        });

        // Enregistrement de l'événement de rendu GUI
        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            GrabVillagerOverlay.render(guiGraphics);
        });
    }
}