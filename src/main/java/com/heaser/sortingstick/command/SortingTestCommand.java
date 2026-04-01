package com.heaser.sortingstick.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import net.minecraft.core.Direction;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;




// Admin command: /sortingstick test [count]
// This one helps me test that the sorting sticks performs as expected.
public class SortingTestCommand {

    private enum ContainerType { SINGLE_CHEST, DOUBLE_CHEST, BARREL }

    private static final int[][] RING_OFFSETS = {
            // Inner ring (~3 blocks out)
            { 3,  0}, {-3,  0}, { 0,  3}, { 0, -3},
            { 2,  2}, {-2,  2}, { 2, -2}, {-2, -2},
            // Outer ring (~5 blocks out)
            { 5,  0}, {-5,  0}, { 0,  5}, { 0, -5},
            { 4,  3}, {-4,  3}, { 4, -3}, {-4, -3},
            { 3,  4}, {-3,  4}, { 3, -4}, {-3, -4},
            { 5,  2}, {-5,  2}, { 5, -2}, {-5, -2},
    };

    private static final List<Item> ITEM_POOL = Arrays.asList(
            Items.STONE, Items.DIRT, Items.GRAVEL, Items.OAK_LOG,
            Items.STICK, Items.IRON_INGOT, Items.GOLD_INGOT, Items.COAL,
            Items.WHEAT, Items.APPLE, Items.COBBLESTONE, Items.SAND,
            Items.GLASS, Items.FEATHER, Items.STRING, Items.BONE,
            Items.IRON_NUGGET, Items.GOLD_NUGGET, Items.PAPER, Items.SUGAR_CANE
    );


    private static final List<Item> TOOL_POOL = Arrays.asList(
            Items.IRON_SWORD, Items.DIAMOND_SWORD, Items.NETHERITE_SWORD,
            Items.IRON_PICKAXE, Items.DIAMOND_PICKAXE, Items.NETHERITE_PICKAXE,
            Items.IRON_AXE, Items.DIAMOND_AXE,
            Items.IRON_SHOVEL, Items.DIAMOND_SHOVEL,
            Items.IRON_HOE, Items.DIAMOND_HOE
    );


    private static final List<ResourceKey<Enchantment>> SWORD_ENCHANTS = Arrays.asList(
            Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.KNOCKBACK, Enchantments.FIRE_ASPECT, Enchantments.LOOTING,
            Enchantments.UNBREAKING, Enchantments.MENDING
    );


    private static final List<ResourceKey<Enchantment>> TOOL_ENCHANTS = Arrays.asList(
            Enchantments.EFFICIENCY, Enchantments.FORTUNE, Enchantments.SILK_TOUCH,
            Enchantments.UNBREAKING, Enchantments.MENDING
    );

