package com.heaser.sortingstick.sorting;

import com.heaser.sortingstick.SortingStick;
import com.heaser.sortingstick.config.SortingStickConfig;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public class SortingEngine {

    public static SortResult sort(ServerLevel level, ServerPlayer player) {
        int radius = SortingStickConfig.SORTING_RADIUS.get();
        List<ChestInventory> inventories = InventoryScanner.scan(level, player, player.blockPosition(), radius);
        SortingStick.LOGGER.debug("[SortingEngine] Scanned radius={}, found {} inventories", radius, inventories.size());

        if (inventories.isEmpty()) {
            SortingStick.LOGGER.debug("[SortingEngine] No inventories found, nothing to do");
            return SortResult.NOTHING_TO_DO;
        }

        boolean internalChanged = false;
        for (ChestInventory ci : inventories) {
            if (ci.inventory() instanceof ISlotLimitContainer) continue;
            if (consolidateInternalSlots(ci.inventory())) {
                SortingStick.LOGGER.debug("[SortingEngine] Step1: consolidated internal stacks at {}", ci.primaryPos());
                internalChanged = true;
            }
        }

        Map<ChestInventory, Map<Item, Integer>> prePassCounts = computeItemCounts(inventories);

        List<SortResult.FailedItem> failedItems = new ArrayList<>();
        boolean hasPartialFailure = false;

        for (int pass = 0; pass < 8; pass++) {
            Map<Item, List<ItemLocation>> itemMap = buildItemMap(inventories);
            SortingStick.LOGGER.debug("[SortingEngine] Pass {} - {} distinct item types across all chests", pass, itemMap.size());

            List<SortResult.FailedItem> passFailures = new ArrayList<>();
            boolean passHasFailure = false;
            boolean passMadeProgress = false;

            for (Map.Entry<Item, List<ItemLocation>> entry : itemMap.entrySet()) {
                Item item = entry.getKey();
                List<ItemLocation> locations = entry.getValue();

                Map<ChestInventory, List<Integer>> slotsByChest = new LinkedHashMap<>();
                for (ItemLocation loc : locations) {
                    if (!loc.chest().inventory().getItem(loc.slot()).isEmpty())
                        slotsByChest.computeIfAbsent(loc.chest(), k -> new ArrayList<>()).add(loc.slot());
                }
                if (slotsByChest.size() <= 1) continue;
                SortingStick.LOGGER.debug("[SortingEngine]   {} spread across {} chests", item.getDescriptionId(), slotsByChest.size());

                List<ChestInventory> ranked = new ArrayList<>(slotsByChest.keySet());
                ranked.sort((a, b) -> {
                    if (a.isDumpingChest() != b.isDumpingChest()) {
                        return a.isDumpingChest() ? 1 : -1;
                    }
                    int countA = slotsByChest.get(a).stream().mapToInt(s -> a.inventory().getItem(s).getCount()).sum();
                    int countB = slotsByChest.get(b).stream().mapToInt(s -> b.inventory().getItem(s).getCount()).sum();
                    return Integer.compare(countB, countA);
                });

                for (int srcRank = ranked.size() - 1; srcRank >= 1; srcRank--) {
                    ChestInventory srcChest = ranked.get(srcRank);
                    List<Integer> srcSlots = slotsByChest.get(srcChest);
                    for (int srcSlot : new ArrayList<>(srcSlots)) {
                        if (srcChest.inventory().getItem(srcSlot).isEmpty()) continue;

                        boolean moved = false;
                        for (int dstRank = 0; dstRank < srcRank; dstRank++) {
                            ChestInventory dstChest = ranked.get(dstRank);
                            if (dstChest.isDumpingChest()) continue;
                            int transferred = transferSlotToChest(srcChest, srcSlot, dstChest);
                            if (transferred > 0) {
                                SortingStick.LOGGER.debug("[SortingEngine]     moved {}x{} from {} (rank {}) → {} (rank {})",
                                        transferred, item.getDescriptionId(),
                                        srcChest.primaryPos(), srcRank,
                                        dstChest.primaryPos(), dstRank);
                                moved = true;
                                passMadeProgress = true;
                            }
                            if (srcChest.inventory().getItem(srcSlot).isEmpty()) break;
                        }

                        ItemStack remaining = srcChest.inventory().getItem(srcSlot);
                        if (!remaining.isEmpty() && !moved) {
                            if (!ranked.get(0).isDumpingChest() && freeSlots(ranked.get(0).inventory()) > 0) {
                                SortingStick.LOGGER.debug("[SortingEngine]     STUCK {}x{} at {} - rank-0 has room but transfer failed",
                                        remaining.getCount(), item.getDescriptionId(), srcChest.primaryPos());
                                passHasFailure = true;
                                passFailures.add(new SortResult.FailedItem(item, remaining.getCount(), srcChest.primaryPos()));
                            } else {
                                SortingStick.LOGGER.debug("[SortingEngine]     {}x{} at {} - rank-0 full, items optimally packed",
                                        remaining.getCount(), item.getDescriptionId(), srcChest.primaryPos());
                            }
                        }
                    }
                }
            }

            failedItems = passFailures;
            hasPartialFailure = passHasFailure;

            if (!passMadeProgress) {
                SortingStick.LOGGER.debug("[SortingEngine] Pass {} made no progress - converged", pass);
                break;
            }
        }

        for (ChestInventory ci : inventories) {
            if (!(ci.inventory() instanceof ISlotLimitContainer)) {
                sortInternalSlots(ci.inventory());
            }
        }

        List<SortMove> moves = buildMovesFromDelta(inventories, prePassCounts);
        SortingStick.LOGGER.debug("[SortingEngine] Done - {} net moves, internalChanged={}, failures={}",
                moves.size(), internalChanged, failedItems.size());

        if (moves.isEmpty() && !internalChanged && failedItems.isEmpty()) {
            SortingStick.LOGGER.debug("[SortingEngine] Net result: nothing changed");
            return SortResult.NOTHING_TO_DO;
        }

        return new SortResult(moves, hasPartialFailure, internalChanged, failedItems);
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private record ItemLocation(ChestInventory chest, int slot, Item item) {}

    // Build item location map
    private static Map<Item, List<ItemLocation>> buildItemMap(List<ChestInventory> inventories) {
        Map<Item, List<ItemLocation>> map = new LinkedHashMap<>();
        for (ChestInventory ci : inventories) {
            Container inv = ci.inventory();
            for (int slot = 0; slot < inv.getContainerSize(); slot++) {
                ItemStack stack = inv.getItem(slot);
                if (stack.isEmpty()) continue;
                map.computeIfAbsent(stack.getItem(), k -> new ArrayList<>())
                        .add(new ItemLocation(ci, slot, stack.getItem()));
            }
        }
        return map;
    }

    private static boolean consolidateInternalSlots(Container inv) {
        boolean changed = false;
        int size = inv.getContainerSize();

        for (int i = 0; i < size - 1; i++) {
            ItemStack target = inv.getItem(i);
            if (target.isEmpty()) continue;

            int maxStack = target.getMaxStackSize();
            if (target.getCount() >= maxStack) continue; // already full, nothing to merge in

            for (int j = i + 1; j < size; j++) {
                ItemStack source = inv.getItem(j);
                if (source.isEmpty()) continue;
                if (!ItemStack.isSameItemSameComponents(target, source)) continue;

                int canFit = maxStack - target.getCount();
                int toMove = Math.min(canFit, source.getCount());

                target.grow(toMove);
                source.shrink(toMove);
                inv.setItem(i, target);
                inv.setItem(j, source.isEmpty() ? ItemStack.EMPTY : source);
                changed = true;

                if (target.getCount() >= maxStack) break;
            }
        }
        return changed;
    }


    private static int transferSlotToChest(ChestInventory src, int srcSlot, ChestInventory dst) {
        Container srcInv = src.inventory();
        Container dstInv = dst.inventory();

        boolean srcIsHandler = srcInv instanceof ItemHandlerContainerAdapter;
        boolean dstIsHandler = dstInv instanceof ItemHandlerContainerAdapter;

        ItemStack srcStack = srcInv.getItem(srcSlot);
        if (srcStack.isEmpty()) return 0;

        int maxStack = srcStack.getMaxStackSize();
        int transferred = 0;

        for (int dstSlot = 0; dstSlot < dstInv.getContainerSize(); dstSlot++) {
            if (srcIsHandler) srcStack = srcInv.getItem(srcSlot);
            if (srcStack.isEmpty()) break;

            ItemStack dstStack = dstInv.getItem(dstSlot);
            if (dstStack.isEmpty()) continue;
            if (!ItemStack.isSameItemSameComponents(dstStack, srcStack)) continue;

            int slotLimit = dstIsHandler
                    ? ((ISlotLimitContainer) dstInv).getSlotLimit(dstSlot)
                    : maxStack;
            if (dstStack.getCount() >= slotLimit) continue;

            int canFit = slotLimit - dstStack.getCount();
            int toMove = Math.min(canFit, srcStack.getCount());
            transferred += atomicTransfer(srcInv, srcSlot, srcStack, dstInv, dstSlot, dstStack, toMove, srcIsHandler, dstIsHandler);
        }

        for (int dstSlot = 0; dstSlot < dstInv.getContainerSize(); dstSlot++) {
            if (srcIsHandler) srcStack = srcInv.getItem(srcSlot);
            if (srcStack.isEmpty()) break;
            if (!dstInv.getItem(dstSlot).isEmpty()) continue;

            int slotLimit = dstIsHandler
                    ? ((ISlotLimitContainer) dstInv).getSlotLimit(dstSlot)
                    : maxStack;
            int toMove = Math.min(slotLimit, srcStack.getCount());
            transferred += atomicTransferToEmpty(srcInv, srcSlot, srcStack, dstInv, dstSlot, toMove, srcIsHandler, dstIsHandler);
        }

        return transferred;
    }

    private static int atomicTransfer(
            Container srcInv, int srcSlot, ItemStack srcStack,
            Container dstInv, int dstSlot, ItemStack dstStack,
            int toMove, boolean srcIsHandler, boolean dstIsHandler) {

        if (dstIsHandler) {
            ItemHandlerContainerAdapter dstAdapter = (ItemHandlerContainerAdapter) dstInv;
            if (srcIsHandler) {
                ItemStack simulated = ((ItemHandlerContainerAdapter) srcInv).getHandler()
                        .extractItem(srcSlot, toMove, true);
                if (simulated.isEmpty()) return 0;
                toMove = simulated.getCount();
            }
            int inserted = dstAdapter.directInsert(dstSlot, srcStack.copyWithCount(toMove));
            if (inserted <= 0) return 0;
            if (srcIsHandler) {
                ((ItemHandlerContainerAdapter) srcInv).directExtract(srcSlot, inserted);
            } else {
                srcStack.shrink(inserted);
                inv_setOrClear(srcInv, srcSlot, srcStack);
            }
            return inserted;

        } else if (srcIsHandler) {
            ItemStack extracted = ((ItemHandlerContainerAdapter) srcInv).directExtract(srcSlot, toMove);
            if (extracted.isEmpty()) return 0;
            dstStack.grow(extracted.getCount());
            dstInv.setItem(dstSlot, dstStack);
            return extracted.getCount();

        } else {
            dstStack.grow(toMove);
            srcStack.shrink(toMove);
            dstInv.setItem(dstSlot, dstStack);
            inv_setOrClear(srcInv, srcSlot, srcStack);
            return toMove;
        }
    }

    private static int atomicTransferToEmpty(
            Container srcInv, int srcSlot, ItemStack srcStack,
            Container dstInv, int dstSlot,
            int toMove, boolean srcIsHandler, boolean dstIsHandler) {

        if (dstIsHandler) {
            ItemHandlerContainerAdapter dstAdapter = (ItemHandlerContainerAdapter) dstInv;
            if (srcIsHandler) {
                ItemStack simulated = ((ItemHandlerContainerAdapter) srcInv).getHandler()
                        .extractItem(srcSlot, toMove, true);
                if (simulated.isEmpty()) return 0;
                toMove = simulated.getCount();
            }
            int inserted = dstAdapter.directInsert(dstSlot, srcStack.copyWithCount(toMove));
            if (inserted <= 0) return 0;
            if (srcIsHandler) {
                ((ItemHandlerContainerAdapter) srcInv).directExtract(srcSlot, inserted);
            } else {
                srcStack.shrink(inserted);
                inv_setOrClear(srcInv, srcSlot, srcStack);
            }
            return inserted;

        } else if (srcIsHandler) {
            ItemStack extracted = ((ItemHandlerContainerAdapter) srcInv).directExtract(srcSlot, toMove);
            if (extracted.isEmpty()) return 0;
            dstInv.setItem(dstSlot, extracted);
            return extracted.getCount();

        } else {
            dstInv.setItem(dstSlot, srcStack.split(toMove));
            inv_setOrClear(srcInv, srcSlot, srcStack);
            return toMove;
        }
    }

    private static void inv_setOrClear(Container inv, int slot, ItemStack stack) {
        inv.setItem(slot, stack.isEmpty() ? ItemStack.EMPTY : stack);
    }

    private static Map<ChestInventory, Map<Item, Integer>> computeItemCounts(List<ChestInventory> inventories) {
        Map<ChestInventory, Map<Item, Integer>> result = new LinkedHashMap<>();
        for (ChestInventory ci : inventories) {
            Map<Item, Integer> counts = new LinkedHashMap<>();
            Container inv = ci.inventory();
            for (int s = 0; s < inv.getContainerSize(); s++) {
                ItemStack stack = inv.getItem(s);
                if (!stack.isEmpty()) counts.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
            result.put(ci, counts);
        }
        return result;
    }


    private static List<SortMove> buildMovesFromDelta(List<ChestInventory> inventories,
            Map<ChestInventory, Map<Item, Integer>> before) {
        Map<ChestInventory, Map<Item, Integer>> after = computeItemCounts(inventories);

        Set<Item> changedItems = new LinkedHashSet<>();
        for (ChestInventory ci : inventories) {
            Map<Item, Integer> b = before.get(ci);
            Map<Item, Integer> a = after.get(ci);
            for (Item item : b.keySet()) {
                if (!a.getOrDefault(item, 0).equals(b.get(item))) changedItems.add(item);
            }
            for (Item item : a.keySet()) {
                if (!b.getOrDefault(item, 0).equals(a.get(item))) changedItems.add(item);
            }
        }

        List<SortMove> moves = new ArrayList<>();
        for (Item item : changedItems) {
            // Build mutable remaining-gain map for each destination
            Map<ChestInventory, Integer> remainingGain = new LinkedHashMap<>();
            for (ChestInventory ci : inventories) {
                int delta = after.get(ci).getOrDefault(item, 0) - before.get(ci).getOrDefault(item, 0);
                if (delta > 0) remainingGain.put(ci, delta);
            }

            for (ChestInventory src : inventories) {
                int lost = before.get(src).getOrDefault(item, 0) - after.get(src).getOrDefault(item, 0);
                if (lost <= 0) continue;
                for (ChestInventory dst : remainingGain.keySet()) {
                    if (lost <= 0) break;
                    int available = remainingGain.get(dst);
                    if (available <= 0) continue;
                    int moved = Math.min(lost, available);
                    moves.add(new SortMove(src.primaryPos(), dst.primaryPos(), new ItemStack(item, moved)));
                    remainingGain.put(dst, available - moved);
                    lost -= moved;
                }
            }
        }
        return moves;
    }

    private static int freeSlots(Container inv) {
        int free = 0;
        for (int s = 0; s < inv.getContainerSize(); s++) {
            if (inv.getItem(s).isEmpty()) free++;
        }
        return free;
    }


    private static void sortInternalSlots(Container inv) {
        int size = inv.getContainerSize();
        List<ItemStack> stacks = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            stacks.add(inv.getItem(i).copy());
        }

        stacks.sort((a, b) -> {
            if (a.isEmpty() && b.isEmpty()) return 0;
            if (a.isEmpty()) return 1;
            if (b.isEmpty()) return -1;
            // Primary: group by item type then by stack of that item type
            int cmp = a.getDescriptionId().compareTo(b.getDescriptionId());
            if (cmp != 0) return cmp;
            return Integer.compare(b.getCount(), a.getCount());
        });

        for (int i = 0; i < size; i++) {
            inv.setItem(i, stacks.get(i));
        }
    }
}
