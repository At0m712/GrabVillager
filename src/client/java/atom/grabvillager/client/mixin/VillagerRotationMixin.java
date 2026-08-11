package atom.grabvillager.client.mixin;

import atom.grabvillager.client.GrabVillagerClientLogic;
import atom.grabvillager.client.IVillagerRotationState;
import atom.grabvillager.config.GrabVillagerConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntityRenderer.class)
public class VillagerRotationMixin {

    // ÉTAPE 1 : On capture les données de l'entité juste avant le rendu et on les stocke dans notre state
    @Inject(method = "extractRenderState", at = @At("TAIL"))
    private void grabvillager$onExtractRenderState(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        if (state instanceof IVillagerRotationState customState) {
            if (entity instanceof Villager villager && villager.getVehicle() instanceof Player player) {
                customState.grabvillager$setRidingPlayer(true);
                customState.grabvillager$setRidingLocalPlayer(player == Minecraft.getInstance().player);
                customState.grabvillager$setPlayerSwimAmount(player.getSwimAmount(partialTick));
                customState.grabvillager$setPlayerCrouching(player.isCrouching());
            } else {
                customState.grabvillager$setRidingPlayer(false);
            }
        }
    }

    // ÉTAPE 2 : On applique les rotations en lisant le state (puisqu'on n'a plus accès à l'entité)
    @Inject(method = "setupRotations", at = @At("TAIL"))
    private void grabvillager$onSetupRotations(LivingEntityRenderState state, PoseStack poseStack, float bodyRot, float scale, CallbackInfo ci) {
        if (state instanceof IVillagerRotationState customState && customState.grabvillager$isRidingPlayer()) {

            boolean isLocal = customState.grabvillager$isRidingLocalPlayer();
            float progress = (GrabVillagerConfig.allowTools) ? 1.0f : (isLocal ? GrabVillagerClientLogic.getChargeProgress() : 0.0f);

            float swimAmount = customState.grabvillager$getPlayerSwimAmount();
            float sneakAngle = customState.grabvillager$isPlayerCrouching() ? 28.65f : 0.0f;

            poseStack.translate(0.0, 0.9, 0.0);

            float angleY = 180.0f * (1.0f - progress);
            poseStack.mulPose(Axis.YP.rotationDegrees(angleY));

            float baseAngleX = Mth.lerp(swimAmount, sneakAngle, 90.0f);
            float angleX = baseAngleX * (1.0f - progress) + 90.0f * progress;
            poseStack.mulPose(Axis.XP.rotationDegrees(angleX));

            poseStack.translate(0.0, -0.9, 0.0);

            float sneakZ = customState.grabvillager$isPlayerCrouching() ? -0.02f : -0.15f;
            float baseZ = Mth.lerp(swimAmount, sneakZ, 1.0f);
            float offsetZ = baseZ * (1.0f - progress) + 1.2f * progress;

            float sneakY = customState.grabvillager$isPlayerCrouching() ? -0.1f : -0.2f;
            float baseY = Mth.lerp(swimAmount, sneakY, -0.6f);
            float offsetY = baseY * (1.0f - progress) + -0.2f * progress;

            poseStack.translate(0.0, offsetY, offsetZ);
        }
    }
}