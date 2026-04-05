package com.heaser.sortingstick.block.entity;

import com.heaser.sortingstick.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DumpingChestBlockEntity extends ChestBlockEntity {

    public DumpingChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DUMPING_CHEST.get(), pos, state);
    }

    @Override
    public void recheckOpen() {
        if (this.level == null) return;
        this.openersCounter.recheckOpeners(this.level, this.worldPosition, this.getBlockState());
    }
}
