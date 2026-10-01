package atom.grabvillager.mixin;

import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Zombie.class)
public abstract class ZombieSunMixin {

    @Inject(method = "isSunSensitive", at = @At("HEAD"), cancellable = true)
    private void grabvillager$noSunburnWhenCarried(CallbackInfoReturnable<Boolean> cir) {
        if (((Zombie) (Object) this).getVehicle() instanceof Player) {
            cir.setReturnValue(false);
        }
    }
}
