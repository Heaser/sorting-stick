package com.heaser.sortingstick.sorting;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;


public class ItemHandlerContainerAdapter implements ISlotLimitContainer {

    private final IItemHandler handler;

    public ItemHandlerContainerAdapter(IItemHandler handler) {
        this.handler = handler;
    }

    @Override
    public int getContainerSize() {
        return handler.getSlots();
    }

    @Override
    public int getSlotLimit(int slot) {
        return handler.getSlotLimit(slot);
    }

    @Override
    public boolean isEmpty() {
        for (int i = 0; i < handler.getSlots(); i++) {
            if (!handler.getStackInSlot(i).isEmpty()) return false;
        }
        return true;
    }

    @Override
    public ItemStack getItem(int slot) {
        return handler.getStackInSlot(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        return handler.extractItem(slot, amount, false);
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        ItemStack current = handler.getStackInSlot(slot);
        return handler.extractItem(slot, current.getCount(), false);
    }

    @Override
    public void setItem(int slot, ItemStack newStack) {
        ItemStack current = handler.getStackInSlot(slot);

        if (newStack.isEmpty()) {
            if (!current.isEmpty()) {
                handler.extractItem(slot, current.getCount(), false);
            }
            return;
        }

        if (!current.isEmpty() && ItemStack.isSameItemSameComponents(current, newStack)) {
            int diff = newStack.getCount() - current.getCount();
            if (diff > 0) {
                handler.insertItem(slot, new ItemStack(newStack.getItem(), diff), false);
            } else if (diff < 0) {
                handler.extractItem(slot, -diff, false);
            }
            return;
        }


        if (current.isEmpty()) {
            handler.insertItem(slot, newStack, false);
            return;
        }

        ItemStack leftover = handler.insertItem(slot, newStack, true);
        if (leftover.isEmpty()) {
            handler.extractItem(slot, current.getCount(), false);
            handler.insertItem(slot, newStack, false);
        }

    }

    int directInsert(int slot, ItemStack stack) {
        ItemStack leftover = handler.insertItem(slot, stack, false);
        return stack.getCount() - leftover.getCount();
    }

    ItemStack directExtract(int slot, int amount) {
        return handler.extractItem(slot, amount, false);
    }

    @Override
    public void setChanged() {
        // IItemHandler implementations manage their own dirty tracking
    }

    @Override
    public boolean stillValid(Player player) {
        return true;
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < handler.getSlots(); i++) {
            ItemStack stack = handler.getStackInSlot(i);
            if (!stack.isEmpty()) {
                handler.extractItem(i, stack.getCount(), false);
            }
        }
    }
}
