package com.atom.grabvillager.logic;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
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

    public static boolean isOwnPassenger(Player player, Entity target) {
        return player.hasPassenger(target);
    }

    public static InteractionResult tryGrab(Player player, Entity target, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && target instanceof Villager) {

            // On vérifie que le client est bien accroupi
            if (player.level().isClientSide() && !player.isCrouching() && !player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }

            if (player.getPassengers().isEmpty()) {
                String side = player.level().isClientSide() ? "CLIENT" : "SERVEUR";
                System.out.println("==================================================");
                System.out.println("[GrabVillager DEBUG] ORDRE DE PRISE REÇU CÔTÉ : " + side);

                // 1. LEURRE DE POSTURE
                Pose originalPose = player.getPose();
                boolean wasShift = player.isShiftKeyDown();
                player.setPose(Pose.STANDING);
                player.setShiftKeyDown(false);

                // On détache le villageois par sécurité s'il était buggé sur le serveur
                if (!player.level().isClientSide() && target.getVehicle() != null) {
                    target.stopRiding();
                }

                // 2. TENTATIVE VANILLA NATURELLE (Réussira sur le Client, échouera sur le Serveur)
                boolean success = target.startRiding(player, true);

                // 3. FORÇAGE ABSOLU SI LE SERVEUR REFUSE
                if (!success && !player.level().isClientSide()) {
                    System.out.println("[GrabVillager DEBUG] Le Serveur refuse. Forçage du piratage des variables !");
                    IGrabVillagerVehicle hackTarget = (IGrabVillagerVehicle) target;
                    IGrabVillagerVehicle hackPlayer = (IGrabVillagerVehicle) player;

                    // On modifie la mémoire du serveur manuellement
                    hackTarget.grabvillager$forceSetVehicle(player);
                    hackPlayer.grabvillager$forceAddPassenger(target);

                    success = true; // On force le succès pour la suite
                }

                // 4. RESTAURATION DE LA POSTURE
                player.setPose(originalPose);
                player.setShiftKeyDown(wasShift);

                System.out.println("[GrabVillager DEBUG] Attachement final côté " + side + " : " + success);
                System.out.println("==================================================");

                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public static void handleDropOrThrow(ServerPlayer player, boolean isThrow, float charge) {
        System.out.println("==================================================");
        System.out.println("[GrabVillager DEBUG] Lancement de la commande DROP/THROW");

        if (!isCarryingVillager(player)) {
            System.out.println("[GrabVillager DEBUG] ERREUR CRITIQUE : Le Serveur pense que ton dos est VIDE !");
            System.out.println("==================================================");
            return;
        }

        Entity passenger = player.getFirstPassenger();

        // 1. Démontage
        passenger.stopRiding();

        // 2. Forçage de la position de départ au niveau du joueur
        passenger.setPos(player.getX(), player.getY() + player.getEyeHeight() - 0.5, player.getZ());
        passenger.setYRot(player.getYRot());
        passenger.setXRot(player.getXRot());

        // 3. Application de la physique
        if (isThrow) {
            Vec3 look = player.getLookAngle();
            float velocity = 0.5f + (charge * 1.2f);
            Vec3 movement = new Vec3(look.x * velocity, (look.y * velocity) + 0.5D, look.z * velocity);

            passenger.setDeltaMovement(movement);
            passenger.hurtMarked = true;
            passenger.hasImpulse = true;
            System.out.println("[GrabVillager DEBUG] Vélocité appliquée avec succès !");
        } else {
            passenger.setDeltaMovement(Vec3.ZERO);
            passenger.hurtMarked = true;
            passenger.hasImpulse = true;
            System.out.println("[GrabVillager DEBUG] Posé sur place avec succès.");
        }
        System.out.println("==================================================");
    }
}