package atom.grabvillager.client;

import atom.grabvillager.config.GrabVillagerConfig;
import atom.grabvillager.network.VillagerDropPayload;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.common.NeoForge;

public class GrabVillagerClient {

    private static int screenOpenDelay = 0;

    public static final KeyMapping.Category GRAB_CATEGORY = KeyMapping.Category.register(
            Identifier.fromNamespaceAndPath("grabvillager", "main")
    );

    public static KeyMapping dropKey;

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(GrabVillagerClient::registerKeys);
        modEventBus.addListener(GrabVillagerClient::registerGuiLayers);

        NeoForge.EVENT_BUS.addListener(GrabVillagerClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(GrabVillagerClient::onRegisterCommands);
    }

    public static void registerKeys(RegisterKeyMappingsEvent event) {
        dropKey = new KeyMapping(
                "key.grabvillager.drop",
                InputConstants.KEY_G,
                GRAB_CATEGORY
        );
        event.register(dropKey);
    }

    public static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(
                Identifier.fromNamespaceAndPath("grabvillager", "overlay"),
                (guiGraphics, deltaTracker) -> GrabVillagerOverlay.render(guiGraphics)
        );
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        GrabVillagerClientLogic.tick((isThrow, charge) -> {
            ClientPacketDistributor.sendToServer(new VillagerDropPayload(isThrow, charge, GrabVillagerConfig.throwMultiplier));
        });

        Minecraft client = Minecraft.getInstance();
        if (screenOpenDelay > 0) {
            screenOpenDelay--;
            if (screenOpenDelay == 0) {
                client.setScreenAndShow(new GrabVillagerConfigScreen());
            }
        }
    }

    public static void onRegisterCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("grabvillager")
                .executes(context -> {
                    context.getSource().sendSystemMessage(Component.literal("§aOpening the Grab Villager configuration..."));
                    screenOpenDelay = 2;
                    return 1;
                }));
    }
}