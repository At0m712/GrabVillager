package com.atom.grabvillager.client;

public interface IGrabVillagerState {
    boolean grabvillager$isLocal();
    void grabvillager$setLocal(boolean local);

    boolean grabvillager$isCarrying();
    void grabvillager$setCarrying(boolean carrying);

    int grabvillager$getThrowTicks();
    void grabvillager$setThrowTicks(int ticks);

    float grabvillager$getChargeProgress();
    void grabvillager$setChargeProgress(float progress);
}