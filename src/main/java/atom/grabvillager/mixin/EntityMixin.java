package atom.grabvillager.mixin;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    // On s'injecte dans la méthode qui vérifie si une entité peut en porter une autre
    @Inject(method = "canAddPassenger", at = @At("HEAD"), cancellable = true)
    private void grabvillager$allowPlayerPassenger(Entity passenger, CallbackInfoReturnable<Boolean> cir) {
        // Si le véhicule est un Joueur et que le passager est un Villageois, on force l'autorisation !
        if ((Object) this instanceof Player && passenger instanceof Villager) {
            cir.setReturnValue(true);
        }
    }
}