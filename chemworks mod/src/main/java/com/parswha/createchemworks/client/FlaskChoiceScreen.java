package com.parswha.createchemworks.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;

public final class FlaskChoiceScreen extends Screen {
    private final InteractionHand hand;
    public FlaskChoiceScreen(InteractionHand hand) { super(Component.literal("Configure Creative Flask")); this.hand = hand; }
    @Override protected void init() {
        addRenderableWidget(Button.builder(Component.literal("Choose Element"), b -> minecraft.setScreen(PeriodicTableScreen.forFlask(hand))).bounds(width/2-105, height/2-24, 210, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Enter Compound or Solution"), b -> minecraft.setScreen(new FlaskFormulaScreen(this, hand))).bounds(width/2-105, height/2+4, 210, 20).build());
    }
    @Override public void render(GuiGraphics g, int mx, int my, float pt) {
        renderBackground(g,mx,my,pt); g.drawCenteredString(font,title,width/2,height/2-58,0xFFFFFF);
        g.drawCenteredString(font,"Elements automatically use their natural diatomic form",width/2,height/2-44,0xAFC8DD);
        super.render(g,mx,my,pt);
    }
    @Override public boolean isPauseScreen() { return false; }
}
