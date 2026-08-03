package com.atom.grabvillager.logic;

import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class GrabVillagerLogic {

    public static boolean isCarryingVillager(Player player) {
        return !player.getPassengers().isEmpty() && player.getFirstPassenger() instanceof Villager;
    }

    public static boolean shouldBlockActions(Player player) {
        return isCarryingVillager(player) && !com.atom.grabvillager.config.GrabVillagerConfig.allowTools;
    }

    public static boolean isOwnPassenger(Player player, Entity target) {
        return player.hasPassenger(target);
    }

    public static InteractionResult tryGrab(Player player, Entity target, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && target instanceof Villager) {

            if (player.level().isClientSide() && !player.isCrouching() && !player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }

            if (player.getPassengers().isEmpty()) {
                Pose originalPose = player.getPose();
                boolean wasShift = player.isShiftKeyDown();

                player.setPose(Pose.STANDING);
                player.setShiftKeyDown(false);

                if (!player.level().isClientSide() && target.getVehicle() != null) {
                    target.stopRiding();
                }

                boolean success = target.startRiding(player, true, true);

                if (!success && !player.level().isClientSide()) {
                    IGrabVillagerVehicle hackTarget = (IGrabVillagerVehicle) target;
                    IGrabVillagerVehicle hackPlayer = (IGrabVillagerVehicle) player;

                    hackTarget.grabvillager$forceSetVehicle(player);
                    hackPlayer.grabvillager$forceAddPassenger(target);
                }

                player.setPose(originalPose);
                player.setShiftKeyDown(wasShift);

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public static void handleDropOrThrow(ServerPlayer player, boolean isThrow, float charge) {
        if (!isCarryingVillager(player)) {
            return;
        }

        Entity passenger = player.getFirstPassenger();
        Vec3 look = player.getLookAngle();


        passenger.stopRiding();


        player.connection.send(new ClientboundSetPassengersPacket(player));


        double spawnX = player.getX() + (look.x * 0.5);
        double spawnY = player.getY() + player.getEyeHeight() - 0.5;
        double spawnZ = player.getZ() + (look.z * 0.5);

        passenger.setPos(spawnX, spawnY, spawnZ);
        passenger.setYRot(player.getYRot());
        passenger.setXRot(player.getXRot());


        if (isThrow) {

            float clampedCharge = Math.max(0.0F, charge);
            float velocity = 0.5f + (clampedCharge * 1.2f);

            Vec3 movement = new Vec3(look.x * velocity, (look.y * velocity) + 0.5D, look.z * velocity);
            passenger.setDeltaMovement(movement);
        } else {
            passenger.setDeltaMovement(Vec3.ZERO);
        }

        passenger.setOnGround(false);


        passenger.hurtMarked = true;


        player.connection.send(new ClientboundSetEntityMotionPacket(passenger));
    }
}