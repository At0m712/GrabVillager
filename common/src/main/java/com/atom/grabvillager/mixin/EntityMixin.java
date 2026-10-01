package com.atom.grabvillager.mixin;

import com.atom.grabvillager.logic.GrabVillagerLogic;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    // 1. Autoriser le joueur à accepter un passager villageois ou grabbable
    @Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
    private void grabvillager$allowPlayerPassenger(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player && GrabVillagerLogic.isGrabbable(passenger)) {
            cir.setReturnValue(true);
        }
    }

    // 2. Débloquer la vérification générale couldAcceptPassenger si nécessaire
    @Inject(method = "couldAcceptPassenger", at = @At("HEAD"), cancellable = true)
    private void grabvillager$allowAccept(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player) {
            cir.setReturnValue(true);
        }
    }

    // 3. Contourner le refus de monture sur le joueur lié à vehicle.getType().canSerialize()
    @Redirect(
        method = "startRiding(Lnet/minecraft/world/entity/Entity;ZZ)Z",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;canSerialize()Z")
    )
    private boolean grabvillager$allowPlayerVehicleSerialize(EntityType<?> instance) {
        if (GrabVillagerLogic.isGrabbable((Entity) (Object) this)) {
            return true;
        }
        return instance.canSerialize();
    }
}