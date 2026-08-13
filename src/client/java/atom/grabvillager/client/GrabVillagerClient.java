package atom.grabvillager.client;

import atom.grabvillager.config.GrabVillagerConfig;
import atom.grabvillager.network.VillagerDropPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.screens.Screen; // L'import magique pour les écrans !
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.network.chat.Component;

public class GrabVillagerClient implements ClientModInitializer {

    private static int screenOpenDelay = 0;

    // 1. Déclarer la catégorie et la touche AU NIVEAU DE LA CLASSE (en dehors des méthodes)
    public static final KeyMapping.Category GRAB_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("grabvillager", "main")
    );

    public static KeyMapping dropKey;

    @Override
    public void onInitializeClient() {
        GrabVillagerConfig.load();

        // 2. Enregistrer la touche directement ici au lancement du client
        dropKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.grabvillager.drop",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                GRAB_CATEGORY
        ));

        // 3. Événement des touches et logique client
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            GrabVillagerClientLogic.tick((isThrow, charge) -> {
                ClientPlayNetworking.send(new VillagerDropPayload(isThrow, charge));
            });

            if (screenOpenDelay > 0) {
                screenOpenDelay--;
                if (screenOpenDelay == 0) {
                    // LA SOLUTION : On force le cast en (Screen) pour que le compilateur accepte !
                    client.setScreenAndShow((Screen) new GrabVillagerConfigScreen());
                }
            }
        });

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("grabvillager", "overlay"),
                (guiGraphics, deltaTracker) -> {
                    GrabVillagerOverlay.render(guiGraphics);
                }
        );

        // 5. Commandes de configuration
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommands.literal("grabvillager")
                    .executes(context -> {
                        context.getSource().sendFeedback(Component.literal("§aOpening the Grab Villager configuration..."));
                        screenOpenDelay = 2;
                        return 1;
                    }));
        });
    }
}