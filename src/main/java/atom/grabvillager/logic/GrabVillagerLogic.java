package atom.grabvillager.logic;

import atom.grabvillager.config.GrabVillagerConfig;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.network.protocol.game.ClientboundSetPassengersPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class GrabVillagerLogic {
    public static java.util.UUID clientPlayerId = null;
    public static float clientChargeProgress = 0.0f;

    private static final Map<UUID, Long> LAST_ACTION_TIMES = new ConcurrentHashMap<>();

    public static final TagKey<EntityType<?>> GRABBABLE_TAG = TagKey.create(
            Registries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath("grabvillager", "grabbable")
    );

    public static boolean isGrabbable(Entity entity) {
        if (entity == null) return false;
        try {
            if (entity.getType().builtInRegistryHolder().is(GRABBABLE_TAG)) return true;
        } catch (Exception ignored) {}
        return entity instanceof Villager || entity instanceof WanderingTrader || entity instanceof ZombieVillager;
    }

    public static SoundEvent getSurpriseSound(Entity entity) {
        if (entity instanceof ZombieVillager) return SoundEvents.ZOMBIE_VILLAGER_AMBIENT;
        if (entity instanceof WanderingTrader) return SoundEvents.WANDERING_TRADER_NO;
        return SoundEvents.VILLAGER_NO;
    }

    public static SoundEvent getThrowSound(Entity entity) {
        if (entity instanceof ZombieVillager) return SoundEvents.ZOMBIE_VILLAGER_HURT;
        if (entity instanceof WanderingTrader) return SoundEvents.WANDERING_TRADER_HURT;
        return SoundEvents.VILLAGER_NO;
    }

    public static SoundEvent getFlightSound(Entity entity) {
        if (entity instanceof ZombieVillager) return SoundEvents.ZOMBIE_VILLAGER_AMBIENT;
        if (entity instanceof WanderingTrader) return SoundEvents.WANDERING_TRADER_NO;
        return SoundEvents.VILLAGER_NO;
    }

    public static SoundEvent getLandingSound(Entity entity) {
        if (entity instanceof ZombieVillager) return SoundEvents.ZOMBIE_VILLAGER_AMBIENT;
        if (entity instanceof WanderingTrader) return SoundEvents.WANDERING_TRADER_YES;
        return SoundEvents.VILLAGER_YES;
    }

    public static boolean isCarryingVillager(Player player) {
        return !player.getPassengers().isEmpty() && isGrabbable(player.getFirstPassenger());
    }

    public static boolean shouldBlockActions(Player player) {
        return isCarryingVillager(player) && !GrabVillagerConfig.allowTools;
    }

    public static boolean isOwnPassenger(Player player, Entity target) {
        return player.hasPassenger(target);
    }

    public static InteractionResult tryGrab(Player player, Entity target, InteractionHand hand) {
        if (hand == InteractionHand.MAIN_HAND && isGrabbable(target)) {

            // S'accroupir (Shift) est obligatoire pour attraper, sinon on laisse passer l'interaction (commerce)
            if (!player.isCrouching() && !player.isShiftKeyDown()) {
                return InteractionResult.PASS;
            }

            // Sécurités de base : spectateur, entité morte/supprimée ou portée excessive
            if (player.isSpectator() || target.isRemoved() || !target.isAlive()) {
                return InteractionResult.PASS;
            }
            if (player.distanceToSqr(target) > 25.0) { // Max 5 blocs
                return InteractionResult.PASS;
            }

            // Protection anti-griefing & respect des claims (WorldGuard, FTB Chunks, etc.)
            if (!player.level().mayInteract(player, target.blockPosition())) {
                return InteractionResult.FAIL;
            }
            if (target.getVehicle() != null && !player.level().mayInteract(player, target.getVehicle().blockPosition())) {
                return InteractionResult.FAIL;
            }

            if (player.getPassengers().isEmpty()) {
                if (!player.level().isClientSide() && target.getVehicle() != null) {
                    target.stopRiding();
                }

                // Extinction immédiate si le zombie villageois brûle au soleil
                if (target.isOnFire()) {
                    target.clearFire();
                }

                boolean success = target.startRiding(player, true, true);

                if (success && !player.level().isClientSide() && player instanceof ServerPlayer serverPlayer) {
                    ClientboundSetPassengersPacket packet = new ClientboundSetPassengersPacket(player);
                    serverPlayer.level().getChunkSource().sendToTrackingPlayersAndSelf(player, packet);

                    // Son de surprise à l'attrapage : Un petit "Huuuuh ?" surpris et aigu dès qu'on le soulève.
                    serverPlayer.level().playSound(null, target.getX(), target.getY(), target.getZ(),
                            getSurpriseSound(target), target.getSoundSource(), 1.0f, 1.45f);
                }

                return success ? InteractionResult.SUCCESS : InteractionResult.PASS;
            }
        }
        return InteractionResult.PASS;
    }

    public static void handleDropOrThrow(ServerPlayer player, boolean isThrow, float charge) {
        handleDropOrThrow(player, isThrow, charge, GrabVillagerConfig.throwMultiplier);
    }

    public static void handleDropOrThrow(ServerPlayer player, boolean isThrow, float charge, float clientMultiplier) {
        if (!isCarryingVillager(player)) {
            return;
        }

        // Anti-spam et rate-limiting des paquets (200ms de cooldown)
        long now = System.currentTimeMillis();
        Long lastTime = LAST_ACTION_TIMES.get(player.getUUID());
        if (lastTime != null && now - lastTime < 200) {
            return;
        }
        LAST_ACTION_TIMES.put(player.getUUID(), now);

        charge = Math.max(0.0f, Math.min(1.0f, charge));

        Entity passenger = player.getFirstPassenger();
        Vec3 look = player.getLookAngle();

        passenger.stopRiding();

        ClientboundSetPassengersPacket passPacket = new ClientboundSetPassengersPacket(player);
        player.level().getChunkSource().sendToTrackingPlayersAndSelf(player, passPacket);

        // Raycast anti-noclip : vérifie s'il y a un obstacle devant le joueur
        Vec3 eyePos = player.getEyePosition();
        Vec3 targetPos = eyePos.add(look.scale(0.8));
        HitResult hit = player.level().clip(new ClipContext(eyePos, targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 dropPos;
        if (hit.getType() != HitResult.Type.MISS) {
            dropPos = hit.getLocation().subtract(look.scale(0.2));
        } else {
            dropPos = targetPos;
        }

        double spawnX = dropPos.x;
        double spawnY = Math.max(player.getY(), dropPos.y - (passenger.getBbHeight() * 0.5));
        double spawnZ = dropPos.z;

        passenger.setPos(spawnX, spawnY, spawnZ);

        // Sécurité anti-suffocation : si la boîte de collision intersecte un mur, on dépose en sécurité sur le joueur
        if (!player.level().noCollision(passenger, passenger.getBoundingBox())) {
            passenger.setPos(player.getX(), player.getY(), player.getZ());
        }

        passenger.setYRot(player.getYRot());
        passenger.setXRot(player.getXRot());

        if (isThrow) {
            // Validation et synchronisation de la puissance choisie par le client, plafonnée par le serveur
            float safeClientMult = Math.max(0.1f, Math.min(3.0f, clientMultiplier));
            float serverCap = Math.max(0.1f, Math.min(3.0f, GrabVillagerConfig.throwMultiplier));
            float multiplier = Math.min(safeClientMult, serverCap);

            float velocity = (0.5f + (charge * 1.2f)) * multiplier;
            Vec3 movement = new Vec3(look.x * velocity, (look.y * velocity) + 0.5D, look.z * velocity);
            passenger.setDeltaMovement(movement);

            if (passenger instanceof IThrownVillager thrownVillager) {
                thrownVillager.grabvillager$setThrownTicks(100);
            }

            player.level().playSound(null, player.getX(), player.getY(), player.getZ(),
                    getThrowSound(passenger), passenger.getSoundSource(),
                    1.0f, 1.2f + (charge * 0.3f));
        } else {
            passenger.setDeltaMovement(Vec3.ZERO);
        }

        passenger.syncVelocity = true;
        passenger.setOnGround(false);

        ClientboundSetEntityMotionPacket motionPacket = new ClientboundSetEntityMotionPacket(passenger);
        player.level().getChunkSource().sendToTrackingPlayersAndSelf(passenger, motionPacket);
    }
}