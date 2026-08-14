package com.parswha.createchemworks.client;

import com.parswha.createchemworks.client.Elements.ElectronBlock;
import com.parswha.createchemworks.client.Elements.Element;
import com.parswha.createchemworks.client.Elements.ElementsData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import java.util.function.Consumer;

final class ElectronBlockScreen extends Screen {
    private static final int PREFERRED_CELL_WIDTH = 26;
    private final Screen parent;
    private final ElectronBlock block;
    private final int requestedStartColumn;
    private int cellWidth;
    private int cellHeight;
    private int startX;
    private int startY;
    private int startColumn;
    private int visibleColumns;
    private ElectronBlock childBlock;
    private final Consumer<Element> selection;

    ElectronBlockScreen(Screen parent, ElectronBlock block) {
        this(parent, block, null);
    }

    ElectronBlockScreen(Screen parent, ElectronBlock block, Consumer<Element> selection) {
        this(parent, block, 0, selection);
    }

    private ElectronBlockScreen(Screen parent, ElectronBlock block, int startColumn,
                                Consumer<Element> selection) {
        super(Component.literal(block.label().toUpperCase() + "-block — " + block.capacity() + " Columns"));
        this.parent = parent;
        this.block = block;
        this.requestedStartColumn = startColumn;
        this.selection = selection;
    }

    @Override
    protected void init() {
        cellWidth = PREFERRED_CELL_WIDTH;
        childBlock = childOf(block);
        int gatewayColumns = childBlock == null ? 0 : 1;
        visibleColumns = Math.min(block.capacity(),
                Math.max(1, (width - 72) / cellWidth - gatewayColumns));
        startColumn = Math.max(0, Math.min(requestedStartColumn, block.capacity() - visibleColumns));
        cellHeight = Math.max(11, Math.min(20, (height - 76) / ElementsData.PERIODS));
        startX = (width - cellWidth * (visibleColumns + gatewayColumns)) / 2;
        startY = 36;

        ElementsData.elementsIn(block).stream()
                .filter(element -> element.column() >= startColumn
                        && element.column() < startColumn + visibleColumns)
                .forEach(this::addElementButton);
        if (childBlock != null) {
            for (int period = childBlock.firstPeriod(); period <= ElementsData.PERIODS; period++) {
                addChildGateway(period);
            }
        }
        if (startColumn > 0) {
            addRenderableWidget(Button.builder(Component.literal("< Columns"), button -> showPage(startColumn - visibleColumns))
                    .bounds(width / 2 - 156, height - 28, 96, 20).build());
        }
        addRenderableWidget(Button.builder(Component.literal("Back"), button -> onClose())
                .bounds(width / 2 - 50, height - 28, 100, 20).build());
        if (startColumn + visibleColumns < block.capacity()) {
            addRenderableWidget(Button.builder(Component.literal("Columns >"), button -> showPage(startColumn + visibleColumns))
                    .bounds(width / 2 + 60, height - 28, 96, 20).build());
        }
    }

    private void addElementButton(Element element) {
        ElementButton button = new ElementButton(
                startX + (childBlock == null ? 0 : cellWidth)
                        + (element.column() - startColumn) * cellWidth,
                startY + (element.period() - 1) * cellHeight,
                cellWidth - 1, cellHeight - 1,
                Component.literal(element.symbol()), block.color(),
                () -> {
                    if (selection != null) selection.accept(element);
                    else minecraft.setScreen(new ElementDetailsScreen(this, element));
                });
        MutableComponent tooltip = Component.literal(element.number() + " — " + element.name())
                .withStyle(ChatFormatting.GRAY);
        if (element.acceleratorOnly()) {
            tooltip = tooltip.append(Component.literal("\nParticle Reaction only (Particle Accelerator)")
                    .withStyle(ChatFormatting.LIGHT_PURPLE));
        }
        button.setTooltip(Tooltip.create(tooltip));
        addRenderableWidget(button);
    }

    private void addChildGateway(int period) {
        ElementButton gateway = new ElementButton(
                startX, startY + (period - 1) * cellHeight,
                cellWidth - 1, cellHeight - 1,
                Component.literal(childBlock.label().toUpperCase()), childBlock.color(),
                () -> minecraft.setScreen(new ElectronBlockScreen(this, childBlock, selection)));
        gateway.setTooltip(Tooltip.create(Component.literal(
                "Open " + childBlock.label().toUpperCase() + "-block for period " + period)));
        addRenderableWidget(gateway);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 14, 0xFFFFFF);
        graphics.drawCenteredString(font, Component.literal(
                        "Columns " + (startColumn + 1) + "–" + (startColumn + visibleColumns)),
                width / 2, 25, 0xA0A0A0);
        drawGrid(graphics);
        if (ElementsData.elementsIn(block).isEmpty()) {
            graphics.drawCenteredString(font, Component.literal("Reserved for future fictional elements"),
                    width / 2, 25, 0xA0A0A0);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawGrid(GuiGraphics graphics) {
        for (int period = 1; period <= ElementsData.PERIODS; period++) {
            int y = startY + (period - 1) * cellHeight;
            graphics.drawString(font, Integer.toString(period), startX - 16, y + 2, 0xA0A0A0, false);
            int gatewayColumns = childBlock == null ? 0 : 1;
            for (int column = 0; column < visibleColumns + gatewayColumns; column++) {
                int x = startX + column * cellWidth;
                graphics.fill(x, y, x + cellWidth - 1, y + cellHeight - 1, 0x44202020);
            }
        }
    }

    private static ElectronBlock childOf(ElectronBlock block) {
        return switch (block) {
            case F -> ElectronBlock.T;
            default -> null;
        };
    }

    private void showPage(int column) {
        minecraft.setScreen(new ElectronBlockScreen(parent, block, column, selection));
    }

    @Override
    public void onClose() { minecraft.setScreen(parent); }

    @Override
    public boolean isPauseScreen() { return false; }
}
