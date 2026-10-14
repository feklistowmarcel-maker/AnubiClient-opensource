/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2IntMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1923
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2350
 *  net.minecraft.class_2596
 *  net.minecraft.class_2672
 *  net.minecraft.class_2676
 *  net.minecraft.class_2680
 *  net.minecraft.class_2741
 *  net.minecraft.class_2769
 *  net.minecraft.class_2818
 *  net.minecraft.class_2826
 *  net.minecraft.class_310
 *  net.minecraft.class_634
 *  net.minecraft.class_638
 *  net.minecraft.class_9848
 */
package net.anubis.client.module.impl.render;

import it.unimi.dsi.fastutil.longs.Long2BooleanOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2IntMap;
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.function.LongConsumer;
import net.anubis.client.event.EventHandler;
import net.anubis.client.event.impl.PacketAppliedEvent;
import net.anubis.client.event.impl.Render3DEvent;
import net.anubis.client.event.impl.TickEvent;
import net.anubis.client.module.ClientModule;
import net.anubis.client.module.ModuleCategory;
import net.anubis.client.module.setting.BooleanSetting;
import net.anubis.client.module.setting.ColorSetting;
import net.anubis.client.module.setting.ModeSetting;
import net.anubis.client.module.setting.NumberSetting;
import net.anubis.client.util.ChunkAreas;
import net.anubis.client.util.ChunkOverlay;
import net.anubis.client.util.GeodeGlowScan;
import net.anubis.client.util.LevelUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1923;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2596;
import net.minecraft.class_2672;
import net.minecraft.class_2676;
import net.minecraft.class_2680;
import net.minecraft.class_2741;
import net.minecraft.class_2769;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_638;
import net.minecraft.class_9848;

