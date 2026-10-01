package com.atom.grabvillager.mixin;

import com.atom.grabvillager.config.GrabVillagerConfig;
import com.atom.grabvillager.logic.GrabVillagerLogic;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class PlayerPassengerMixin {

    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V", at = @At("HEAD"), cancellable = true)
    private void grabvillager$onPositionRider(Entity passenger, Entity.MoveFunction callback, CallbackInfo ci) {
        if ((Object) this instanceof Player player && GrabVillagerLogic.isGrabbable(passenger)) {

            boolean isLocal = player.level().isClientSide() && GrabVillagerLogic.clientPlayerId != null && player.getUUID().equals(GrabVillagerLogic.clientPlayerId);
            float progress = (!GrabVillagerConfig.allowTools) ? 1.0f : (isLocal ? GrabVillagerLogic.clientChargeProgress : 0.0f);

            if (passenger instanceof LivingEntity livingPassenger) {
                livingPassenger.setYBodyRot(player.yBodyRot);
                livingPassenger.yBodyRotO = player.yBodyRotO;
                livingPassenger.setYHeadRot(player.yHeadRot);
                livingPassenger.yHeadRotO = player.yHeadRotO;
            }

            double backY = player.getY() + (player.getBbHeight() * 0.4);

            if (player.isCrouching()) {
                backY -= 0.15;
            }

            if (player.isVisuallySwimming()) {
                backY = player.getY() + 0.7;
            }

            double headY = player.getY() + player.getBbHeight() + 0.4;
            double newY = backY + (headY - backY) * progress;

            double offsetX = 0.0;
            double offsetZ = 0.0;

            if (passenger instanceof LivingEntity living && living.isBaby()) {
                double backwardDistance = 0.4 * progress;
                float bodyYawRad = player.yBodyRot * ((float) Math.PI / 180.0F);
                offsetX = Math.sin(bodyYawRad) * backwardDistance;
                offsetZ = -Math.cos(bodyYawRad) * backwardDistance;
            }

            callback.accept(passenger, player.getX() + offsetX, newY, player.getZ() + offsetZ);
            ci.cancel();
        }
    }
}