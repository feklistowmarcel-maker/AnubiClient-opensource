/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.anubis.client.module.impl.render;

import java.util.Arrays;
import net.anubis.client.util.glyph.GlyphShapes;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
final class RelicGlyphs {
    static final float[] ANKH = GlyphShapes.ANKH;
    static final float[] EYE = RelicGlyphs.join(GlyphShapes.EYE, GlyphShapes.EYE_PUPIL);
    static final float[] SCARAB = GlyphShapes.SCARAB;
    static final float[] DJED = RelicGlyphs.djed();
    static final float[] FEATHER = RelicGlyphs.feather();
    static final float[] WAS = RelicGlyphs.was();
    static final float[] DOLLAR = RelicGlyphs.dollar();
    static final float[] EURO = RelicGlyphs.euro();
    static final float[] BITCOIN = RelicGlyphs.bitcoin();

    private RelicGlyphs() {
    }

    private static float[] djed() {
        Sketch s = new Sketch();
        s.quad(-0.36f, -1.0f, 0.36f, -1.0f, 0.32f, -0.88f, -0.32f, -0.88f);
        s.quad(-0.2f, -0.88f, 0.2f, -0.88f, 0.15f, 0.2f, -0.15f, 0.2f);
        for (int k = 0; k < 4; ++k) {
            float low = 0.2f + (float)k * 0.2f;
            float high = low + 0.12f;
            float reach = k == 3 ? 0.48f : 0.44f;
            s.quad(-reach, low, reach, low, reach, high, -reach, high);
            if (k >= 3) continue;
            s.quad(-0.15f, high, 0.15f, high, 0.15f, high + 0.08f, -0.15f, high + 0.08f);
        }
        s.quad(-0.36f, 0.92f, 0.36f, 0.92f, 0.26f, 1.0f, -0.26f, 1.0f);
        return s.build();
    }

    private static float[] feather() {
        float[] spine = new float[]{0.0f, -0.72f, 0.0f, -0.54f, 0.01f, -0.3f, 0.02f, 0.0f, 0.03f, 0.26f, 0.06f, 0.48f, 0.11f, 0.64f, 0.19f, 0.77f, 0.3f, 0.86f, 0.42f, 0.9f, 0.53f, 0.87f, 0.6f, 0.8f};
        float[] edge = new float[]{0.05f, 0.21f, 0.28f, 0.3f, 0.29f, 0.26f, 0.22f, 0.17f, 0.13f, 0.09f, 0.05f, 0.02f};
        int n = edge.length;
        float[] rachis = new float[n];
        float[] outer = new float[n];
        float[] inner = new float[n];
        for (int i = 0; i < n; ++i) {
            rachis[i] = Math.min(0.026f, edge[i] * 0.45f);
            outer[i] = -edge[i];
            inner[i] = -rachis[i];
        }
        Sketch s = new Sketch();
        s.quad(-0.04f, -1.0f, 0.04f, -1.0f, 0.04f, -0.7f, -0.04f, -0.7f);
        s.band(spine, rachis, edge);
        s.band(spine, outer, inner);
        return s.build();
    }

    private static float[] was() {
        Sketch s = new Sketch();
        s.quad(-0.08f, -0.7f, 0.08f, -0.7f, 0.08f, 0.5f, -0.08f, 0.5f);
        s.quad(-0.3f, -1.0f, -0.16f, -1.0f, 0.03f, -0.7f, -0.08f, -0.7f);
        s.quad(0.16f, -1.0f, 0.3f, -1.0f, 0.08f, -0.7f, -0.03f, -0.7f);
        s.quad(-0.08f, 0.5f, 0.08f, 0.5f, 0.26f, 0.78f, 0.06f, 0.86f);
        s.stroke(0.15f, 0.12f, 0.8f, 0.36f, 0.84f, 0.62f, 0.7f);
        s.stroke(0.1f, 0.08f, 0.82f, -0.02f, 1.0f);
        return s.build();
    }

    private static float[] dollar() {
        float r = 0.34f;
        float[] upper = RelicGlyphs.arc(0.0f, r, r, 28.0f, 270.0f, 16);
        float[] lower = RelicGlyphs.arc(0.0f, -r, r, 90.0f, -152.0f, 16);
        Sketch s = new Sketch();
        s.stroke(0.17f, RelicGlyphs.join(upper, Arrays.copyOfRange(lower, 2, lower.length)));
        s.quad(-0.055f, -0.98f, 0.055f, -0.98f, 0.055f, 0.98f, -0.055f, 0.98f);
        return s.build();
    }

    private static float[] euro() {
        Sketch s = new Sketch();
        s.stroke(0.17f, RelicGlyphs.arc(0.1f, 0.0f, 0.62f, 48.0f, 312.0f, 26));
        s.quad(-0.82f, 0.06f, 0.16f, 0.06f, 0.16f, 0.2f, -0.82f, 0.2f);
        s.quad(-0.82f, -0.22f, 0.1f, -0.22f, 0.1f, -0.08f, -0.82f, -0.08f);
        return s.build();
    }

