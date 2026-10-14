/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_12179
 *  net.minecraft.class_12180
 *  net.minecraft.class_1922
 *  net.minecraft.class_1923
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_259
 *  net.minecraft.class_265
 *  net.minecraft.class_2680
 *  net.minecraft.class_2818
 *  net.minecraft.class_2826
 *  net.minecraft.class_310
 *  net.minecraft.class_638
 */
package net.anubis.client.module.impl.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.anubis.client.event.EventHandler;
import net.anubis.client.event.impl.BlockUpdateEvent;
import net.anubis.client.event.impl.Render3DEvent;
import net.anubis.client.event.impl.TickEvent;
import net.anubis.client.module.ClientModule;
import net.anubis.client.module.ModuleCategory;
import net.anubis.client.module.setting.BooleanSetting;
import net.anubis.client.module.setting.ColorSetting;
import net.anubis.client.module.setting.ModeSetting;
import net.anubis.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12179;
import net.minecraft.class_12180;
import net.minecraft.class_1922;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_259;
import net.minecraft.class_265;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_638;

@Environment(value=EnvType.CLIENT)
public class HoleEspModule
extends ClientModule {
    private static final String MODE_ALL = "All";
    private static final String MODE_HOLES_TUNNELS = "Holes + Tunnels";
    private static final String MODE_HOLES_STAIRS = "Holes + Stairs";
    private static final String MODE_TUNNELS_STAIRS = "Tunnels + Stairs";
    private static final String MODE_HOLES = "Holes";
    private static final String MODE_TUNNELS = "Tunnels";
    private static final String MODE_STAIRS = "Stairs";
    private static final int TYPE_HOLE = 0;
    private static final int TYPE_HOLE_3X1 = 1;
    private static final int TYPE_TUNNEL = 2;
    private static final int TYPE_STAIR = 3;
    private static final int MAX_HITS_PER_CHUNK = 1024;
    private static final int MAX_RENDER_BOXES = 8192;
    private static final int HOLE_GRID_W = 20;
    private static final int ELYTRA_REFRESH_TICKS = 5;
    private static final int NORMAL_REFRESH_TICKS = 20;
    private static final float ELYTRA_SCAN_RADIUS = 12.0f;
    private static final float ELYTRA_SCAN_BUDGET_MS = 8.0f;
    private static final int[][] HORIZONTAL_DIRS = new int[][]{{1, 0}, {-1, 0}, {0, -1}, {0, 1}};
    private static final ThreadLocal<class_2338.class_2339> PASSABLE_POS = ThreadLocal.withInitial(class_2338.class_2339::new);
    private final ConcurrentHashMap<Long, List<Hit>> chunkCache = new ConcurrentHashMap();
    private final Queue<Long> scanQueue = new ConcurrentLinkedQueue<Long>();
    private final Set<Long> queued = ConcurrentHashMap.newKeySet();
    private long lastPlayerChunk = Long.MIN_VALUE;
    private int refreshTicks = 0;
    private final ModeSetting sMode = this.addSetting("detection", new ModeSetting("Detection", "Holes", "Holes", "All", "Holes + Tunnels", "Holes + Stairs", "Tunnels + Stairs", "Tunnels", "Stairs"));
    private final NumberSetting sScanRadius = this.addSetting("scan-radius", new NumberSetting("Scan Radius", 8.0f, 1.0f, 24.0f).step(1.0f));
    private final NumberSetting sChunksPerTick = this.addSetting("chunks-tick", new NumberSetting("Chunks/Tick", 64.0f, 1.0f, 100.0f).step(1.0f));
    private final NumberSetting sScanBudgetMs = this.addSetting("scan-budget-ms", new NumberSetting("Scan Budget ms", 3.0f, 1.0f, 10.0f).step(1.0f));
    private final BooleanSetting sAirOnly = this.addSetting("only-air-blocks", new BooleanSetting("Only Air Blocks", true));
    private final NumberSetting sMinYOffset = this.addSetting("min-y-offset", new NumberSetting("Min Y Offset", 0.0f, 0.0f, 319.0f).step(1.0f));
    private final NumberSetting sMaxYOffset = this.addSetting("max-y-offset", new NumberSetting("Max Y Offset", 0.0f, 0.0f, 319.0f).step(1.0f));
    private final NumberSetting sMinHoleDepth = this.addSetting("min-hole-depth", new NumberSetting("Min Hole Depth", 4.0f, 1.0f, 20.0f).step(1.0f));
    private final NumberSetting sMinTunnelLength = this.addSetting("min-tunnel-length", new NumberSetting("Min Tunnel Length", 3.0f, 1.0f, 20.0f).step(1.0f));
    private final NumberSetting sMinTunnelHeight = this.addSetting("min-tunnel-height", new NumberSetting("Min Tunnel Height", 2.0f, 1.0f, 10.0f).step(1.0f));
    private final NumberSetting sMaxTunnelHeight = this.addSetting("max-tunnel-height", new NumberSetting("Max Tunnel Height", 3.0f, 2.0f, 10.0f).step(1.0f));
    private final NumberSetting sMinStairLength = this.addSetting("min-stair-length", new NumberSetting("Min Stair Length", 3.0f, 1.0f, 20.0f).step(1.0f));
    private final NumberSetting sMinStairHeight = this.addSetting("min-stair-height", new NumberSetting("Min Stair Height", 3.0f, 2.0f, 10.0f).step(1.0f));
    private final NumberSetting sMaxStairHeight = this.addSetting("max-stair-height", new NumberSetting("Max Stair Height", 5.0f, 2.0f, 10.0f).step(1.0f));
    private final BooleanSetting sFill = this.addSetting("fill", new BooleanSetting("Fill", true));
    private final BooleanSetting sOutline = this.addSetting("outline", new BooleanSetting("Outline", true));
    private final NumberSetting sLineWidth = this.addSetting("line-width", new NumberSetting("Line Width", 1.5f, 0.5f, 5.0f).step(0.5f));
    private final ColorSetting sHoleLine = this.addSetting("1x1-line", new ColorSetting("1x1 Line", 0x4CFFFFFF));
    private final ColorSetting sHoleFill = this.addSetting("1x1-fill", new ColorSetting("1x1 Fill", 0x4FFFFFFF));
    private final ColorSetting sHole3Line = this.addSetting("3x1-line", new ColorSetting("3x1 Line", 0x26666666));
    private final ColorSetting sHole3Fill = this.addSetting("3x1-fill", new ColorSetting("3x1 Fill", 0x26666666));
    private final ColorSetting sTunnelLine = this.addSetting("tunnel-line", new ColorSetting("Tunnel Line", 0x5F0000FF));
    private final ColorSetting sTunnelFill = this.addSetting("tunnel-fill", new ColorSetting("Tunnel Fill", 503316735));
    private final ColorSetting sStairLine = this.addSetting("stair-line", new ColorSetting("Stair Line", 0x5FFF00FF));
    private final ColorSetting sStairFill = this.addSetting("stair-fill", new ColorSetting("Stair Fill", 520028415));

    public HoleEspModule() {
        super("hole-esp", "Hole ESP", "Highlights holes, tunnels, and staircases.", ModuleCategory.RENDER);
    }

    @Override
    protected void onEnable() {
        this.lastPlayerChunk = Long.MIN_VALUE;
        this.refreshTicks = 0;
        this.chunkCache.clear();
        this.scanQueue.clear();
        this.queued.clear();
    }

    @Override
    protected void onDisable() {
        this.chunkCache.clear();
        this.scanQueue.clear();
        this.queued.clear();
        this.lastPlayerChunk = Long.MIN_VALUE;
        this.refreshTicks = 0;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        Long key2;
        boolean periodicRefresh;
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) {
            return;
        }
        int cx = mc.field_1724.method_31477() >> 4;
        int cz = mc.field_1724.method_31479() >> 4;
        long curChunk = class_1923.method_8331((int)cx, (int)cz);
        int radius = this.scanRadius(mc);
        boolean fastTravel = HoleEspModule.isFastTravel(mc);
        boolean bl = periodicRefresh = ++this.refreshTicks >= (fastTravel ? 5 : 20);
        if (curChunk != this.lastPlayerChunk || periodicRefresh) {
            this.refreshTicks = 0;
            this.lastPlayerChunk = curChunk;
            this.chunkCache.keySet().removeIf(key -> {
                int kx = HoleEspModule.chunkX(key);
                int kz = HoleEspModule.chunkZ(key);
                return Math.abs(kx - cx) > radius || Math.abs(kz - cz) > radius;
            });
            ArrayList<long[]> toQueue = new ArrayList<long[]>();
            class_243 velocity = mc.field_1724.method_18798();
            double vx = velocity.field_1352;
            double vz = velocity.field_1350;
            double horizontalSpeed = Math.sqrt(vx * vx + vz * vz);
            if (horizontalSpeed > 1.0E-4) {
                vx /= horizontalSpeed;
                vz /= horizontalSpeed;
            }
            for (int x = cx - radius; x <= cx + radius; ++x) {
                for (int z = cz - radius; z <= cz + radius; ++z) {
                    long key3 = class_1923.method_8331((int)x, (int)z);
                    if (this.chunkCache.containsKey(key3) || this.queued.contains(key3) || !mc.field_1687.method_2935().method_12123(x, z)) continue;
                    int dx = x - cx;
                    int dz = z - cz;
                    long distScore = (long)(dx * dx + dz * dz) * 1024L;
                    long aheadScore = fastTravel ? Math.round(((double)dx * vx + (double)dz * vz) * 1536.0) : 0L;
                    toQueue.add(new long[]{key3, distScore - aheadScore});
                }
            }
            toQueue.sort(Comparator.comparingLong(a -> a[1]));
            for (long[] entry : toQueue) {
                this.scanQueue.add(entry[0]);
                this.queued.add(entry[0]);
            }
        }
        int chunksPerTick = Math.max(1, Math.round(HoleEspModule.value(this.sChunksPerTick, 4.0f)));
        float budgetMs = fastTravel ? Math.max(HoleEspModule.value(this.sScanBudgetMs, 3.0f), 8.0f) : HoleEspModule.value(this.sScanBudgetMs, 3.0f);
        long deadline = System.nanoTime() + (long)(Math.max(1.0f, budgetMs) * 1000000.0f);
        for (int i = 0; i < chunksPerTick && System.nanoTime() < deadline && (key2 = this.scanQueue.poll()) != null; ++i) {
            this.queued.remove(key2);
            int sx = HoleEspModule.chunkX(key2);
            int sz = HoleEspModule.chunkZ(key2);
            if (!mc.field_1687.method_2935().method_12123(sx, sz)) continue;
            this.scanChunk(mc.field_1687, sx, sz, key2);
        }
    }

    @EventHandler
    public void onBlockUpdate(BlockUpdateEvent event) {
        if (event.pos == null) {
            return;
        }
        long key = class_1923.method_8331((int)(event.pos.method_10263() >> 4), (int)(event.pos.method_10260() >> 4));
        this.chunkCache.remove(key);
        if (this.queued.add(key)) {
            this.scanQueue.add(key);
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        boolean outline;
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null) {
            return;
        }
        boolean fill = this.sFill == null || this.sFill.value().get();
        boolean bl = outline = this.sOutline == null || this.sOutline.value().get();
        if (!fill && !outline) {
            return;
        }
        int px = mc.field_1724.method_31477() >> 4;
        int pz = mc.field_1724.method_31479() >> 4;
        int radius = this.scanRadius(mc);
        float lineWidth = HoleEspModule.value(this.sLineWidth, 1.5f);
        int rendered = 0;
        for (Map.Entry<Long, List<Hit>> entry : this.chunkCache.entrySet()) {
            long key = entry.getKey();
            if (Math.abs(HoleEspModule.chunkX(key) - px) > radius || Math.abs(HoleEspModule.chunkZ(key) - pz) > radius) continue;
            for (Hit hit : entry.getValue()) {
                if (++rendered > 8192) {
                    return;
                }
                class_12179 style = !fill ? class_12179.method_75536((int)this.lineColor(hit.type()), (float)lineWidth) : (!outline ? class_12179.method_75539((int)this.fillColor(hit.type())) : class_12179.method_75537((int)this.lineColor(hit.type()), (float)lineWidth, (int)this.fillColor(hit.type())));
                class_12180.method_75541((class_238)hit.box(), (class_12179)style).method_75533();
            }
        }
    }

    private void scanChunk(class_638 level, int chunkX, int chunkZ, long chunkKey) {
        boolean airOnly;
        class_2818 chunk = level.method_2935().method_21730(chunkX, chunkZ);
        if (chunk == null) {
            return;
        }
        int baseX = chunkX << 4;
        int baseZ = chunkZ << 4;
        int minBuildY = level.method_8597().comp_651();
        int maxBuildY = minBuildY + chunk.method_12006().length * 16;
        int minY = Math.min(maxBuildY - 1, minBuildY + Math.round(HoleEspModule.value(this.sMinYOffset, 0.0f)));
        int maxY = Math.max(minY + 1, maxBuildY - Math.round(HoleEspModule.value(this.sMaxYOffset, 0.0f)));
        Mode mode = this.currentMode();
        boolean bl = airOnly = this.sAirOnly != null && this.sAirOnly.value().get();
        if (mode.holes() && !mode.tunnels() && !mode.stairs()) {
            this.scanHolesFast(level, chunkKey, baseX, baseZ, minY, maxY, airOnly);
            return;
        }
        ArrayList<Hit> hits = new ArrayList<Hit>();
        HashSet<String> seen = new HashSet<String>();
        class_2338.class_2339 pos = new class_2338.class_2339();
        class_2826[] sections = chunk.method_12006();
        for (int si = 0; si < sections.length; ++si) {
            class_2826 section = sections[si];
            if (section == null || section.method_38292()) continue;
            int sectionBaseY = minBuildY + si * 16;
            for (int ly = 0; ly < 16; ++ly) {
                int y = sectionBaseY + ly;
                if (y <= minY || y >= maxY) continue;
                for (int z = 0; z < 16; ++z) {
                    for (int x = 0; x < 16; ++x) {
                        int wx = baseX + x;
                        int wz = baseZ + z;
                        pos.method_10103(wx, y, wz);
                        if (!HoleEspModule.isPassable(level, (class_2338)pos, airOnly)) continue;
                        if (mode.holes() && !HoleEspModule.isPassable(level, wx, y - 1, wz, airOnly)) {
                            this.checkHole(level, (class_2338)pos, airOnly, hits, seen);
                            this.check3x1Hole(level, (class_2338)pos, airOnly, hits, seen);
                        }
                        if (mode.tunnels()) {
                            this.checkTunnelStarts(level, (class_2338)pos, airOnly, hits, seen);
                        }
                        if (mode.stairs()) {
                            this.checkStaircaseStarts(level, (class_2338)pos, airOnly, hits, seen);
                        }
                        if (hits.size() < 1024) continue;
                        this.chunkCache.put(chunkKey, List.copyOf(hits));
                        return;
                    }
                }
            }
        }
        if (hits.isEmpty()) {
            this.chunkCache.remove(chunkKey);
        } else {
            this.chunkCache.put(chunkKey, List.copyOf(hits));
        }
    }

    private void scanHolesFast(class_638 level, long chunkKey, int baseX, int baseZ, int minY, int maxY, boolean airOnly) {
        int lz;
        int gridMaxY = maxY;
        int gridMinY = minY - 1;
        int yCount = gridMaxY - gridMinY + 1;
        if (yCount <= 1) {
            this.chunkCache.remove(chunkKey);
            return;
        }
        boolean[] passable = new boolean[yCount * 20 * 20];
        class_2338.class_2339 pos = new class_2338.class_2339();
        for (int yi = 0; yi < yCount; ++yi) {
            int y = gridMinY + yi;
            int yBase = yi * 20 * 20;
            for (lz = -1; lz <= 18; ++lz) {
                int zBase = yBase + (lz + 1) * 20;
                for (int lx = -1; lx <= 18; ++lx) {
                    pos.method_10103(baseX + lx, y, baseZ + lz);
                    passable[zBase + lx + 1] = HoleEspModule.isPassable(level, (class_2338)pos, airOnly);
                }
            }
        }
        int minDepth = Math.round(HoleEspModule.value(this.sMinHoleDepth, 4.0f));
        ArrayList<Hit> hits = new ArrayList<Hit>();
        HashSet<String> seen = new HashSet<String>();
        for (lz = 0; lz < 16; ++lz) {
            for (int lx = 0; lx < 16; ++lx) {
                int y = minY;
                while (y < maxY) {
                    int depthZ;
                    int depthX;
                    if (!HoleEspModule.grid(passable, y, lx, lz, gridMinY) || HoleEspModule.grid(passable, y - 1, lx, lz, gridMinY)) {
                        ++y;
                        continue;
                    }
                    int depth = HoleEspModule.count1x1Depth(passable, y, lx, lz, gridMinY, maxY);
                    if (depth >= minDepth) {
                        HoleEspModule.addHit(hits, seen, new class_238((double)(baseX + lx), (double)y, (double)(baseZ + lz), (double)(baseX + lx + 1), (double)(y + depth), (double)(baseZ + lz + 1)), 0);
                        if (hits.size() >= 1024) {
                            this.chunkCache.put(chunkKey, List.copyOf(hits));
                            return;
                        }
                    }
                    if ((depthX = HoleEspModule.count3x1DepthX(passable, y, lx, lz, gridMinY, maxY)) >= minDepth) {
                        HoleEspModule.addHit(hits, seen, new class_238((double)(baseX + lx), (double)y, (double)(baseZ + lz), (double)(baseX + lx + 3), (double)(y + depthX), (double)(baseZ + lz + 1)), 1);
                        if (hits.size() >= 1024) {
                            this.chunkCache.put(chunkKey, List.copyOf(hits));
                            return;
                        }
                    }
                    if ((depthZ = HoleEspModule.count3x1DepthZ(passable, y, lx, lz, gridMinY, maxY)) >= minDepth) {
                        HoleEspModule.addHit(hits, seen, new class_238((double)(baseX + lx), (double)y, (double)(baseZ + lz), (double)(baseX + lx + 1), (double)(y + depthZ), (double)(baseZ + lz + 3)), 1);
                        if (hits.size() >= 1024) {
                            this.chunkCache.put(chunkKey, List.copyOf(hits));
                            return;
                        }
                    }
                    y += Math.max(1, Math.max(depth, Math.max(depthX, depthZ)));
                }
            }
        }
        if (hits.isEmpty()) {
            this.chunkCache.remove(chunkKey);
        } else {
            this.chunkCache.put(chunkKey, List.copyOf(hits));
        }
    }

    private static int count1x1Depth(boolean[] grid, int startY, int lx, int lz, int gridMinY, int maxY) {
        int y;
        for (y = startY; !(y >= maxY || !HoleEspModule.grid(grid, y, lx, lz, gridMinY) || HoleEspModule.grid(grid, y, lx - 1, lz, gridMinY) || HoleEspModule.grid(grid, y, lx + 1, lz, gridMinY) || HoleEspModule.grid(grid, y, lx, lz - 1, gridMinY) || HoleEspModule.grid(grid, y, lx, lz + 1, gridMinY)); ++y) {
        }
        return y - startY;
    }

    private static int count3x1DepthX(boolean[] grid, int startY, int lx, int lz, int gridMinY, int maxY) {
        int y;
        for (y = startY; !(y >= maxY || !HoleEspModule.grid(grid, y, lx, lz, gridMinY) || !HoleEspModule.grid(grid, y, lx + 1, lz, gridMinY) || !HoleEspModule.grid(grid, y, lx + 2, lz, gridMinY) || HoleEspModule.grid(grid, y, lx - 1, lz, gridMinY) || HoleEspModule.grid(grid, y, lx + 3, lz, gridMinY) || HoleEspModule.grid(grid, y, lx, lz - 1, gridMinY) || HoleEspModule.grid(grid, y, lx, lz + 1, gridMinY) || HoleEspModule.grid(grid, y, lx + 1, lz - 1, gridMinY) || HoleEspModule.grid(grid, y, lx + 1, lz + 1, gridMinY) || HoleEspModule.grid(grid, y, lx + 2, lz - 1, gridMinY) || HoleEspModule.grid(grid, y, lx + 2, lz + 1, gridMinY)); ++y) {
        }
        return y - startY;
    }

    private static int count3x1DepthZ(boolean[] grid, int startY, int lx, int lz, int gridMinY, int maxY) {
        int y;
        for (y = startY; !(y >= maxY || !HoleEspModule.grid(grid, y, lx, lz, gridMinY) || !HoleEspModule.grid(grid, y, lx, lz + 1, gridMinY) || !HoleEspModule.grid(grid, y, lx, lz + 2, gridMinY) || HoleEspModule.grid(grid, y, lx, lz - 1, gridMinY) || HoleEspModule.grid(grid, y, lx, lz + 3, gridMinY) || HoleEspModule.grid(grid, y, lx - 1, lz, gridMinY) || HoleEspModule.grid(grid, y, lx + 1, lz, gridMinY) || HoleEspModule.grid(grid, y, lx - 1, lz + 1, gridMinY) || HoleEspModule.grid(grid, y, lx + 1, lz + 1, gridMinY) || HoleEspModule.grid(grid, y, lx - 1, lz + 2, gridMinY) || HoleEspModule.grid(grid, y, lx + 1, lz + 2, gridMinY)); ++y) {
        }
        return y - startY;
    }

    private static boolean grid(boolean[] grid, int y, int lx, int lz, int gridMinY) {
        int yi = y - gridMinY;
        return grid[yi * 20 * 20 + (lz + 1) * 20 + lx + 1];
    }

    private void checkHole(class_638 level, class_2338 pos, boolean airOnly, List<Hit> hits, Set<String> seen) {
        if (!this.isValidHoleSection(level, pos, airOnly)) {
            return;
        }
        class_2338.class_2339 current = new class_2338.class_2339(pos.method_10263(), pos.method_10264(), pos.method_10260());
        while (this.isValidHoleSection(level, (class_2338)current, airOnly)) {
            current.method_10100(0, 1, 0);
        }
        int depth = current.method_10264() - pos.method_10264();
        if (depth >= Math.round(HoleEspModule.value(this.sMinHoleDepth, 4.0f))) {
            HoleEspModule.addHit(hits, seen, new class_238((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.method_10260(), (double)(pos.method_10263() + 1), (double)current.method_10264(), (double)(pos.method_10260() + 1)), 0);
        }
    }

    private void check3x1Hole(class_638 level, class_2338 pos, boolean airOnly, List<Hit> hits, Set<String> seen) {
        int depth;
        class_2338.class_2339 current;
        if (this.isValid3x1HoleSectionX(level, pos, airOnly)) {
            current = new class_2338.class_2339(pos.method_10263(), pos.method_10264(), pos.method_10260());
            while (this.isValid3x1HoleSectionX(level, (class_2338)current, airOnly)) {
                current.method_10100(0, 1, 0);
            }
            depth = current.method_10264() - pos.method_10264();
            if (depth >= Math.round(HoleEspModule.value(this.sMinHoleDepth, 4.0f))) {
                HoleEspModule.addHit(hits, seen, new class_238((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.method_10260(), (double)(pos.method_10263() + 3), (double)current.method_10264(), (double)(pos.method_10260() + 1)), 1);
            }
        }
        if (this.isValid3x1HoleSectionZ(level, pos, airOnly)) {
            current = new class_2338.class_2339(pos.method_10263(), pos.method_10264(), pos.method_10260());
            while (this.isValid3x1HoleSectionZ(level, (class_2338)current, airOnly)) {
                current.method_10100(0, 1, 0);
            }
            depth = current.method_10264() - pos.method_10264();
            if (depth >= Math.round(HoleEspModule.value(this.sMinHoleDepth, 4.0f))) {
                HoleEspModule.addHit(hits, seen, new class_238((double)pos.method_10263(), (double)pos.method_10264(), (double)pos.method_10260(), (double)(pos.method_10263() + 1), (double)current.method_10264(), (double)(pos.method_10260() + 3)), 1);
            }
        }
    }

    private void checkTunnelStarts(class_638 level, class_2338 pos, boolean airOnly, List<Hit> hits, Set<String> seen) {
        int minLength = Math.round(HoleEspModule.value(this.sMinTunnelLength, 3.0f));
        for (int[] dir : HORIZONTAL_DIRS) {
            class_2338.class_2339 previous = new class_2338.class_2339(pos.method_10263() - dir[0], pos.method_10264(), pos.method_10260() - dir[1]);
            if (this.isTunnelSection(level, (class_2338)previous, dir, airOnly)) continue;
            class_2338.class_2339 current = new class_2338.class_2339(pos.method_10263(), pos.method_10264(), pos.method_10260());
            class_2338 start = null;
            class_2338 end = null;
            int steps = 0;
            int maxHeight = 0;
            if (this.isTunnelSection(level, (class_2338)current, dir, airOnly)) {
                start = current.method_10062();
            }
            while (this.isTunnelSection(level, (class_2338)current, dir, airOnly)) {
                maxHeight = Math.max(maxHeight, this.getTunnelHeight(level, (class_2338)current, airOnly));
                end = current.method_10062();
                current.method_10100(dir[0], 0, dir[1]);
                ++steps;
            }
            if (start == null || end == null || steps < minLength) continue;
            HoleEspModule.addHit(hits, seen, new class_238((double)Math.min(start.method_10263(), end.method_10263()), (double)start.method_10264(), (double)Math.min(start.method_10260(), end.method_10260()), (double)(Math.max(start.method_10263(), end.method_10263()) + 1), (double)(start.method_10264() + maxHeight), (double)(Math.max(start.method_10260(), end.method_10260()) + 1)), 2);
        }
    }

    private void checkStaircaseStarts(class_638 level, class_2338 pos, boolean airOnly, List<Hit> hits, Set<String> seen) {
        int minLength = Math.round(HoleEspModule.value(this.sMinStairLength, 3.0f));
        for (int[] dir : HORIZONTAL_DIRS) {
            class_2338.class_2339 previous = new class_2338.class_2339(pos.method_10263() - dir[0], pos.method_10264() - 1, pos.method_10260() - dir[1]);
            if (this.isStaircaseSection(level, (class_2338)previous, dir, airOnly)) continue;
            class_2338.class_2339 current = new class_2338.class_2339(pos.method_10263(), pos.method_10264(), pos.method_10260());
            int steps = 0;
            ArrayList<class_238> boxes = new ArrayList<class_238>();
            while (this.isStaircaseSection(level, (class_2338)current, dir, airOnly)) {
                int height = this.getStaircaseHeight(level, (class_2338)current, airOnly);
                boxes.add(new class_238((double)current.method_10263(), (double)current.method_10264(), (double)current.method_10260(), (double)(current.method_10263() + 1), (double)(current.method_10264() + height), (double)(current.method_10260() + 1)));
                current.method_10100(dir[0], 1, dir[1]);
                ++steps;
            }
            if (steps < minLength) continue;
            for (class_238 box : boxes) {
                HoleEspModule.addHit(hits, seen, box, 3);
            }
        }
    }

    private boolean isValidHoleSection(class_638 level, class_2338 pos, boolean airOnly) {
        return HoleEspModule.isPassable(level, pos, airOnly) && !HoleEspModule.isPassable(level, pos.method_10263(), pos.method_10264(), pos.method_10260() - 1, airOnly) && !HoleEspModule.isPassable(level, pos.method_10263(), pos.method_10264(), pos.method_10260() + 1, airOnly) && !HoleEspModule.isPassable(level, pos.method_10263() + 1, pos.method_10264(), pos.method_10260(), airOnly) && !HoleEspModule.isPassable(level, pos.method_10263() - 1, pos.method_10264(), pos.method_10260(), airOnly);
    }

    private boolean isValid3x1HoleSectionX(class_638 level, class_2338 pos, boolean airOnly) {
        int z;
        int y;
        int x = pos.method_10263();
        return HoleEspModule.isPassable(level, x, y = pos.method_10264(), z = pos.method_10260(), airOnly) && HoleEspModule.isPassable(level, x + 1, y, z, airOnly) && HoleEspModule.isPassable(level, x + 2, y, z, airOnly) && !HoleEspModule.isPassable(level, x, y, z - 1, airOnly) && !HoleEspModule.isPassable(level, x, y, z + 1, airOnly) && !HoleEspModule.isPassable(level, x + 1, y, z - 1, airOnly) && !HoleEspModule.isPassable(level, x + 1, y, z + 1, airOnly) && !HoleEspModule.isPassable(level, x + 2, y, z - 1, airOnly) && !HoleEspModule.isPassable(level, x + 2, y, z + 1, airOnly) && !HoleEspModule.isPassable(level, x - 1, y, z, airOnly) && !HoleEspModule.isPassable(level, x + 3, y, z, airOnly);
    }

    private boolean isValid3x1HoleSectionZ(class_638 level, class_2338 pos, boolean airOnly) {
        int z;
        int y;
        int x = pos.method_10263();
        return HoleEspModule.isPassable(level, x, y = pos.method_10264(), z = pos.method_10260(), airOnly) && HoleEspModule.isPassable(level, x, y, z + 1, airOnly) && HoleEspModule.isPassable(level, x, y, z + 2, airOnly) && !HoleEspModule.isPassable(level, x - 1, y, z, airOnly) && !HoleEspModule.isPassable(level, x + 1, y, z, airOnly) && !HoleEspModule.isPassable(level, x - 1, y, z + 1, airOnly) && !HoleEspModule.isPassable(level, x + 1, y, z + 1, airOnly) && !HoleEspModule.isPassable(level, x - 1, y, z + 2, airOnly) && !HoleEspModule.isPassable(level, x + 1, y, z + 2, airOnly) && !HoleEspModule.isPassable(level, x, y, z - 1, airOnly) && !HoleEspModule.isPassable(level, x, y, z + 3, airOnly);
    }

    private boolean isTunnelSection(class_638 level, class_2338 pos, int[] dir, boolean airOnly) {
        int height = this.getTunnelHeight(level, pos, airOnly);
        if (height < Math.round(HoleEspModule.value(this.sMinTunnelHeight, 2.0f)) || height > Math.round(HoleEspModule.value(this.sMaxTunnelHeight, 3.0f))) {
            return false;
        }
        if (HoleEspModule.isPassable(level, pos.method_10263(), pos.method_10264() - 1, pos.method_10260(), airOnly) || HoleEspModule.isPassable(level, pos.method_10263(), pos.method_10264() + height, pos.method_10260(), airOnly)) {
            return false;
        }
        boolean alongX = dir[0] != 0;
        for (int i = 0; i < height; ++i) {
            int y = pos.method_10264() + i;
            if (!(alongX ? HoleEspModule.isPassable(level, pos.method_10263(), y, pos.method_10260() - 1, airOnly) || HoleEspModule.isPassable(level, pos.method_10263(), y, pos.method_10260() + 1, airOnly) : HoleEspModule.isPassable(level, pos.method_10263() - 1, y, pos.method_10260(), airOnly) || HoleEspModule.isPassable(level, pos.method_10263() + 1, y, pos.method_10260(), airOnly))) continue;
            return false;
        }
        return true;
    }

    private boolean isStaircaseSection(class_638 level, class_2338 pos, int[] dir, boolean airOnly) {
        int height = this.getStaircaseHeight(level, pos, airOnly);
        if (height < Math.round(HoleEspModule.value(this.sMinStairHeight, 3.0f)) || height > Math.round(HoleEspModule.value(this.sMaxStairHeight, 5.0f))) {
            return false;
        }
        if (HoleEspModule.isPassable(level, pos.method_10263(), pos.method_10264() - 1, pos.method_10260(), airOnly) || HoleEspModule.isPassable(level, pos.method_10263(), pos.method_10264() + height, pos.method_10260(), airOnly)) {
            return false;
        }
        boolean alongX = dir[0] != 0;
        for (int i = 0; i < height; ++i) {
            int y = pos.method_10264() + i;
            if (!(alongX ? HoleEspModule.isPassable(level, pos.method_10263(), y, pos.method_10260() - 1, airOnly) || HoleEspModule.isPassable(level, pos.method_10263(), y, pos.method_10260() + 1, airOnly) : HoleEspModule.isPassable(level, pos.method_10263() - 1, y, pos.method_10260(), airOnly) || HoleEspModule.isPassable(level, pos.method_10263() + 1, y, pos.method_10260(), airOnly))) continue;
            return false;
        }
        return true;
    }

    private int getTunnelHeight(class_638 level, class_2338 pos, boolean airOnly) {
        int height;
        int max = Math.round(HoleEspModule.value(this.sMaxTunnelHeight, 3.0f));
        for (height = 0; height < max && HoleEspModule.isPassable(level, pos.method_10263(), pos.method_10264() + height, pos.method_10260(), airOnly); ++height) {
        }
        return height;
    }

    private int getStaircaseHeight(class_638 level, class_2338 pos, boolean airOnly) {
        int height;
        int max = Math.round(HoleEspModule.value(this.sMaxStairHeight, 5.0f));
        for (height = 0; height < max && HoleEspModule.isPassable(level, pos.method_10263(), pos.method_10264() + height, pos.method_10260(), airOnly); ++height) {
        }
        return height;
    }

    private static boolean isPassable(class_638 level, int x, int y, int z, boolean airOnly) {
        return HoleEspModule.isPassable(level, (class_2338)PASSABLE_POS.get().method_10103(x, y, z), airOnly);
    }

    private static boolean isPassable(class_638 level, class_2338 pos, boolean airOnly) {
        class_2680 state = level.method_8320(pos);
        if (state.method_26215()) {
            return true;
        }
        if (airOnly) {
            return false;
        }
        class_265 shape = state.method_26220((class_1922)level, pos);
        return shape.method_1110() || !class_259.method_1077().equals((Object)shape);
    }

    private static void addHit(List<Hit> hits, Set<String> seen, class_238 box, int type) {
        String key = type + ":" + box.field_1323 + "," + box.field_1322 + "," + box.field_1321 + "," + box.field_1320 + "," + box.field_1325 + "," + box.field_1324;
        if (!seen.add(key)) {
            return;
        }
        for (Hit hit : hits) {
            if (hit.type() != type || !hit.box().method_994(box)) continue;
            return;
        }
        hits.add(new Hit(box, type));
    }

    private Mode currentMode() {
        String mode;
        return switch (mode = this.sMode == null ? MODE_ALL : this.sMode.value()) {
            case MODE_HOLES_TUNNELS -> new Mode(true, true, false);
            case MODE_HOLES_STAIRS -> new Mode(true, false, true);
            case MODE_TUNNELS_STAIRS -> new Mode(false, true, true);
            case MODE_HOLES -> new Mode(true, false, false);
            case MODE_TUNNELS -> new Mode(false, true, false);
            case MODE_STAIRS -> new Mode(false, false, true);
            default -> new Mode(true, true, true);
        };
    }

    private int lineColor(int type) {
        return switch (type) {
            case 1 -> HoleEspModule.color(this.sHole3Line, 1610589440);
            case 2 -> HoleEspModule.color(this.sTunnelLine, 0x5F0000FF);
            case 3 -> HoleEspModule.color(this.sStairLine, 0x5FFF00FF);
            default -> HoleEspModule.color(this.sHoleLine, 0x5FFF0000);
        };
    }

    private int fillColor(int type) {
        return switch (type) {
            case 1 -> HoleEspModule.color(this.sHole3Fill, 520070400);
            case 2 -> HoleEspModule.color(this.sTunnelFill, 503316735);
            case 3 -> HoleEspModule.color(this.sStairFill, 520028415);
            default -> HoleEspModule.color(this.sHoleFill, 520028160);
        };
    }

    private static int chunkX(long key) {
        return (int)key;
    }

    private static int chunkZ(long key) {
        return (int)(key >> 32);
    }

    private int scanRadius(class_310 mc) {
        int renderDistance = (Integer)mc.field_1690.method_42503().method_41753();
        float configuredValue = HoleEspModule.value(this.sScanRadius, 4.0f);
        if (HoleEspModule.isFastTravel(mc)) {
            configuredValue = Math.max(configuredValue, 12.0f);
        }
        int configured = Math.max(1, Math.round(configuredValue));
        return Math.min(renderDistance, configured);
    }

    private static boolean isFastTravel(class_310 mc) {
        if (mc.field_1724 == null) {
            return false;
        }
        class_243 velocity = mc.field_1724.method_18798();
        double horizontalSpeedSq = velocity.field_1352 * velocity.field_1352 + velocity.field_1350 * velocity.field_1350;
        return mc.field_1724.method_6128() || horizontalSpeedSq > 0.18;
    }

    private static float value(NumberSetting setting, float fallback) {
        return setting == null ? fallback : setting.value()[0];
    }

    private static int color(ColorSetting setting, int fallback) {
        return setting == null ? fallback : setting.argb();
    }

    @Environment(value=EnvType.CLIENT)
    private record Hit(class_238 box, int type) {
    }

    @Environment(value=EnvType.CLIENT)
    private record Mode(boolean holes, boolean tunnels, boolean stairs) {
    }
}