    public static void onRegisterCommands(RegisterCommandsEvent event) {
        register(event.getDispatcher());
    }

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(
                Commands.literal("sortingstick")
                        .requires(source -> source.hasPermission(2))
                        .then(Commands.literal("test")
                                .executes(ctx -> placeTestContainers(ctx.getSource(), 6))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 20))
                                        .executes(ctx -> placeTestContainers(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "count")))))
                        .then(Commands.literal("testnbt")
                                .executes(ctx -> placeNbtTestContainers(ctx.getSource(), 6))
                                .then(Commands.argument("count", IntegerArgumentType.integer(1, 20))
                                        .executes(ctx -> placeNbtTestContainers(
                                                ctx.getSource(),
                                                IntegerArgumentType.getInteger(ctx, "count")))))
        );
    }

    private static int placeTestContainers(CommandSourceStack source, int count) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("Must be run by a player."));
            return 0;
        }

        ServerLevel level = source.getLevel();
        Random random = new Random();

        // Build shared + unique item lists
        List<Item> pool = new ArrayList<>(ITEM_POOL);
        Collections.shuffle(pool, random);
        List<Item> sharedItems = new ArrayList<>(pool.subList(0, Math.min(5, pool.size())));
        List<Item> uniquePool  = new ArrayList<>(pool.subList(Math.min(5, pool.size()), pool.size()));

        // Shuffle placement positions
        List<int[]> offsets = new ArrayList<>(Arrays.asList(RING_OFFSETS));
        Collections.shuffle(offsets, random);

        int placed = 0;
        int offsetIdx = 0;

        for (int i = 0; i < count && offsetIdx < offsets.size(); i++) {
            ContainerType type = pickType(random);

            int[] off = offsets.get(offsetIdx++);
            BlockPos base = validPos(level, player.blockPosition().east(off[0]).south(off[1]));

            switch (type) {
                case SINGLE_CHEST -> {
                    clearContainer(level, base);
                    level.setBlock(base, Blocks.CHEST.defaultBlockState(), 3);
                    if (level.getBlockEntity(base) instanceof ChestBlockEntity chest) {
                        fillContainer(chest, sharedItems, uniquePool, random);
                        placed++;
                    }
                }

                case BARREL -> {
                    clearContainer(level, base);
                    level.setBlock(base, Blocks.BARREL.defaultBlockState()
                            .setValue(BarrelBlock.FACING, Direction.UP), 3);
                    if (level.getBlockEntity(base) instanceof BarrelBlockEntity barrel) {
                        fillContainer(barrel, sharedItems, uniquePool, random);
                        placed++;
                    }
                }

                case DOUBLE_CHEST -> {
                    BlockPos partnerBase = base.east();

                    clearContainer(level, base);
                    clearContainer(level, partnerBase);

                    level.setBlock(base, Blocks.CHEST.defaultBlockState()
                            .setValue(ChestBlock.TYPE, ChestType.LEFT), 3);
                    level.setBlock(partnerBase, Blocks.CHEST.defaultBlockState()
                            .setValue(ChestBlock.TYPE, ChestType.RIGHT), 3);

                    if (level.getBlockEntity(base) instanceof ChestBlockEntity half1) {
                        fillContainer(half1, sharedItems, uniquePool, random);
                    }
                    if (level.getBlockEntity(partnerBase) instanceof ChestBlockEntity half2) {
                        fillContainer(half2, sharedItems, uniquePool, random);
                    }
                    placed++;
                }
            }
        }

        final int finalPlaced = placed;
        source.sendSuccess(
                () -> Component.literal("Placed " + finalPlaced
                        + " test containers (mix of chests, double chests, barrels) around you."
                        + " Use your Sorting Stick!"),
                true
        );
        return placed;
    }

    private static ContainerType pickType(Random random) {
        int roll = random.nextInt(10);
        if (roll < 4) return ContainerType.SINGLE_CHEST;
        if (roll < 8) return ContainerType.DOUBLE_CHEST;
        return ContainerType.BARREL;
    }

    private static BlockPos validPos(ServerLevel level, BlockPos base) {
        for (int dy = 0; dy <= 4; dy++) {
            BlockPos candidate = base.above(dy);
            if (level.getBlockState(candidate).canBeReplaced()) {
                return candidate;
            }
        }
        return base.above(5);
    }


    private static void clearContainer(ServerLevel level, BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof Container container) {
            for (int i = 0; i < container.getContainerSize(); i++) {
                container.setItem(i, ItemStack.EMPTY);
            }
        }
    }


    private static void fillContainer(Container inv, List<Item> sharedItems,
                                       List<Item> uniquePool, Random random) {
        int size = inv.getContainerSize();

        int slot = 0;
        for (Item item : sharedItems) {
            if (slot >= size / 2) break;
            inv.setItem(slot++, new ItemStack(item, 3 + random.nextInt(10)));
        }

        slot = size / 2;

        int splitCount = 1 + random.nextInt(2);
        Collections.shuffle(sharedItems, random);
        for (int s = 0; s < splitCount && slot < size - 3; s++) {
            inv.setItem(slot++, new ItemStack(sharedItems.get(s), 1 + random.nextInt(6)));
        }

        // Unique items fill remaining slots
        List<Item> shuffledUnique = new ArrayList<>(uniquePool);
        Collections.shuffle(shuffledUnique, random);
        int uniqueCount = 2 + random.nextInt(3);
        for (int u = 0; u < uniqueCount && u < shuffledUnique.size() && slot < size; u++) {
            inv.setItem(slot++, new ItemStack(shuffledUnique.get(u), 1 + random.nextInt(16)));
        }
    }

