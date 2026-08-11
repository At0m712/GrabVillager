package atom.grabvillager.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityPassengerMixin {

    // 1. Débloque la pré-vérification générale pour le joueur
    @Inject(method = "couldAcceptPassenger", at = @At("HEAD"), cancellable = true)
    private void grabvillager$allowAccept(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player) {
            cir.setReturnValue(true);
        }
    }

    // 2. Débloque la vérification finale spécifiquement pour le villageois
    @Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
    private void grabvillager$allowVillager(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Player && passenger instanceof Villager) {
            System.out.println("[GrabVillager DEBUG MIXIN] Le jeu Vanilla autorise officiellement le passager !");
            cir.setReturnValue(true);
        }
    }
}