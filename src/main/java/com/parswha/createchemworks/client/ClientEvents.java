package com.parswha.createchemworks.client;

import com.parswha.createchemworks.CreateChemworks;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterClientCommandsEvent;

import static net.minecraft.commands.Commands.literal;

@EventBusSubscriber(modid = CreateChemworks.MOD_ID, value = Dist.CLIENT)
public final class ClientEvents {
    private ClientEvents() {
    }

    @SubscribeEvent
    public static void registerClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(literal("periodictable").executes(context -> {
            Minecraft.getInstance().setScreen(new PeriodicTableScreen());
            return 1;
        }));
    }
}
