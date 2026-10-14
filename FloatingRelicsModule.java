/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1922
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2350$class_2351
 *  net.minecraft.class_238
 *  net.minecraft.class_265
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_4604
 *  net.minecraft.class_5819
 *  net.minecraft.class_638
 *  net.minecraft.class_746
 *  net.minecraft.class_9848
 */
package net.anubis.client.module.impl.render;

import net.anubis.client.event.Stage;
import net.anubis.client.event.impl.Render3DEvent;
import net.anubis.client.event.impl.TickEvent;
import net.anubis.client.module.ClientModule;
import net.anubis.client.module.ModuleCategory;
import net.anubis.client.module.impl.client.PerformanceModule;
import net.anubis.client.module.impl.render.CosmeticAccent;
import net.anubis.client.module.impl.render.RelicGlyphs;
import net.anubis.client.module.setting.BooleanSetting;
import net.anubis.client.module.setting.ColorSetting;
import net.anubis.client.module.setting.ModeSetting;
import net.anubis.client.module.setting.NumberSetting;
import net.anubis.client.util.glyph.GlyphCanvas;
import net.anubis.client.util.glyph.GlyphShapes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1922;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_238;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_4604;
import net.minecraft.class_5819;
import net.minecraft.class_638;
import net.minecraft.class_746;
import net.minecraft.class_9848;

