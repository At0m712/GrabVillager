package atom.grabvillager.client.mixin;

import atom.grabvillager.client.GrabVillagerClientLogic;
import atom.grabvillager.client.IGrabVillagerState;
import atom.grabvillager.config.GrabVillagerConfig;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelMixin {

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/AvatarRenderState;)V", at = @At("TAIL"))
    private void grabvillager$onSetupAnim(AvatarRenderState state, CallbackInfo ci) {
        if (!(state instanceof IGrabVillagerState customState)) return;

        boolean isCarrying = customState.grabvillager$isCarrying();
        int throwTicks = customState.grabvillager$getThrowTicks();
        float chargeProgress = customState.grabvillager$getChargeProgress();

        // Si ce joueur porte un villageois ou est en train de le lancer
        if (isCarrying || throwTicks > 0) {

            PlayerModel model = (PlayerModel) (Object) this;

            float targetXRight = model.rightArm.xRot;
            float targetXLeft = model.leftArm.xRot;
            float targetYRight = model.rightArm.yRot;
            float targetYLeft = model.leftArm.yRot;
            float targetZRight = model.rightArm.zRot;
            float targetZLeft = model.leftArm.zRot;

            if (throwTicks > 0) {
                float animProgress = (GrabVillagerClientLogic.MAX_THROW_ANIM_TICKS - throwTicks) / (float) GrabVillagerClientLogic.MAX_THROW_ANIM_TICKS;

                float currentAnimX;
                if (animProgress < 0.33f) {
                    // Phase 1 : Anticipation (Bras tirés violemment en arrière)
                    float p = animProgress / 0.33f;
                    currentAnimX = Mth.lerp(p, -2.8f, -4.2f);
                } else if (animProgress < 0.5f) {
                    // Phase 2 : Jet éclair (Bras fouettés en avant)
                    float p = (animProgress - 0.33f) / 0.17f;
                    currentAnimX = Mth.lerp(p, -4.2f, 0.5f);
                } else {
                    // Phase 3 : Retour progressif à la normale
                    float p = (animProgress - 0.5f) / 0.5f;
                    currentAnimX = Mth.lerp(p, 0.5f, targetXRight);
                }

                targetXRight = currentAnimX;
                targetXLeft = currentAnimX;

                targetZRight = Mth.lerp(animProgress, 0.15f, model.rightArm.zRot);
                targetZLeft = Mth.lerp(animProgress, -0.15f, model.leftArm.zRot);

            } else if (isCarrying) {
                float progress = (!GrabVillagerConfig.allowTools) ? 1.0f : chargeProgress;

                if (progress > 0.0f) {
                    targetXRight = Mth.lerp(progress, targetXRight, -2.8f);
                    targetXLeft = Mth.lerp(progress, targetXLeft, -2.8f);
                    targetYRight = Mth.lerp(progress, targetYRight, 0.0f);
                    targetYLeft = Mth.lerp(progress, targetYLeft, 0.0f);
                    targetZRight = Mth.lerp(progress, targetZRight, 0.15f);
                    targetZLeft = Mth.lerp(progress, targetZLeft, -0.15f);
                }
            }

            // 3. On applique les rotations calculées au modèle
            model.rightArm.xRot = targetXRight;
            model.leftArm.xRot = targetXLeft;
            model.rightArm.yRot = targetYRight;
            model.leftArm.yRot = targetYLeft;
            model.rightArm.zRot = targetZRight;
            model.leftArm.zRot = targetZLeft;

        }
    }
}