package com.heaser.sortingstick.network.packets;

import com.heaser.sortingstick.client.SortingAnimationRenderer;
import com.heaser.sortingstick.network.ClientboundPacket;
import com.heaser.sortingstick.network.ModPayload;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public record SortAnimationPacket(BlockPos fromPos, BlockPos toPos, ItemStack item, int delayTicks)
        implements ClientboundPacket {

    public static final CustomPacketPayload.Type<SortAnimationPacket> TYPE =
            ModPayload.createType("sort_animation");

    public static final StreamCodec<RegistryFriendlyByteBuf, SortAnimationPacket> STREAM_CODEC =
            StreamCodec.ofMember(SortAnimationPacket::write, SortAnimationPacket::decode);

    public static SortAnimationPacket decode(RegistryFriendlyByteBuf buf) {
        BlockPos from = buf.readBlockPos();
        BlockPos to = buf.readBlockPos();
        ItemStack item = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        int delay = buf.readInt();
        return new SortAnimationPacket(from, to, item, delay);
    }

    public void write(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(fromPos);
        buf.writeBlockPos(toPos);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, item);
        buf.writeInt(delayTicks);
    }

    @Override
    public CustomPacketPayload.Type<SortAnimationPacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        SortingAnimationRenderer.addArc(fromPos, toPos, item, delayTicks);
    }
}
