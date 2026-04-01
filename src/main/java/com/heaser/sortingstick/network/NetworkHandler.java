package com.heaser.sortingstick.network;

import com.heaser.sortingstick.SortingStick;
import com.heaser.sortingstick.network.packets.SortAnimationPacket;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class NetworkHandler {

    public static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar(SortingStick.MODID);
        clientbound(registrar, SortAnimationPacket.TYPE, SortAnimationPacket.STREAM_CODEC);
        SortingStick.LOGGER.debug("Registered network packets for {}", SortingStick.MODID);
    }

    private static <T extends ClientboundPacket> void clientbound(
            PayloadRegistrar registrar,
            CustomPacketPayload.Type<T> type,
            StreamCodec<RegistryFriendlyByteBuf, T> codec) {
        registrar.playToClient(type, codec, ClientboundPacket::handleOnClient);
    }
}
