package com.heaser.sortingstick.item;

import com.heaser.sortingstick.ModItems;
import com.heaser.sortingstick.ModParticleTypes;
import com.heaser.sortingstick.config.SortingStickConfig;
import com.heaser.sortingstick.network.packets.SortAnimationPacket;
import com.heaser.sortingstick.sorting.SortMove;
import com.heaser.sortingstick.sorting.SortResult;
import com.heaser.sortingstick.sorting.SortingEngine;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SortingStickItem extends Item {

    public SortingStickItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level.isClientSide()) {
            return InteractionResultHolder.success(stack);
        }

        ServerLevel serverLevel = (ServerLevel) level;
        ServerPlayer serverPlayer = (ServerPlayer) player;

        SortResult result = SortingEngine.sort(serverLevel, serverPlayer);

        if (result.nothingToDo()) {
            player.sendSystemMessage(Component.translatable(
                    "item.sortingstick.sorting_stick.message.already_sorted"));
            return InteractionResultHolder.pass(stack);
        }

        if (!result.anythingActuallyDone()) {
            player.sendSystemMessage(Component.translatable(
                    "item.sortingstick.sorting_stick.message.no_room"));
            sendFailureDetails(serverLevel, serverPlayer, result);
            return InteractionResultHolder.pass(stack);
        }

        EquipmentSlot slot = hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND;
        player.onEquippedItemBroken(stack.getItem(), slot);
        stack.shrink(1);
        player.getCooldowns().addCooldown(ModItems.SORTING_STICK.get(), 100);
        player.playSound(SoundEvents.ENDER_CHEST_OPEN, 1.0f, 1.2f);

        for (SortMove move : result.moves()) {
            PacketDistributor.sendToPlayer(
                    serverPlayer,
                    new SortAnimationPacket(move.fromPos(), move.toPos(), move.item(), 0)
            );
        }

        if (result.hasPartialFailure()) {
            player.sendSystemMessage(Component.translatable(
                    "item.sortingstick.sorting_stick.message.partial_failure"));
            sendFailureDetails(serverLevel, serverPlayer, result);
        }

        return InteractionResultHolder.success(stack);
    }


    private static void sendFailureDetails(ServerLevel level, ServerPlayer player, SortResult result) {
        // Deduplicate: multiple failed stacks in the same chest for the same item type → group
        Map<String, SortResult.FailedItem> deduped = new LinkedHashMap<>();
        for (SortResult.FailedItem fi : result.failedItems()) {
            String key = fi.item().toString() + "@" + fi.chestPos().toShortString();
            deduped.merge(key, fi, (a, b) ->
                    new SortResult.FailedItem(a.item(), a.count() + b.count(), a.chestPos()));
        }

        for (SortResult.FailedItem fi : deduped.values()) {
            String itemName = fi.item().getDescription().getString();
            String coords = "(" + fi.chestPos().getX() + ", " + fi.chestPos().getY() + ", " + fi.chestPos().getZ() + ")";
            player.sendSystemMessage(Component.translatable(
                    "item.sortingstick.sorting_stick.message.failed_item",
                    itemName, fi.count(), coords
            ));
            highlightChest(level, player, fi.chestPos());
        }
    }

    private static void highlightChest(ServerLevel level, ServerPlayer player, net.minecraft.core.BlockPos pos) {
        double cx = pos.getX() + 0.5;
        double cy = pos.getY() + 1.0;
        double cz = pos.getZ() + 0.5;
        // Send to only the sorting player, forced (ignores distance)
        // Each call with count=0 sends one exact particle; scatter positions manually for multi-column effect
        double[] offsets = { -0.3, 0.0, 0.3, -0.15, 0.15 };
        for (double off : offsets) {
            level.sendParticles(player, ModParticleTypes.SORT_BURST.get(), true, cx + off, cy, cz + off * 0.5, 0, 0, 1, 0, 0.12);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable("item.sortingstick.sorting_stick.tooltip"));
        tooltipComponents.add(Component.translatable(
                "item.sortingstick.sorting_stick.tooltip.detail",
                SortingStickConfig.SORTING_RADIUS.get()
        ));
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
