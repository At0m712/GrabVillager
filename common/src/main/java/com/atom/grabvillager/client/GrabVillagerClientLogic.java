package com.atom.grabvillager.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;
import com.atom.grabvillager.config.GrabVillagerConfig;

public class GrabVillagerClientLogic {

    // Constantes pour l'animation
    public static final int MAX_THROW_ANIM_TICKS = 10;
    public static int throwAnimTicks = 0;

    // Variables d'état
    // chargeProgress reste STRICTEMENT entre 0.0f et 1.0f pour que ton interface graphique ne bug jamais
    private static float chargeProgress = 0.0f;
    private static int ticksHeld = 0;
    private static boolean wasDown = false;

    // --- LES MÉTHODES POUR TON OVERLAY ---
    public static float getChargeProgress() {
        return chargeProgress;
    }

    public static int getTicksHeld() {
        return ticksHeld;
    }
    // -------------------------------------

    public interface DropCallback {
        void execute(boolean isThrow, float charge);
    }

    public static void tick(DropCallback callback) {
        if (throwAnimTicks > 0) {
            throwAnimTicks--;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        // SÉCURITÉ ABSOLUE : On annule tout calcul si on ne porte personne.
        if (client.player.getPassengers().isEmpty()) {
            ticksHeld = 0;
            chargeProgress = 0.0f;
            wasDown = false;
            return;
        }

        boolean isDown = DROP_KEY.isDown();

        if (isDown) {
            ticksHeld++;
            chargeProgress = Math.min(1.0f, ticksHeld / 20.0f);

        } else if (wasDown) {
            boolean isThrow = ticksHeld > 5;


            float maxPowerFromConfig = GrabVillagerConfig.throwMultiplier;;

            float finalCharge = isThrow ? (chargeProgress * maxPowerFromConfig) : 0.0f;

            if (isThrow) {
                throwAnimTicks = MAX_THROW_ANIM_TICKS;
            }

            System.out.println("[CLIENT] Barre visuelle relâchée à : " + (int)(chargeProgress * 100) + "%. Puissance envoyée au serveur : " + finalCharge);

            callback.execute(isThrow, finalCharge);


            ticksHeld = 0;
            chargeProgress = 0.0f;
        }

        wasDown = isDown;
    }

    public static final KeyMapping DROP_KEY = new KeyMapping(
            "key.grabvillager.drop",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_R,
            KeyMapping.Category.MISC
    );
}