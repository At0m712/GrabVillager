package com.atom.grabvillager.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

public class GrabVillagerOverlay {

    public static void render(GuiGraphics guiGraphics) {
        int ticksHeld = GrabVillagerClientLogic.getTicksHeld();

        // On n'affiche la jauge que si la touche est maintenue
        if (ticksHeld > 0) {
            Minecraft mc = Minecraft.getInstance();
            if (mc.getWindow() == null) return;

            int screenWidth = mc.getWindow().getGuiScaledWidth();
            int screenHeight = mc.getWindow().getGuiScaledHeight();

            // Dimensions et position de la jauge (sous le réticule)
            int barWidth = 60;
            int barHeight = 5;
            int x = (screenWidth - barWidth) / 2;
            int y = (screenHeight / 2) + 15;

            // Calcul du remplissage
            float charge = Math.min(ticksHeld / 20.0f, 1.0f);
            int fillWidth = (int) (barWidth * charge);

            // Dessin du fond (Gris foncé semi-transparent, format ARGB)
            guiGraphics.fill(x, y, x + barWidth, y + barHeight, 0x80000000);

            // Dessin de la progression (Jaune en charge, Vert si charge max)
            int color = charge >= 1.0f ? 0xFF00FF00 : 0xFFFFFF00;
            guiGraphics.fill(x, y, x + fillWidth, y + barHeight, color);
        }
    }
}