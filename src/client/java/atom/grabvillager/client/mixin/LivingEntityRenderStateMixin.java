package atom.grabvillager.client.mixin;

import atom.grabvillager.client.IVillagerRotationState;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LivingEntityRenderState.class)
public class LivingEntityRenderStateMixin implements IVillagerRotationState {

    @Unique private boolean isRidingPlayer;
    @Unique private boolean isRidingLocalPlayer;
    @Unique private float playerSwimAmount;
    @Unique private boolean isPlayerCrouching;

    @Override public boolean grabvillager$isRidingPlayer() { return isRidingPlayer; }
    @Override public void grabvillager$setRidingPlayer(boolean riding) { this.isRidingPlayer = riding; }

    @Override public boolean grabvillager$isRidingLocalPlayer() { return isRidingLocalPlayer; }
    @Override public void grabvillager$setRidingLocalPlayer(boolean local) { this.isRidingLocalPlayer = local; }

    @Override public float grabvillager$getPlayerSwimAmount() { return playerSwimAmount; }
    @Override public void grabvillager$setPlayerSwimAmount(float amount) { this.playerSwimAmount = amount; }

    @Override public boolean grabvillager$isPlayerCrouching() { return isPlayerCrouching; }
    @Override public void grabvillager$setPlayerCrouching(boolean crouching) { this.isPlayerCrouching = crouching; }
}