package atom.grabvillager.logic;

import atom.grabvillager.config.GrabVillagerConfig;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class GrabVillagerLogic {
    public static java.util.UUID clientPlayerId = null;
    public static float clientChargeProgress = 0.0f;

    public static boolean isCarryingVillager(Player player) {
        return !player.getPassengers().isEmpty() && player.getFirstPassenger() instanceof Villager;
    }

    public static boolean shouldBlockActions(Player player) {
        return isCarryingVillager(player) && !GrabVillagerConfig.allowTools;
    }

    public static boolean isOwnPassenger(Player player, Entity target) {
        return player.hasPassenger(target);
    }

    public static InteractionResult tryGrab(Player player, Entity target, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && target instanceof Villager) {

            // S'accroupir (Shift) est obligatoire pour attraper, sinon on laisse passer l'interaction (commerce)
            if (!player.isCrouching() && !player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }

            if (player.getPassengers().isEmpty()) {
                if (!player.level().isClientSide() && target.getVehicle() != null) {
                    target.stopRiding();
                }

                boolean success = target.startRiding(player, true, true);

                if (success && !player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    ClientboundSetPassengersPacket packet = new ClientboundSetPassengersPacket(player);
                    serverPlayer.level().getChunkSource().sendToTrackingPlayersAndSelf(player, packet);
                }

                return success ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
        }
        return InteractionResult.PASS;
    }

    public static void handleDropOrThrow(ServerPlayer player, boolean isThrow, float charge) {
        if (!isCarryingVillager(player)) {
            return;
        }

        charge = Math.max(0.0f, Math.min(1.0f, charge));

        Entity passenger = player.getFirstPassenger();
        Vec3 look = player.getLookAngle();

        passenger.stopRiding();

        ClientboundSetPassengersPacket passPacket = new ClientboundSetPassengersPacket(player);
        player.level().getChunkSource().sendToTrackingPlayersAndSelf(player, passPacket);

        double spawnX = player.getX() + (look.x * 0.5);
        double spawnY = player.getY() + player.getEyeHeight() - 0.5;
        double spawnZ = player.getZ() + (look.z * 0.5);

        passenger.setPos(spawnX, spawnY, spawnZ);
        passenger.setYRot(player.getYRot());
        passenger.setXRot(player.getXRot());

        if (isThrow) {
            float multiplier = Math.max(0.1f, Math.min(3.0f, GrabVillagerConfig.throwMultiplier));
            float velocity = (0.5f + (charge * 1.2f)) * multiplier;
            Vec3 movement = new Vec3(look.x * velocity, (look.y * velocity) + 0.5D, look.z * velocity);
            passenger.setDeltaMovement(movement);

            if (passenger instanceof IThrownVillager thrownVillager) {
                thrownVillager.grabvillager$setThrownTicks(100);
            }

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    net.minecraft.sounds.SoundEvents.VILLAGER_NO, net.minecraft.sounds.SoundSource.NEUTRAL,
                    1.0f, 1.2f + (charge * 0.3f));
        } else {
            passenger.setDeltaMovement(Vec3.ZERO);
        }

        passenger.syncVelocity = true;
        passenger.setOnGround(false);

        ClientboundSetEntityMotionPacket motionPacket = new ClientboundSetEntityMotionPacket(passenger);
        player.level().getChunkSource().sendToTrackingPlayersAndSelf(passenger, motionPacket);
    }
}