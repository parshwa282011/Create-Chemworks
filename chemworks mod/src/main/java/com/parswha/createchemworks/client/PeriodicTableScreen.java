package com.parswha.createchemworks.client;

import com.parswha.createchemworks.client.Elements.ElectronBlock;
import com.parswha.createchemworks.client.Elements.Element;
import com.parswha.createchemworks.client.Elements.ElementsData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.PacketDistributor;
import com.parswha.createchemworks.chemistry.SelectFlaskElementPayload;

import java.util.function.Consumer;

public final class PeriodicTableScreen extends Screen {
    private int cellWidth;
    private int rowHeight;
    private int startX;
    private int startY;
    private final Consumer<Element> selection;

    public PeriodicTableScreen() {
        this(null);
    }

    private PeriodicTableScreen(Consumer<Element> selection) {
        super(Component.translatable(selection == null ? "screen.create_chemworks.periodic_table"
                : "screen.create_chemworks.periodic_table.select"));
        this.selection = selection;
    }

    public static PeriodicTableScreen forFlask(InteractionHand hand) {
        return new PeriodicTableScreen(element -> {
            PacketDistributor.sendToServer(new SelectFlaskElementPayload(
                    element.number(), hand == InteractionHand.OFF_HAND));
            Minecraft.getInstance().setScreen(null);
        });
    }

    @Override
    protected void init() {
        calculateLayout();
        ElementsData.mainTableElements().forEach(this::addElementButton);

        for (int period = ElectronBlock.F.firstPeriod(); period <= ElementsData.PERIODS; period++) {
            addGatewayButton(period, ElectronBlock.F);
        }
    }

    private void calculateLayout() {
        cellWidth = Math.max(20, Math.min(28, (width - 42) / ElementsData.MAIN_COLUMNS));
        rowHeight = Math.max(11, Math.min(20, (height - 64) / ElementsData.PERIODS));
        startX = (width - cellWidth * ElementsData.MAIN_COLUMNS) / 2;
        startY = Math.max(36, (height - rowHeight * ElementsData.PERIODS) / 2 + 8);
    }

    private void addElementButton(Element element) {
        MutableComponent tooltip = Component.literal(element.number() + " — " + element.name())
                .append(Component.literal("  [" + element.block().label() + "-block]")
                        .withStyle(ChatFormatting.DARK_GRAY));
        if (element.acceleratorOnly()) {
            tooltip = tooltip.append(Component.literal("\nParticle Reaction only (Particle Accelerator)")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        ElementButton button = new ElementButton(
                xForColumn(element.column()), yForPeriod(element.period()),
                cellWidth - 1, rowHeight - 1, Component.literal(element.symbol()),
                element.block().color(), () -> selectOrInspect(element));
        button.setTooltip(Tooltip.create(tooltip));
        addRenderableWidget(button);
    }

    private void selectOrInspect(Element element) {
        if (selection != null) selection.accept(element);
        else minecraft.setScreen(new ElementDetailsScreen(this, element));
    }

    private void addGatewayButton(int period, ElectronBlock block) {
        ElementButton gateway = new ElementButton(
                xForColumn(block.gatewayColumn()), yForPeriod(period),
                cellWidth - 1, rowHeight - 1, Component.literal(block.label().toUpperCase()),
                block.color(), () -> minecraft.setScreen(new ElectronBlockScreen(this, block, selection)));
        gateway.setTooltip(Tooltip.create(Component.literal(
                "Open " + block.label() + "-block for period " + period)));
        addRenderableWidget(gateway);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(startX - 20, startY - 5,
                startX + cellWidth * ElementsData.MAIN_COLUMNS + 5,
                startY + rowHeight * ElementsData.PERIODS + 5, 0xB810151D);
        graphics.drawCenteredString(font, title, width / 2, 12, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.translatable(selection == null
                        ? "screen.create_chemworks.periodic_table.hint"
                        : "screen.create_chemworks.periodic_table.select_hint"),
                width / 2, height - 12, 0xFFB9C6D8);
        drawLegend(graphics);
        drawReservedPeriods(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawLegend(GuiGraphics graphics) {
        int x = startX + cellWidth * 2;
        for (ElectronBlock block : ElectronBlock.values()) {
            graphics.fill(x, 24, x + 8, 32, block.color());
            graphics.drawString(font, block.label(), x + 11, 24, 0xFFFFFF, false);
            x += 36;
        }
    }

    private void drawReservedPeriods(GuiGraphics graphics) {
        for (int period = 1; period <= ElementsData.PERIODS; period++) {
            int y = yForPeriod(period);
            graphics.drawString(font, Integer.toString(period), startX - 14, y + 2, 0xA0A0A0, false);
            if (period < 8) continue;
            for (int column = 0; column < ElementsData.MAIN_COLUMNS; column++) {
                int x = xForColumn(column);
                graphics.fill(x, y, x + cellWidth - 1, y + rowHeight - 1, 0x33202020);
            }
        }
    }

    private int xForColumn(int column) { return startX + column * cellWidth; }
    private int yForPeriod(int period) { return startY + (period - 1) * rowHeight; }

    @Override
    public boolean isPauseScreen() { return false; }
}
