package com.heaser.sortingstick.client;

import com.heaser.sortingstick.SortingStick;
import com.heaser.sortingstick.block.entity.DumpingChestBlockEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.properties.ChestType;

public class DumpingChestRenderer extends ChestRenderer<DumpingChestBlockEntity> {

    private static final Material DUMPING_CHEST_MATERIAL = new Material(
            Sheets.CHEST_SHEET,
            ResourceLocation.fromNamespaceAndPath(SortingStick.MODID, "entity/chest/dumping_chest")
    );

    public DumpingChestRenderer(BlockEntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected Material getMaterial(DumpingChestBlockEntity blockEntity, ChestType chestType) {
        return DUMPING_CHEST_MATERIAL;
    }
}
