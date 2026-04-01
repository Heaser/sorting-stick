package com.heaser.sortingstick.client;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

public class ClientEvents {
    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onClientTick(LevelTickEvent.Post event) {
        if (event.getLevel().isClientSide()) {
            SortingAnimationRenderer.tick();
        }
    }
    @SuppressWarnings("unused")
    @SubscribeEvent
    public static void onRenderWorld(RenderLevelStageEvent event) {
        if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER) {
            SortingAnimationRenderer.render(event);
        }
    }
}
