package com.heaser.sortingstick.client;

import com.heaser.sortingstick.ModBlockEntities;
import com.heaser.sortingstick.ModParticleTypes;
import com.heaser.sortingstick.SortingStick;
import com.heaser.sortingstick.client.particles.SortParticle;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;

@EventBusSubscriber(modid = SortingStick.MODID, value = Dist.CLIENT)
public class ClientModEvents {
    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.DUMPING_CHEST.get(), DumpingChestRenderer::new);
    }

    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void registerParticleProviders(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ModParticleTypes.SORT_TRAIL.get(),   SortParticle.TrailProvider::new);
        event.registerSpriteSet(ModParticleTypes.SORT_WISP.get(),    SortParticle.WispProvider::new);
        event.registerSpriteSet(ModParticleTypes.SORT_BURST.get(),   SortParticle.BurstProvider::new);
        event.registerSpriteSet(ModParticleTypes.SORT_SPARKLE.get(), SortParticle.SparkleProvider::new);
    }
}