// Admin command: /sortingstick testnbt [count]
// This one helps me test that the sorting sticks performs as expected with items that stack to 1.


    private static int placeNbtTestContainers(CommandSourceStack source, int count) {
        ServerPlayer player;
        try {
            player = source.getPlayerOrException();
        } catch (Exception e) {
            source.sendFailure(Component.literal("Must be run by a player."));
            return 0;
        }

        ServerLevel level = source.getLevel();
        Random random = new Random();

        List<Item> pool = new ArrayList<>(TOOL_POOL);
        Collections.shuffle(pool, random);
        List<Item> sharedTools  = new ArrayList<>(pool.subList(0, Math.min(4, pool.size())));
        List<Item> uniqueTools  = new ArrayList<>(pool.subList(Math.min(4, pool.size()), pool.size()));

        List<int[]> offsets = new ArrayList<>(Arrays.asList(RING_OFFSETS));
        Collections.shuffle(offsets, random);

        int placed = 0;
        int offsetIdx = 0;

        for (int i = 0; i < count && offsetIdx < offsets.size(); i++) {
            ContainerType type = pickType(random);
            int[] off = offsets.get(offsetIdx++);
            BlockPos base = validPos(level, player.blockPosition().east(off[0]).south(off[1]));

            switch (type) {
                case SINGLE_CHEST -> {
                    clearContainer(level, base);
                    level.setBlock(base, Blocks.CHEST.defaultBlockState(), 3);
                    if (level.getBlockEntity(base) instanceof ChestBlockEntity chest) {
                        fillContainerWithTools(chest, sharedTools, uniqueTools, level, random);
                        placed++;
                    }
                }
                case BARREL -> {
                    clearContainer(level, base);
                    level.setBlock(base, Blocks.BARREL.defaultBlockState()
                            .setValue(BarrelBlock.FACING, Direction.UP), 3);
                    if (level.getBlockEntity(base) instanceof BarrelBlockEntity barrel) {
                        fillContainerWithTools(barrel, sharedTools, uniqueTools, level, random);
                        placed++;
                    }
                }
                case DOUBLE_CHEST -> {
                    BlockPos partnerBase = base.east();
                    clearContainer(level, base);
                    clearContainer(level, partnerBase);
                    level.setBlock(base, Blocks.CHEST.defaultBlockState()
                            .setValue(ChestBlock.TYPE, ChestType.LEFT), 3);
                    level.setBlock(partnerBase, Blocks.CHEST.defaultBlockState()
                            .setValue(ChestBlock.TYPE, ChestType.RIGHT), 3);
                    if (level.getBlockEntity(base) instanceof ChestBlockEntity half1) {
                        fillContainerWithTools(half1, sharedTools, uniqueTools, level, random);
                    }
                    if (level.getBlockEntity(partnerBase) instanceof ChestBlockEntity half2) {
                        fillContainerWithTools(half2, sharedTools, uniqueTools, level, random);
                    }
                    placed++;
                }
            }
        }

        final int finalPlaced = placed;
        source.sendSuccess(
                () -> Component.literal("Placed " + finalPlaced
                        + " test containers with randomly enchanted/damaged tools around you."
                        + " Use your Sorting Stick!"),
                true
        );
        return placed;
    }

    private static void fillContainerWithTools(Container inv, List<Item> sharedTools,
                                               List<Item> uniqueTools,
                                               ServerLevel level, Random random) {
        int size = inv.getContainerSize();
        int slot = 0;

        for (Item tool : sharedTools) {
            for (int copy = 0; copy < 2 && slot < size; copy++) {
                inv.setItem(slot++, makeRandomTool(tool, level, random));
            }
            slot++; // intentional gap between tool types
        }

        if (slot < size / 2) slot = size / 2;

        List<Item> shuffledUnique = new ArrayList<>(uniqueTools);
        Collections.shuffle(shuffledUnique, random);
        int uniqueCount = 1 + random.nextInt(2);
        for (int u = 0; u < uniqueCount && u < shuffledUnique.size() && slot < size; u++) {
            inv.setItem(slot++, makeRandomTool(shuffledUnique.get(u), level, random));
        }
    }

    private static ItemStack makeRandomTool(Item toolItem, ServerLevel level, Random random) {
        ItemStack stack = new ItemStack(toolItem);

        // Random durability
        int maxDamage = stack.getMaxDamage();
        if (maxDamage > 1) {
            stack.setDamageValue(random.nextInt(maxDamage - 1));
        }

        boolean isSword = toolItem == Items.IRON_SWORD
                || toolItem == Items.DIAMOND_SWORD
                || toolItem == Items.NETHERITE_SWORD;
        List<ResourceKey<Enchantment>> enchPool = new ArrayList<>(isSword ? SWORD_ENCHANTS : TOOL_ENCHANTS);
        Collections.shuffle(enchPool, random);

        // Apply enchantments
        int enchCount = 1 + random.nextInt(Math.min(3, enchPool.size()));
        var enchRegistry = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT);
        ItemEnchantments.Mutable mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);

        for (int e = 0; e < enchCount; e++) {
            enchRegistry.get(enchPool.get(e)).ifPresent((Holder<Enchantment> holder) -> {
                int maxLevel = holder.value().getMaxLevel();
                mutable.set(holder, 1 + random.nextInt(maxLevel));
            });
        }

        stack.set(DataComponents.ENCHANTMENTS, mutable.toImmutable());
        return stack;
    }
}
