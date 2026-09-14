package atom.grabvillager.mixin;

import atom.grabvillager.logic.IThrownVillager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Villager.class)
public abstract class VillagerFlightMixin extends LivingEntity implements IThrownVillager {

    @Unique
    private int grabvillager$thrownTicks = 0;

    protected VillagerFlightMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void grabvillager$setThrownTicks(int ticks) {
        this.grabvillager$thrownTicks = ticks;
    }

    @Override
    public int grabvillager$getThrownTicks() {
        return this.grabvillager$thrownTicks;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void grabvillager$onFlightTick(CallbackInfo ci) {
        if (this.grabvillager$thrownTicks > 0) {
            this.grabvillager$thrownTicks--;

            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                if (!this.onGround() && !this.isInWater() && this.getVehicle() == null) {
                    // Traînée de particules de vol
                    serverLevel.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.4, this.getZ(), 2, 0.08, 0.08, 0.08, 0.01);
                    serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 0.5, this.getZ(), 1, 0.08, 0.08, 0.08, 0.04);
                    if (this.random.nextFloat() < 0.25f) {
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.6, this.getZ(), 1, 0.05, 0.05, 0.05, 0.02);
                    }
                } else {
                    // Atterrissage en douceur
                    this.resetFallDistance();
                    serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.2, this.getZ(), 6, 0.2, 0.05, 0.2, 0.03);
                    serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.VILLAGER_YES, SoundSource.NEUTRAL, 1.0f, 1.1f);
                    this.grabvillager$thrownTicks = 0;
                }
            }
        }
    }
}