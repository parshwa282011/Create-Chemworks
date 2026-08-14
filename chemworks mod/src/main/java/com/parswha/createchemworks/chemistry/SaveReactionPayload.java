package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.CreateChemworks;
import com.parswha.createchemworks.ReactionSchematicItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record SaveReactionPayload(BlockPos pos, int index) implements CustomPacketPayload {
    public static final Type<SaveReactionPayload> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID,"save_reaction"));
    public static final StreamCodec<RegistryFriendlyByteBuf,SaveReactionPayload> STREAM_CODEC = StreamCodec.of(
            (b,p)->{b.writeBlockPos(p.pos);b.writeVarInt(p.index);}, b->new SaveReactionPayload(b.readBlockPos(),b.readVarInt()));
    public static void handle(SaveReactionPayload payload, net.neoforged.neoforge.network.handling.IPayloadContext context) {
        context.enqueueWork(() -> {
            if (!(context.player() instanceof ServerPlayer player) || player.blockPosition().distSqr(payload.pos) > 64) return;
            if (!(player.level().getBlockEntity(payload.pos) instanceof ReactionTesterBlockEntity tester)) return;
            if (payload.index < 0 || payload.index >= tester.lastResults().size()) return;
            var stack = ReactionSchematicItem.create(tester.lastResults().get(payload.index));
            if (!player.addItem(stack)) player.drop(stack, false);
        });
    }
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
