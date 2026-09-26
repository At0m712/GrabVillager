package atom.grabvillager.mixin;

import atom.grabvillager.logic.GrabVillagerLogic;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class GhostVillagerMixin {

    // Rend l'entité portée totalement transparente au curseur (Raytrace)
    // Tes clics d'outils et de minage passeront au travers d'elle !
    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    private void grabvillager$onIsPickable(CallbackInfoReturnable<Boolean> cir) {
        if (GrabVillagerLogic.isGrabbable((Entity) (Object) this) && ((LivingEntity) (Object) this).getVehicle() instanceof Player) {
            cir.setReturnValue(false);
        }
    }
}