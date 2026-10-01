package atom.grabvillager.mixin;

import atom.grabvillager.logic.GrabVillagerLogic;
import atom.grabvillager.logic.IThrownVillager;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class VillagerFlightMixin extends Entity implements IThrownVillager {

    @Unique
    private int grabvillager$thrownTicks = 0;

    protected VillagerFlightMixin(EntityType<?> entityType, Level level) {
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

    @Inject(method = "causeFallDamage", at = @At("HEAD"), cancellable = true)
    private void grabvillager$cancelFallDamage(double fallDistance, float multiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> cir) {
        if (this.grabvillager$thrownTicks > 0) {
            this.resetFallDistance();
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void grabvillager$onFlightTick(CallbackInfo ci) {
        if (this.grabvillager$thrownTicks > 0) {
            this.grabvillager$thrownTicks--;

            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                // Réinitialise la distance de chute continuellement pendant toute la durée du vol
                this.resetFallDistance();

                if (!this.onGround() && !this.isInWater() && !this.isInLava() && this.getVehicle() == null) {
                    // Traînée de particules de vol
                    serverLevel.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.4, this.getZ(), 2, 0.08, 0.08, 0.08, 0.01);
                    serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 0.5, this.getZ(), 1, 0.08, 0.08, 0.08, 0.04);
                    if (this.random.nextFloat() < 0.25f) {
                        if ((Object) this instanceof ZombieVillager) {
                            serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.6, this.getZ(), 1, 0.05, 0.05, 0.05, 0.02);
                        } else {
                            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.6, this.getZ(), 1, 0.05, 0.05, 0.05, 0.02);
                        }
                    }

                    // 🔊 Cri avec effet Doppler pendant le vol
                    if (this.grabvillager$thrownTicks % 8 == 0) {
                        float progress = Math.min(1.0f, (100 - this.grabvillager$thrownTicks) / 75.0f);
                        float dopplerPitch = Math.max(0.55f, 1.4f - (progress * 0.8f));
                        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                                GrabVillagerLogic.getFlightSound(this), this.getSoundSource(),
                                0.9f, dopplerPitch);
                    }

                    // Si les 100 ticks sont écoulés mais que l'entité est toujours dans le vide (chute de falaise), on prolonge la protection
                    if (this.grabvillager$thrownTicks == 0 && this.getY() > this.level().getMinY()) {
                        this.grabvillager$thrownTicks = 1;
                    }
                } else {
                    // Atterrissage en douceur
                    this.resetFallDistance();
                    serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.2, this.getZ(), 6, 0.2, 0.05, 0.2, 0.03);
                    serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                            GrabVillagerLogic.getLandingSound(this), this.getSoundSource(),
                            1.0f, 1.1f);
                    this.grabvillager$thrownTicks = 0;
                }
            }
        }
    }
}