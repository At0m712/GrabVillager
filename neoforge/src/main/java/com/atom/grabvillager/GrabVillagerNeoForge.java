package com.atom.grabvillager.neoforge;

import com.atom.grabvillager.logic.GrabVillagerLogic;
import com.atom.grabvillager.network.VillagerDropPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@Mod("grabvillager")
public class GrabVillagerNeoForge {

    public GrabVillagerNeoForge(IEventBus modEventBus) {
        modEventBus.addListener(this::registerPayloads);

        // Inscription aux événements d'interaction (Blocage)
        NeoForge.EVENT_BUS.addListener(this::onEntityInteract);
        NeoForge.EVENT_BUS.addListener(this::onRightClickItem);
        NeoForge.EVENT_BUS.addListener(this::onRightClickBlock);
        NeoForge.EVENT_BUS.addListener(this::onLeftClickBlock);

        if (net.neoforged.fml.loading.FMLEnvironment.dist.isClient()) {
            modEventBus.addListener(GrabVillagerClientNeoForge::registerKeybinds);
            NeoForge.EVENT_BUS.addListener(GrabVillagerClientNeoForge::onClientTick);
            NeoForge.EVENT_BUS.addListener(GrabVillagerClientNeoForge::onRenderGui);
        }
    }

    private void registerPayloads(final RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("grabvillager");
        registrar.playToServer(
                VillagerDropPayload.TYPE,
                VillagerDropPayload.CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        GrabVillagerLogic.handleDropOrThrow(
                                (ServerPlayer) context.player(),
                                payload.isThrow(),
                                payload.charge()
                        );
                    });
                }
        );
    }

    // --- GESTION ET BLOCAGE DES INTERACTIONS ---

    private void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (GrabVillagerLogic.isCarryingVillager(event.getEntity())) {
            event.setCanceled(true);
            return;
        }
        var result = GrabVillagerLogic.tryGrab(event.getEntity(), event.getTarget(), event.getHand());
        if (result.consumesAction()) {
            event.setCanceled(true);
            event.setCancellationResult(result);
        }
    }

    private void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (GrabVillagerLogic.isCarryingVillager(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (GrabVillagerLogic.isCarryingVillager(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    private void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (GrabVillagerLogic.isCarryingVillager(event.getEntity())) {
            event.setCanceled(true);
        }
    }
}