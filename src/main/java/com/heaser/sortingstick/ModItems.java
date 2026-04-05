package com.heaser.sortingstick;

import com.heaser.sortingstick.item.DumpingChestBlockItem;
import com.heaser.sortingstick.item.SortingStickItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SortingStick.MODID);

    public static final DeferredItem<SortingStickItem> SORTING_STICK =
            ITEMS.register("sorting_stick",
                    () -> new SortingStickItem(new Item.Properties().stacksTo(64)));

    public static final DeferredItem<DumpingChestBlockItem> DUMPING_CHEST =
            ITEMS.register("dumping_chest",
                    () -> new DumpingChestBlockItem(ModBlocks.DUMPING_CHEST.get(), new Item.Properties()));
}
