package com.heaser.sortingstick.client.particles;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;


public class SortParticle extends TextureSheetParticle {

    SortParticle(ClientLevel level, double x, double y, double z,
                 double xd, double yd, double zd,
                 SpriteSet sprites, int lifetime, float gravity, float scale) {
        super(level, x, y, z, xd, yd, zd);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.pickSprite(sprites);
        this.lifetime = lifetime;
        this.gravity = gravity;
        this.quadSize = scale;
        this.hasPhysics = false;
    }

    @Override
    public void tick() {
        super.tick();
        this.alpha = 1.0f - (float) this.age / this.lifetime;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }


    public record TrailProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                double x, double y, double z, double xd, double yd, double zd) {
            return new SortParticle(level, x, y, z, xd, yd + 0.02, zd, sprites, 12, 0f, 0.08f);
        }
    }

    public record WispProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                double x, double y, double z, double xd, double yd, double zd) {
            return new SortParticle(level, x, y, z, xd, yd + 0.03, zd, sprites, 20, -0.01f, 0.12f);
        }
    }

    public record BurstProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                double x, double y, double z, double xd, double yd, double zd) {
            SortParticle p = new SortParticle(level, x, y, z, xd, yd, zd, sprites, 25, -0.003f, 0.45f);
            p.alpha = 0.9f;
            return p;
        }
    }

    public record SparkleProvider(SpriteSet sprites) implements ParticleProvider<SimpleParticleType> {
        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level,
                double x, double y, double z, double xd, double yd, double zd) {
            return new SortParticle(level, x, y, z, xd, yd + 0.05, zd, sprites, 18, -0.02f, 0.07f);
        }
    }
}
