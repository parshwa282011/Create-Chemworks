package com.parswha.createchemworks.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

final class ElementButton extends AbstractButton {
    private final int color;
    private final Runnable action;

    ElementButton(int x, int y, int width, int height, Component message, int color, Runnable action) {
        super(x, y, width, height, message);
        this.color = color;
        this.action = action;
    }

    @Override
    public void onPress() {
        action.run();
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        int border = isHoveredOrFocused() ? 0xFFFFFFFF : 0xFF202020;
        int fill = isHoveredOrFocused() ? brighten(color) : color;
        graphics.fill(getX(), getY(), getX() + width, getY() + height, border);
        graphics.fill(getX() + 1, getY() + 1, getX() + width - 1, getY() + height - 1, fill);
        graphics.drawCenteredString(Minecraft.getInstance().font, getMessage(),
                getX() + width / 2, getY() + (height - 8) / 2, 0xFFFFFFFF);
    }

    private static int brighten(int color) {
        int r = Math.min(255, ((color >> 16) & 0xFF) + 28);
        int g = Math.min(255, ((color >> 8) & 0xFF) + 28);
        int b = Math.min(255, (color & 0xFF) + 28);
        return 0xFF000000 | r << 16 | g << 8 | b;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
