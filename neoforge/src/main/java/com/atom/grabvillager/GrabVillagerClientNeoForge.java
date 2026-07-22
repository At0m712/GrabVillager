package com.atom.grabvillager.neoforge;

import com.atom.grabvillager.client.GrabVillagerClientLogic;
import com.atom.grabvillager.client.GrabVillagerConfigScreen;
import com.atom.grabvillager.client.GrabVillagerOverlay;
import com.atom.grabvillager.config.GrabVillagerConfig;
import com.atom.grabvillager.network.VillagerDropPayload;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

public class GrabVillagerClientNeoForge {

    // Variable pour retarder l'ouverture de l'interface
    private static boolean openConfigScreen = false;

    public static void registerKeybinds(RegisterKeyMappingsEvent event) {
        // Chargement du fichier JSON
        GrabVillagerConfig.load();
        event.register(GrabVillagerClientLogic.DROP_KEY);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        GrabVillagerClientLogic.tick((isThrow, charge) -> {
            // NOUVEAU SYSTEME 1.21.4+ : On utilise directement la connexion native de Minecraft
            if (Minecraft.getInstance().getConnection() != null) {
                Minecraft.getInstance().getConnection().send(new VillagerDropPayload(isThrow, charge));
            }
        });

        // On ouvre l'écran ici, une fois que le tchat est bien fermé
        if (openConfigScreen) {
            Minecraft.getInstance().setScreen(new GrabVillagerConfigScreen());
            openConfigScreen = false;
        }
    }

    public static void onRenderGui(RenderGuiEvent.Post event) {
        GrabVillagerOverlay.render(event.getGuiGraphics());
    }

    // Déclaration de la commande client /grabvillager
    public static void onClientCommand(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(
                net.minecraft.commands.Commands.literal("grabvillager")
                        .executes(context -> {
                            // On demande l'ouverture au prochain tick pour éviter que le tchat ne le ferme
                            openConfigScreen = true;
                            return 1;
                        })
        );
    }
}