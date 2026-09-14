package atom.grabvillager.client.mixin;

import atom.grabvillager.client.IGrabVillagerState;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(AvatarRenderState.class)
public class AvatarRenderStateMixin implements IGrabVillagerState {
    @Unique private boolean grabvillager$isLocal;
    @Unique private boolean grabvillager$isCarrying;
    @Unique private int grabvillager$throwTicks;
    @Unique private float grabvillager$chargeProgress;

    @Override public boolean grabvillager$isLocal() { return grabvillager$isLocal; }
    @Override public void grabvillager$setLocal(boolean local) { this.grabvillager$isLocal = local; }

    @Override public boolean grabvillager$isCarrying() { return grabvillager$isCarrying; }
    @Override public void grabvillager$setCarrying(boolean carrying) { this.grabvillager$isCarrying = carrying; }

    @Override public int grabvillager$getThrowTicks() { return grabvillager$throwTicks; }
    @Override public void grabvillager$setThrowTicks(int ticks) { this.grabvillager$throwTicks = ticks; }

    @Override public float grabvillager$getChargeProgress() { return grabvillager$chargeProgress; }
    @Override public void grabvillager$setChargeProgress(float progress) { this.grabvillager$chargeProgress = progress; }
}