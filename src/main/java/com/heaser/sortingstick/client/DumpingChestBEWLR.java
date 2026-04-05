package com.heaser.sortingstick.client;

import com.heaser.sortingstick.ModBlocks;
import com.heaser.sortingstick.block.entity.DumpingChestBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class DumpingChestBEWLR extends BlockEntityWithoutLevelRenderer {

    private static DumpingChestBEWLR instance;

    private DumpingChestBlockEntity chestEntity;

    public DumpingChestBEWLR(BlockEntityRenderDispatcher dispatcher, EntityModelSet modelSet) {
        super(dispatcher, modelSet);
    }

    public static DumpingChestBEWLR getInstance() {
        if (instance == null) {
            Minecraft mc = Minecraft.getInstance();
            instance = new DumpingChestBEWLR(mc.getBlockEntityRenderDispatcher(), mc.getEntityModels());
        }
        return instance;
    }

    @Override
    public void onResourceManagerReload(ResourceManager resourceManager) {
        this.chestEntity = new DumpingChestBlockEntity(
                BlockPos.ZERO,
                ModBlocks.DUMPING_CHEST.get().defaultBlockState()
        );
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext,
                              PoseStack poseStack, MultiBufferSource buffer,
                              int packedLight, int packedOverlay) {
        if (this.chestEntity == null) {
            onResourceManagerReload(null);
        }
        BlockEntityRenderer<DumpingChestBlockEntity> renderer =
                Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(this.chestEntity);
        if (renderer != null) {
            renderer.render(this.chestEntity, 0f, poseStack, buffer, packedLight, packedOverlay);
        }
    }
}
