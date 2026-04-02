package com.heaser.sortingstick;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class ModTags {

    public static final TagKey<Block> BLACKLIST = BlockTags.create(
            ResourceLocation.fromNamespaceAndPath(SortingStick.MODID, "blacklist"));

    public static final TagKey<Block> WHITELIST = BlockTags.create(
            ResourceLocation.fromNamespaceAndPath(SortingStick.MODID, "whitelist"));
}
