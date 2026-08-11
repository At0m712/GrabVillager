package atom.grabvillager;

import atom.grabvillager.logic.GrabVillagerLogic;
import atom.grabvillager.network.VillagerDropPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
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
        LOG.info("Le mod Grab Villager est chargé !");

        // 1. CORRECTION NOMENCLATURE : playC2S() devient serverboundPlay()
        PayloadTypeRegistry.serverboundPlay().register(VillagerDropPayload.PACKET_ID, VillagerDropPayload.PACKET_CODEC);

        // 2. Enregistrement du récepteur avec PACKET_ID
        ServerPlayNetworking.registerGlobalReceiver(VillagerDropPayload.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                GrabVillagerLogic.handleDropOrThrow(context.player(), payload.isThrow(), payload.charge());
            });
        });

        // 3. Interagir avec une Entité
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (GrabVillagerLogic.shouldBlockActions(player)) return InteractionResult.FAIL;
            return GrabVillagerLogic.tryGrab(player, entity, hand);
        });

        // 4. Interagir avec un Item
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (GrabVillagerLogic.shouldBlockActions(player)) {
                return InteractionResult.FAIL;
            }
            return InteractionResult.PASS;
        });

        // 5. Interagir avec un Bloc
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (GrabVillagerLogic.shouldBlockActions(player)) return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });

        // 6. Attaquer un Bloc
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (GrabVillagerLogic.shouldBlockActions(player)) return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });

        // 7. Attaquer une Entité
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (GrabVillagerLogic.isOwnPassenger(player, entity)) return InteractionResult.FAIL;
            if (GrabVillagerLogic.shouldBlockActions(player)) return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });
    }
}
