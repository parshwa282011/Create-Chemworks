package com.parswha.createchemworks.client;

import com.parswha.createchemworks.chemistry.ReactionKinetics;
import com.parswha.createchemworks.chemistry.ReactionResultSnapshot;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ReactionBoardScreen extends Screen {
    private final ReactionResultSnapshot result;
    private EditBox temperature, pressure;
    private double fixedTemperature, fixedPressure;
    private String error = "";

    public ReactionBoardScreen(ReactionResultSnapshot result) {
        super(Component.literal("Reaction Board"));
        this.result = result;
        fixedTemperature = Math.max(298.15, result.minimumTemperatureK());
        fixedPressure = Math.max(101.325, result.pressureKpa());
    }

    @Override protected void init() {
        temperature = new EditBox(font, width / 2 - 180, 43, 100, 18, Component.literal("Fixed temperature"));
        temperature.setValue(Double.toString(fixedTemperature)); addRenderableWidget(temperature);
        pressure = new EditBox(font, width / 2 + 25, 43, 100, 18, Component.literal("Fixed pressure"));
        pressure.setValue(Double.toString(fixedPressure)); addRenderableWidget(pressure);
        addRenderableWidget(Button.builder(Component.literal("Update charts"), b -> updateValues())
                .bounds(width / 2 + 135, 43, 100, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Close"), b -> onClose())
                .bounds(width / 2 - 50, height - 24, 100, 20).build());
    }

    private void updateValues() {
        try {
            double t = Double.parseDouble(temperature.getValue()), p = Double.parseDouble(pressure.getValue());
            if (!Double.isFinite(t) || !Double.isFinite(p) || t <= 0 || p <= 0) throw new NumberFormatException();
            fixedTemperature = t; fixedPressure = p; error = "";
        } catch (NumberFormatException exception) { error = "Temperature and pressure must be positive numbers"; }
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 8, 0xFFFFFF);
        graphics.drawCenteredString(font, result.equation(), width / 2, 21, 0xFFFFD166);
        graphics.drawString(font, "Fixed T (K)", width / 2 - 240, 48, 0xBFD7EA, false);
        graphics.drawString(font, "Fixed P (kPa)", width / 2 - 55, 48, 0xBFD7EA, false);
        graphics.drawCenteredString(font, result.reversible() ? "Reversible reaction" : "Forward-only reaction",
                width / 2, 67, result.reversible() ? 0x66FFAA : 0xFFAA66);
        drawChart(graphics, width / 2 - 310, 88, 290, 135, true);
        drawChart(graphics, width / 2 + 20, 88, 290, 135, false);
        graphics.drawString(font, "Minimum T: " + result.minimumTemperatureK() + " K  |  target P: "
                + result.pressureKpa() + " kPa  |  ΔH: " + result.enthalpyKj() + " kJ", width / 2 - 310, 232, 0xCCCCCC, false);
        graphics.drawString(font, "Catalyst: " + result.catalyst() + "  |  " + result.confidence(),
                width / 2 - 310, 244, 0x999999, false);
        if (!error.isEmpty()) graphics.drawCenteredString(font, error, width / 2, height - 38, 0xFF5555);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawChart(GuiGraphics graphics, int x, int y, int w, int h, boolean varyTemperature) {
        graphics.fill(x, y, x + w, y + h, 0xAA101820);
        graphics.hLine(x, x + w, y + h - 17, 0xFF778899);
        graphics.vLine(x + 30, y, y + h - 17, 0xFF778899);
        double maxX = varyTemperature ? Math.max(5000, result.minimumTemperatureK() * 2)
                : Math.max(1000, result.pressureKpa() * 2);
        double maxLogRate = 1;
        double[] rates = new double[w - 34];
        double[] products = new double[w - 34];
        for (int px = 0; px < rates.length; px++) {
            double value = 1 + maxX * px / Math.max(1, rates.length - 1);
            double t = varyTemperature ? value : fixedTemperature;
            double p = varyTemperature ? fixedPressure : value;
            ReactionKinetics kinetics = ReactionKinetics.estimate(result.reaction(), t, p, 1, 1, 0);
            rates[px] = Math.log10(1 + Math.max(0, kinetics.forwardRate()));
            maxLogRate = Math.max(maxLogRate, rates[px]);
            products[px] = kinetics.reversible()
                    ? kinetics.forwardRate() / Math.max(1e-12, kinetics.forwardRate() + kinetics.reverseRate()) : 1;
        }
        int plotHeight = h - 24;
        for (int px = 0; px < rates.length; px++) {
            int drawX = x + 31 + px;
            int productY = y + plotHeight - (int) Math.round(products[px] * plotHeight);
            int rateY = y + plotHeight - (int) Math.round(rates[px] / maxLogRate * plotHeight);
            graphics.fill(drawX, productY, drawX + 1, productY + 1, 0xFF55DD88);
            graphics.fill(drawX, rateY, drawX + 1, rateY + 1, 0xFFFFAA44);
        }
        double currentT = fixedTemperature, currentP = fixedPressure;
        ReactionKinetics current = ReactionKinetics.estimate(result.reaction(), currentT, currentP, 1, 1, 0);
        graphics.drawString(font, varyTemperature ? "Vary temperature; fixed pressure" : "Vary pressure; fixed temperature",
                x + 4, y + 3, 0xFFFFFF, false);
        graphics.drawString(font, "Product concentration", x + 4, y + 14, 0x55DD88, false);
        graphics.drawString(font, "Reaction rate: " + current.forwardRate() + " " + current.unit(), x + 4, y + 25, 0xFFAA44, false);
        graphics.drawString(font, "0", x + 27, y + h - 14, 0xAAAAAA, false);
        graphics.drawString(font, (varyTemperature ? "T up to " : "P up to ") + Math.round(maxX), x + w - 90, y + h - 14, 0xAAAAAA, false);
    }

    @Override public boolean isPauseScreen() { return false; }
}
