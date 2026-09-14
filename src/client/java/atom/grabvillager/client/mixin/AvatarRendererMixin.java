package atom.grabvillager.client.mixin;

import atom.grabvillager.client.GrabVillagerClientLogic;
import atom.grabvillager.client.IGrabVillagerState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.world.entity.Avatar;
import net.minecraft.world.entity.npc.villager.Villager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(AvatarRenderer.class)
public class AvatarRendererMixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/world/entity/Avatar;Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;F)V", at = @At("TAIL"))
    private void grabvillager$onExtractAvatarRenderState(Avatar entity, AvatarRenderState state, float partialTick, CallbackInfo ci) {
        if (state instanceof IGrabVillagerState customState) {
            boolean isCarrying = !entity.getPassengers().isEmpty() && entity.getFirstPassenger() instanceof Villager;
            customState.grabvillager$setCarrying(isCarrying);

            boolean isLocal = (entity == Minecraft.getInstance().player);
            customState.grabvillager$setLocal(isLocal);

            if (isLocal) {
                customState.grabvillager$setThrowTicks(GrabVillagerClientLogic.throwAnimTicks);
                customState.grabvillager$setChargeProgress(GrabVillagerClientLogic.getChargeProgress());
            } else {
                customState.grabvillager$setThrowTicks(0);
                customState.grabvillager$setChargeProgress(0.0f);
            }
        }
    }
}