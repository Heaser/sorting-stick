package com.heaser.sortingstick;

import com.heaser.sortingstick.item.SortingStickItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredItem;
import net.minecraft.world.item.Item;

public class ModItems {

    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SortingStick.MODID);

    public static final DeferredItem<SortingStickItem> SORTING_STICK =
            ITEMS.register("sorting_stick",
                    () -> new SortingStickItem(new Item.Properties().stacksTo(64)));
}
