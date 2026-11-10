package atom.grabvillager.mixin;

import atom.grabvillager.logic.GrabVillagerLogic;
import atom.grabvillager.logic.IThrownVillager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;
import java.util.UUID;

@Mixin(LivingEntity.class)
public abstract class VillagerFlightMixin extends Entity implements IThrownVillager {

    @Unique
    private int grabvillager$thrownTicks = 0;
    @Unique
    private int grabvillager$flightTotalTicks = 0;
    @Unique
    private UUID grabvillager$thrower = null;
    @Unique
    private boolean grabvillager$hasBounced = false;

    protected VillagerFlightMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void grabvillager$setThrownTicks(int ticks) {
        this.grabvillager$thrownTicks = ticks;
        this.grabvillager$flightTotalTicks = 0;
        this.grabvillager$hasBounced = false;
    }

    @Override
    public int grabvillager$getThrownTicks() {
        return this.grabvillager$thrownTicks;
    }

    @Override
    public void grabvillager$setThrower(UUID uuid) {
        this.grabvillager$thrower = uuid;
    }

    @Override
    public UUID grabvillager$getThrower() {
        return this.grabvillager$thrower;
    }

    @Unique
    private boolean grabvillager$isSoftBlock(BlockState state, boolean inWater) {
        if (inWater) return true;
        if (state.is(Blocks.HAY_BLOCK)) return true;
        if (state.is(Blocks.MUD)) return true;
        if (state.is(Blocks.SLIME_BLOCK) || state.is(Blocks.HONEY_BLOCK)) return true;
        if (state.is(BlockTags.BEDS)) return true;
        if (state.is(BlockTags.WOOL)) return true;
        return false;
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
            this.grabvillager$flightTotalTicks++;

            if (!this.level().isClientSide() && this.level() instanceof ServerLevel serverLevel) {
                // Réinitialise la distance de chute continuellement pendant toute la durée du vol
                this.resetFallDistance();

                boolean isBaby = (Object) this instanceof LivingEntity living && living.isBaby();

                if (!this.onGround() && !this.isInWater() && !this.isInLava() && this.getVehicle() == null) {
                    // 🤾 Passe à un autre joueur (Catch Mechanic) : Un joueur les mains vides attrape le villageois au vol
                    if (this.grabvillager$flightTotalTicks >= 3) {
                        AABB catchBox = this.getBoundingBox().inflate(0.75, 0.5, 0.75);
                        List<ServerPlayer> nearbyCatchers = serverLevel.getEntitiesOfClass(ServerPlayer.class, catchBox, p -> {
                            if (!p.isAlive() || p.isSpectator()) return false;
                            // Le lanceur ne peut pas rattraper son propre tir avant un court délai (15 ticks)
                            if (this.grabvillager$thrower != null && p.getUUID().equals(this.grabvillager$thrower) && this.grabvillager$flightTotalTicks < 15) {
                                return false;
                            }
                            // Doit avoir les mains vides
                            if (!p.getMainHandItem().isEmpty() || !p.getOffhandItem().isEmpty()) {
                                return false;
                            }
                            // Ne doit pas déjà porter quelqu'un
                            if (!p.getPassengers().isEmpty()) {
                                return false;
                            }
                            return serverLevel.mayInteract(p, p.blockPosition());
                        });

                        if (!nearbyCatchers.isEmpty()) {
                            ServerPlayer catcher = nearbyCatchers.get(0);
                            this.setDeltaMovement(Vec3.ZERO);
                            boolean caught = this.startRiding(catcher, true, true);
                            if (caught) {
                                this.grabvillager$thrownTicks = 0;
                                this.grabvillager$flightTotalTicks = 0;
                                this.grabvillager$hasBounced = false;

                                ClientboundSetPassengersPacket passengerPacket = new ClientboundSetPassengersPacket(catcher);
                                serverLevel.getChunkSource().sendToTrackingPlayersAndSelf(catcher, passengerPacket);

                                serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, catcher.getX(), catcher.getY() + 1.2, catcher.getZ(), 8, 0.25, 0.25, 0.25, 0.05);
                                serverLevel.sendParticles(ParticleTypes.HEART, catcher.getX(), catcher.getY() + 1.5, catcher.getZ(), 3, 0.2, 0.2, 0.2, 0.02);

                                serverLevel.playSound(null, catcher.getX(), catcher.getY(), catcher.getZ(),
                                        SoundEvents.ITEM_PICKUP, catcher.getSoundSource(), 1.0f, 1.2f);
                                float catchPitch = isBaby ? 1.6f : 1.3f;
                                serverLevel.playSound(null, catcher.getX(), catcher.getY(), catcher.getZ(),
                                        SoundEvents.VILLAGER_YES, this.getSoundSource(), 1.0f, catchPitch);
                                return;
                            }
                        }
                    }

                    // Traînée de particules de vol
                    serverLevel.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.4, this.getZ(), 2, 0.08, 0.08, 0.08, 0.01);
                    serverLevel.sendParticles(ParticleTypes.CRIT, this.getX(), this.getY() + 0.5, this.getZ(), 1, 0.08, 0.08, 0.08, 0.04);

                    if (isBaby) {
                        // Particules festives pour les bébés villageois ravis de voler
                        serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.4, this.getZ(), 2, 0.12, 0.12, 0.12, 0.02);
                        if (this.random.nextFloat() < 0.35f) {
                            serverLevel.sendParticles(ParticleTypes.HEART, this.getX(), this.getY() + 0.5, this.getZ(), 1, 0.08, 0.08, 0.08, 0.01);
                        }
                    } else if (this.random.nextFloat() < 0.25f) {
                        if ((Object) this instanceof ZombieVillager) {
                            serverLevel.sendParticles(ParticleTypes.SMOKE, this.getX(), this.getY() + 0.6, this.getZ(), 1, 0.05, 0.05, 0.05, 0.02);
                        } else {
                            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.6, this.getZ(), 1, 0.05, 0.05, 0.05, 0.02);
                        }
                    }

                    // 🔊 Cri avec effet Doppler ou rires festifs pour les bébés
                    if (this.grabvillager$thrownTicks % 8 == 0) {
                        if (isBaby) {
                            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                                    GrabVillagerLogic.getFlightSound(this), this.getSoundSource(),
                                    0.9f, 1.5f + (this.random.nextFloat() * 0.3f));
                        } else {
                            float progress = Math.min(1.0f, (100 - this.grabvillager$thrownTicks) / 75.0f);
                            float dopplerPitch = Math.max(0.55f, 1.4f - (progress * 0.8f));
                            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                                    GrabVillagerLogic.getFlightSound(this), this.getSoundSource(),
                                    0.9f, dopplerPitch);
                        }
                    }

                    // Si les 100 ticks sont écoulés mais que l'entité est toujours dans le vide, on prolonge la protection (max 300 ticks / 15s)
                    if (this.grabvillager$thrownTicks == 0 && this.getY() > this.level().getMinY() && this.grabvillager$flightTotalTicks < 300) {
                        this.grabvillager$thrownTicks = 1;
                    }
                } else {
                    // Vérification de l'amorti sur blocs mous (Botte de foin, Lit, Eau, Boue, Slime...)
                    boolean inWater = this.isInWater() || this.level().getFluidState(this.blockPosition()).is(FluidTags.WATER);
                    BlockPos feet = this.blockPosition();
                    BlockState stateAt = this.level().getBlockState(feet);
                    BlockState stateBelow = this.level().getBlockState(feet.below());
                    boolean isSoft = grabvillager$isSoftBlock(stateAt, inWater) || grabvillager$isSoftBlock(stateBelow, inWater);

                    if (!this.grabvillager$hasBounced && isSoft) {
                        this.grabvillager$hasBounced = true;
                        this.resetFallDistance();

                        Vec3 motion = this.getDeltaMovement();
                        double bounceY;
                        if (stateAt.is(BlockTags.BEDS) || stateBelow.is(BlockTags.BEDS) || stateAt.is(Blocks.SLIME_BLOCK) || stateBelow.is(Blocks.SLIME_BLOCK)) {
                            bounceY = 0.48D;
                            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_SQUISH, this.getSoundSource(), 1.0f, 1.2f);
                            serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, this.getX(), this.getY() + 0.2, this.getZ(), 8, 0.2, 0.1, 0.2, 0.05);
                        } else if (inWater) {
                            bounceY = 0.28D;
                            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_SPLASH, this.getSoundSource(), 0.9f, 1.2f);
                            serverLevel.sendParticles(ParticleTypes.SPLASH, this.getX(), this.getY() + 0.3, this.getZ(), 10, 0.25, 0.1, 0.25, 0.05);
                            serverLevel.sendParticles(ParticleTypes.BUBBLE, this.getX(), this.getY() + 0.2, this.getZ(), 6, 0.2, 0.1, 0.2, 0.02);
                        } else if (stateAt.is(Blocks.HAY_BLOCK) || stateBelow.is(Blocks.HAY_BLOCK)) {
                            bounceY = 0.36D;
                            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GRASS_FALL, this.getSoundSource(), 1.0f, 1.0f);
                            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.3, this.getZ(), 6, 0.2, 0.1, 0.2, 0.02);
                            serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.2, this.getZ(), 4, 0.15, 0.05, 0.15, 0.02);
                        } else if (stateAt.is(Blocks.MUD) || stateBelow.is(Blocks.MUD)) {
                            bounceY = 0.26D;
                            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.MUD_FALL, this.getSoundSource(), 1.0f, 1.1f);
                            serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.2, this.getZ(), 5, 0.15, 0.05, 0.15, 0.02);
                        } else {
                            bounceY = 0.34D;
                            serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.WOOL_FALL, this.getSoundSource(), 1.0f, 1.1f);
                            serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.2, this.getZ(), 4, 0.15, 0.05, 0.15, 0.02);
                        }

                        this.setDeltaMovement(motion.x * 0.5D, bounceY, motion.z * 0.5D);
                        this.syncVelocity = true;
                        this.setOnGround(false);
                        ClientboundSetEntityMotionPacket motionPacket = new ClientboundSetEntityMotionPacket(this);
                        serverLevel.getChunkSource().sendToTrackingPlayersAndSelf(this, motionPacket);

                        this.grabvillager$thrownTicks = 16;

                        float bouncePitch = isBaby ? 1.7f : 1.35f;
                        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                                SoundEvents.VILLAGER_YES, this.getSoundSource(),
                                1.0f, bouncePitch);
                    } else {
                        // Atterrissage classique au sol
                        this.resetFallDistance();
                        serverLevel.sendParticles(ParticleTypes.POOF, this.getX(), this.getY() + 0.2, this.getZ(), 6, 0.2, 0.05, 0.2, 0.03);
                        if (isBaby) {
                            serverLevel.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + 0.3, this.getZ(), 6, 0.15, 0.15, 0.15, 0.02);
                        }
                        float landPitch = isBaby ? 1.6f : 1.1f;
                        serverLevel.playSound(null, this.getX(), this.getY(), this.getZ(),
                                GrabVillagerLogic.getLandingSound(this), this.getSoundSource(),
                                1.0f, landPitch);
                        this.grabvillager$thrownTicks = 0;
                        this.grabvillager$flightTotalTicks = 0;
                        this.grabvillager$hasBounced = false;
                    }
                }
            }
        }
    }
}
