package com.heaser.sortingstick;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SortingStick.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SORTING_STICK_TAB =
            TABS.register("sorting_stick_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.sortingstick.tab"))
                    .icon(() -> ModItems.SORTING_STICK.get().getDefaultInstance())
                    .displayItems((params, output) -> output.accept(ModItems.SORTING_STICK.get()))
                    .build());
}
