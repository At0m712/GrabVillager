package com.atom.grabvillager.client;

import com.atom.grabvillager.config.GrabVillagerConfig;
import com.atom.grabvillager.logic.GrabVillagerLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;

public class GrabVillagerOverlay {

    public static void render(GuiGraphics guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || !GrabVillagerLogic.isCarryingVillager(player)) return;

        int ticksHeld = GrabVillagerClientLogic.getTicksHeld();

        if (ticksHeld > 0 && mc.getWindow() != null) {
            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            float charge = Math.min(ticksHeld / 20.0f, 1.0f);
            boolean isVert = GrabVillagerConfig.barVertical;

            int baseW = isVert ? 5 : 182;
            int baseH = isVert ? 182 : 5;

            // 1. Point de départ par défaut (Horizontale)
            int baseX = (screenWidth - baseW) / 2;
            int baseY = screenHeight - 39; // 29 (inventaire) + 10

            // 2. Point de départ spécifique (Verticale)
            if (isVert) {
                baseX += 200;
                baseY -= 180;
            }

            // 3. Application des offsets de personnalisation
            int x = baseX + GrabVillagerConfig.barOffsetX;
            int y = baseY + GrabVillagerConfig.barOffsetY;

            int topColor, bottomColor;
            switch (GrabVillagerConfig.barColorIndex) {
                case 1: topColor = charge >= 1.0f ? 0xFF00FFFF : 0xFF0088FF; bottomColor = charge >= 1.0f ? 0xFF00AAAA : 0xFF0044AA; break;
                case 2: topColor = charge >= 1.0f ? 0xFFFF4444 : 0xFFFF8888; bottomColor = charge >= 1.0f ? 0xFFAA0000 : 0xFFAA4444; break;
                case 3: topColor = charge >= 1.0f ? 0xFFFF00FF : 0xFFAA00AA; bottomColor = charge >= 1.0f ? 0xFFAA00AA : 0xFF550055; break;
                default: topColor = charge >= 1.0f ? 0xFF80FF20 : 0xFFDDDD22; bottomColor = charge >= 1.0f ? 0xFF408010 : 0xFFAA8811; break;
            }

            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(x + (baseW / 2.0f), y + (baseH / 2.0f), 0);
            guiGraphics.pose().scale(GrabVillagerConfig.barSize, GrabVillagerConfig.barSize, 1.0f);
            guiGraphics.pose().translate(-(baseW / 2.0f), -(baseH / 2.0f), 0);

            guiGraphics.fill(-1, -1, baseW + 1, baseH + 1, 0xFF000000);
            guiGraphics.fill(0, 0, baseW, baseH, 0xFF222222);

            if (charge > 0) {
                if (isVert) {
                    int fillH = (int) (baseH * charge);
                    guiGraphics.fill(0, baseH - fillH, baseW - 2, baseH, topColor);
                    guiGraphics.fill(baseW - 2, baseH - fillH, baseW, baseH, bottomColor);
                } else {
                    int fillW = (int) (baseW * charge);
                    guiGraphics.fill(0, 0, fillW, baseH - 2, topColor);
                    guiGraphics.fill(0, baseH - 2, fillW, baseH, bottomColor);
                }
            }

            guiGraphics.pose().popPose();
        }
    }
}