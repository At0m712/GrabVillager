package com.atom.grabvillager.fabric.client;

import com.atom.grabvillager.client.GrabVillagerClientLogic;
import com.atom.grabvillager.client.GrabVillagerConfigScreen;
import com.atom.grabvillager.client.GrabVillagerOverlay;
import com.atom.grabvillager.config.GrabVillagerConfig;
import com.atom.grabvillager.network.VillagerDropPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class GrabVillagerClientFabric implements ClientModInitializer {

    // NOUVEAU : Un compteur pour retarder l'ouverture de l'écran
    private static int screenOpenDelay = 0;

    @Override
    public void onInitializeClient() {
        // Chargement du fichier JSON
        GrabVillagerConfig.load();

        KeyBindingHelper.registerKeyBinding(GrabVillagerClientLogic.DROP_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            GrabVillagerClientLogic.tick((isThrow, charge) -> {
                ClientPlayNetworking.send(new VillagerDropPayload(isThrow, charge));
            });

            // GESTION DU COMPTE À REBOURS
            if (screenOpenDelay > 0) {
                screenOpenDelay--;
                // Quand le compteur tombe à 0, le tchat est 100% fermé, on peut ouvrir !
                if (screenOpenDelay == 0) {
                    client.setScreen(new GrabVillagerConfigScreen());
                }
            }
        });

        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            GrabVillagerOverlay.render(guiGraphics);
        });

        // Déclaration de la commande client /grabvillager
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("grabvillager")
                    .executes(context -> {
                        // Retour visuel dans le tchat
                        context.getSource().sendFeedback(Component.literal("§aOpening the Grab Villager configuration..."));

                        // On lance le compte à rebours : on attend 2 ticks avant d'ouvrir le menu
                        screenOpenDelay = 2;

                        return 1;
                    }));
        });
    }
}