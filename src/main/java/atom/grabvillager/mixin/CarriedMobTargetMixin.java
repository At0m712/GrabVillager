package atom.grabvillager.mixin;

import atom.grabvillager.logic.GrabVillagerLogic;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class CarriedMobTargetMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void grabvillager$onSetTarget(LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        // Si le mob est porté par un joueur, il ne peut cibler personne
        if (self.getVehicle() instanceof Player) {
            ci.cancel();
            return;
        }
        // Si la cible est portée par un joueur et est grabbable, les autres mobs (ex: Golems, monstres) ne la ciblent pas
        if (target != null && target.getVehicle() instanceof Player && GrabVillagerLogic.isGrabbable(target)) {
            ci.cancel();
        }
    }

    @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
    private void grabvillager$onCanAttack(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        Mob self = (Mob) (Object) this;
        // Si le mob est porté par un joueur, il ne peut attaquer
        if (self.getVehicle() instanceof Player) {
            cir.setReturnValue(false);
            return;
        }
        // Si la cible est portée par un joueur, les autres mobs ne peuvent l'attaquer
        if (target != null && target.getVehicle() instanceof Player && GrabVillagerLogic.isGrabbable(target)) {
            cir.setReturnValue(false);
        }
    }
}
