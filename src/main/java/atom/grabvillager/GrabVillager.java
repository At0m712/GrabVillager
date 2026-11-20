package atom.grabvillager;

import atom.grabvillager.config.GrabVillagerConfig;
import atom.grabvillager.logic.GrabVillagerLogic;
import atom.grabvillager.network.ConfigSyncPayload;
import atom.grabvillager.network.VillagerDropPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.InteractionResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrabVillager implements ModInitializer {

    public static final String MOD_ID = "grabvillager";
    public static final String MOD_NAME = "Grab Villager";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    @Override
    public void onInitialize() {
        LOG.info("Grab Villager loaded!");
        GrabVillagerConfig.load();

        // 1. Register network payloads
        PayloadTypeRegistry.serverboundPlay().register(VillagerDropPayload.PACKET_ID, VillagerDropPayload.PACKET_CODEC);
        PayloadTypeRegistry.clientboundPlay().register(ConfigSyncPayload.PACKET_ID, ConfigSyncPayload.PACKET_CODEC);

        // 2. Register C2S receiver
        ServerPlayNetworking.registerGlobalReceiver(VillagerDropPayload.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                GrabVillagerLogic.handleDropOrThrow(context.player(), payload.isThrow(), payload.charge(), payload.multiplier());
            });
        });

        // Sync server config to joining clients
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            if (ServerPlayNetworking.canSend(handler.getPlayer(), ConfigSyncPayload.PACKET_ID)) {
                ServerPlayNetworking.send(handler.getPlayer(), new ConfigSyncPayload(
                        GrabVillagerConfig.throwMultiplier,
                        GrabVillagerConfig.allowTools
                ));
            }
        });

        // Clean up player cooldown map on disconnect
        ServerPlayConnectionEvents.DISCONNECT.register((handler, server) -> {
            GrabVillagerLogic.onPlayerDisconnect(handler.getPlayer().getUUID());
        });

        // 3. Entity interaction
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (GrabVillagerLogic.isGrabbable(entity)) {
                if (GrabVillagerLogic.isCarryingVillager(player)) {
                    if (GrabVillagerLogic.shouldBlockActions(player)) {
                        return InteractionResult.FAIL;
                    }
                    return InteractionResult.PASS;
                }
                return GrabVillagerLogic.tryGrab(player, entity, hand);
            }

            // Targeted placement into vehicles (Minecarts & Boats)
            if (GrabVillagerLogic.isCarryingVillager(player)) {
                InteractionResult vehicleResult = GrabVillagerLogic.tryPlaceInVehicle(player, entity, hand);
                if (vehicleResult != InteractionResult.PASS) {
                    return vehicleResult;
                }
            }

            if (GrabVillagerLogic.shouldBlockActions(player)) return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });

        // 4. Item use
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (GrabVillagerLogic.shouldBlockActions(player)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        // 5. Block use
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (GrabVillagerLogic.shouldBlockActions(player)) return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });

        // 6. Block attack
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (GrabVillagerLogic.shouldBlockActions(player)) return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });

        // 7. Entity attack
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (GrabVillagerLogic.isOwnPassenger(player, entity)) return InteractionResult.FAIL;
            if (GrabVillagerLogic.shouldBlockActions(player)) return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });
    }
}
