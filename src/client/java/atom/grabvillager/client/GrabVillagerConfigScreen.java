package atom.grabvillager.client;

import atom.grabvillager.config.GrabVillagerConfig;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSliderButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.Locale;
import java.util.function.Consumer;

public class GrabVillagerConfigScreen extends Screen {
    private Button btnTools, btnColor, btnOrient;

    public GrabVillagerConfigScreen() {
        super(Component.literal("Personnalisation Grab Villager"));
    }

    @Override
    protected void init() {
        int midX = this.width / 2;
        int y = 40;
        int spacing = 24;

        // 1. Slider pour la Distance (Max 3.0)
        ConfigSlider throwSlider = new ConfigSlider(midX - 75, y, 150, 20, "Distance Lancer", GrabVillagerConfig.throwMultiplier, 0.1, 3.0, false, val -> GrabVillagerConfig.throwMultiplier = val.floatValue());
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> throwSlider.setDirectValue(GrabVillagerConfig.throwMultiplier - 0.1)).bounds(midX - 100, y, 20, 20).build());
        this.addRenderableWidget(throwSlider);
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> throwSlider.setDirectValue(GrabVillagerConfig.throwMultiplier + 0.1)).bounds(midX + 80, y, 20, 20).build());
        y += spacing;

        // 2. Boutons d'options (Outils, Couleur, Orientation)
        btnTools = this.addRenderableWidget(Button.builder(Component.empty(), b -> { GrabVillagerConfig.allowTools = !GrabVillagerConfig.allowTools; updateLabels(); }).bounds(midX - 100, y, 200, 20).build());
        y += spacing;
        btnColor = this.addRenderableWidget(Button.builder(Component.empty(), b -> { GrabVillagerConfig.barColorIndex = (GrabVillagerConfig.barColorIndex + 1) % 4; updateLabels(); }).bounds(midX - 100, y, 200, 20).build());
        y += spacing;
        btnOrient = this.addRenderableWidget(Button.builder(Component.empty(), b -> { GrabVillagerConfig.barVertical = !GrabVillagerConfig.barVertical; updateLabels(); }).bounds(midX - 100, y, 200, 20).build());
        y += spacing;

        // 3. Slider pour la Taille (Max 1.0)
        ConfigSlider sizeSlider = new ConfigSlider(midX - 75, y, 150, 20, "Taille Jauge", GrabVillagerConfig.barSize, 0.1, 1.0, false, val -> GrabVillagerConfig.barSize = val.floatValue());
        this.addRenderableWidget(Button.builder(Component.literal("-"), b -> sizeSlider.setDirectValue(GrabVillagerConfig.barSize - 0.1)).bounds(midX - 100, y, 20, 20).build());
        this.addRenderableWidget(sizeSlider);
        this.addRenderableWidget(Button.builder(Component.literal("+"), b -> sizeSlider.setDirectValue(GrabVillagerConfig.barSize + 0.1)).bounds(midX + 80, y, 20, 20).build());
        y += spacing;

        // 4. Slider pour la Position X (Offset)
        ConfigSlider xSlider = new ConfigSlider(midX - 75, y, 150, 20, "Position Écran X", GrabVillagerConfig.barOffsetX, -1000, 1000, true, val -> GrabVillagerConfig.barOffsetX = val.intValue());
        this.addRenderableWidget(Button.builder(Component.literal("<"), b -> xSlider.setDirectValue(GrabVillagerConfig.barOffsetX - 10)).bounds(midX - 100, y, 20, 20).build());
        this.addRenderableWidget(xSlider);
        this.addRenderableWidget(Button.builder(Component.literal(">"), b -> xSlider.setDirectValue(GrabVillagerConfig.barOffsetX + 10)).bounds(midX + 80, y, 20, 20).build());
        y += spacing;

        // 5. Slider pour la Position Y (Offset)
        ConfigSlider ySlider = new ConfigSlider(midX - 75, y, 150, 20, "Position Écran Y", GrabVillagerConfig.barOffsetY, -1000, 1000, true, val -> GrabVillagerConfig.barOffsetY = val.intValue());
        this.addRenderableWidget(Button.builder(Component.literal("^"), b -> ySlider.setDirectValue(GrabVillagerConfig.barOffsetY - 10)).bounds(midX - 100, y, 20, 20).build());
        this.addRenderableWidget(ySlider);
        this.addRenderableWidget(Button.builder(Component.literal("v"), b -> ySlider.setDirectValue(GrabVillagerConfig.barOffsetY + 10)).bounds(midX + 80, y, 20, 20).build());
        y += spacing + 10;

        // Bouton de Sauvegarde
        this.addRenderableWidget(Button.builder(Component.literal("Sauvegarder & Quitter"), b -> {
            GrabVillagerConfig.save();
            this.minecraft.setScreen(null);
        }).bounds(midX - 100, y, 200, 20).build());

        updateLabels();
    }

    private void updateLabels() {
        btnTools.setMessage(Component.literal("Outils avec Villageois : " + (GrabVillagerConfig.allowTools ? "OUI" : "NON")));
        String[] colors = {"Vanilla", "Océan", "Lave", "Ender"};
        btnColor.setMessage(Component.literal("Couleur Jauge : " + colors[GrabVillagerConfig.barColorIndex]));
        btnOrient.setMessage(Component.literal("Orientation : " + (GrabVillagerConfig.barVertical ? "Verticale" : "Horizontale")));
    }


    public void render(GuiGraphicsExtractor guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.extractRenderState(guiGraphics, mouseX, mouseY, partialTicks);

        guiGraphics.centeredText(this.font, this.title, this.width / 2, 15, 0xFFFFFF);
    }

    // --- CLASSE INTERNE DU SLIDER PERSONNALISÉ ---
    private static class ConfigSlider extends AbstractSliderButton {
        private final String prefix;
        private final double min, max;
        private final boolean isInt;
        private final Consumer<Double> onApply;

        public ConfigSlider(int x, int y, int w, int h, String prefix, double current, double min, double max, boolean isInt, Consumer<Double> onApply) {
            super(x, y, w, h, Component.empty(), (current - min) / (max - min));
            this.prefix = prefix;
            this.min = min;
            this.max = max;
            this.isInt = isInt;
            this.onApply = onApply;
            this.updateMessage();
        }

        @Override
        protected void updateMessage() {
            double current = min + (this.value * (max - min));
            if (isInt) {
                this.setMessage(Component.literal(String.format("%s : %d", prefix, (int) Math.round(current))));
            } else {
                this.setMessage(Component.literal(String.format(Locale.US, "%s : x%.1f", prefix, current)));
            }
        }

        @Override
        protected void applyValue() {
            onApply.accept(min + (this.value * (max - min)));
        }

        // Permet de mettre à jour le slider depuis les boutons + et -
        public void setDirectValue(double val) {
            double clamped = Math.max(min, Math.min(max, val));
            this.value = (clamped - min) / (max - min);
            this.applyValue();
            this.updateMessage();
        }
    }
}