    private static float[] bitcoin() {
        Sketch b = new Sketch();
        b.quad(-0.3f, -0.56f, -0.17f, -0.56f, -0.17f, 0.56f, -0.3f, 0.56f);
        b.stroke(0.13f, RelicGlyphs.join({-0.24f, 0.5f}, RelicGlyphs.arc(0.06f, 0.27f, 0.23f, 90.0f, -90.0f, 12), {-0.24f, 0.04f}));
        b.stroke(0.13f, RelicGlyphs.join({-0.24f, 0.04f}, RelicGlyphs.arc(0.1f, -0.24f, 0.28f, 90.0f, -90.0f, 12), {-0.24f, -0.52f}));
        b.quad(-0.21f, 0.5f, -0.13f, 0.5f, -0.13f, 0.74f, -0.21f, 0.74f);
        b.quad(-0.03f, 0.5f, 0.05f, 0.5f, 0.05f, 0.74f, -0.03f, 0.74f);
        b.quad(-0.21f, -0.74f, -0.13f, -0.74f, -0.13f, -0.5f, -0.21f, -0.5f);
        b.quad(-0.03f, -0.74f, 0.05f, -0.74f, 0.05f, -0.5f, -0.03f, -0.5f);
        float[] mark = b.build();
        float c = (float)Math.cos(Math.toRadians(-14.0));
        float sn = (float)Math.sin(Math.toRadians(-14.0));
        for (int i = 0; i < mark.length; i += 2) {
            float x = mark[i] + 0.04f;
            float y = mark[i + 1];
            mark[i] = x * c - y * sn;
            mark[i + 1] = x * sn + y * c;
        }
        Sketch rim = new Sketch();
        rim.stroke(0.1f, RelicGlyphs.arc(0.0f, 0.0f, 0.92f, 0.0f, 360.0f, 44));
        return RelicGlyphs.join(rim.build(), mark);
    }

    private static float[] arc(float cx, float cy, float radius, float from, float to, int steps) {
        float[] points = new float[(steps + 1) * 2];
        for (int i = 0; i <= steps; ++i) {
            double angle = Math.toRadians(from + (to - from) * (float)i / (float)steps);
            points[i * 2] = cx + radius * (float)Math.cos(angle);
            points[i * 2 + 1] = cy + radius * (float)Math.sin(angle);
        }
        return points;
    }

    private static float[] join(float[] ... parts) {
        int length = 0;
        for (float[] part : parts) {
            length += part.length;
        }
        float[] out = new float[length];
        int at = 0;
        for (float[] part : parts) {
            System.arraycopy(part, 0, out, at, part.length);
            at += part.length;
        }
        return out;
    }

    @Environment(value=EnvType.CLIENT)
    private static final class Sketch {
        private float[] data = new float[64];
        private int size;

        private Sketch() {
        }

        void quad(float ax, float ay, float bx, float by, float cx, float cy, float dx, float dy) {
            if (this.size + 8 > this.data.length) {
                this.data = Arrays.copyOf(this.data, this.data.length * 2);
            }
            this.data[this.size++] = ax;
            this.data[this.size++] = ay;
            this.data[this.size++] = bx;
            this.data[this.size++] = by;
            this.data[this.size++] = cx;
            this.data[this.size++] = cy;
            this.data[this.size++] = dx;
            this.data[this.size++] = dy;
        }

        void stroke(float width, float ... points) {
            float[] low = new float[points.length / 2];
            float[] high = new float[points.length / 2];
            Arrays.fill(low, -width * 0.5f);
            Arrays.fill(high, width * 0.5f);
            this.band(points, low, high);
        }

        void band(float[] points, float[] low, float[] high) {
            int i;
            int n = points.length / 2;
            float[] nx = new float[n];
            float[] ny = new float[n];
            for (i = 0; i < n; ++i) {
                int a = Math.max(0, i - 1);
                int b = Math.min(n - 1, i + 1);
                float dx = points[b * 2] - points[a * 2];
                float dy = points[b * 2 + 1] - points[a * 2 + 1];
                float length = (float)Math.sqrt(dx * dx + dy * dy);
                nx[i] = -dy / length;
                ny[i] = dx / length;
            }
            i = 0;
            for (int j = 1; j < n; ++j) {
                float ax = points[i * 2];
                float ay = points[i * 2 + 1];
                float bx = points[j * 2];
                float by = points[j * 2 + 1];
                this.quad(ax + nx[i] * low[i], ay + ny[i] * low[i], bx + nx[j] * low[j], by + ny[j] * low[j], bx + nx[j] * high[j], by + ny[j] * high[j], ax + nx[i] * high[i], ay + ny[i] * high[i]);
                ++i;
            }
        }

        float[] build() {
            return Arrays.copyOf(this.data, this.size);
        }
    }
}