@Environment(value=EnvType.CLIENT)
public final class SusChunkFinderModule
extends ClientModule {
    private static final long SCAN_BUDGET_NANOS = 3000000L;
    private static final int MIN_SCANNED_NEIGHBOURS = 3;
    private static final int NO_PACKET_SENSITIVITY = 1;
    private static final int VISIBLE_REFRESH_TICKS = 20;
    private static final int PACKET_MIN_Y = -16;
    private static final int PACKET_MAX_Y = 30;
    private static final String METHOD_CLASSIC = "Classic";
    private static final String METHOD_ANBS = "ANBS+ Scan";
    private static final int GLOW_CELLS_PER_POINT = 12;
    private final ModeSetting method = this.addSetting("method", new ModeSetting("Method", "Classic", "Classic", "ANBS+ Scan"));
    private final NumberSetting spreadRadius = this.addSetting("spread-radius", new NumberSetting("Simulation Distance", 5.0f, 2.0f, 16.0f).step(1.0f));
    private final NumberSetting sensitivity = this.addSetting("packet-sensitivity", new NumberSetting("Sensitivity", 6.0f, 1.0f, 20.0f).step(1.0f));
    private final ColorSetting chunkColor = this.addSetting("chunk-color", new ColorSetting("Chunk Color", 1627325444));
    private final BooleanSetting tracers = this.addSetting("tracers", new BooleanSetting("Tracers", true));
    private final ChunkAreas chunkAreas = new ChunkAreas();
    private final ChunkOverlay chunkOverlay = new ChunkOverlay();
    private final GeodeGlowScan glow = new GeodeGlowScan();
    private final Long2IntOpenHashMap heat = new Long2IntOpenHashMap();
    private final Long2IntOpenHashMap vetoes = new Long2IntOpenHashMap();
    private final Long2IntOpenHashMap chunkScore = new Long2IntOpenHashMap();
    private final Long2BooleanOpenHashMap chunkUngrown = new Long2BooleanOpenHashMap();
    private final Long2BooleanOpenHashMap chunkHasPacket = new Long2BooleanOpenHashMap();
    private final LongOpenHashSet scannedChunks = new LongOpenHashSet();
    private final LongArrayFIFOQueue queue = new LongArrayFIFOQueue();
    private final LongOpenHashSet queued = new LongOpenHashSet();
    private Long2IntOpenHashMap visible = new Long2IntOpenHashMap();
    private boolean visibleDirty = true;
    private int visibleAge;
    private class_638 lastLevel;
    private int appliedRadius;

    public SusChunkFinderModule() {
        super("sus-chunk-finder", "Sus Chunk Finder", "Finds chunks with signs of player activity from fully grown amethyst.", ModuleCategory.BASE_RAID);
        this.method.onChange(this::rescan);
        this.sensitivity.onChange(() -> {
            this.visibleDirty = true;
        });
    }

    private void rescan() {
        this.clearAll();
        this.queueLoadedChunks();
    }

    @Override
    protected void onEnable() {
        this.clearAll();
        this.lastLevel = class_310.method_1551().field_1687;
        this.queueLoadedChunks();
    }

    @Override
    protected void onDisable() {
        this.clearAll();
        this.lastLevel = null;
    }

    private void clearAll() {
        this.heat.clear();
        this.vetoes.clear();
        this.chunkScore.clear();
        this.chunkUngrown.clear();
        this.chunkHasPacket.clear();
        this.scannedChunks.clear();
        this.queue.clear();
        this.queued.clear();
        this.visible = new Long2IntOpenHashMap();
        this.visibleDirty = true;
        this.appliedRadius = this.spreadRadius.getInt();
    }

    private void queueLoadedChunks() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) {
            return;
        }
        int radius = (Integer)mc.field_1690.method_42503().method_41753() + 1;
        class_1923 center = mc.field_1724.method_31476();
        for (int x = center.field_9181 - radius; x <= center.field_9181 + radius; ++x) {
            for (int z = center.field_9180 - radius; z <= center.field_9180 + radius; ++z) {
                if (!LevelUtil.isLoaded(mc.field_1687, x, z)) continue;
                this.enqueue(class_1923.method_8331((int)x, (int)z));
            }
        }
    }

    private void enqueue(long key) {
        if (this.queued.add(key)) {
            this.queue.enqueue(key);
        }
    }

    @EventHandler
    public void onPacketApplied(PacketAppliedEvent event) {
        class_2596<?> class_25962 = event.packet;
        if (class_25962 instanceof class_2672) {
            class_2672 chunk = (class_2672)class_25962;
            this.enqueue(class_1923.method_8331((int)chunk.method_11523(), (int)chunk.method_11524()));
        } else {
            class_25962 = event.packet;
            if (class_25962 instanceof class_2676) {
                class_2676 light = (class_2676)class_25962;
                if (this.method.is(METHOD_ANBS)) {
                    this.enqueue(class_1923.method_8331((int)light.method_11558(), (int)light.method_11554()));
                }
            }
        }
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_310 mc = class_310.method_1551();
        class_638 level = mc.field_1687;
        if (level != this.lastLevel) {
            this.clearAll();
            this.lastLevel = level;
            this.queueLoadedChunks();
        }
        if (level == null) {
            return;
        }
        if (this.spreadRadius.getInt() != this.appliedRadius) {
            this.clearAll();
            this.queueLoadedChunks();
        }
        long deadline = System.nanoTime() + 3000000L;
        while (!this.queue.isEmpty() && System.nanoTime() < deadline) {
            long key = this.queue.dequeueLong();
            this.queued.remove(key);
            int cx = class_1923.method_8325((long)key);
            int cz = class_1923.method_8332((long)key);
            if (!LevelUtil.isLoaded(level, cx, cz)) continue;
            this.updateChunk(key, level.method_8497(cx, cz), level);
            this.scannedChunks.add(key);
            this.visibleDirty = true;
        }
        if (this.visibleDirty || ++this.visibleAge >= 20) {
            this.visible = this.computeVisible(level);
            this.visibleDirty = false;
            this.visibleAge = 0;
        }
    }

    private void updateChunk(long key, class_2818 chunk, class_638 level) {
        int r = this.spreadRadius.getInt();
        int cx = class_1923.method_8325((long)key);
        int cz = class_1923.method_8332((long)key);
        int oldHeat = this.chunkScore.getOrDefault(key, 0);
        boolean oldUngrown = this.chunkUngrown.getOrDefault(key, false);
        if (oldHeat > 0) {
            SusChunkFinderModule.forArea(cx, cz, r, area -> SusChunkFinderModule.subtract(this.heat, area, oldHeat));
        }
        if (oldUngrown) {
            SusChunkFinderModule.forArea(cx, cz, r, area -> SusChunkFinderModule.subtract(this.vetoes, area, 1));
        }
        this.chunkHasPacket.put(key, SusChunkFinderModule.hasBlocksInRange(chunk, -16, 30));
        ChunkScore score = this.scoreChunk(chunk, level);
        this.chunkScore.put(key, score.selfHeat());
        this.chunkUngrown.put(key, score.hasUngrown());
        if (score.hasUngrown()) {
            SusChunkFinderModule.forArea(cx, cz, r, area -> this.vetoes.addTo(area, 1));
        }
        if (score.selfHeat() > 0) {
            SusChunkFinderModule.forArea(cx, cz, r, area -> this.heat.addTo(area, score.selfHeat()));
        }
    }

    private static void forArea(int cx, int cz, int r, LongConsumer action) {
        for (int dx = -r; dx <= r; ++dx) {
            for (int dz = -r; dz <= r; ++dz) {
                action.accept(class_1923.method_8331((int)(cx + dx), (int)(cz + dz)));
            }
        }
    }

    private static void subtract(Long2IntOpenHashMap map, long key, int amount) {
        int left = map.get(key) - amount;
        if (left > 0) {
            map.put(key, left);
        } else {
            map.remove(key);
        }
    }

    private static boolean hasBlocksInRange(class_2818 chunk, int minY, int maxY) {
        class_2826[] sections = chunk.method_12006();
        int minSectionY = chunk.method_32891();
        for (int i = 0; i < sections.length; ++i) {
            int baseY;
            class_2826 section = sections[i];
            if (section == null || section.method_38292() || (baseY = minSectionY + i << 4) + 15 < minY || baseY > maxY) continue;
            for (int y = 0; y < 16; ++y) {
                int worldY = baseY + y;
                if (worldY < minY || worldY > maxY) continue;
                for (int x = 0; x < 16; ++x) {
                    for (int z = 0; z < 16; ++z) {
                        if (section.method_12254(x, y, z).method_26215()) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private ChunkScore scoreChunk(class_2818 chunk, class_638 level) {
        if (this.method.is(METHOD_ANBS)) {
            return new ChunkScore(this.glow.scan(level, chunk, null) / 12, false);
        }
        class_2826[] sections = chunk.method_12006();
        int minSectionY = chunk.method_32891();
        class_1923 pos = chunk.method_12004();
        boolean paletteOnly = SusChunkFinderModule.isDonutFolia();
        int grown = 0;
        int ungrown = 0;
        boolean sawBud = false;
        boolean sawCluster = false;
        class_2338.class_2339 cursor = new class_2338.class_2339();
        for (int i = 0; i < sections.length; ++i) {
            class_2826 section = sections[i];
            if (section == null || section.method_38292()) continue;
            if (paletteOnly) {
                if (!sawBud && section.method_19523(SusChunkFinderModule::isBud)) {
                    sawBud = true;
                }
                if (sawCluster || !section.method_19523(state -> state.method_27852(class_2246.field_27161))) continue;
                sawCluster = true;
                continue;
            }
            if (!section.method_19523(SusChunkFinderModule::isAmethystGrowth)) continue;
            int baseY = minSectionY + i << 4;
            for (int x = 0; x < 16; ++x) {
                for (int y = 0; y < 16; ++y) {
                    for (int z = 0; z < 16; ++z) {
                        class_2680 state2 = section.method_12254(x, y, z);
                        if (state2.method_27852(class_2246.field_27161)) {
                            ++grown;
                            continue;
                        }
                        if (!SusChunkFinderModule.isBud(state2) || !state2.method_28498((class_2769)class_2741.field_12525)) continue;
                        class_2350 facing = (class_2350)state2.method_11654((class_2769)class_2741.field_12525);
                        cursor.method_10103(pos.method_8326() + x, baseY + y, pos.method_8328() + z).method_10098(facing.method_10153());
                        if (level.method_8320((class_2338)cursor).method_27852(class_2246.field_27160)) {
                            ++ungrown;
                            continue;
                        }
                        ++grown;
                    }
                }
            }
        }
        if (paletteOnly) {
            if (sawBud) {
                ++ungrown;
            } else if (sawCluster) {
                ++grown;
            }
        }
        return new ChunkScore(grown, ungrown > 0);
    }

    private static boolean isBud(class_2680 state) {
        class_2248 block = state.method_26204();
        return block == class_2246.field_27164 || block == class_2246.field_27163 || block == class_2246.field_27162;
    }

    private static boolean isAmethystGrowth(class_2680 state) {
        return state.method_27852(class_2246.field_27161) || SusChunkFinderModule.isBud(state);
    }

    private static boolean isDonutFolia() {
        class_634 connection = class_310.method_1551().method_1562();
        if (connection == null) {
            return false;
        }
        String brand = connection.method_52790();
        return brand != null && brand.contains("DonutFolia");
    }

    private Long2IntOpenHashMap computeVisible(class_638 level) {
        Long2IntOpenHashMap eligible = new Long2IntOpenHashMap();
        for (Long2IntMap.Entry entry : this.heat.long2IntEntrySet()) {
            int cz;
            int cx;
            long key = entry.getLongKey();
            if (this.vetoes.containsKey(key) || !LevelUtil.isLoaded(level, cx = class_1923.method_8325((long)key), cz = class_1923.method_8332((long)key)) || this.scannedNeighbours(cx, cz) < 3) continue;
            eligible.put(key, entry.getIntValue());
        }
        int withPacket = this.sensitivity.getInt();
        int reach = this.appliedRadius;
        Long2IntOpenHashMap kept = new Long2IntOpenHashMap();
        for (Long2IntMap.Entry entry : eligible.long2IntEntrySet()) {
            int threshold;
            long key = entry.getLongKey();
            int value = entry.getIntValue();
            if (value < (threshold = this.chunkHasPacket.getOrDefault(key, true) ? withPacket : 1) || !SusChunkFinderModule.isPeak(eligible, key, value, reach)) continue;
            kept.put(key, value);
        }
        return kept;
    }

    private static boolean isPeak(Long2IntOpenHashMap field, long key, int value, int reach) {
        int cx = class_1923.method_8325((long)key);
        int cz = class_1923.method_8332((long)key);
        for (int dx = -reach; dx <= reach; ++dx) {
            for (int dz = -reach; dz <= reach; ++dz) {
                if (dx == 0 && dz == 0 || field.get(class_1923.method_8331((int)(cx + dx), (int)(cz + dz))) <= value) continue;
                return false;
            }
        }
        return true;
    }

    private int scannedNeighbours(int cx, int cz) {
        int count = 0;
        for (int dx = -1; dx <= 1; ++dx) {
            for (int dz = -1; dz <= 1; ++dz) {
                if (dx == 0 && dz == 0 || !this.scannedChunks.contains(class_1923.method_8331((int)(cx + dx), (int)(cz + dz)))) continue;
                ++count;
            }
        }
        return count;
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (class_310.method_1551().field_1687 == null) {
            return;
        }
        this.chunkOverlay.draw(this.visible.keySet(), this.chunkColor.argb(), event.partialTick);
        if (this.tracers.get()) {
            this.chunkAreas.drawTracers(this.visible.keySet(), 63.0, class_9848.method_61334((int)this.chunkColor.argb()));
        }
    }

    @Environment(value=EnvType.CLIENT)
    private record ChunkScore(int selfHeat, boolean hasUngrown) {
    }
}

