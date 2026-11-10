package atom.grabvillager.logic;

import java.util.UUID;

public interface IThrownVillager {
    void grabvillager$setThrownTicks(int ticks);
    int grabvillager$getThrownTicks();
    void grabvillager$setThrower(UUID uuid);
    UUID grabvillager$getThrower();
}
