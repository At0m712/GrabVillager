package com.atom.grabvillager.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class GhostVillagerMixin {

    // Rend le villageois totalement transparent au curseur (Raytrace)
    // Tes clics d'outils et de minage passeront au travers de lui !
    @Inject(method = "isPickable", at = @At("HEAD"), cancellable = true)
    private void grabvillager$onIsPickable(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof Villager villager && villager.getVehicle() instanceof Player) {
            cir.setReturnValue(false);
        }
    }
}