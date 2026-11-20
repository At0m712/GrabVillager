package atom.grabvillager.client.mixin;

import atom.grabvillager.config.GrabVillagerConfig;
import atom.grabvillager.logic.GrabVillagerLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.FirstPersonHandsAndItemsRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(FirstPersonHandsAndItemsRenderer.class)
public class HideHandMixin {

    @Inject(method = "submitArmWithItem", at = @At("HEAD"), cancellable = true)
    private void grabvillager$hideHandWhenCarrying(CallbackInfo ci) {
        if (GrabVillagerConfig.getEffectiveAllowTools()) {
            return;
        }

        var player = Minecraft.getInstance().player;
        if (player != null && player.getPassengers().stream().anyMatch(GrabVillagerLogic::isGrabbable)) {
            ci.cancel();
        }
    }
}
