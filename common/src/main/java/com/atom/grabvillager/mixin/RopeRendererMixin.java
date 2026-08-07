package com.atom.grabvillager.mixin;

import com.atom.grabvillager.client.GrabVillagerClientLogic;
import com.atom.grabvillager.logic.GrabVillagerLogic;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.WeakHashMap;

@Mixin(LivingEntityRenderer.class)
public class RopeRendererMixin {


    @Unique
    private static final WeakHashMap<PlayerRenderState, float[]> ROPE_DATA_MAP = new WeakHashMap<>();

    @Inject(method = "extractRenderState", at = @At("RETURN"))
    public void onExtractRenderState(LivingEntity entity, LivingEntityRenderState state, float partialTick, CallbackInfo ci) {
        if (entity instanceof AbstractClientPlayer player && state instanceof PlayerRenderState playerState) {


            boolean shouldRender = GrabVillagerLogic.isCarryingVillager(player)
                    && GrabVillagerClientLogic.throwAnimTicks <= 0
                    && GrabVillagerClientLogic.getTicksHeld() <= 0;

            float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
            float crouch = player.isCrouching() ? 1.0f : 0.0f;
            float swim = player.getSwimAmount(partialTick);

            ROPE_DATA_MAP.put(playerState, new float[]{ shouldRender ? 1.0f : 0.0f, bodyYaw, crouch, swim });
        }
    }


    @Inject(method = "render", at = @At("TAIL"))
    public void injectRopeRender(LivingEntityRenderState state, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {

        if (!(state instanceof PlayerRenderState playerState)) return;


        float[] data = ROPE_DATA_MAP.get(playerState);

        // Si le tableau est vide, qu'on ne doit pas rendre (0.0f), ou que le joueur nage, on coupe
        if (data == null || data[0] == 0.0f || data[3] > 0.0f) return;


        float bodyYaw = data[1];
        boolean isCrouching = data[2] > 0.0f;

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - bodyYaw));

        VertexConsumer builder = buffer.getBuffer(RenderType.leash());
        Matrix4f matrix = poseStack.last().pose();

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

    @Unique
    private void addDegenerate(Matrix4f matrix, VertexConsumer builder, float lastX, float lastY, float lastZ, float nextX, float nextY, float nextZ, int light) {
        builder.addVertex(matrix, lastX, lastY, lastZ).setColor(0, 0, 0, 0).setLight(light);
        builder.addVertex(matrix, nextX, nextY, nextZ).setColor(0, 0, 0, 0).setLight(light);
    }
}