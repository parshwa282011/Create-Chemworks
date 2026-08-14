package com.parswha.createchemworks.client;

import com.parswha.createchemworks.chemistry.ElementProfile;
import com.parswha.createchemworks.client.Elements.Element;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

final class ElementDetailsScreen extends Screen {
    private final Screen parent;
    private final Element element;
    private final ElementProfile profile;

    ElementDetailsScreen(Screen parent, Element element) {
        super(Component.literal(element.name() + " (" + element.symbol() + ")"));
        this.parent = parent;
        this.element = element;
        this.profile = ElementProfile.calculate(element);
    }

    @Override
    protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Back"), button -> onClose())
                .bounds(width / 2 - 50, height - 28, 100, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 12, element.block().color());
        List<String> lines = details();
        int columns = width >= 520 ? 2 : 1;
        int rowsPerColumn = (lines.size() + columns - 1) / columns;
        int columnWidth = Math.min(260, (width - 40) / columns);
        int left = (width - columnWidth * columns) / 2;
        for (int index = 0; index < lines.size(); index++) {
            int column = index / rowsPerColumn;
            int row = index % rowsPerColumn;
            graphics.drawString(font, lines.get(index), left + column * columnWidth,
                    34 + row * 13, 0xFFE0E0E0, false);
        }
        graphics.drawCenteredString(font,
                Component.literal("Properties are calculated; measured-value overrides can be added later."),
                width / 2, height - 42, 0xFFFFC857);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private List<String> details() {
        List<String> lines = new ArrayList<>();
        lines.add("Atomic number: " + element.number());
        lines.add("Symbol: " + element.symbol());
        lines.add("Period: " + element.period());
        lines.add("Block: " + element.block().label().toUpperCase());
        lines.add("Atomic mass: " + profile.atomicMass() + " u");
        lines.add("Electron configuration: " + profile.electronConfiguration());
        lines.add("Valence electrons: " + profile.valenceElectrons());
        lines.add("Oxidation states: " + profile.oxidationStates());
        lines.add("Electronegativity: " + profile.electronegativity());
        lines.add("Atomic radius: " + profile.atomicRadiusPm() + " pm");
        lines.add("Density: " + profile.density() + " g/cm³");
        lines.add("Melting point: " + profile.meltingPointK() + " K");
        lines.add("Boiling point: " + profile.boilingPointK() + " K");
        lines.add("Standard phase: " + profile.standardPhase());
        lines.add("Stability: " + profile.stability());
        lines.add("Availability: " + (element.acceleratorOnly() ? "Particle accelerator" : "Natural/processing"));
        lines.add("Model source: " + profile.provenance());
        return lines;
    }

    @Override public void onClose() { minecraft.setScreen(parent); }
    @Override public boolean isPauseScreen() { return false; }
}
