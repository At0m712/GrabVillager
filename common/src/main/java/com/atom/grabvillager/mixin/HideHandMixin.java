package com.atom.grabvillager.mixin;

import com.atom.grabvillager.logic.GrabVillagerLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HideHandMixin {

    // On s'injecte au début de la méthode qui dessine le bras et l'outil tenu
    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void grabvillager$hideHandWhenCarrying(AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equipProgress, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, CallbackInfo ci) {
        // Si le joueur porte un villageois, on annule le rendu graphique de sa main et de son item
        if (player != null && GrabVillagerLogic.isCarryingVillager(player)) {
            ci.cancel();
        }
    }
}