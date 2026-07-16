package com.atom.grabvillager.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class PlayerPassengerMixin {

    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V", at = @At("HEAD"), cancellable = true)
    private void grabvillager$onPositionRider(Entity passenger, Entity.MoveFunction callback, CallbackInfo ci) {
        if ((Object) this instanceof Player player && passenger instanceof Villager) {

            // On réduit la hauteur pour le mettre pile dans les mains ! (+0.1 au lieu de +0.6)
            double newY = player.getY() + player.getBbHeight() + 0.1;

            callback.accept(passenger, player.getX(), newY, player.getZ());
            ci.cancel();
        }
    }
}