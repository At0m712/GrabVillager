package com.atom.grabvillager.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public class GrabVillagerClientLogic {

    // Variables d'animation
    public static final int MAX_THROW_ANIM_TICKS = 10;
    public static int throwAnimTicks = 0;
    private static float chargeProgress = 0.0f;

    // Variables pour la logique de chargement
    private static int ticksHeld = 0;

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

    // L'interface pour accepter la lambda de ton fichier Fabric/NeoForge
    public interface DropCallback {
        void execute(boolean isThrow, float charge);
    }

    // La méthode tick appelée par ton client à chaque image
    public static void tick(DropCallback callback) {
        if (throwAnimTicks > 0) {
            throwAnimTicks--;
        }

        // Tant que la touche est enfoncée, on charge
        if (DROP_KEY.isDown()) {
            ticksHeld++;
            chargeProgress = Math.min(1.0f, ticksHeld / 20.0f); // 20 ticks = 1 seconde pour une charge complète
        } else {
            // Dès que la touche est relâchée
            if (ticksHeld > 0) {
                // On considère que c'est un "lancer" si la charge a un peu progressé
                boolean isThrow = chargeProgress > 0.15f;

                // On déclenche le callback (ce qui envoie le paquet au serveur)
                callback.execute(isThrow, chargeProgress);

                if (isThrow) {
                    throwAnimTicks = MAX_THROW_ANIM_TICKS;
                }

                // On réinitialise l'état
                ticksHeld = 0;
                chargeProgress = 0.0f;
            }
        }
    }

    // Déclaration de la touche avec la norme stricte de la 1.21
    public static final KeyMapping DROP_KEY = new KeyMapping(
            "key.grabvillager.drop",                 // Le nom de la touche
            InputConstants.Type.KEYSYM,              // Le type d'entrée
            GLFW.GLFW_KEY_U,                         // Le code de la touche (85 = U)
            // On utilise un ResourceLocation pour lier la catégorie à ton Mod ID
            new KeyMapping.Category(ResourceLocation.fromNamespaceAndPath("grabvillager", "keys"))
    );
}