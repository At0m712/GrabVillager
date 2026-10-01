package com.atom.grabvillager.mixin;

import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(VillagerModel.class)
public abstract class VillagerModelMixin<T extends Entity> {

    @Shadow @Final private ModelPart head;
    @Shadow @Final private ModelPart rightLeg;
    @Shadow @Final private ModelPart leftLeg;
    @Shadow @Final private ModelPart arms;

    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void grabvillager$animateFlyingVillager(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (!entity.onGround() && !entity.isInWater() && entity.getVehicle() == null && entity.getDeltaMovement().lengthSqr() > 0.03) {
            float wiggle = ageInTicks * 1.3f;

            // Gigotement effréné des jambes (pédalage de panique)
            this.rightLeg.xRot = Mth.sin(wiggle) * 0.9f;
            this.rightLeg.zRot = Mth.cos(wiggle * 0.5f) * 0.25f;
            this.leftLeg.xRot = -Mth.sin(wiggle) * 0.9f;
            this.leftLeg.zRot = -Mth.cos(wiggle * 0.5f) * 0.25f;

            // Bras qui tremblent
            this.arms.xRot = -0.5f + Mth.sin(wiggle * 0.8f) * 0.25f;
            this.arms.zRot = Mth.cos(wiggle * 0.6f) * 0.15f;

            // Tête qui oscille en panique
            this.head.zRot = Mth.sin(wiggle * 0.6f) * 0.25f;
            this.head.xRot += Mth.cos(wiggle * 0.7f) * 0.2f;
        }
    }
}
