package com.parswha.createchemworks.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class ChemDebugScreen extends Screen {
    private final List<String> lines;
    private int scroll;

    public ChemDebugScreen(List<String> lines) {
        super(Component.literal("Chemical Network Debugger"));
        this.lines = lines;
    }

    @Override protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Close"), button -> onClose())
                .bounds(width / 2 - 45, height - 26, 90, 20).build());
    }

    @Override public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int visible = Math.max(1, (height - 70) / 11);
        scroll = Math.max(0, Math.min(Math.max(0, lines.size() - visible), scroll - (int)Math.signum(scrollY) * 3));
        return true;
    }

    @Override public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 12, 0x55E8FF);
        graphics.drawCenteredString(font, "Live server snapshot — scroll for more", width / 2, 25, 0xA0A0A0);
        int visible = Math.max(1, (height - 70) / 11);
        for (int i = 0; i < visible && scroll + i < lines.size(); i++) {
            String line = lines.get(scroll + i);
            int color = line.startsWith("WARNING") ? 0xFFAA44 : line.endsWith("closed") ? 0xFF7777 : 0xE8E8E8;
            graphics.drawString(font, line, 12, 43 + i * 11, color, false);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
    }
}