@Environment(value=EnvType.CLIENT)
public final class FloatingRelicsModule
extends ClientModule {
    private static final String SYMBOL_ANUBIS = "Anubis";
    private static final String SYMBOL_LARP = "Larp";
    private static final float[][] ANUBIS_GLYPHS = new float[][]{RelicGlyphs.ANKH, RelicGlyphs.EYE, RelicGlyphs.SCARAB, RelicGlyphs.DJED, RelicGlyphs.FEATHER, RelicGlyphs.WAS};
    private static final float[] ANUBIS_FIT = new float[]{1.0f, 0.84f, 0.94f, 1.0f, 1.0f, 1.0f};
    private static final float[][] LARP_GLYPHS = new float[][]{RelicGlyphs.DOLLAR, RelicGlyphs.EURO, RelicGlyphs.BITCOIN};
    private static final float[] LARP_FIT = new float[]{1.0f, 0.92f, 0.96f};
    private static final String COLOR_THEME = "Theme";
    private static final String COLOR_MANUAL = "Manual";
    private static final int MAX_RELICS = 32;
    private static final float GRAVITY = 0.016f;
    private static final float BUOYANCY = 0.045f;
    private static final float AIR_DRAG = 0.982f;
    private static final float GROUND_GRIP = 0.86f;
    private static final float ORBIT_PULL = 0.012f;
    private static final float SWIRL = 0.0025f;
    private static final float FOLLOW = 0.035f;
    private static final float WANDER = 0.003f;
    private static final float MAX_SPEED = 0.9f;
    private static final float SPIN_SETTLE = 0.03f;
    private static final float ANGLE_WRAP = 512.0f;
    private static final float TILT_SPRING = 0.09f;
    private static final float TILT_DAMP = 0.9f;
    private static final float MAX_TILT = 0.55f;
    private static final int APPEAR_TICKS = 12;
    private static final int STAGGER_TICKS = 3;
    private static final float GLYPH_SCALE = 1.25f;
    private static final float THICKNESS = 0.07f;
    private static final float SWAY = 0.12f;
    private static final float BOB = 0.2f;
    private static final float FACE_ALPHA = 0.94f;
    private static final float BACK_ALPHA = 0.9f;
    private static final float GLINT = 0.45f;
    private static final float[] HALO_SCALES = new float[]{1.35f, 2.0f};
    private static final float[] HALO_WEIGHTS = new float[]{1.0f, 0.5f};
    private static final float HALO_PUSH = 0.6f;
    private static final double DETAIL_RANGE_SQR = 576.0;
    private static final float LIGHT_X;
    private static final float LIGHT_Y;
    private static final float LIGHT_Z;
    private static final long EPOCH;
    private final ModeSetting symbols = this.addSetting("symbols", new ModeSetting("Symbols", "Anubis", "Anubis", "Larp"));
    private final NumberSetting count = this.addSetting("count", new NumberSetting("Count", 9.0f, 1.0f, 32.0f).step(1.0f));
    private final NumberSetting size = this.addSetting("size", new NumberSetting("Size", 0.3f, 0.15f, 0.75f).step(0.05f));
    private final NumberSetting spread = this.addSetting("spread", new NumberSetting("Spread", 2.6f, 1.2f, 6.0f).step(0.1f));
    private final NumberSetting bounce = this.addSetting("bounce", new NumberSetting("Bounce", 0.6f, 0.1f, 0.95f).step(0.05f));
    private final NumberSetting drift = this.addSetting("drift", new NumberSetting("Drift", 0.5f, 0.0f, 2.5f).step(0.1f));
    private final NumberSetting spinSpeed = this.addSetting("spin", new NumberSetting("Spin", 1.0f, 0.2f, 3.0f).step(0.1f));
    private final ModeSetting colorMode = this.addSetting("color", new ModeSetting("Color", "Theme", "Theme", "Manual"));
    private final ColorSetting mainColor = this.addSetting("main-color", new ColorSetting("Main Color", -12614409));
    private final ColorSetting secondColor = this.addSetting("second-color", new ColorSetting("Second Color", -14923108));
    private final BooleanSetting halo = this.addSetting("halo", new BooleanSetting("Halo", true));
    private final BooleanSetting blocks = this.addSetting("block-bounce", new BooleanSetting("Block Bounce", true));
    private final Relic[] relics = new Relic[32];
    private final GlyphCanvas canvas = new GlyphCanvas();
    private final class_5819 random = class_5819.method_43047();
    private final class_2338.class_2339 cursor = new class_2338.class_2339();
    private int live;
    private int age;
    private class_638 boundLevel;
    private int tintMain;
    private int tintShade;
    private int tintShine;

    public FloatingRelicsModule() {
        super("floating-relics", "Floating Relics", "Egyptian symbols float and spin around you.", ModuleCategory.VISUALS);
        this.mainColor.visibleWhen(() -> this.colorMode.is(COLOR_MANUAL));
        this.secondColor.visibleWhen(() -> this.colorMode.is(COLOR_MANUAL));
        for (int i = 0; i < 32; ++i) {
            this.relics[i] = new Relic();
        }
    }

    @Override
    protected void onEnable() {
        this.reset();
        this.on(TickEvent.TYPE, Stage.POST, this::onTick);
        this.on(Render3DEvent.TYPE, this::onRender3D);
    }

    @Override
    protected void onDisable() {
        this.reset();
    }

    private void reset() {
        this.live = 0;
        this.age = 0;
        this.boundLevel = null;
    }

    private void syncLevel(class_638 level) {
        if (level == this.boundLevel) {
            return;
        }
        this.boundLevel = level;
        this.live = 0;
    }

    private void onTick(TickEvent event) {
        int i;
        class_310 mc = class_310.method_1551();
        class_746 player = mc.field_1724;
        class_638 level = mc.field_1687;
        if (player == null || level == null) {
            this.reset();
            return;
        }
        this.syncLevel(level);
        ++this.age;
        int want = Math.min(32, this.count.getInt());
        for (int i2 = this.live; i2 < want; ++i2) {
            this.wake(this.relics[i2], (i2 - this.live) * 3);
        }
        this.live = want;
        double px = player.method_23317();
        double py = player.method_23318();
        double pz = player.method_23321();
        float height = player.method_17682();
        float moveX = (float)(px - player.field_6014);
        float moveZ = (float)(pz - player.field_5969);
        float restitution = this.bounce.get();
        for (i = 0; i < this.live; ++i) {
            this.step(this.relics[i], level, px, py, pz, height, moveX, moveZ, restitution);
        }
        for (i = 0; i < this.live; ++i) {
            for (int j = i + 1; j < this.live; ++j) {
                this.knock(this.relics[i], this.relics[j], restitution);
            }
        }
    }

    private void wake(Relic r, int delay) {
        r.placed = false;
        r.wakeAt = this.age + delay;
        r.orbit = 0.55f + 0.45f * this.random.method_43057();
        r.lift = this.random.method_43057();
        r.grain = 0.82f + 0.3f * this.random.method_43057();
        r.phase = this.random.method_43057() * ((float)Math.PI * 2);
        r.restTurn = this.signed(0.07f, 0.12f);
    }

    private float signed(float min, float max) {
        float value = min + (max - min) * this.random.method_43057();
        return this.random.method_43056() ? value : -value;
    }

    private void place(Relic r, double px, double py, double pz) {
        float angle = this.random.method_43057() * ((float)Math.PI * 2);
        double radius = this.spread.get() * r.orbit;
        r.x = px + (double)class_3532.method_15362((double)angle) * radius;
        r.y = py + 0.15;
        r.z = pz + (double)class_3532.method_15374((double)angle) * radius;
        r.lastX = r.x;
        r.lastY = r.y;
        r.lastZ = r.z;
        r.vx = -class_3532.method_15374((double)angle) * 0.03f;
        r.vy = 0.14f + 0.08f * this.random.method_43057();
        r.vz = class_3532.method_15362((double)angle) * 0.03f;
        r.lastYaw = r.yaw = this.random.method_43057() * ((float)Math.PI * 2);
        r.turn = r.restTurn * 3.0f;
        r.tiltX = 0.0f;
        r.tiltZ = 0.0f;
        r.lastTiltX = 0.0f;
        r.lastTiltZ = 0.0f;
        r.swayX = this.signed(0.02f, 0.05f);
        r.swayZ = this.signed(0.02f, 0.05f);
        r.placed = true;
        r.placedAt = this.age;
    }

    private void step(Relic r, class_638 level, double px, double py, double pz, float height, float moveX, float moveZ, float restitution) {
        if (!r.placed) {
            if (this.age < r.wakeAt) {
                return;
            }
            this.place(r, px, py, pz);
        }
        r.lastX = r.x;
        r.lastY = r.y;
        r.lastZ = r.z;
        double dx = r.x - px;
        double dz = r.z - pz;
        double flat = Math.sqrt(dx * dx + dz * dz);
        double hover = py + 0.35 + (double)r.lift * ((double)height + 0.2) + 0.16 * (double)class_3532.method_15374((double)(((float)this.age + r.phase * 16.0f) * 0.045f));
        double leash = (double)this.spread.get() * 3.0 + 8.0;
        if (flat > leash || Math.abs(r.y - hover) > leash) {
            this.place(r, px, py, pz);
            return;
        }
        float nx = flat > 1.0E-4 ? (float)(dx / flat) : 1.0f;
        float nz = flat > 1.0E-4 ? (float)(dz / flat) : 0.0f;
        float pull = (float)((double)(this.spread.get() * r.orbit) - flat) * 0.012f;
        float swirl = this.drift.get() * 0.0025f;
        r.vx += nx * pull - nz * swirl + moveX * 0.035f;
        r.vz += nz * pull + nx * swirl + moveZ * 0.035f;
        float wander = ((float)this.age + r.phase * 20.0f) * 0.06f;
        r.vx += class_3532.method_15374((double)wander) * 0.003f;
        r.vz += class_3532.method_15362((double)(wander * 1.31f)) * 0.003f;
        r.vy -= 0.016f;
        double sink = hover - r.y;
        if (sink > 0.0) {
            r.vy += (float)Math.min(sink, 1.5) * 0.045f;
        }
        r.vx *= 0.982f;
        r.vy *= 0.982f;
        r.vz *= 0.982f;
        float speed = r.vx * r.vx + r.vy * r.vy + r.vz * r.vz;
        if (speed > 0.80999994f) {
            float k = 0.9f / (float)Math.sqrt(speed);
            r.vx *= k;
            r.vy *= k;
            r.vz *= k;
        }
        boolean collide = this.blocks.get();
        float half = this.size.get() * r.grain * 0.5f;
        double next = r.x + (double)r.vx;
        if (collide && this.solid(level, next + (double)Math.copySign(half, r.vx), r.y, r.z)) {
            r.vx = -r.vx * restitution;
            this.tumble(r, 0.6f);
        } else {
            r.x = next;
        }
        next = r.y + (double)r.vy;
        if (collide && this.solid(level, r.x, next + (double)Math.copySign(half, r.vy), r.z)) {
            if (r.vy < 0.0f) {
                r.vx *= 0.86f;
                r.vz *= 0.86f;
            }
            r.vy = -r.vy * restitution;
            this.tumble(r, 0.8f);
        } else {
            r.y = next;
        }
        next = r.z + (double)r.vz;
        if (collide && this.solid(level, r.x, r.y, next + (double)Math.copySign(half, r.vz))) {
            r.vz = -r.vz * restitution;
            this.tumble(r, 0.6f);
        } else {
            r.z = next;
        }
        if (collide && this.solid(level, r.x, r.y, r.z)) {
            r.y += 0.2;
            r.lastY = r.y;
            r.vy = Math.max(r.vy, 0.08f);
        }
        if (Math.abs(r.yaw) > 512.0f) {
            r.yaw %= (float)Math.PI * 2;
        }
        r.lastYaw = r.yaw;
        r.lastTiltX = r.tiltX;
        r.lastTiltZ = r.tiltZ;
        r.turn += (r.restTurn * this.spinSpeed.get() - r.turn) * 0.03f;
        r.yaw += r.turn;
        r.swayX = (r.swayX - r.tiltX * 0.09f) * 0.9f;
        r.swayZ = (r.swayZ - r.tiltZ * 0.09f) * 0.9f;
        r.tiltX = class_3532.method_15363((float)(r.tiltX + r.swayX), (float)-0.55f, (float)0.55f);
        r.tiltZ = class_3532.method_15363((float)(r.tiltZ + r.swayZ), (float)-0.55f, (float)0.55f);
    }

    private void knock(Relic a, Relic b, float restitution) {
        if (!a.placed || !b.placed) {
            return;
        }
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double dz = b.z - a.z;
        double d2 = dx * dx + dy * dy + dz * dz;
        double reach = (double)(this.size.get() * (a.grain + b.grain)) * 0.5 * 0.95;
        if (d2 >= reach * reach || d2 < 1.0E-8) {
            return;
        }
        double d = Math.sqrt(d2);
        float nx = (float)(dx / d);
        float ny = (float)(dy / d);
        float nz = (float)(dz / d);
        double push = (reach - d) * 0.5;
        a.x -= (double)nx * push;
        a.y -= (double)ny * push;
        a.z -= (double)nz * push;
        b.x += (double)nx * push;
        b.y += (double)ny * push;
        b.z += (double)nz * push;
        float closing = (a.vx - b.vx) * nx + (a.vy - b.vy) * ny + (a.vz - b.vz) * nz;
        if (closing <= 0.0f) {
            return;
        }
        float impulse = closing * (1.0f + restitution) * 0.5f;
        a.vx -= impulse * nx;
        a.vy -= impulse * ny;
        a.vz -= impulse * nz;
        b.vx += impulse * nx;
        b.vy += impulse * ny;
        b.vz += impulse * nz;
        this.tumble(a, 0.5f);
        this.tumble(b, 0.5f);
    }

    private void tumble(Relic r, float strength) {
        r.turn += (this.random.method_43057() - 0.5f) * 0.3f * strength;
        r.swayX += (this.random.method_43057() - 0.5f) * 0.12f * strength;
        r.swayZ += (this.random.method_43057() - 0.5f) * 0.12f * strength;
    }

    private boolean solid(class_638 level, double x, double y, double z) {
        this.cursor.method_10103(class_3532.method_15357((double)x), class_3532.method_15357((double)y), class_3532.method_15357((double)z));
        class_2680 state = level.method_8320((class_2338)this.cursor);
        if (state.method_26215()) {
            return false;
        }
        class_265 shape = state.method_26220((class_1922)level, (class_2338)this.cursor);
        if (shape.method_1110()) {
            return false;
        }
        double lx = x - (double)this.cursor.method_10263();
        double ly = y - (double)this.cursor.method_10264();
        double lz = z - (double)this.cursor.method_10260();
        return lx >= shape.method_1091(class_2350.class_2351.field_11048) && lx <= shape.method_1105(class_2350.class_2351.field_11048) && ly >= shape.method_1091(class_2350.class_2351.field_11052) && ly <= shape.method_1105(class_2350.class_2351.field_11052) && lz >= shape.method_1091(class_2350.class_2351.field_11051) && lz <= shape.method_1105(class_2350.class_2351.field_11051);
    }

    private void onRender3D(Render3DEvent event) {
        if (this.live == 0) {
            return;
        }
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) {
            return;
        }
        this.canvas.begin(mc.field_1773.method_19418());
        float pt = event.partialTick;
        float time = (float)(System.nanoTime() - EPOCH) / 1.0E9f;
        class_4604 frustum = PerformanceModule.frustumCulling() ? event.frustum : null;
        float base = this.size.get() * 0.5f * 1.25f;
        boolean larp = this.symbols.is(SYMBOL_LARP);
        float[][] glyphs = larp ? LARP_GLYPHS : ANUBIS_GLYPHS;
        float[] fits = larp ? LARP_FIT : ANUBIS_FIT;
        boolean glow = this.halo.get();
        this.tint();
        for (int i = 0; i < this.live; ++i) {
            float since;
            int kind;
            float extent;
            Relic r = this.relics[i];
            if (!r.placed || (extent = base * r.grain * fits[kind = i % glyphs.length] * FloatingRelicsModule.appear((since = (float)(this.age - r.placedAt) + pt) / 12.0f)) < 0.004f) continue;
            double x = class_3532.method_16436((double)pt, (double)r.lastX, (double)r.x);
            double y = class_3532.method_16436((double)pt, (double)r.lastY, (double)r.y) + (double)(extent * 0.2f * class_3532.method_15374((double)(time * 2.4f + r.phase * 3.0f)));
            double z = class_3532.method_16436((double)pt, (double)r.lastZ, (double)r.z);
            double reach = (double)extent * 2.0;
            if (frustum != null && !frustum.method_23093(new class_238(x - reach, y - reach, z - reach, x + reach, y + reach, z + reach))) continue;
            float fade = Math.min(1.0f, since / 7.2000003f);
            float yaw = class_3532.method_16439((float)pt, (float)r.lastYaw, (float)r.yaw);
            float tiltX = class_3532.method_16439((float)pt, (float)r.lastTiltX, (float)r.tiltX) + 0.12f * class_3532.method_15374((double)(time * 1.3f + r.phase));
            float tiltZ = class_3532.method_16439((float)pt, (float)r.lastTiltZ, (float)r.tiltZ) + 0.12f * class_3532.method_15374((double)(time * 0.9f + r.phase * 2.3f));
            float shimmer = glow ? (0.1f + 0.07f * class_3532.method_15374((double)(time * 2.1f + r.phase))) * fade : 0.0f;
            this.drawRelic(glyphs[kind], x, y, z, extent, yaw, tiltX, tiltZ, fade, shimmer);
        }
        this.canvas.submit(false);
    }

    private void tint() {
        if (this.colorMode.is(COLOR_MANUAL)) {
            this.tintMain = class_9848.method_61334((int)this.mainColor.argb());
            this.tintShade = class_9848.method_61334((int)this.secondColor.argb());
        } else {
            this.tintMain = CosmeticAccent.argb();
            this.tintShade = class_9848.method_61321((int)this.tintMain, (float)0.45f);
        }
        this.tintShine = class_9848.method_61319((float)0.6f, (int)this.tintMain, (int)-1);
    }

    private void drawRelic(float[] glyph, double x, double y, double z, float extent, float yaw, float tiltX, float tiltZ, float fade, float shimmer) {
        double tz;
        double ty;
        float cy = class_3532.method_15362((double)yaw);
        float sy = class_3532.method_15374((double)yaw);
        float cx = class_3532.method_15362((double)tiltX);
        float sx = class_3532.method_15374((double)tiltX);
        float cz = class_3532.method_15362((double)tiltZ);
        float sz = class_3532.method_15374((double)tiltZ);
        float ux = cy * cz + sy * sx * sz;
        float uy = cx * sz;
        float uz = -sy * cz + cy * sx * sz;
        float vx = -cy * sz + sy * sx * cz;
        float vy = cx * cz;
        float vz = sy * sz + cy * sx * cz;
        float nx = sy * cx;
        float ny = -sx;
        float nz = cy * cx;
        double tx = this.canvas.camX() - x;
        double distSqr = tx * tx + (ty = this.canvas.camY() - y) * ty + (tz = this.canvas.camZ() - z) * tz;
        if (distSqr < 1.0E-8) {
            return;
        }
        double dist = Math.sqrt(distSqr);
        float facing = (float)(((double)nx * tx + (double)ny * ty + (double)nz * tz) / dist);
        if (facing < 0.0f) {
            ux = -ux;
            uy = -uy;
            uz = -uz;
            nx = -nx;
            ny = -ny;
            nz = -nz;
            facing = -facing;
        }
        if (shimmer > 0.0f) {
            double push = (double)(extent * 0.6f) / dist;
            double hx = x - tx * push;
            double hy = y - ty * push;
            double hz = z - tz * push;
            for (int layer = 0; layer < HALO_SCALES.length; ++layer) {
                this.canvas.sprite(GlyphShapes.DISK, hx, hy, hz, extent * HALO_SCALES[layer], 0.0f, FloatingRelicsModule.withAlpha(this.tintMain, shimmer * HALO_WEIGHTS[layer]));
            }
        }
        double au = ux * extent;
        double bu = uy * extent;
        double cu = uz * extent;
        double av = vx * extent;
        double bv = vy * extent;
        double cv = vz * extent;
        double depth = extent * 0.07f;
        if (distSqr < 576.0 && facing < 0.995f) {
            this.canvas.shape(glyph, x - (double)nx * depth, y - (double)ny * depth, z - (double)nz * depth, au, bu, cu, av, bv, cv, FloatingRelicsModule.withAlpha(this.tintShade, 0.9f * fade));
        }
        float light = Math.max(0.0f, nx * LIGHT_X + ny * LIGHT_Y + nz * LIGHT_Z);
        float glint = facing * facing;
        glint *= glint;
        glint *= glint;
        int face = class_9848.method_61319((float)(glint * 0.45f), (int)class_9848.method_61321((int)this.tintMain, (float)(0.6f + 0.22f * light + 0.18f * facing)), (int)this.tintShine);
        this.canvas.shape(glyph, x + (double)nx * depth, y + (double)ny * depth, z + (double)nz * depth, au, bu, cu, av, bv, cv, FloatingRelicsModule.withAlpha(face, 0.94f * fade));
    }

    private static float appear(float t) {
        if (t >= 1.0f) {
            return 1.0f;
        }
        if (t <= 0.0f) {
            return 0.0f;
        }
        float inv = 1.0f - t;
        return 1.0f - inv * inv * inv + 0.22f * class_3532.method_15374((double)(t * (float)Math.PI)) * inv;
    }

    private static int withAlpha(int rgb, float alpha) {
        int a = (int)(Math.max(0.0f, Math.min(1.0f, alpha)) * 255.0f + 0.5f);
        return a << 24 | rgb & 0xFFFFFF;
    }

    static {
        EPOCH = System.nanoTime();
        float lx = 0.32f;
        float ly = 0.87f;
        float lz = 0.37f;
        float length = (float)Math.sqrt(lx * lx + ly * ly + lz * lz);
        LIGHT_X = lx / length;
        LIGHT_Y = ly / length;
        LIGHT_Z = lz / length;
    }

    @Environment(value=EnvType.CLIENT)
    private static final class Relic {
        double x;
        double y;
        double z;
        double lastX;
        double lastY;
        double lastZ;
        float vx;
        float vy;
        float vz;
        float yaw;
        float lastYaw;
        float turn;
        float restTurn;
        float tiltX;
        float tiltZ;
        float lastTiltX;
        float lastTiltZ;
        float swayX;
        float swayZ;
        float orbit;
        float lift;
        float grain;
        float phase;
        boolean placed;
        int wakeAt;
        int placedAt;

        private Relic() {
        }
    }
}

