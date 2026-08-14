package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.CreateChemworks;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record OpenReactionBoardPayload(ReactionResultSnapshot result) implements CustomPacketPayload {
    public static final Type<OpenReactionBoardPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID,"open_reaction_board"));
    public static final StreamCodec<RegistryFriendlyByteBuf,OpenReactionBoardPayload> STREAM_CODEC = StreamCodec.of(
            (b,p)->p.result.write(b), b->new OpenReactionBoardPayload(ReactionResultSnapshot.read(b)));
    public static void handle(OpenReactionBoardPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(()->{try{Class<?> c=Class.forName("com.parswha.createchemworks.client.ClientEvents");c.getMethod("openReactionBoardScreen",OpenReactionBoardPayload.class).invoke(null,payload);}catch(ReflectiveOperationException e){throw new IllegalStateException(e);}});
    }
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
