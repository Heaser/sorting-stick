package com.heaser.sortingstick;

import com.heaser.sortingstick.block.entity.DumpingChestBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SortingStick.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DumpingChestBlockEntity>> DUMPING_CHEST =
            BLOCK_ENTITY_TYPES.register("dumping_chest",
                    () -> BlockEntityType.Builder
                            .of(DumpingChestBlockEntity::new, ModBlocks.DUMPING_CHEST.get())
                            .build(null));
}
