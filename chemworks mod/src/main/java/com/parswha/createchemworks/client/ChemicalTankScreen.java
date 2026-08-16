package com.parswha.createchemworks.client;

import com.parswha.createchemworks.chemistry.ConfigureChemicalTankPayload;
import com.parswha.createchemworks.chemistry.OpenChemicalTankPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.network.PacketDistributor;

public final class ChemicalTankScreen extends Screen {
    private final OpenChemicalTankPayload data;
    private EditBox target;
    private String error = "";
    public ChemicalTankScreen(OpenChemicalTankPayload data) { super(Component.literal("Chemical Tank Controller")); this.data = data; }
    @Override protected void init() {
        int left = width / 2 - 100;
        target = new EditBox(font, left + 100, height / 2 + 32, 95, 18, Component.literal("Target temperature"));
        target.setValue(String.format(java.util.Locale.ROOT, "%.2f", data.targetTemperatureK()));
        addRenderableWidget(target);
        addRenderableWidget(Button.builder(Component.literal("Set"), b -> save()).bounds(left + 35, height / 2 + 60, 60, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose()).bounds(left + 105, height / 2 + 60, 60, 20).build());
    }
    private void save() {
        try { PacketDistributor.sendToServer(new ConfigureChemicalTankPayload(data.pos(), Double.parseDouble(target.getValue()))); onClose(); }
        catch (NumberFormatException e) { error = "Enter a temperature from 1 to 5000 K"; }
    }
    @Override public void render(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        renderBackground(g, mouseX, mouseY, partialTick);
        int left = width / 2 - 100, y = height / 2 - 70;
        g.drawCenteredString(font, title, width / 2, y, 0xFFFFFF);
        g.drawString(font, "Chemical: " + (data.chemical().isBlank() ? "Empty" : data.chemical()), left, y + 22, 0xD8F3FF, false);
        g.drawString(font, String.format(java.util.Locale.ROOT, "Amount: %.3f mol / %.1f L", data.moles(), data.volumeLiters()), left, y + 36, 0xFFFFFF, false);
        g.drawString(font, String.format(java.util.Locale.ROOT, "Temperature: %.2f K", data.temperatureK()), left, y + 50, 0xFFB45E, false);
        g.drawString(font, String.format(java.util.Locale.ROOT, "Pressure: %.2f kPa", data.pressureKpa()), left, y + 64, 0x70D6FF, false);
        g.drawString(font, "FE: " + data.energy() + " / " + data.maxEnergy(), left, y + 78, 0xFFE66D, false);
        g.drawString(font, "Target K", left, height / 2 + 37, 0xFFFFFF, false);
        if (!error.isEmpty()) g.drawCenteredString(font, error, width / 2, height / 2 + 86, 0xFF5555);
        super.render(g, mouseX, mouseY, partialTick);
    }
    @Override public boolean isPauseScreen() { return false; }
}
