package com.atom.grabvillager.fabric;

import com.atom.grabvillager.logic.GrabVillagerLogic;
import com.atom.grabvillager.network.VillagerDropPayload;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.player.AttackBlockCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;

public class GrabVillagerFabric implements ModInitializer {
    @Override
    public void onInitialize() {
        // Enregistrement du Packet Réseau
        PayloadTypeRegistry.playC2S().register(VillagerDropPayload.TYPE, VillagerDropPayload.CODEC);

        // Réception de la commande de Lancer côté Serveur
        ServerPlayNetworking.registerGlobalReceiver(VillagerDropPayload.TYPE, (payload, context) -> {
            context.server().execute(() -> {
                GrabVillagerLogic.handleDropOrThrow(context.player(), payload.isThrow(), payload.charge());
            });
        });

        // 1. Interagir avec une Entité (Attraper ou échanger)
        UseEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (GrabVillagerLogic.shouldBlockActions(player)) return InteractionResult.FAIL;
            return GrabVillagerLogic.tryGrab(player, entity, hand);
        });

        // 2. Interagir avec un Item (Manger, tirer à l'arc...)
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (GrabVillagerLogic.shouldBlockActions(player)) return InteractionResultHolder.fail(player.getItemInHand(hand));
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        });

        // 3. Interagir avec un Bloc (Poser un bloc, ouvrir un coffre...)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (GrabVillagerLogic.shouldBlockActions(player)) return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });

        // 4. Attaquer un Bloc (Casser / Miner)
        AttackBlockCallback.EVENT.register((player, world, hand, pos, direction) -> {
            if (GrabVillagerLogic.shouldBlockActions(player)) return InteractionResult.FAIL;
            return InteractionResult.PASS;
        });

        // 5. Attaquer une Entité (Taper avec l'épée / Clic gauche)
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            // IMMUNITÉ : On ne peut pas taper le villageois qu'on porte sur le dos !
            if (GrabVillagerLogic.isOwnPassenger(player, entity)) return InteractionResult.FAIL;

            // BLOCAGE CLASSIQUE : Si l'option des outils est désactivée dans le menu
            if (GrabVillagerLogic.shouldBlockActions(player)) return InteractionResult.FAIL;

            return InteractionResult.PASS;
        });
    }
}