package com.atom.grabvillager.mixin;

import com.atom.grabvillager.client.IGrabVillagerState;
import net.minecraft.client.renderer.entity.state.PlayerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(PlayerRenderState.class)
public class PlayerRenderStateMixin implements IGrabVillagerState {

    @Unique private boolean isLocal;
    @Unique private boolean isCarrying;
    @Unique private int throwTicks;
    @Unique private float chargeProgress;

    @Override public boolean grabvillager$isLocal() { return isLocal; }
    @Override public void grabvillager$setLocal(boolean local) { this.isLocal = local; }

    @Override public boolean grabvillager$isCarrying() { return isCarrying; }
    @Override public void grabvillager$setCarrying(boolean carrying) { this.isCarrying = carrying; }

    @Override public int grabvillager$getThrowTicks() { return throwTicks; }
    @Override public void grabvillager$setThrowTicks(int ticks) { this.throwTicks = ticks; }

    @Override public float grabvillager$getChargeProgress() { return chargeProgress; }
    @Override public void grabvillager$setChargeProgress(float progress) { this.chargeProgress = progress; }
}