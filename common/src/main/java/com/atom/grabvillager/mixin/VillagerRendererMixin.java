package com.atom.grabvillager.mixin;

import com.atom.grabvillager.client.IVillagerFlightState;
import net.minecraft.client.renderer.entity.VillagerRenderer;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerRenderer.class)
public class VillagerRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/npc/villager/Villager;Lnet/minecraft/client/renderer/entity/state/VillagerRenderState;F)V", at = @At("TAIL"))
    private void grabvillager$onExtractVillagerFlightState(Villager entity, VillagerRenderState state, float partialTick, CallbackInfo ci) {
        if (state instanceof IVillagerFlightState flightState) {
            boolean inAir = !entity.onGround() && !entity.isInWater() && entity.getVehicle() == null && entity.getDeltaMovement().lengthSqr() > 0.03;
            flightState.grabvillager$setFlyingInAir(inAir);
        }
    }
}
