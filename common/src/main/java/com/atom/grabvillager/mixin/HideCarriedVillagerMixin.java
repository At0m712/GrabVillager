package com.atom.grabvillager.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderer.class)
public class HideCarriedVillagerMixin {

    // L'astuce ultime : on cible "render" ET son nom codé Fabric "method_3936"
    @Inject(method = {"render", "method_3936"}, at = @At("HEAD"), cancellable = true)
    private void grabvillager$onRender(Entity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {

        // On vérifie que l'entité est un villageois et qu'il est sur le joueur local
        if (entity instanceof Villager && entity.getVehicle() instanceof LocalPlayer) {

            // Si le joueur est en vue à la première personne, on annule purement et simplement le rendu
            if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                ci.cancel();
            }
        }
    }
}