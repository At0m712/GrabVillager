package atom.grabvillager.mixin;

import atom.grabvillager.logic.GrabVillagerLogic;
import atom.grabvillager.config.GrabVillagerConfig;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class PlayerPassengerMixin {

    @Inject(method = "positionRider(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity$MoveFunction;)V", at = @At("HEAD"), cancellable = true)
    private void grabvillager$onPositionRider(Entity passenger, Entity.MoveFunction callback, CallbackInfo ci) {
        if ((Object) this instanceof Player player && passenger instanceof Villager villager) {

            boolean isLocal = GrabVillagerLogic.clientPlayerId != null && player.getUUID().equals(GrabVillagerLogic.clientPlayerId);
            float progress = (!GrabVillagerConfig.allowTools) ? 1.0f : (isLocal ? GrabVillagerLogic.clientChargeProgress : 0.0f);

            villager.setYBodyRot(player.yBodyRot);
            villager.yBodyRotO = player.yBodyRotO;
            villager.setYHeadRot(player.yHeadRot);
            villager.yHeadRotO = player.yHeadRotO;

            double backY = player.getY() + (player.getBbHeight() * 0.4);

            if (player.isCrouching()) {
                backY -= 0.15;
            }

            if (player.isVisuallySwimming()) {
                backY = player.getY() + 0.7;
            }

            double headY = player.getY() + player.getBbHeight() + 0.4;
            double newY = backY + (headY - backY) * progress;

            double offsetX = 0.0;
            double offsetZ = 0.0;

            if (villager.isBaby()) {

                double backwardDistance = 0.4 * progress;

                float bodyYawRad = player.yBodyRot * ((float) Math.PI / 180.0F);

                offsetX = Math.sin(bodyYawRad) * backwardDistance;
                offsetZ = -Math.cos(bodyYawRad) * backwardDistance;
            }

            callback.accept(passenger, player.getX() + offsetX, newY, player.getZ() + offsetZ);
            ci.cancel();
        }
    }
}