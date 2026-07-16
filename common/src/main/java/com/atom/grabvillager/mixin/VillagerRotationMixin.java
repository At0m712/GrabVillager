package com.atom.grabvillager.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class VillagerRotationMixin {

    @Inject(method = "setupRotations", at = @At("TAIL"))
    private void grabvillager$onSetupRotations(LivingEntity entity, PoseStack poseStack, float bob, float yBodyRot, float partialTick, float scale, CallbackInfo ci) {
        if (entity instanceof Villager && entity.getVehicle() instanceof Player) {

            // 1. On translate AVANT la rotation (axes classiques de Minecraft)
            // Y = -0.4 (pour le descendre dans les bras)
            // Z = -0.5 (pour l'avancer vers l'avant du joueur)
            poseStack.translate(0.0, -0.4, -0.5);

            // 2. On le fait pivoter sur le ventre
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
        }
    }
}