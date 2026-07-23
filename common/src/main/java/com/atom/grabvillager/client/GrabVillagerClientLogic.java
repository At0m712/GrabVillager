package com.atom.grabvillager.client;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class GrabVillagerClientLogic {

    // Constantes pour l'animation
    public static final int MAX_THROW_ANIM_TICKS = 10;
    public static int throwAnimTicks = 0;

    // Variables d'état
    private static float chargeProgress = 0.0f;
    private static int ticksHeld = 0;
    private static boolean wasDown = false;

    public static float getChargeProgress() {
        return chargeProgress;
    }

    public static void setChargeProgress(float progress) {
        chargeProgress = progress;
    }

    public static int getTicksHeld() {
        return ticksHeld;
    }

    public static void setTicksHeld(int ticks) {
        ticksHeld = ticks;
    }

    public interface DropCallback {
        void execute(boolean isThrow, float charge);
    }

    public static void tick(DropCallback callback) {
        if (throwAnimTicks > 0) {
            throwAnimTicks--;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        boolean isDown = DROP_KEY.isDown();

        if (isDown) {
            ticksHeld++;
            // On charge la barre jusqu'à 1.0 (en 1 seconde, soit 20 ticks)
            chargeProgress = Math.min(1.0f, ticksHeld / 20.0f);
        } else if (wasDown) {
            // Le joueur vient de relâcher la touche
            boolean isThrow = ticksHeld > 5;
            float finalCharge = isThrow ? chargeProgress : 0.0f;

            if (isThrow) {
                throwAnimTicks = MAX_THROW_ANIM_TICKS;
            }

            callback.execute(isThrow, finalCharge);

            // Réinitialisation
            ticksHeld = 0;
            chargeProgress = 0.0f;
        }

        wasDown = isDown;
    }

    // Déclaration de la touche (R par défaut)
    public static final KeyMapping DROP_KEY = new KeyMapping(
            "key.grabvillager.drop",
            GLFW.GLFW_KEY_R,
            "key.categories.grabvillager"
    );
}