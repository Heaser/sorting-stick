package com.heaser.sortingstick.sorting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;

public record ChestInventory(BlockPos primaryPos, Container inventory, boolean isDouble) {
}
