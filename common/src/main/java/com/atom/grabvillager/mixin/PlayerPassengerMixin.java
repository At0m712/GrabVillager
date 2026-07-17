package com.atom.grabvillager.mixin;

import net.minecraft.client.Minecraft;
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
        if ((Object) this instanceof Player player && passenger instanceof Villager villager) {

            boolean isLocal = player == Minecraft.getInstance().player;
            float progress = (!com.atom.grabvillager.config.GrabVillagerConfig.allowTools) ? 1.0f : (isLocal ? com.atom.grabvillager.client.GrabVillagerClientLogic.getChargeProgress() : 0.0f);

            villager.setYBodyRot(player.yBodyRot);
            villager.yBodyRotO = player.yBodyRotO;
            villager.setYHeadRot(player.yHeadRot);
            villager.yHeadRotO = player.yHeadRotO;

            double backY = player.getY() + (player.getBbHeight() * 0.4);

            if (player.isCrouching()) {
                backY -= 0.15;
            }

            // On stabilise la racine au centre du joueur qui nage
            if (player.isVisuallySwimming()) {
                backY = player.getY() + 0.7;
            }

            double headY = player.getY() + player.getBbHeight() + 0.4;
            double newY = backY + (headY - backY) * progress;

            callback.accept(passenger, player.getX(), newY, player.getZ());
            ci.cancel();
        }
    }
}