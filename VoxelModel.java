/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 */
package net.anubis.client.module.impl.render;

import net.anubis.client.module.impl.render.PharaohMesh;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

@Environment(value=EnvType.CLIENT)
final class VoxelModel {
    static final int X_NEG = 0;
    static final int X_POS = 1;
    static final int TOP = 2;
    static final int BOTTOM = 3;
    static final int FRONT = 4;
    static final int BACK = 5;
    private static final int SPAN = 40;
    private static final int OFFSET = 20;
    private static final float EDGE_SHADOW = 0.11f;
    private static final float CORNER_SHADOW = 0.05f;
    private static final int[] STEP_X = new int[]{-1, 1, 0, 0, 0, 0};
    private static final int[] STEP_Y = new int[]{0, 0, -1, 1, 0, 0};
    private static final int[] STEP_Z = new int[]{0, 0, 0, 0, -1, 1};
    private final byte[] cells = new byte[64000];
    private final byte[] paint = new byte[384000];

    VoxelModel() {
    }

    void fill(int x0, int y0, int z0, int x1, int y1, int z1, int ink) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); ++x) {
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); ++y) {
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); ++z) {
                    this.cells[VoxelModel.index((int)x, (int)y, (int)z)] = (byte)(ink + 1);
                }
            }
        }
    }

    void paint(int x0, int y0, int z0, int x1, int y1, int z1, int face, int ink) {
        for (int x = Math.min(x0, x1); x <= Math.max(x0, x1); ++x) {
            for (int y = Math.min(y0, y1); y <= Math.max(y0, y1); ++y) {
                for (int z = Math.min(z0, z1); z <= Math.max(z0, z1); ++z) {
                    this.paint[VoxelModel.index((int)x, (int)y, (int)z) * 6 + face] = (byte)(ink + 1);
                }
            }
        }
    }

    void paint(int x, int y, int z, int face, int ink) {
        this.paint[VoxelModel.index((int)x, (int)y, (int)z) * 6 + face] = (byte)(ink + 1);
    }

    boolean solid(int x, int y, int z) {
        if (x < -20 || y < -20 || z < -20 || x >= 20 || y >= 20 || z >= 20) {
            return false;
        }
        return this.cells[VoxelModel.index(x, y, z)] != 0;
    }

    PharaohMesh mesh(Ink[] inks, float ox, float oy, float oz, float sx, float sy, float sz) {
        PharaohMesh.Builder b = new PharaohMesh.Builder();
        this.emit(b, inks, ox, oy, oz, sx, sy, sz);
        return new PharaohMesh(b);
    }

    void emit(PharaohMesh.Builder b, Ink[] inks, float ox, float oy, float oz, float sx, float sy, float sz) {
        float[] lo = new float[3];
        float[] hi = new float[3];
        for (int x = -20; x < 20; ++x) {
            for (int y = -20; y < 20; ++y) {
                for (int z = -20; z < 20; ++z) {
                    int cell = VoxelModel.index(x, y, z);
                    if (this.cells[cell] == 0) continue;
                    lo[0] = ox + (float)x * sx;
                    lo[1] = oy + (float)y * sy;
                    lo[2] = oz + (float)z * sz;
                    hi[0] = lo[0] + sx;
                    hi[1] = lo[1] + sy;
                    hi[2] = lo[2] + sz;
                    float grain = VoxelModel.grain(x, y, z);
                    for (int face = 0; face < 6; ++face) {
                        if (this.solid(x + STEP_X[face], y + STEP_Y[face], z + STEP_Z[face])) continue;
                        byte painted = this.paint[cell * 6 + face];
                        Ink ink = inks[(painted != 0 ? painted : this.cells[cell]) - 1];
                        b.shade(ink.tone() * (1.0f - ink.grain() * grain) * this.occlusion(x, y, z, face));
                        VoxelModel.face(b, face, lo, hi, ink.material());
                    }
                }
            }
        }
        b.shade(1.0f);
    }

    private float occlusion(int x, int y, int z, int face) {
        int fx = x + STEP_X[face];
        int fy = y + STEP_Y[face];
        int fz = z + STEP_Z[face];
        int edges = 0;
        int corners = 0;
        for (int du = -1; du <= 1; ++du) {
            for (int dv = -1; dv <= 1; ++dv) {
                boolean hit;
                if (du == 0 && dv == 0) continue;
                switch (face >> 1) {
                    case 0: {
                        boolean bl = this.solid(fx, fy + du, fz + dv);
                        break;
                    }
                    case 1: {
                        boolean bl = this.solid(fx + du, fy, fz + dv);
                        break;
                    }
                    default: {
                        boolean bl = hit = this.solid(fx + du, fy + dv, fz);
                    }
                }
                if (!hit) continue;
                if (du == 0 || dv == 0) {
                    ++edges;
                    continue;
                }
                ++corners;
            }
        }
        return Math.max(0.55f, 1.0f - 0.11f * (float)edges - 0.05f * (float)corners);
    }

    private static void face(PharaohMesh.Builder b, int face, float[] lo, float[] hi, int material) {
        float cx = (lo[0] + hi[0]) * 0.5f;
        float cy = (lo[1] + hi[1]) * 0.5f;
        float cz = (lo[2] + hi[2]) * 0.5f;
        float x0 = lo[0];
        float y0 = lo[1];
        float z0 = lo[2];
        float x1 = hi[0];
        float y1 = hi[1];
        float z1 = hi[2];
        switch (face) {
            case 0: {
                b.quad(x0, y0, z0, x0, y0, z1, x0, y1, z1, x0, y1, z0, cx, cy, cz, material);
                break;
            }
            case 1: {
                b.quad(x1, y0, z0, x1, y1, z0, x1, y1, z1, x1, y0, z1, cx, cy, cz, material);
                break;
            }
            case 2: {
                b.quad(x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1, cx, cy, cz, material);
                break;
            }
            case 3: {
                b.quad(x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0, cx, cy, cz, material);
                break;
            }
            case 4: {
                b.quad(x0, y0, z0, x0, y1, z0, x1, y1, z0, x1, y0, z0, cx, cy, cz, material);
                break;
            }
            default: {
                b.quad(x0, y0, z1, x1, y0, z1, x1, y1, z1, x0, y1, z1, cx, cy, cz, material);
            }
        }
    }

    private static float grain(int x, int y, int z) {
        int h = x * 0x1F1F1F1F ^ y * 1540483477 ^ z * 668265263;
        h ^= h >>> 13;
        h *= -2048144789;
        h ^= h >>> 16;
        return (float)(h & 0xFF) / 255.0f;
    }

    private static int index(int x, int y, int z) {
        return ((x + 20) * 40 + y + 20) * 40 + z + 20;
    }

    @Environment(value=EnvType.CLIENT)
    record Ink(int material, float tone, float grain) {
    }
}

