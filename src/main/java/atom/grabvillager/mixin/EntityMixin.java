package atom.grabvillager.mixin;

import atom.grabvillager.logic.GrabVillagerLogic;
import atom.grabvillager.logic.IEntityPassengerCheck;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin implements IEntityPassengerCheck {

    @Shadow
    protected abstract boolean canAddPassenger(Entity passenger);

    @Override
    public boolean grabvillager$canAcceptPassenger(Entity passenger) {
        return this.canAddPassenger(passenger);
    }

    // 1. Allow the player to accept a villager or grabbable passenger
    @Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
    private void grabvillager$allowPlayerPassenger(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player && GrabVillagerLogic.isGrabbable(passenger)) {
            cir.setReturnValue(true);
        }
    }

    // 2. Allow couldAcceptPassenger on players
    @Inject(method = "couldAcceptPassenger", at = @At("HEAD"), cancellable = true)
    private void grabvillager$allowAccept(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player) {
            cir.setReturnValue(true);
        }
    }

    // 3. Bypass vehicle.getType().canSerialize() rejection when mounting players
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
