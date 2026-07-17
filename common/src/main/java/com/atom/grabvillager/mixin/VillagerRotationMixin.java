package com.atom.grabvillager.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.util.Mth;
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
        if (entity instanceof Villager villager && villager.getVehicle() instanceof Player player) {

            boolean isLocal = player == Minecraft.getInstance().player;
            float progress = (!com.atom.grabvillager.config.GrabVillagerConfig.allowTools) ? 1.0f : (isLocal ? com.atom.grabvillager.client.GrabVillagerClientLogic.getChargeProgress() : 0.0f);

            float swimAmount = player.getSwimAmount(partialTick);
            float sneakAngle = player.isCrouching() ? 28.65f : 0.0f;

            poseStack.translate(0.0, 0.9, 0.0);

            float angleY = 180.0f * (1.0f - progress);
            poseStack.mulPose(Axis.YP.rotationDegrees(angleY));

            float baseAngleX = Mth.lerp(swimAmount, sneakAngle, 90.0f);
            float angleX = baseAngleX * (1.0f - progress) + 90.0f * progress;
            poseStack.mulPose(Axis.XP.rotationDegrees(angleX));

            poseStack.translate(0.0, -0.9, 0.0);

            // CORRECTION INVERSE x2 : On passe à +1.0f !
            // L'axe Z local pointant vers le bas, cette valeur positive va l'écraser contre le dos.
            float sneakZ = player.isCrouching() ? -0.02f : -0.15f;
            float baseZ = Mth.lerp(swimAmount, sneakZ, 1.0f);
            float offsetZ = baseZ * (1.0f - progress) + 1.2f * progress;

            float sneakY = player.isCrouching() ? -0.1f : -0.2f;
            float baseY = Mth.lerp(swimAmount, sneakY, -0.6f);
            float offsetY = baseY * (1.0f - progress) + -0.2f * progress;

            poseStack.translate(0.0, offsetY, offsetZ);
        }
    }
}