package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.CreateChemworks;
import com.parswha.createchemworks.ModItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;

public record ConfigureFlaskPayload(String contents, boolean offhand) implements CustomPacketPayload {
    public static final Type<ConfigureFlaskPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID, "configure_flask"));
    public static final StreamCodec<RegistryFriendlyByteBuf, ConfigureFlaskPayload> STREAM_CODEC = StreamCodec.of(
            (b,p) -> { b.writeUtf(p.contents, 512); b.writeBoolean(p.offhand); }, b -> new ConfigureFlaskPayload(b.readUtf(512), b.readBoolean()));
    public static void handle(ConfigureFlaskPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            var stack = context.player().getItemInHand(payload.offhand ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (!stack.is(ModItems.CREATIVE_FLASK.get())) return;
            try {
                FlaskContents contents = FlaskContents.parse(payload.contents);
                if (!contents.possible()) throw new IllegalArgumentException("Predicted charge is outside supported range");
                stack.set(DataComponents.CUSTOM_NAME, Component.literal(contents.display()));
            } catch (IllegalArgumentException error) { context.player().sendSystemMessage(Component.literal("Flask rejected: " + error.getMessage())); }
        });
    }
    @Override public Type<? extends CustomPacketPayload> type() { return TYPE; }
}
