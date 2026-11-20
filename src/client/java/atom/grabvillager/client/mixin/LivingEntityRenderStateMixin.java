package atom.grabvillager.client.mixin;

import atom.grabvillager.client.IVillagerFlightState;
import atom.grabvillager.client.IVillagerRotationState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements IVillagerRotationState, IVillagerFlightState {

    @Unique private boolean isRidingPlayer;
    @Unique private boolean isRidingLocalPlayer;
    @Unique private float playerSwimAmount;
    @Unique private boolean isPlayerCrouching;
    @Unique private boolean grabvillager$flyingInAir;

    @Override public boolean grabvillager$isRidingPlayer() { return isRidingPlayer; }
    @Override public void grabvillager$setRidingPlayer(boolean riding) { this.isRidingPlayer = riding; }

    @Override public boolean grabvillager$isRidingLocalPlayer() { return isRidingLocalPlayer; }
    @Override public void grabvillager$setRidingLocalPlayer(boolean local) { this.isRidingLocalPlayer = local; }

    @Override public float grabvillager$getPlayerSwimAmount() { return playerSwimAmount; }
    @Override public void grabvillager$setPlayerSwimAmount(float amount) { this.playerSwimAmount = amount; }

    @Override public boolean grabvillager$isPlayerCrouching() { return isPlayerCrouching; }
    @Override public void grabvillager$setPlayerCrouching(boolean crouching) { this.isPlayerCrouching = crouching; }

    @Override public boolean grabvillager$isFlyingInAir() { return grabvillager$flyingInAir; }
    @Override public void grabvillager$setFlyingInAir(boolean flying) { this.grabvillager$flyingInAir = flying; }
}
