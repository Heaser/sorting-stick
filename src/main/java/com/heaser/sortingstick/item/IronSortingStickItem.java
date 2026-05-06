package com.heaser.sortingstick.item;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;

public class IronSortingStickItem extends SortingStickItem {

    public IronSortingStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public int getEnchantmentValue() {
        return 14;
    }

    @Override
    protected void consumeStackOnSuccessfulSort(ItemStack stack, ServerPlayer player, EquipmentSlot heldSlot) {
        stack.hurtAndBreak(1, player, heldSlot);
    }

    @Override
    protected String getTooltipKeyPrefix() {
        return "item.sortingstick.iron_sorting_stick";
    }
}
