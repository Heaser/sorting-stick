package com.heaser.sortingstick.config;

import net.neoforged.neoforge.common.ModConfigSpec;

public class SortingStickConfig {

    public enum FilterMode { BLACKLIST, WHITELIST }

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.IntValue SORTING_RADIUS = BUILDER
            .comment("Radius in blocks around the player to scan for inventories when using the Sorting Stick.")
            .defineInRange("sortingRadius", 16, 8, 64);

    public static final ModConfigSpec.EnumValue<FilterMode> FILTER_MODE = BUILDER
            .comment("BLACKLIST: all containers are sorted except those in the sortingstick:blacklist block tag.",
                     "WHITELIST: only containers in the sortingstick:whitelist block tag are sorted.")
            .defineEnum("filterMode", FilterMode.BLACKLIST);

    public static final ModConfigSpec SPEC = BUILDER.build();
}
