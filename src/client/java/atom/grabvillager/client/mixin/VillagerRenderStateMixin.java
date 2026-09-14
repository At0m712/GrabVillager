package atom.grabvillager.client.mixin;

import atom.grabvillager.client.IVillagerFlightState;
import net.minecraft.client.renderer.entity.state.VillagerRenderState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(VillagerRenderState.class)
public class VillagerRenderStateMixin implements IVillagerFlightState {

    @Unique
    private boolean grabvillager$flyingInAir;

    @Override
    public boolean grabvillager$isFlyingInAir() {
        return grabvillager$flyingInAir;
    }

    @Override
    public void grabvillager$setFlyingInAir(boolean flying) {
        this.grabvillager$flyingInAir = flying;
    }
}