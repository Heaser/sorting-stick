package com.heaser.sortingstick;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModParticleTypes {

    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
            DeferredRegister.create(BuiltInRegistries.PARTICLE_TYPE, SortingStick.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SORT_TRAIL =
            PARTICLE_TYPES.register("sort_trail", () -> new SimpleParticleType(false));


    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SORT_WISP =
            PARTICLE_TYPES.register("sort_wisp", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SORT_BURST =
            PARTICLE_TYPES.register("sort_burst", () -> new SimpleParticleType(false));

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SORT_SPARKLE =
            PARTICLE_TYPES.register("sort_sparkle", () -> new SimpleParticleType(false));
}
