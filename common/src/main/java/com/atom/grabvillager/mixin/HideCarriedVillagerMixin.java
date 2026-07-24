package com.atom.grabvillager.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// On cible directement l'entité de base, pas le moteur de rendu !
@Mixin(Entity.class)
public class HideCarriedVillagerMixin {

    // isInvisible n'a aucun paramètre, Fabric ne pourra pas se tromper
    @Inject(method = "isInvisible", at = @At("HEAD"), cancellable = true)
    private void grabvillager$hideWhenCarried(CallbackInfoReturnable<Boolean> cir) {

        // On vérifie que l'entité actuelle est bien un villageois
        if ((Object) this instanceof Villager villager) {

            // On vérifie s'il est porté par le joueur local
            if (villager.getVehicle() instanceof LocalPlayer) {

                // Si on est en vue à la première personne
                if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {

                    // On force le jeu à considérer le villageois comme invisible
                    cir.setReturnValue(true);
                }
            }
        }
    }
}