package com.heaser.sortingstick.client;

import com.heaser.sortingstick.ModParticleTypes;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import java.util.ArrayList;
import java.util.List;

public class SortingAnimationRenderer {

    // Ticks between each arc launching.
    private static final int ARC_STAGGER_TICKS = 1;

    private static final List<AnimArc> ACTIVE_ARCS = new ArrayList<>();

    private record AnimArc(Vec3 from, Vec3 to, ItemStack item, int delayTicks, int ticksElapsed, int totalTicks) {
        AnimArc tick() {
            if (delayTicks > 0) {
                return new AnimArc(from, to, item, delayTicks - 1, ticksElapsed, totalTicks);
            }
            return new AnimArc(from, to, item, 0, ticksElapsed + 1, totalTicks);
        }

        boolean finished() {
            return delayTicks <= 0 && ticksElapsed >= totalTicks;
        }

        boolean active() {
            return delayTicks <= 0;
        }
    }

    public static void addArc(BlockPos from, BlockPos to, ItemStack item, int delayTicks) {
        Vec3 f = Vec3.atCenterOf(from).add(0, 0.5, 0);
        Vec3 t = Vec3.atCenterOf(to).add(0, 0.5, 0);
        double dist = f.distanceTo(t);
        int totalTicks = Math.max(30, (int) (dist * 1));
        int staggered = delayTicks + ACTIVE_ARCS.size() * ARC_STAGGER_TICKS;
        ACTIVE_ARCS.add(new AnimArc(f, t, item.copy(), staggered, 0, totalTicks));
    }

    public static void tick() {
        Minecraft mc = Minecraft.getInstance();
        List<AnimArc> updated = new ArrayList<>(ACTIVE_ARCS.size());

        for (AnimArc arc : ACTIVE_ARCS) {
            AnimArc next = arc.tick();

            if (arc.active() && next.finished()) {
                Vec3 dest = arc.to();

                if (mc.player != null) {
                    mc.player.playSound(SoundEvents.ITEM_PICKUP, 0.6f, 1.0f + (float) Math.random() * 0.4f);
                }

                if (mc.level != null) {
                    for (int p = 0; p < 10; p++) {
                        mc.level.addParticle(ModParticleTypes.SORT_BURST.get(),
                                dest.x + (Math.random() - 0.5) * 0.7,
                                dest.y + 0.5,
                                dest.z + (Math.random() - 0.5) * 0.7,
                                0, 0.24, 0);
                    }
                    for (int p = 0; p < 3; p++) {
                        mc.level.addParticle(ModParticleTypes.SORT_SPARKLE.get(),
                                dest.x, dest.y + 0.5, dest.z,
                                (Math.random() - 0.5) * 0.25,
                                Math.random() * 0.25 + 0.05,
                                (Math.random() - 0.5) * 0.25);
                    }
                }
            }

            if (next.active() && !next.finished() && mc.level != null) {
                float t = Math.min((float) next.ticksElapsed() / next.totalTicks(), 1.0f);
                Vec3 pos = bezier(arc.from(), arc.to(), t);
                // Trail sparkle: every other tick
                if (next.ticksElapsed() % 2 == 0) {
                    mc.level.addParticle(ModParticleTypes.SORT_TRAIL.get(),
                            pos.x, pos.y, pos.z,
                            0, 0.04, 0);
                }
                if (next.ticksElapsed() % 5 == 0) {
                    mc.level.addParticle(ModParticleTypes.SORT_WISP.get(),
                            pos.x, pos.y, pos.z,
                            (Math.random() - 0.5) * 0.2, 0.05, (Math.random() - 0.5) * 0.2);
                }
            }

            if (!next.finished()) {
                updated.add(next);
            }
        }
        ACTIVE_ARCS.clear();
        ACTIVE_ARCS.addAll(updated);
    }

    public static void render(RenderLevelStageEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) return;

        List<AnimArc> arcs = new ArrayList<>(ACTIVE_ARCS);
        if (arcs.isEmpty()) return;

        float partialTick = event.getPartialTick().getGameTimeDeltaPartialTick(true);
        Vec3 camera = mc.gameRenderer.getMainCamera().getPosition();

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        ItemRenderer itemRenderer = mc.getItemRenderer();

        for (AnimArc arc : arcs) {
            if (!arc.active()) continue;

            float t = (arc.ticksElapsed() + partialTick) / arc.totalTicks();
            t = Math.min(t, 1.0f);

            Vec3 pos = bezier(arc.from(), arc.to(), t);

            poseStack.pushPose();
            poseStack.translate(pos.x - camera.x, pos.y - camera.y, pos.z - camera.z);
            poseStack.scale(0.5f, 0.5f, 0.5f);

            itemRenderer.renderStatic(
                    arc.item(),
                    ItemDisplayContext.GROUND,
                    15728880, // full bright
                    655360,   // no overlay
                    poseStack,
                    bufferSource,
                    mc.level,
                    0
            );

            poseStack.popPose();
        }

        bufferSource.endBatch();
    }

    private static Vec3 bezier(Vec3 from, Vec3 to, float t) {
        double dx = to.x - from.x;
        double dz = to.z - from.z;
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);
        double peakHeight = Math.max(1.5, horizontalDist * 0.5);

        Vec3 ctrl = new Vec3(
                (from.x + to.x) * 0.5,
                Math.max(from.y, to.y) + peakHeight,
                (from.z + to.z) * 0.5
        );

        float u = 1.0f - t;
        return new Vec3(
                u * u * from.x + 2 * u * t * ctrl.x + t * t * to.x,
                u * u * from.y + 2 * u * t * ctrl.y + t * t * to.y,
                u * u * from.z + 2 * u * t * ctrl.z + t * t * to.z
        );
    }
}
