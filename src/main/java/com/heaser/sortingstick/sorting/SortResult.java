package com.heaser.sortingstick.sorting;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;

import java.util.Collections;
import java.util.List;


public class SortResult {
    public record FailedItem(Item item, int count, BlockPos chestPos) {}

    public static final SortResult NOTHING_TO_DO =
            new SortResult(Collections.emptyList(), false, false, Collections.emptyList());

    private final List<SortMove> moves;
    private final boolean internalConsolidationDone;
    private final boolean hasPartialFailure;
    private final List<FailedItem> failedItems;

    public SortResult(List<SortMove> moves, boolean hasPartialFailure,
                      boolean internalConsolidationDone, List<FailedItem> failedItems) {
        this.moves = moves;
        this.hasPartialFailure = hasPartialFailure;
        this.internalConsolidationDone = internalConsolidationDone;
        this.failedItems = failedItems;
    }

    public List<SortMove> moves() { return moves; }
    public boolean hasPartialFailure() { return hasPartialFailure; }
    public boolean internalConsolidationDone() { return internalConsolidationDone; }
    public List<FailedItem> failedItems() { return failedItems; }

    // If there's nothing to sort
    public boolean nothingToDo() {
        return moves.isEmpty() && !internalConsolidationDone && failedItems.isEmpty();
    }

    public boolean anythingActuallyDone() {
        return !moves.isEmpty() || internalConsolidationDone;
    }
}
