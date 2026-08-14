package com.parswha.createchemworks.chemistry;

import com.parswha.createchemworks.CreateChemworks;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public record ConfigureTesterPayload(BlockPos pos,double temperatureK,double pressureKpa,double[] concentrations,boolean[] catalysts) implements CustomPacketPayload {
    public static final Type<ConfigureTesterPayload> TYPE=new Type<>(ResourceLocation.fromNamespaceAndPath(CreateChemworks.MOD_ID,"configure_tester"));
    public static final StreamCodec<RegistryFriendlyByteBuf,ConfigureTesterPayload> STREAM_CODEC=StreamCodec.of((b,p)->{b.writeBlockPos(p.pos);b.writeDouble(p.temperatureK);b.writeDouble(p.pressureKpa);for(int i=0;i<ReactionTesterBlockEntity.CAPACITY;i++){b.writeDouble(p.concentrations[i]);b.writeBoolean(p.catalysts[i]);}},b->{var pos=b.readBlockPos();double t=b.readDouble(),p=b.readDouble();double[] c=new double[ReactionTesterBlockEntity.CAPACITY];boolean[] r=new boolean[c.length];for(int i=0;i<c.length;i++){c[i]=b.readDouble();r[i]=b.readBoolean();}return new ConfigureTesterPayload(pos,t,p,c,r);});
    public static void handle(ConfigureTesterPayload payload,net.neoforged.neoforge.network.handling.IPayloadContext context){context.enqueueWork(()->{if(context.player().blockPosition().distSqr(payload.pos)>64)return;if(context.player().level().getBlockEntity(payload.pos) instanceof ReactionTesterBlockEntity tester)try{tester.configure(payload.temperatureK,payload.pressureKpa,payload.concentrations,payload.catalysts);if(context.player().level() instanceof net.minecraft.server.level.ServerLevel server)tester.runAll(server);if(context.player() instanceof net.minecraft.server.level.ServerPlayer player)net.neoforged.neoforge.network.PacketDistributor.sendToPlayer(player,OpenTesterPayload.from(payload.pos,tester));}catch(IllegalArgumentException e){context.player().sendSystemMessage(net.minecraft.network.chat.Component.literal(e.getMessage()));}});}
    @Override public Type<? extends CustomPacketPayload> type(){return TYPE;}
}
