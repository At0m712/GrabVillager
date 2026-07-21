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

            // SOLUTION 1.21.6 : Mathématiques pures au lieu des matrices !
            // On calcule les dimensions finales avec l'échelle (barSize).
            float scale = GrabVillagerConfig.barSize;
            int scaledW = Math.round(baseW * scale);
            int scaledH = Math.round(baseH * scale);

            // On calcule le point de dessin pour rester centré malgré le redimensionnement
            int drawX = Math.round(x + (baseW / 2.0f) - (scaledW / 2.0f));
            int drawY = Math.round(y + (baseH / 2.0f) - (scaledH / 2.0f));

            // Contour noir de la jauge
            guiGraphics.fill(drawX - 1, drawY - 1, drawX + scaledW + 1, drawY + scaledH + 1, 0xFF000000);

            // Fond gris de la jauge vide
            guiGraphics.fill(drawX, drawY, drawX + scaledW, drawY + scaledH, 0xFF222222);

            // Remplissage progressif
            if (charge > 0) {
                if (isVert) {
                    int fillH = Math.round(scaledH * charge);
                    int splitW = Math.round((baseW - 2) * scale);

                    guiGraphics.fill(drawX, drawY + scaledH - fillH, drawX + splitW, drawY + scaledH, topColor);
                    guiGraphics.fill(drawX + splitW, drawY + scaledH - fillH, drawX + scaledW, drawY + scaledH, bottomColor);
                } else {
                    int fillW = Math.round(scaledW * charge);
                    int splitH = Math.round((baseH - 2) * scale);

                    guiGraphics.fill(drawX, drawY, drawX + fillW, drawY + splitH, topColor);
                    guiGraphics.fill(drawX, drawY + splitH, drawX + fillW, drawY + scaledH, bottomColor);
                }
            }
        }
    }
}