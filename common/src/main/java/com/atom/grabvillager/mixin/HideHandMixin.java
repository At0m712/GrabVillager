package com.atom.grabvillager.mixin;

import com.atom.grabvillager.config.GrabVillagerConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HideHandMixin {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"), cancellable = true)
    private void grabvillager$hideHandWhenCarrying(AbstractClientPlayer player, float f1, float f2, InteractionHand hand, float f3, ItemStack itemStack, float f4, PoseStack poseStack, SubmitNodeCollector buffer, int packedLight, CallbackInfo ci) {

        // 1. SÉCURITÉ : Si la configuration AUTORISE l'usage des outils, on ne cache rien du tout.
        if (GrabVillagerConfig.allowTools) {
            return;
        }

        // 2. Si les outils sont INTERDITS, on vérifie si on porte un villageois
        if (player.getPassengers().stream().anyMatch(entity -> entity instanceof Villager)) {

            // On annule l'affichage de la main et de l'objet
            ci.cancel();
        }
    }
}