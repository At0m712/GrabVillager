package atom.grabvillager.client;

import atom.grabvillager.logic.GrabVillagerLogic;
import net.minecraft.client.Minecraft;

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

        boolean isDown = GrabVillagerClient.dropKey.isDown();

        if (isDown) {
            ticksHeld++;
            chargeProgress = Math.min(1.0f, ticksHeld / 20.0f);
        } else if (wasDown) {

            boolean isThrow = ticksHeld > 5;
            float finalCharge = isThrow ? chargeProgress : 0.0f;

            if (isThrow) {
                throwAnimTicks = MAX_THROW_ANIM_TICKS;
            }

            callback.execute(isThrow, finalCharge);


            ticksHeld = 0;
            chargeProgress = 0.0f;
        }

        wasDown = isDown;
    }

}
