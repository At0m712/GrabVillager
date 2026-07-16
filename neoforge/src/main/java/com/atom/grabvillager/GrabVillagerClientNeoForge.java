package com.atom.grabvillager.neoforge;

import com.atom.grabvillager.client.GrabVillagerClientLogic;
import com.atom.grabvillager.client.GrabVillagerOverlay;
import com.atom.grabvillager.network.VillagerDropPayload;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class GrabVillagerClientNeoForge {

    public static void registerKeybinds(RegisterKeyMappingsEvent event) {
        event.register(GrabVillagerClientLogic.DROP_KEY);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        GrabVillagerClientLogic.tick((isThrow, charge) -> {
            PacketDistributor.sendToServer(new VillagerDropPayload(isThrow, charge));
        });
    }

    // Méthode de rendu de la jauge
    public static void onRenderGui(RenderGuiEvent.Post event) {
        GrabVillagerOverlay.render(event.getGuiGraphics());
    }
}