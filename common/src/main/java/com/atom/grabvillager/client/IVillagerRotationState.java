package com.atom.grabvillager.client;

public interface IVillagerRotationState {
    boolean grabvillager$isRidingPlayer();
    void grabvillager$setRidingPlayer(boolean riding);

    boolean grabvillager$isRidingLocalPlayer();
    void grabvillager$setRidingLocalPlayer(boolean local);

    float grabvillager$getPlayerSwimAmount();
    void grabvillager$setPlayerSwimAmount(float amount);

    boolean grabvillager$isPlayerCrouching();
    void grabvillager$setPlayerCrouching(boolean crouching);
}