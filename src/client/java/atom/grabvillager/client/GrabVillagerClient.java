package atom.grabvillager.client;

import atom.grabvillager.config.GrabVillagerConfig;
import atom.grabvillager.network.ConfigSyncPayload;
import atom.grabvillager.network.VillagerDropPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.ClientCommands;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class GrabVillagerClient implements ClientModInitializer {

    private static int screenOpenDelay = 0;

    public static final KeyMapping.Category GRAB_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("grabvillager", "main")
    );

    public static KeyMapping dropKey;

    @Override
    public void onInitializeClient() {
        GrabVillagerConfig.load();

        dropKey = KeyMappingHelper.registerKeyMapping(new KeyMapping(
                "key.grabvillager.drop",
                InputConstants.Type.KEYSYM,
                GLFW.GLFW_KEY_G,
                GRAB_CATEGORY
        ));

        // Receive server config sync on dedicated multiplayer servers
        ClientPlayNetworking.registerGlobalReceiver(ConfigSyncPayload.PACKET_ID, (payload, context) -> {
            context.client().execute(() -> {
                if (!context.client().isLocalServer()) {
                    GrabVillagerConfig.applyServerSync(payload.throwMultiplier(), payload.allowTools());
                }
            });
        });

        ClientPlayConnectionEvents.DISCONNECT.register((handler, client) -> {
            GrabVillagerConfig.clearServerSync();
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            GrabVillagerClientLogic.tick((isThrow, charge) -> {
                ClientPlayNetworking.send(new VillagerDropPayload(isThrow, charge, GrabVillagerConfig.throwMultiplier));
            });

            if (screenOpenDelay > 0) {
                screenOpenDelay--;
                if (screenOpenDelay == 0) {
                    client.setScreenAndShow(new GrabVillagerConfigScreen());
                }
            }
        });

        HudElementRegistry.addLast(
                Identifier.fromNamespaceAndPath("grabvillager", "overlay"),
                (guiGraphics, deltaTracker) -> {
                    GrabVillagerOverlay.render(guiGraphics);
                }
        );

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
