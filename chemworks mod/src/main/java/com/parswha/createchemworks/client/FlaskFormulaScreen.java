package com.parswha.createchemworks.client;

import com.parswha.createchemworks.chemistry.ConfigureFlaskPayload;
import com.parswha.createchemworks.chemistry.FlaskContents;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.neoforged.neoforge.network.PacketDistributor;

final class FlaskFormulaScreen extends Screen {
    private final Screen parent; private final InteractionHand hand; private EditBox input; private String status = ""; private int statusColor;
    FlaskFormulaScreen(Screen parent, InteractionHand hand) { super(Component.literal("Compound / Solution Flask")); this.parent=parent; this.hand=hand; }
    @Override protected void init() {
        input = new EditBox(font,width/2-150,height/2-18,300,20,Component.literal("Contents"));
        input.setMaxLength(512); input.setHint(Component.literal("H2O  or  2(H2O) + 1(NaCl)  or  e_-")); input.setResponder(v -> validate()); addRenderableWidget(input);
        addRenderableWidget(Button.builder(Component.literal("Fill Flask"),b -> save()).bounds(width/2-102,height/2+34,100,20).build());
        addRenderableWidget(Button.builder(Component.literal("Back"),b -> onClose()).bounds(width/2+2,height/2+34,100,20).build()); setInitialFocus(input); validate();
    }
    private void validate() { try { var v=FlaskContents.parse(input.getValue()); status=(v.possible()?"Possible":"Impossible")+" • estimated net charge: "+signed(v.estimatedCharge()); statusColor=v.possible()?0x55FF88:0xFF5555; } catch(Exception e) { status="Impossible • "+e.getMessage(); statusColor=0xFF5555; } }
    private void save() { try { var v=FlaskContents.parse(input.getValue()); if(!v.possible()) return; PacketDistributor.sendToServer(new ConfigureFlaskPayload(input.getValue(),hand==InteractionHand.OFF_HAND)); minecraft.setScreen(null); } catch(Exception ignored) {} }
    private static String signed(int n) { return n>0?"+"+n:Integer.toString(n); }
    @Override public void render(GuiGraphics g,int mx,int my,float pt) { renderBackground(g,mx,my,pt); g.drawCenteredString(font,title,width/2,height/2-58,0xFFFFFF); g.drawCenteredString(font,"Solutions require ratios: x(compound) + y(compound)",width/2,height/2-42,0xAFC8DD); g.drawCenteredString(font,status,width/2,height/2+8,statusColor); super.render(g,mx,my,pt); }
    @Override public void onClose(){minecraft.setScreen(parent);} @Override public boolean isPauseScreen(){return false;}
}
