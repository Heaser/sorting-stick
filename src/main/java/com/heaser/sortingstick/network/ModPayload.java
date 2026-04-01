package com.heaser.sortingstick.network;

import com.heaser.sortingstick.SortingStick;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;

public interface ModPayload extends CustomPacketPayload {

    static <T extends CustomPacketPayload> CustomPacketPayload.Type<T> createType(String name) {
        return new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(SortingStick.MODID, name));
    }
}
