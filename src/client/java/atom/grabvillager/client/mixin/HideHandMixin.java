package atom.grabvillager.client.mixin;

import atom.grabvillager.config.GrabVillagerConfig;
import atom.grabvillager.logic.GrabVillagerLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public class HideHandMixin {

    @Inject(method = {"submitArmWithItem", "renderArmWithItem"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void grabvillager$hideHandWhenCarrying(AbstractClientPlayer player, float f1, float f2, InteractionHand hand, float f3, ItemStack itemStack, float f4, PoseStack poseStack, SubmitNodeCollector buffer, int packedLight, CallbackInfo ci) {
        if (GrabVillagerConfig.getEffectiveAllowTools()) {
            return;
        }

        if (player.getPassengers().stream().anyMatch(GrabVillagerLogic::isGrabbable)) {
            ci.cancel();
        }
    }
}
