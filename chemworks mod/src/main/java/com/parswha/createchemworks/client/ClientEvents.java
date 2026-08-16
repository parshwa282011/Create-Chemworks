package com.parswha.createchemworks.client;

import com.parswha.createchemworks.CreateChemworks;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;
import net.minecraft.world.InteractionHand;
import com.parswha.createchemworks.chemistry.OpenTesterPayload;
import com.parswha.createchemworks.chemistry.OpenReactionBoardPayload;
import com.parswha.createchemworks.chemistry.OpenChemicalTankPayload;
import com.parswha.createchemworks.chemistry.OpenChemDebugPayload;

import static net.minecraft.commands.Commands.literal;

@EventBusSubscriber(modid = CreateChemworks.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    public static void openFlaskScreen(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new FlaskChoiceScreen(hand));
    }

    public static void openTesterScreen(OpenTesterPayload payload) {
        Minecraft.getInstance().setScreen(new ReactionTesterScreen(payload));
    }

    public static void openReactionBoardScreen(OpenReactionBoardPayload payload) {
        Minecraft.getInstance().setScreen(new ReactionBoardScreen(payload.result()));
    }

    public static void openChemicalTankScreen(OpenChemicalTankPayload payload) {
        Minecraft.getInstance().setScreen(new ChemicalTankScreen(payload));
    }

    public static void openChemDebugScreen(OpenChemDebugPayload payload) {
        Minecraft.getInstance().setScreen(new ChemDebugScreen(payload.lines()));
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(literal("periodictable").executes(context -> {
            Minecraft.getInstance().setScreen(new PeriodicTableScreen());
            return 1;
        }));
    }
}
