package com.atom.grabvillager.logic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class GrabVillagerLogic {

    public static boolean isCarryingVillager(Player player) {
        return !player.getPassengers().isEmpty() && player.getFirstPassenger() instanceof Villager;
    }

    public static boolean shouldBlockActions(Player player) {
        return isCarryingVillager(player) && !com.atom.grabvillager.config.GrabVillagerConfig.allowTools;
    }

    // NOUVEAU : Vérifie si on est en train d'essayer de taper NOTRE villageois
    public static boolean isOwnPassenger(Player player, Entity target) {
        return player.hasPassenger(target);
    }

    public static InteractionResult tryGrab(Player player, Entity target, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && player.isShiftKeyDown() && target instanceof Villager) {
            if (player.getPassengers().isEmpty()) {
                target.startRiding(player, true);
            }
            return InteractionResult.sidedSuccess(player.level().isClientSide());
        }
        return InteractionResult.PASS;
    }

    public static void handleDropOrThrow(ServerPlayer player, boolean isThrow, float charge) {
        if (isCarryingVillager(player)) {
            Entity passenger = player.getFirstPassenger();
            passenger.stopRiding();

            if (isThrow) {
                Vec3 look = player.getLookAngle();
                float velocity = 0.5f + (charge * 1.2f);
                passenger.setDeltaMovement(look.x * velocity, (look.y * velocity) + 0.5D, look.z * velocity);
                passenger.hurtMarked = true;
            }
        }
    }
}