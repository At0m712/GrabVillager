package com.atom.grabvillager.client;

import com.atom.grabvillager.config.GrabVillagerConfig;
import com.atom.grabvillager.logic.GrabVillagerLogic;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;

import java.util.function.BiConsumer;

public class GrabVillagerClientLogic {
    public static final KeyMapping DROP_KEY = new KeyMapping("key.grabvillager.drop", 85, "category.grabvillager.keys");

    private static int ticksHeld = 0;

    public static int throwAnimTicks = 0;
    public static final int MAX_THROW_ANIM_TICKS = 15;
    // Tick précis où les bras fouettent en avant (1/3 de l'animation)
    public static final int THROW_RELEASE_TICK = 10;

    private static float pendingCharge = 0.0f;
    private static boolean pendingIsThrow = false;

    public static int getTicksHeld() {
        return ticksHeld;
    }

    public static float getChargeProgress() {
        // Maintient le villageois en haut pendant l'anticipation du jet
        if (throwAnimTicks > 0) return 1.0f;
        return Math.min(ticksHeld / 10.0f, 1.0f);
    }

    public static void tick(BiConsumer<Boolean, Float> sendPacketCallback) {
        LocalPlayer player = Minecraft.getInstance().player;

        // Déroulement de l'animation
        if (throwAnimTicks > 0) {
            throwAnimTicks--;

            // C'est LE moment : on lâche le villageois et on avertit le serveur !
            if (throwAnimTicks == THROW_RELEASE_TICK) {
                sendPacketCallback.accept(pendingIsThrow, pendingCharge);
                if (player != null) {
                    Entity passenger = player.getFirstPassenger();
                    if (passenger instanceof Villager) {
                        passenger.stopRiding();
                    }
                }
            }
        }

        if (player != null && GrabVillagerLogic.isCarryingVillager(player)) {
            // On empêche de recharger un coup si une animation est en cours
            if (throwAnimTicks == 0) {
                if (DROP_KEY.isDown()) {
                    ticksHeld++;
                } else if (ticksHeld > 0) {
                    boolean isThrow = ticksHeld >= 10;
                    float charge = Math.min(ticksHeld / 20.0f, 1.0f) * GrabVillagerConfig.throwMultiplier;

                    if (isThrow) {
                        // On lance l'animation de jet (le villageois reste accroché pour le moment)
                        throwAnimTicks = MAX_THROW_ANIM_TICKS;
                        pendingCharge = charge;
                        pendingIsThrow = true;
                    } else {
                        // C'est un drop classique, on le pose instantanément
                        sendPacketCallback.accept(false, charge);
                        Entity passenger = player.getFirstPassenger();
                        if (passenger instanceof Villager) {
                            passenger.stopRiding();
                        }
                    }
                    ticksHeld = 0;
                }
            }
        } else if (throwAnimTicks == 0) {
            ticksHeld = 0;
        }
    }
}