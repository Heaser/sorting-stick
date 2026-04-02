package com.heaser.sortingstick.sorting;

import com.heaser.sortingstick.ModTags;
import com.heaser.sortingstick.config.SortingStickConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InventoryScanner {


    public static List<ChestInventory> scan(ServerLevel level, ServerPlayer player, BlockPos center, int radius) {
        List<ChestInventory> result = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();

        for (BlockPos pos : BlockPos.betweenClosed(
                center.offset(-radius, -radius, -radius),
                center.offset(radius, radius, radius))) {

            BlockPos immutablePos = pos.immutable();
            if (visited.contains(immutablePos)) continue;

            BlockState state = level.getBlockState(immutablePos);

            if (SortingStickConfig.FILTER_MODE.get() == SortingStickConfig.FilterMode.WHITELIST) {
                if (!state.is(ModTags.WHITELIST)) continue;
            } else {
                if (state.is(ModTags.BLACKLIST)) continue;
            }
            if (!(level.getBlockEntity(immutablePos) instanceof Container)) continue;
            if (!hasPermission(player, immutablePos)) continue;

            Block block = state.getBlock();

            if (block instanceof ChestBlock chestBlock) {
                Container combined = ChestBlock.getContainer(chestBlock, state, level, immutablePos, true);
                if (combined != null && combined.getContainerSize() > 27) {
                    Direction facing = ChestBlock.getConnectedDirection(state);
                    if (facing != null) {
                        BlockPos partnerPos = immutablePos.relative(facing);
                        visited.add(partnerPos.immutable());
                    }
                    result.add(new ChestInventory(immutablePos, combined, true));
                } else {
                    Container single = (Container) level.getBlockEntity(immutablePos);
                    result.add(new ChestInventory(immutablePos, single, false));
                }
            } else if (level.getBlockEntity(immutablePos) instanceof Container container) {
                result.add(new ChestInventory(immutablePos, container, false));
            }
        }

        return result;
    }
    // Tries to handle FTB Chunks and player permissions
    private static boolean hasPermission(ServerPlayer player, BlockPos pos) {
        BlockHitResult hitResult = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        PlayerInteractEvent.RightClickBlock event = new PlayerInteractEvent.RightClickBlock(
                player,
                InteractionHand.MAIN_HAND,
                pos,
                hitResult
        );
        NeoForge.EVENT_BUS.post(event);
        return !event.isCanceled();
    }
}
