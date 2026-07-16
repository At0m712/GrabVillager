package com.atom.grabvillager.client;


import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;


import java.util.function.BiConsumer;

public class GrabVillagerClientLogic {
    public static final KeyMapping DROP_KEY = new KeyMapping(
            "key.grabvillager.drop",
            85, // 85 est le code entier pour la touche 'U'
            "category.grabvillager.keys"
    );

    private static int ticksHeld = 0;

    // Permet à la jauge visuelle de lire la valeur de charge actuelle
    public static int getTicksHeld() {
        return ticksHeld;
    }

    public static void tick(BiConsumer<Boolean, Float> sendPacketCallback) {
        if (DROP_KEY.isDown()) {
            ticksHeld++;
        } else if (ticksHeld > 0) {
            boolean isThrow = ticksHeld >= 10;
            float charge = Math.min(ticksHeld / 20.0f, 1.0f);

            sendPacketCallback.accept(isThrow, charge);

            // PRÉDICTION CLIENT : On lâche visuellement le villageois instantanément
            LocalPlayer player = Minecraft.getInstance().player;
            if (player != null && !player.getPassengers().isEmpty()) {
                Entity passenger = player.getFirstPassenger();
                if (passenger instanceof Villager) {
                    passenger.stopRiding();
                }
            }

            ticksHeld = 0;
        }
    }
}