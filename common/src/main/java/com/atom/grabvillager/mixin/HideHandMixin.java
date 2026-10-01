package com.atom.grabvillager.mixin;

import com.atom.grabvillager.config.GrabVillagerConfig;
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

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void grabvillager$hideHandWhenCarrying(AbstractClientPlayer player, float f1, float f2, InteractionHand hand, float f3, ItemStack itemStack, float f4, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        if (GrabVillagerConfig.allowTools) {
            return;
        }

        if (player.getPassengers().stream().anyMatch(GrabVillagerLogic::isGrabbable)) {
            ci.cancel();
        }
    }
}