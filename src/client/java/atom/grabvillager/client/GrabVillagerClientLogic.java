package atom.grabvillager.client;

import atom.grabvillager.config.GrabVillagerConfig;
import atom.grabvillager.logic.GrabVillagerLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class GrabVillagerClientLogic {

    public static final int MAX_THROW_ANIM_TICKS = 10;
    public static int throwAnimTicks = 0;

    private static float chargeProgress = 0.0f;
    private static int ticksHeld = 0;
    private static boolean wasDown = false;

    public static float getChargeProgress() {
        return chargeProgress;
    }

    public static void setChargeProgress(float progress) {
        chargeProgress = progress;
    }

    public static int getTicksHeld() {
        return ticksHeld;
    }

    public static void setTicksHeld(int ticks) {
        ticksHeld = ticks;
    }

    public interface DropCallback {
        void execute(boolean isThrow, float charge);
    }

    public static void tick(DropCallback callback) {
        if (throwAnimTicks > 0) {
            throwAnimTicks--;
        }

        Minecraft client = Minecraft.getInstance();
        if (client.player == null) return;

        GrabVillagerLogic.clientPlayerId = client.player.getUUID();
        GrabVillagerLogic.clientChargeProgress = chargeProgress;

        boolean isCarrying = GrabVillagerLogic.isCarryingVillager(client.player);
        boolean isDown = isCarrying && GrabVillagerClient.dropKey.isDown();

        if (isDown) {
            ticksHeld++;
            chargeProgress = Math.min(1.0f, ticksHeld / 20.0f);

            // Prévisualisation de trajectoire en pointillés pendant la charge du lancer
            if (ticksHeld >= 3 && client.level != null) {
                renderTrajectoryPreview(client.player, client.level, chargeProgress);
            }
        } else if (wasDown) {
            boolean isThrow = ticksHeld > 5;
            float finalCharge = isThrow ? chargeProgress : 0.0f;

            if (isThrow) {
                throwAnimTicks = MAX_THROW_ANIM_TICKS;
            }

            callback.execute(isThrow, finalCharge);

            ticksHeld = 0;
            chargeProgress = 0.0f;
        } else {
            ticksHeld = 0;
            chargeProgress = 0.0f;
        }

        wasDown = isDown;
    }

    private static void renderTrajectoryPreview(LocalPlayer player, Level level, float charge) {
        Vec3 eyePos = player.getEyePosition();
        Vec3 look = player.getLookAngle();
        Vec3 targetPos = eyePos.add(look.scale(0.8));
        HitResult hit = level.clip(new ClipContext(eyePos, targetPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 dropPos = (hit.getType() != HitResult.Type.MISS) ? hit.getLocation().subtract(look.scale(0.2)) : targetPos;

        Entity passenger = player.getFirstPassenger();
        double bbHeight = passenger != null ? passenger.getBbHeight() : 1.95;
        double spawnY = Math.max(player.getY(), dropPos.y - (bbHeight * 0.5));
        Vec3 simPos = new Vec3(dropPos.x, spawnY, dropPos.z);

        float safeClientMult = Math.max(0.1f, Math.min(3.0f, GrabVillagerConfig.throwMultiplier));
        float velocity = (0.5f + (charge * 1.2f)) * safeClientMult;
        Vec3 simVel = new Vec3(look.x * velocity, (look.y * velocity) + 0.5D, look.z * velocity);

        for (int step = 0; step < 30; step++) {
            Vec3 nextPos = simPos.add(simVel);
            HitResult blockHit = level.clip(new ClipContext(simPos, nextPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            Vec3 stepEnd = (blockHit.getType() != HitResult.Type.MISS) ? blockHit.getLocation() : nextPos;

            double dist = simPos.distanceTo(stepEnd);
            int subSteps = Math.max(1, (int) Math.ceil(dist / 0.7));
            for (int s = 0; s < subSteps; s++) {
                double t = (double) s / (double) subSteps;
                double px = simPos.x + (stepEnd.x - simPos.x) * t;
                double py = simPos.y + (stepEnd.y - simPos.y) * t + (bbHeight * 0.4);
                double pz = simPos.z + (stepEnd.z - simPos.z) * t;

                level.addParticle(ParticleTypes.CRIT, px, py, pz, 0, 0, 0);
            }

            if (blockHit.getType() != HitResult.Type.MISS) {
                Vec3 impact = blockHit.getLocation();
                level.addParticle(ParticleTypes.HAPPY_VILLAGER, impact.x, impact.y + 0.1, impact.z, 0, 0.02, 0);
                level.addParticle(ParticleTypes.END_ROD, impact.x, impact.y + 0.05, impact.z, 0, 0, 0);
                break;
            }

            simPos = nextPos;
            simVel = new Vec3(simVel.x * 0.91, (simVel.y - 0.08) * 0.98, simVel.z * 0.91);
        }
    }
}
