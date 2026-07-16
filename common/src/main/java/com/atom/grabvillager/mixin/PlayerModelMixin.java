package com.atom.grabvillager.mixin;

import net.minecraft.client.model.PlayerModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerModel.class)
public class PlayerModelMixin<T extends LivingEntity> {

    // On s'injecte à la fin de l'animation pour écraser la position des bras
    @Inject(method = "setupAnim", at = @At("TAIL"))
    private void grabvillager$onSetupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        // On vérifie qu'il s'agit bien d'un joueur qui porte un villageois
        if (entity instanceof Player player && !player.getPassengers().isEmpty() && player.getFirstPassenger() instanceof Villager) {
            PlayerModel<?> model = (PlayerModel<?>) (Object) this;

            // Lever les bras vers le haut (-2.8 radians donne un bel effet de portage au-dessus de la tête)
            model.rightArm.xRot = -2.8f;
            model.leftArm.xRot = -2.8f;

            // On s'assure qu'ils ne sont pas tordus sur les autres axes
            model.rightArm.yRot = 0.0f;
            model.leftArm.yRot = 0.0f;

            // On les écarte très légèrement pour encadrer le corps du villageois
            model.rightArm.zRot = 0.15f;
            model.leftArm.zRot = -0.15f;
        }
    }
}