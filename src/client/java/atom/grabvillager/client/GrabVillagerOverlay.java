package atom.grabvillager.client;

import atom.grabvillager.config.GrabVillagerConfig;
import atom.grabvillager.logic.GrabVillagerLogic;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.zombie.ZombieVillager;
import net.minecraft.world.entity.npc.villager.Villager;
import net.minecraft.world.entity.npc.villager.VillagerData;
import net.minecraft.world.entity.npc.villager.VillagerProfession;
import net.minecraft.world.entity.npc.wanderingtrader.WanderingTrader;

public class GrabVillagerOverlay {

    public static void render(GuiGraphicsExtractor guiGraphics) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;

        if (player == null || mc.getWindow() == null || !GrabVillagerLogic.isCarryingVillager(player)) {
            return;
        }

        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 1. Discreet Carried Villager HUD Badge above the hotbar
        renderCarriedBadge(guiGraphics, mc.font, player, screenWidth, screenHeight);

        // 2. Throw Charge Gauge when holding the drop/throw key
        int ticksHeld = GrabVillagerClientLogic.getTicksHeld();
        if (ticksHeld > 0) {
            renderChargeGauge(guiGraphics, screenWidth, screenHeight, ticksHeld);
        }
    }

    private static void renderCarriedBadge(GuiGraphicsExtractor guiGraphics, Font font, LocalPlayer player, int screenWidth, int screenHeight) {
        Entity passenger = player.getFirstPassenger();
        if (passenger == null) return;

        boolean isBaby = passenger instanceof LivingEntity living && living.isBaby();
        Component label;
        int accentColor;

        if (passenger instanceof ZombieVillager zombieVillager) {
            accentColor = 0xFF55AA55;
            if (isBaby) {
                label = Component.translatable("text.grabvillager.hud.baby_zombie_villager");
            } else {
                VillagerData data = zombieVillager.getVillagerData();
                Holder<VillagerProfession> profHolder = data.profession();
                Component base = Component.translatable("text.grabvillager.hud.zombie_villager");
                if (!profHolder.is(VillagerProfession.NONE) && !profHolder.is(VillagerProfession.NITWIT)) {
                    label = base.copy()
                            .append(Component.literal(" • ").withColor(0x888888))
                            .append(profHolder.value().name().copy().withColor(0xA5D6A7));
                } else {
                    label = base;
                }
            }
        } else if (passenger instanceof Villager villager) {
            if (isBaby) {
                label = Component.translatable("text.grabvillager.hud.baby_villager");
                accentColor = 0xFFFFD966;
            } else {
                VillagerData data = villager.getVillagerData();
                Holder<VillagerProfession> profHolder = data.profession();
                if (profHolder.is(VillagerProfession.NONE)) {
                    label = Component.translatable("text.grabvillager.hud.unemployed");
                    accentColor = 0xFFAAAAAA;
                } else if (profHolder.is(VillagerProfession.NITWIT)) {
                    label = profHolder.value().name();
                    accentColor = 0xFF55AA55;
                } else {
                    int level = Math.max(1, Math.min(5, data.level()));
                    int tierColor = getVillagerTierColor(level);
                    Component profName = profHolder.value().name();
                    Component levelName = Component.translatable("merchant.level." + level);
                    label = profName.copy()
                            .append(Component.literal(" • ").withColor(0x888888))
                            .append(levelName.copy().withColor(tierColor));
                    accentColor = tierColor;
                }
            }
        } else if (passenger instanceof WanderingTrader) {
            label = passenger.getType().getDescription();
            accentColor = 0xFF4FC3F7;
        } else {
            if (isBaby) {
                label = Component.translatable("text.grabvillager.hud.baby_prefix")
                        .append(" ")
                        .append(passenger.getType().getDescription());
                accentColor = 0xFFFFD966;
            } else {
                label = passenger.getDisplayName();
                accentColor = 0xFFD7CCC8;
            }
        }

        // Health / injured indicator
        boolean injured = false;
        String hpText = null;
        int hpColor = 0xFFFF5555;
        if (passenger instanceof LivingEntity living && living.getHealth() < living.getMaxHealth() - 0.05f) {
            injured = true;
            int hp = Math.max(1, (int) Math.ceil(living.getHealth()));
            int maxHp = Math.max(1, (int) Math.ceil(living.getMaxHealth()));
            hpText = hp + "/" + maxHp;
            float ratio = living.getHealth() / Math.max(1.0f, living.getMaxHealth());
            hpColor = ratio <= 0.35f ? 0xFFFF5555 : (ratio <= 0.7f ? 0xFFFFAA00 : 0xFFFF7777);
        }

        int labelWidth = font.width(label);
        int injuredWidth = injured ? (6 + 7 + 3 + font.width(hpText)) : 0;
        int pillW = 5 + 2 + 4 + labelWidth + injuredWidth + 5;
        int pillH = 14;

        int pillX = (screenWidth - pillW) / 2;
        int pillY = screenHeight - 58;

        // Discreet dark backdrop with subtle border
        guiGraphics.fill(pillX, pillY, pillX + pillW, pillY + pillH, 0x90000000);
        guiGraphics.fill(pillX, pillY, pillX + pillW, pillY + 1, 0x38FFFFFF);
        guiGraphics.fill(pillX, pillY + pillH - 1, pillX + pillW, pillY + pillH, 0x38FFFFFF);
        guiGraphics.fill(pillX, pillY + 1, pillX + 1, pillY + pillH - 1, 0x38FFFFFF);
        guiGraphics.fill(pillX + pillW - 1, pillY + 1, pillX + pillW, pillY + pillH - 1, 0x38FFFFFF);

        // Left tier/type accent strip
        guiGraphics.fill(pillX + 3, pillY + 3, pillX + 5, pillY + pillH - 3, accentColor);

        // Label text
        int textX = pillX + 9;
        int textY = pillY + 3;
        guiGraphics.text(font, label, textX, textY, 0xFFFFFFFF);

        // Injured heart icon + HP
        if (injured) {
            int heartX = textX + labelWidth + 6;
            int heartY = pillY + 4;
            drawMiniHeart(guiGraphics, heartX, heartY, hpColor);
            guiGraphics.text(font, hpText, heartX + 10, textY, hpColor);
        }
    }

    private static int getVillagerTierColor(int level) {
        return switch (level) {
            case 2 -> 0xFFE8E8E8; // Iron
            case 3 -> 0xFFFFD54F; // Gold
            case 4 -> 0xFF55FF55; // Emerald
            case 5 -> 0xFF55FFFF; // Diamond
            default -> 0xFFB8B8B8; // Novice (Stone)
        };
    }

    private static void drawMiniHeart(GuiGraphicsExtractor guiGraphics, int x, int y, int color) {
        int shadow = 0xCC220000;
        // Shadow / subtle backing
        guiGraphics.fill(x + 1, y + 1, x + 3, y + 2, shadow);
        guiGraphics.fill(x + 4, y + 1, x + 6, y + 2, shadow);
        guiGraphics.fill(x, y + 2, x + 7, y + 4, shadow);
        guiGraphics.fill(x + 1, y + 4, x + 6, y + 5, shadow);
        guiGraphics.fill(x + 2, y + 5, x + 5, y + 6, shadow);
        guiGraphics.fill(x + 3, y + 6, x + 4, y + 7, shadow);

        // Crisp 7x6 pixel heart
        guiGraphics.fill(x + 1, y, x + 3, y + 1, color);
        guiGraphics.fill(x + 4, y, x + 6, y + 1, color);
        guiGraphics.fill(x, y + 1, x + 7, y + 3, color);
        guiGraphics.fill(x + 1, y + 3, x + 6, y + 4, color);
        guiGraphics.fill(x + 2, y + 4, x + 5, y + 5, color);
        guiGraphics.fill(x + 3, y + 5, x + 4, y + 6, color);
        // Tiny highlight pixel
        guiGraphics.fill(x + 1, y + 1, x + 2, y + 2, 0xFFFFCCCC);
    }

    private static void renderChargeGauge(GuiGraphicsExtractor guiGraphics, int screenWidth, int screenHeight, int ticksHeld) {
        float charge = Math.min(ticksHeld / 20.0f, 1.0f);
        boolean isVert = GrabVillagerConfig.barVertical;

        int baseW = isVert ? 5 : 182;
        int baseH = isVert ? 182 : 5;

        int baseX = (screenWidth - baseW) / 2;
        int baseY = screenHeight - 39;

        if (isVert) {
            baseX += 200;
            baseY -= 180;
        }

        int x = baseX + GrabVillagerConfig.barOffsetX;
        int y = baseY + GrabVillagerConfig.barOffsetY;

        int topColor, bottomColor;
        switch (GrabVillagerConfig.barColorIndex) {
            case 1 -> {
                topColor = charge >= 1.0f ? 0xFF00FFFF : 0xFF0088FF;
                bottomColor = charge >= 1.0f ? 0xFF00AAAA : 0xFF0044AA;
            }
            case 2 -> {
                topColor = charge >= 1.0f ? 0xFFFF4444 : 0xFFFF8888;
                bottomColor = charge >= 1.0f ? 0xFFAA0000 : 0xFFAA4444;
            }
            case 3 -> {
                topColor = charge >= 1.0f ? 0xFFFF00FF : 0xFFAA00AA;
                bottomColor = charge >= 1.0f ? 0xFFAA00AA : 0xFF550055;
            }
            default -> {
                topColor = charge >= 1.0f ? 0xFF80FF20 : 0xFFDDDD22;
                bottomColor = charge >= 1.0f ? 0xFF408010 : 0xFFAA8811;
            }
        }

        float scale = GrabVillagerConfig.barSize;
        int scaledW = Math.round(baseW * scale);
        int scaledH = Math.round(baseH * scale);

        int drawX = Math.round(x + (baseW / 2.0f) - (scaledW / 2.0f));
        int drawY = Math.round(y + (baseH / 2.0f) - (scaledH / 2.0f));

        guiGraphics.fill(drawX - 1, drawY - 1, drawX + scaledW + 1, drawY + scaledH + 1, 0xFF000000);
        guiGraphics.fill(drawX, drawY, drawX + scaledW, drawY + scaledH, 0xFF222222);

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
