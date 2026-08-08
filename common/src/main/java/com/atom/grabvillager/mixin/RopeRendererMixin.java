package com.atom.grabvillager.mixin;

import com.atom.grabvillager.client.GrabVillagerClientLogic;
import com.atom.grabvillager.logic.GrabVillagerLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher; // LE GRAND CHEF DU RENDU
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

// On esquive tout le bordel des RenderStates en ciblant le Dispatcher global !
@Mixin(EntityRenderDispatcher.class)
public class RopeRendererMixin {

    // On utilise les alias au cas où, mais la signature (Entity, double, double, double, float, float...) est immortelle.
    @Inject(
            method = {
                    "render",
                    "m_114384_",
                    "method_3954"
            },
            at = @At("TAIL")
    )
    public void onRenderEntity(Entity entity, double x, double y, double z, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {

        // 1. On vérifie instantanément si l'entité est notre joueur, sans passer par un "State"
        if (entity instanceof AbstractClientPlayer player) {

            // 2. Nos conditions classiques
            if (!GrabVillagerLogic.isCarryingVillager(player)) return;
            if (GrabVillagerClientLogic.throwAnimTicks > 0) return;
            if (GrabVillagerClientLogic.getTicksHeld() > 0) return;
            if (player.getSwimAmount(partialTick) > 0.0f) return;

            poseStack.pushPose();

            // 3. Le Dispatcher nous donne les coordonnées (x,y,z) parfaites !
            // On translate la caméra directement sur les pieds du joueur.
            poseStack.translate(x, y, z);

            // 4. On fait pivoter la corde avec le corps du joueur
            float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
            poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));

            VertexConsumer builder = buffer.getBuffer(RenderType.leash());
            Matrix4f matrix = poseStack.last().pose();

            boolean isCrouching = player.isCrouching();

            float t = 0.07f;
            float width = 0.28f;

            float frontY = 1.05f;
            float frontZ = -0.15f;
            float backY = 1.35f;
            float backZ = 0.40f;

            if (isCrouching) {
                frontY = 0.85f;
                frontZ = -0.15f;
                backY = 1.25f;
                backZ = 0.55f;
            }

            float[] Xs = { width, width + 0.05f, -width - 0.05f, -width, width };
            float[] Ys = { frontY, backY, backY, frontY, frontY };
            float[] Zs = { frontZ, backZ, backZ, frontZ, frontZ };

            float[] oX = { t, t, -t, -t, t };
            float[] oZ = { -t, t, t, -t, -t };

            for (int i = 0; i < 5; i++) {
                builder.addVertex(matrix, Xs[i] + oX[i], Ys[i] + t, Zs[i] + oZ[i]).setColor(210, 185, 145, 255).setLight(packedLight);
                builder.addVertex(matrix, Xs[i] + oX[i], Ys[i] - t, Zs[i] + oZ[i]).setColor(210, 185, 145, 255).setLight(packedLight);
            }
            addDegenerate(matrix, builder, Xs[4] + oX[4], Ys[4] - t, Zs[4] + oZ[4], Xs[0] - oX[0], Ys[0] + t, Zs[0] - oZ[0], packedLight);

            for (int i = 0; i < 5; i++) {
                builder.addVertex(matrix, Xs[i] - oX[i], Ys[i] + t, Zs[i] - oZ[i]).setColor(180, 155, 115, 255).setLight(packedLight);
                builder.addVertex(matrix, Xs[i] - oX[i], Ys[i] - t, Zs[i] - oZ[i]).setColor(180, 155, 115, 255).setLight(packedLight);
            }
            addDegenerate(matrix, builder, Xs[4] - oX[4], Ys[4] - t, Zs[4] - oZ[4], Xs[0] + oX[0], Ys[0] + t, Zs[0] + oZ[0], packedLight);

            for (int i = 0; i < 5; i++) {
                builder.addVertex(matrix, Xs[i] + oX[i], Ys[i] + t, Zs[i] + oZ[i]).setColor(135, 126, 86, 255).setLight(packedLight);
                builder.addVertex(matrix, Xs[i] - oX[i], Ys[i] + t, Zs[i] - oZ[i]).setColor(135, 126, 86, 255).setLight(packedLight);
            }
            addDegenerate(matrix, builder, Xs[4] - oX[4], Ys[4] + t, Zs[4] - oZ[4], Xs[0] + oX[0], Ys[0] - t, Zs[0] + oZ[0], packedLight);

            for (int i = 0; i < 5; i++) {
                builder.addVertex(matrix, Xs[i] + oX[i], Ys[i] - t, Zs[i] + oZ[i]).setColor(160, 135, 95, 255).setLight(packedLight);
                builder.addVertex(matrix, Xs[i] - oX[i], Ys[i] - t, Zs[i] - oZ[i]).setColor(160, 135, 95, 255).setLight(packedLight);
            }

            poseStack.popPose();
        }
    }

    @Unique
    private void addDegenerate(Matrix4f matrix, VertexConsumer builder, float lastX, float lastY, float lastZ, float nextX, float nextY, float nextZ, int light) {
        builder.addVertex(matrix, lastX, lastY, lastZ).setColor(0, 0, 0, 0).setLight(light);
        builder.addVertex(matrix, nextX, nextY, nextZ).setColor(0, 0, 0, 0).setLight(light);
    }
}