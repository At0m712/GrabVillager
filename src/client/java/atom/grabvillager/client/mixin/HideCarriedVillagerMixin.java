package atom.grabvillager.client.mixin;

import atom.grabvillager.logic.GrabVillagerLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public class HideCarriedVillagerMixin {

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void grabvillager$hideWhenCarried(Entity entity, Frustum frustum, double x, double y, double z, float partialTick, CallbackInfoReturnable<Boolean> cir) {
        if (GrabVillagerLogic.isGrabbable(entity) && entity.getVehicle() instanceof LocalPlayer) {
            if (Minecraft.getInstance().options.getCameraType().isFirstPerson()) {
                cir.setReturnValue(false);
            }
        }
    }
}