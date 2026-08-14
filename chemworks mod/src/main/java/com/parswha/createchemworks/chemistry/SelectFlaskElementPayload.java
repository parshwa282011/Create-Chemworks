package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.CreateChemworks;
import com.parswha.createchemworks.ModItems;
import com.parswha.createchemworks.client.Elements.ElementsData;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public record SelectFlaskElementPayload(int atomicNumber, boolean offhand) implements CustomPacketPayload {
    public static final Type<SelectFlaskElementPayload> TYPE = new Type<>(
            ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID, "select_flask_element"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SelectFlaskElementPayload> STREAM_CODEC =
            StreamCodec.of((buffer, payload) -> {
                buffer.writeVarInt(payload.atomicNumber);
                buffer.writeBoolean(payload.offhand);
            }, buffer -> new SelectFlaskElementPayload(buffer.readVarInt(), buffer.readBoolean()));

    public static void handle(SelectFlaskElementPayload payload,
                              net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            if (payload.atomicNumber < 1 || payload.atomicNumber > ElementsData.all().size()) return;
            InteractionHand hand = payload.offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack stack = context.player().getItemInHand(hand);
            if (!stack.is(ModItems.CREATIVE_FLASK.get())) return;
            var element = ElementsData.all().get(payload.atomicNumber - 1);
            String formula = element.symbol() + (ChemistryRules.elementalMoleculeSize(element) == 2 ? "2" : "");
            stack.set(DataComponents.CUSTOM_NAME, Component.literal(formula));
        });
    }

    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
