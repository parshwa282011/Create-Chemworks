package com.parswha.createchemworks.client;

import com.parswha.createchemworks.chemistry.ConfigureTesterPayload;
import com.parswha.createchemworks.chemistry.OpenTesterPayload;
import com.parswha.createchemworks.chemistry.SaveReactionPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public final class ReactionTesterScreen extends Screen {
    private static final int RESULTS_PER_PAGE = 8;
    private final OpenTesterPayload data;
    private EditBox temperature, pressure;
    private final List<EditBox> concentrations = new ArrayList<>();
    private final boolean[] catalyst = new boolean[9];
    private int page;
    private int selected = -1;
    private String error = "";
    private String temperatureValue, pressureValue;
    private final String[] concentrationValues = new String[9];

    public ReactionTesterScreen(OpenTesterPayload data) {
        super(Component.literal("Reaction Tester"));
        this.data = data;
        temperatureValue = Double.toString(data.temperatureK()); pressureValue = Double.toString(data.pressureKpa());
        for (int i = 0; i < 9; i++) { catalyst[i] = data.catalysts().get(i); concentrationValues[i] = Double.toString(data.concentrations().get(i)); }
    }

    @Override protected void init() {
        int left = width / 2 - 300;
        temperature = new EditBox(font, left + 76, 24, 90, 18, Component.literal("Temperature K"));
        temperature.setValue(temperatureValue); addRenderableWidget(temperature);
        pressure = new EditBox(font, left + 265, 24, 90, 18, Component.literal("Pressure kPa"));
        pressure.setValue(pressureValue); addRenderableWidget(pressure);
        concentrations.clear();
        for (int i = 0; i < 9; i++) {
            int slot = i, y = 55 + i * 18;
            EditBox concentration = new EditBox(font, left + 168, y, 90, 16, Component.literal("Concentration"));
            concentration.setValue(concentrationValues[i]);
            concentration.setEditable(!data.names().get(i).isEmpty()); concentrations.add(concentration);
            addRenderableWidget(concentration);
            Button role = Button.builder(roleText(i), button -> {
                catalyst[slot] = !catalyst[slot]; button.setMessage(roleText(slot));
            }).bounds(left + 265, y, 90, 16).build();
            role.active = !data.names().get(i).isEmpty(); addRenderableWidget(role);
        }
        int resultLeft = width / 2 + 75;
        int start = page * RESULTS_PER_PAGE;
        for (int row = 0; row < RESULTS_PER_PAGE && start + row < data.results().size(); row++) {
            int index = start + row;
            var result = data.results().get(index);
            String marker = result.reversible() ? "⇌ " : "→ ";
            String equation = result.equation().length() > 36 ? result.equation().substring(0, 35) + "…" : result.equation();
            addRenderableWidget(Button.builder(Component.literal(marker + equation), button -> { selected = index; captureAndRebuild(); })
                    .bounds(resultLeft, 42 + row * 21, 220, 19).build());
        }
        int pages = Math.max(1, (data.results().size() + RESULTS_PER_PAGE - 1) / RESULTS_PER_PAGE);
        Button previous = Button.builder(Component.literal("<"), b -> { page--; captureAndRebuild(); })
                .bounds(resultLeft, 214, 28, 18).build(); previous.active = page > 0; addRenderableWidget(previous);
        Button next = Button.builder(Component.literal(">"), b -> { page++; captureAndRebuild(); })
                .bounds(resultLeft + 192, 214, 28, 18).build(); next.active = page + 1 < pages; addRenderableWidget(next);
        Button schematic = Button.builder(Component.literal("Save reusable schematic"), b -> saveSchematic())
                .bounds(resultLeft + 34, 214, 152, 18).build(); schematic.active = selected >= 0; addRenderableWidget(schematic);
        addRenderableWidget(Button.builder(Component.literal("Compute & Refresh"), b -> save()).bounds(left + 55, height - 25, 130, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose()).bounds(left + 174, height - 25, 100, 20).build());
    }

    private Component roleText(int i) { return Component.literal(catalyst[i] ? "Catalyst" : "Reactant"); }
    private void captureAndRebuild() {
        temperatureValue = temperature.getValue(); pressureValue = pressure.getValue();
        for (int i = 0; i < concentrations.size(); i++) concentrationValues[i] = concentrations.get(i).getValue();
        rebuildWidgets();
    }

    private void save() {
        try {
            double t = Double.parseDouble(temperature.getValue()), p = Double.parseDouble(pressure.getValue());
            double[] values = new double[9];
            for (int i = 0; i < 9; i++) values[i] = Double.parseDouble(concentrations.get(i).getValue());
            PacketDistributor.sendToServer(new ConfigureTesterPayload(data.pos(), t, p, values, catalyst));
            minecraft.setScreen(null);
        } catch (Exception exception) { error = "Use valid positive numbers"; }
    }

    private void saveSchematic() {
        if (selected >= 0) PacketDistributor.sendToServer(new SaveReactionPayload(data.pos(), selected));
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = width / 2 - 300, resultLeft = width / 2 + 75;
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        graphics.drawString(font, "Temperature (K)", left, 29, 0xBFD7EA, false);
        graphics.drawString(font, "Pressure (kPa)", left + 185, 29, 0xBFD7EA, false);
        graphics.drawString(font, "Flask", left, 46, 0xAAB7C4, false);
        graphics.drawString(font, "Concentration", left + 168, 46, 0xAAB7C4, false);
        graphics.drawString(font, "Role", left + 265, 46, 0xAAB7C4, false);
        for (int i = 0; i < 9; i++) {
            String name = data.names().get(i).isEmpty() ? "— empty —" : data.names().get(i);
            graphics.drawString(font, (i + 1) + ". " + name, left, 59 + i * 18,
                    data.names().get(i).isEmpty() ? 0x777777 : 0xFFFFFF, false);
        }
        graphics.drawString(font, "All reactions (" + data.results().size() + ")", resultLeft, 27, 0xFFFFD166, false);
        if (selected >= 0 && selected < data.results().size()) {
            var result = data.results().get(selected);
            graphics.drawString(font, "Selected: " + (result.reversible() ? "reversible" : "forward-only")
                    + " | rate " + result.forwardRate(), resultLeft, 238, 0xBFD7EA, false);
        }
        if (!error.isEmpty()) graphics.drawCenteredString(font, error, width / 2, height - 38, 0xFF5555);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override public boolean isPauseScreen() { return false; }
}
