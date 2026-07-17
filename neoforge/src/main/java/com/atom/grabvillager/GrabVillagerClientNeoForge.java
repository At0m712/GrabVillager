package com.atom.grabvillager.neoforge;

import com.atom.grabvillager.client.GrabVillagerClientLogic;
import com.atom.grabvillager.client.GrabVillagerConfigScreen;
import com.atom.grabvillager.client.GrabVillagerOverlay;
import com.atom.grabvillager.config.GrabVillagerConfig; // <-- Bon import depuis config
import com.atom.grabvillager.network.VillagerDropPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class GrabVillagerClientNeoForge {

    public static void registerKeybinds(RegisterKeyMappingsEvent event) {
        // Chargement du fichier JSON
        GrabVillagerConfig.load();
        event.register(GrabVillagerClientLogic.DROP_KEY);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        GrabVillagerClientLogic.tick((isThrow, charge) -> {
            PacketDistributor.sendToServer(new VillagerDropPayload(isThrow, charge));
        });
    }

    public static void onRenderGui(RenderGuiEvent.Post event) {
        GrabVillagerOverlay.render(event.getGuiGraphics());
    }

    // Déclaration de la commande client /grabvillager
    public static void onClientCommand(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                net.minecraft.commands.Commands.literal("grabvillager")
                        .executes(context -> {
                            Minecraft.getInstance().tell(() -> {
                                Minecraft.getInstance().setScreen(new GrabVillagerConfigScreen());
                            });
                            return 1;
                        })
        );
    }
}