package com.heaser.sortingstick.sorting;

import net.minecraft.world.Container;

public interface ISlotLimitContainer extends Container {
    int getSlotLimit(int slot);
}
