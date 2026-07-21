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

public class GrabVillagerClientFabric implements ClientModInitializer {

    // NOUVEAU : Variable pour retarder l'ouverture de l'interface
    private static boolean openConfigScreen = false;

    @Override
    public void onInitializeClient() {
        // Chargement du fichier JSON
        GrabVillagerConfig.load();

        KeyBindingHelper.registerKeyBinding(GrabVillagerClientLogic.DROP_KEY);

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            GrabVillagerClientLogic.tick((isThrow, charge) -> {
                ClientPlayNetworking.send(new VillagerDropPayload(isThrow, charge));
            });

            // NOUVEAU : On ouvre l'écran ici, une fois que le tchat est bien fermé
            if (openConfigScreen) {
                Minecraft.getInstance().setScreen(new GrabVillagerConfigScreen());
                openConfigScreen = false;
            }
        });

        HudRenderCallback.EVENT.register((guiGraphics, deltaTracker) -> {
            GrabVillagerOverlay.render(guiGraphics);
        });

        // Déclaration de la commande client /grabvillager
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("grabvillager")
                    .executes(context -> {
                        // On demande l'ouverture au prochain tick pour éviter que le tchat ne le ferme
                        openConfigScreen = true;
                        return 1;
                    }));
        });
    }
}