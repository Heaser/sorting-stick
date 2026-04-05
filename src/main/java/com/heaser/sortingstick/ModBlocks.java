package com.heaser.sortingstick;

import com.heaser.sortingstick.block.DumpingChestBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(SortingStick.MODID);

    public static final DeferredBlock<DumpingChestBlock> DUMPING_CHEST =
            BLOCKS.register("dumping_chest",
                    () -> new DumpingChestBlock(
                            BlockBehaviour.Properties.of()
                                    .strength(2.5f)
                                    .sound(SoundType.WOOD)
                                    .noOcclusion()
                    ));
}
