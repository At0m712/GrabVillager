package com.atom.grabvillager.mixin;

import com.atom.grabvillager.logic.IGrabVillagerVehicle;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(Entity.class)
public abstract class EntityForceRideMixin implements IGrabVillagerVehicle {

    @Shadow private Entity vehicle;
    @Shadow protected abstract void addPassenger(Entity passenger);

    @Override
    public void grabvillager$forceSetVehicle(Entity newVehicle) {
        this.vehicle = newVehicle;
    }

    @Override
    public void grabvillager$forceAddPassenger(Entity passenger) {
        this.addPassenger(passenger);
    }
}