package com.atom.grabvillager.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(EntityRenderDispatcher.class)
public class HideCarriedVillagerMixin {

    // On s'injecte tout au début de la méthode de rendu des entités.
    // En 1.21.2+, le paramètre "rotationYaw" n'existe plus, la méthode prend 8 paramètres.
    @Inject(method = "render", at = @At("HEAD"), cancellable = true)
    private void onRender(Entity entity, double x, double y, double z, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        // On vérifie que l'entité est un villageois et qu'il est sur le joueur local
        if (entity instanceof Villager && entity.getVehicle() instanceof LocalPlayer) {
            // Si le joueur est en vue à la première personne, on annule le rendu du villageois
            if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                ci.cancel();
            }
        }
    }
}