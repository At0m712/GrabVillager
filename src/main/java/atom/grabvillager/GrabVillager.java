package atom.grabvillager;

import atom.grabvillager.client.GrabVillagerClient;
import atom.grabvillager.config.GrabVillagerConfig;
import atom.grabvillager.logic.GrabVillagerLogic;
import atom.grabvillager.network.VillagerDropPayload;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(GrabVillager.MOD_ID)
public class GrabVillager {

    public static final String MOD_ID = "grabvillager";
    public static final String MOD_NAME = "Grab Villager";
    public static final Logger LOG = LoggerFactory.getLogger(MOD_NAME);

    public GrabVillager(IEventBus modEventBus) {
        LOG.info("Le mod Grab Villager (NeoForge) est chargé !");
        GrabVillagerConfig.load();

        modEventBus.addListener(this::registerPayloadHandlers);

        NeoForge.EVENT_BUS.register(this);

        if (FMLEnvironment.getDist() == Dist.CLIENT) {
            GrabVillagerClient.init(modEventBus);
        }
    }

    private void registerPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID);
        registrar.playToServer(
                VillagerDropPayload.PACKET_ID,
                VillagerDropPayload.PACKET_CODEC,
                (payload, context) -> context.enqueueWork(() -> {
                    if (context.player() instanceof ServerPlayer serverPlayer) {
                        GrabVillagerLogic.handleDropOrThrow(serverPlayer, payload.isThrow(), payload.charge());
                    }
                })
        );
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        Player player = event.getEntity();
        if (GrabVillagerLogic.isGrabbable(event.getTarget())) {
            if (GrabVillagerLogic.isCarryingVillager(player)) {
                return;
            }
            InteractionResult result = GrabVillagerLogic.tryGrab(player, event.getTarget(), event.getHand());
            if (result != InteractionResult.PASS) {
                event.setCancellationResult(result);
                event.setCanceled(true);
            }
            return;
        }

        if (GrabVillagerLogic.shouldBlockActions(player)) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (GrabVillagerLogic.shouldBlockActions(event.getEntity())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (GrabVillagerLogic.shouldBlockActions(event.getEntity())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (GrabVillagerLogic.shouldBlockActions(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if (GrabVillagerLogic.isOwnPassenger(player, event.getTarget())) {
            event.setCanceled(true);
            return;
        }
        if (GrabVillagerLogic.shouldBlockActions(player)) {
            event.setCanceled(true);
        }
    }
}