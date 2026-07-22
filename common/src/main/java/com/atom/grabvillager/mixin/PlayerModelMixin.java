package com.atom.grabvillager.mixin;

import com.atom.grabvillager.client.GrabVillagerClientLogic;
import com.atom.grabvillager.config.GrabVillagerConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelMixin<T extends LivingEntity> {

    // On cible la méthode classique de la 1.21.1 avec tous ses paramètres
    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void grabvillager$onSetupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {

        // 1. On calcule nos variables directement grâce à l'entité
        boolean isLocal = entity == Minecraft.getInstance().player;
        boolean isCarrying = !entity.getPassengers().isEmpty() && entity.getFirstPassenger() instanceof Villager;
        int throwTicks = isLocal ? GrabVillagerClientLogic.throwAnimTicks : 0;
        float chargeProgress = isLocal ? GrabVillagerClientLogic.getChargeProgress() : 0.0f;

        // 2. Si le joueur porte un villageois ou est en train de le lancer
        if (isCarrying || throwTicks > 0) {
            // LA CORRECTION EST ICI : On retire les <?>
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
                float progress = (!GrabVillagerConfig.allowTools) ? 1.0f : (isLocal ? chargeProgress : 0.0f);

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

            // 4. On synchronise les manches avec les bras
            model.rightSleeve.xRot = model.rightArm.xRot;
            model.leftSleeve.xRot = model.leftArm.xRot;
            model.rightSleeve.yRot = model.rightArm.yRot;
            model.leftSleeve.yRot = model.leftArm.yRot;
            model.rightSleeve.zRot = model.rightArm.zRot;
            model.leftSleeve.zRot = model.leftArm.zRot;
        }
    }
}