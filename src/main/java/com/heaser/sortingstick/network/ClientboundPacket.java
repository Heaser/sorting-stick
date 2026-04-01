package com.heaser.sortingstick.network;

import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public interface ClientboundPacket extends ModPayload {

    default void handleOnClient(IPayloadContext context) {
        context.enqueueWork(() -> handleOnClient(context.player()));
    }

    default void handleOnClient(Player player) {
        throw new AbstractMethodError("Unimplemented handleOnClient on " + getClass());
    }
}
