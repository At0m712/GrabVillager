package atom.grabvillager.client.mixin;

import atom.grabvillager.client.IVillagerFlightState;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.entity.state.ZombieVillagerRenderState;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public class HumanoidFlightModelMixin {

    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/client/renderer/entity/state/HumanoidRenderState;)V", at = @At("TAIL"))
    private void grabvillager$animateFlyingZombieVillager(HumanoidRenderState state, CallbackInfo ci) {
        if (state instanceof ZombieVillagerRenderState && state instanceof IVillagerFlightState flightState && flightState.grabvillager$isFlyingInAir()) {
            float wiggle = state.ageInTicks * 1.3f;

            // Panicked leg flailing
            this.rightLeg.xRot = Mth.sin(wiggle) * 0.9f;
            this.rightLeg.zRot = Mth.cos(wiggle * 0.5f) * 0.25f;
            this.leftLeg.xRot = -Mth.sin(wiggle) * 0.9f;
            this.leftLeg.zRot = -Mth.cos(wiggle * 0.5f) * 0.25f;

            // Arms waving in mid-air
            this.rightArm.xRot = -2.2f + Mth.sin(wiggle * 0.9f) * 0.4f;
            this.leftArm.xRot = -2.2f - Mth.sin(wiggle * 0.9f) * 0.4f;
            this.rightArm.zRot = 0.35f + Mth.cos(wiggle * 0.6f) * 0.2f;
            this.leftArm.zRot = -0.35f - Mth.cos(wiggle * 0.6f) * 0.2f;

            // Panicked head wobble
            this.head.zRot = Mth.sin(wiggle * 0.6f) * 0.25f;
            this.head.xRot += Mth.cos(wiggle * 0.7f) * 0.2f;
        }
    }
}
