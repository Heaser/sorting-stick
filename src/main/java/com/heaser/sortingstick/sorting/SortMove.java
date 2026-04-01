package com.heaser.sortingstick.sorting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;


public record SortMove(BlockPos fromPos, BlockPos toPos, ItemStack item) {
}
