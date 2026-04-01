package com.heaser.sortingstick.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SortingStickConfig {

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue SORTING_RADIUS = BUILDER
            .comment("Radius in blocks around the player to scan for inventories when using the Sorting Stick.")
            .defineInRange("sortingRadius", 16, 8, 64);

public static final ModConfigSpec SPEC = BUILDER.build();
}
