package com.atom.grabvillager.mixin;

import com.atom.grabvillager.client.GrabVillagerClientLogic;
import com.atom.grabvillager.client.IGrabVillagerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.world.entity.npc.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerRenderer.class)
public class PlayerRendererMixin {

    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void onExtractRenderState(AbstractClientPlayer player, PlayerRenderState state, float partialTicks, CallbackInfo ci) {
        // On vérifie que notre state a bien reçu l'interface via Mixin
        if (state instanceof IGrabVillagerState customState) {
            boolean isLocal = player == Minecraft.getInstance().player;
            boolean isCarrying = !player.getPassengers().isEmpty() && player.getFirstPassenger() instanceof Villager;

            // On sauvegarde les informations dans le RenderState pour que le modèle puisse les lire plus tard
            customState.grabvillager$setLocal(isLocal);
            customState.grabvillager$setCarrying(isCarrying);
            customState.grabvillager$setThrowTicks(isLocal ? GrabVillagerClientLogic.throwAnimTicks : 0);
            customState.grabvillager$setChargeProgress(isLocal ? GrabVillagerClientLogic.getChargeProgress() : 0.0f);
        }
    }
}