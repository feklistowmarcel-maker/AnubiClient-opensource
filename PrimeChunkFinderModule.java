/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.ints.IntArrayList
 *  it.unimi.dsi.fastutil.longs.Long2LongMap$Entry
 *  it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.longs.LongArrayList
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongListIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_156
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1923
 *  net.minecraft.class_1935
 *  net.minecraft.class_1944
 *  net.minecraft.class_2231
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2269
 *  net.minecraft.class_2309
 *  net.minecraft.class_2312
 *  net.minecraft.class_2315
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2350
 *  net.minecraft.class_2377
 *  net.minecraft.class_2401
 *  net.minecraft.class_2426
 *  net.minecraft.class_2428
 *  net.minecraft.class_2436
 *  net.minecraft.class_2453
 *  net.minecraft.class_2457
 *  net.minecraft.class_2459
 *  net.minecraft.class_2537
 *  net.minecraft.class_2561
 *  net.minecraft.class_2596
 *  net.minecraft.class_2665
 *  net.minecraft.class_2667
 *  net.minecraft.class_2671
 *  net.minecraft.class_2672
 *  net.minecraft.class_2676
 *  net.minecraft.class_2678
 *  net.minecraft.class_2680
 *  net.minecraft.class_2804
 *  net.minecraft.class_2806
 *  net.minecraft.class_2818
 *  net.minecraft.class_2826
 *  net.minecraft.class_310
 *  net.minecraft.class_3562
 *  net.minecraft.class_368
 *  net.minecraft.class_374
 *  net.minecraft.class_4076
 *  net.minecraft.class_4850
 *  net.minecraft.class_5803
 *  net.minecraft.class_631
 *  net.minecraft.class_638
 *  net.minecraft.class_6606
 *  net.minecraft.class_8886
 *  net.minecraft.class_9848
 */
package net.anubis.client.module.impl.render;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.longs.Long2LongMap;
import it.unimi.dsi.fastutil.longs.Long2LongOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongLinkedOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.anubis.client.event.EventHandler;
import net.anubis.client.event.Stage;
import net.anubis.client.event.impl.BlockUpdateEvent;
import net.anubis.client.event.impl.PacketReceiveNettyEvent;
import net.anubis.client.event.impl.Render3DEvent;
import net.anubis.client.event.impl.TickEvent;
import net.anubis.client.gui.ChallengeToast;
import net.anubis.client.module.ClientModule;
import net.anubis.client.module.ModuleCategory;
import net.anubis.client.module.setting.BooleanSetting;
import net.anubis.client.module.setting.ColorSetting;
import net.anubis.client.util.ChunkAreas;
import net.anubis.client.util.ChunkOverlay;
import net.anubis.client.util.LevelUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_156;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1923;
import net.minecraft.class_1935;
import net.minecraft.class_1944;
import net.minecraft.class_2231;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2269;
import net.minecraft.class_2309;
import net.minecraft.class_2312;
import net.minecraft.class_2315;
import net.minecraft.class_2338;
import net.minecraft.class_2350;
import net.minecraft.class_2377;
import net.minecraft.class_2401;
import net.minecraft.class_2426;
import net.minecraft.class_2428;
import net.minecraft.class_2436;
import net.minecraft.class_2453;
import net.minecraft.class_2457;
import net.minecraft.class_2459;
import net.minecraft.class_2537;
import net.minecraft.class_2561;
import net.minecraft.class_2596;
import net.minecraft.class_2665;
import net.minecraft.class_2667;
import net.minecraft.class_2671;
import net.minecraft.class_2672;
import net.minecraft.class_2676;
import net.minecraft.class_2678;
import net.minecraft.class_2680;
import net.minecraft.class_2804;
import net.minecraft.class_2806;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import net.minecraft.class_3562;
import net.minecraft.class_368;
import net.minecraft.class_374;
import net.minecraft.class_4076;
import net.minecraft.class_4850;
import net.minecraft.class_5803;
import net.minecraft.class_631;
import net.minecraft.class_638;
import net.minecraft.class_6606;
import net.minecraft.class_8886;
import net.minecraft.class_9848;

@Environment(value=EnvType.CLIENT)
public final class PrimeChunkFinderModule
extends ClientModule {
    private static final int SECTION_BYTES = 2048;
    private static final int MAX_FLAGGED_CHUNKS = 256;
    private static final int AMETHYST_MAX_LIGHT = 5;
    private static final int AMETHYST_REACH = 5;
    private static final long REDSTONE_NOTIFY_COOLDOWN_MS = 15000L;
    private static final int ORE_LIGHT = 9;
    private static final long[] NO_ORES = new long[0];
    private static final int LAMP_LIGHT = 14;
    private static final int[] NO_LAMPS = new int[0];
    private static final long GONE_LAMP_MS = 5000L;
    private static final int MAX_TRACKED_CHUNKS = 5000;
    private static final int SNAPSHOT_RADIUS = 32;
    private static final byte[] ZERO_SECTION = new byte[2048];
    private static final double MAX_Y = 0.0;
    private final ColorSetting chunkColor = this.addSetting("chunk-color", new ColorSetting("Chunk Color", 1342242650));
    private final BooleanSetting tracers = this.addSetting("tracers", new BooleanSetting("Tracers", true));
    private final BooleanSetting sound = this.addSetting("sound", new BooleanSetting("Sound", true));
    private final LinkedHashMap<Long, Map<Integer, byte[]>> chunkLight = new LinkedHashMap(256, 0.75f, true);
    private final LongOpenHashSet passedChunks = new LongOpenHashSet();
    private final LongLinkedOpenHashSet flaggedChunks = new LongLinkedOpenHashSet();
    private final LongOpenHashSet shownChunks = new LongOpenHashSet();
    private final ChunkAreas chunkAreas = new ChunkAreas();
    private final ChunkOverlay chunkOverlay = new ChunkOverlay();
    private final LongOpenHashSet checkedSections = new LongOpenHashSet();
    private final LongOpenHashSet amethystSections = new LongOpenHashSet();
    private final Long2ObjectOpenHashMap<long[]> oreSections = new Long2ObjectOpenHashMap();
    private final Long2ObjectOpenHashMap<int[]> lampSections = new Long2ObjectOpenHashMap();
    private final Long2LongOpenHashMap goneLamps = new Long2LongOpenHashMap();
    private final class_2338.class_2339 cursor = new class_2338.class_2339();
    private final Long2LongOpenHashMap redstoneNotified = new Long2LongOpenHashMap();

    public PrimeChunkFinderModule() {
        super("prime-chunk-finder", "Prime Chunk Finder", "Flags chunks with light and redstone changes below Y 0.", ModuleCategory.BASE_RAID);
        BooleanSetting notifications = this.usesNotifications();
        this.sound.visibleWhen(notifications::get);
        this.redstoneNotified.defaultReturnValue(-4611686018427387904L);
    }

    @Override
    public boolean persistEnabled() {
        return false;
    }

    @Override
    protected void onEnable() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.field_1687 == null || mc.field_1724.method_23318() >= 0.0) {
            PrimeChunkFinderModule.warnDeepslateOnly(mc);
            this.requestDisable();
            return;
        }
        this.reset();
        this.snapshotLoadedChunks();
        this.on(PacketReceiveNettyEvent.TYPE, this::onPacket);
    }

    @Override
    protected void onDisable() {
        this.reset();
    }

    private static void warnDeepslateOnly(class_310 mc) {
        class_374 toasts = mc.method_1566();
        if (toasts.method_1997(DeepslateToast.class, class_368.field_2208) == null) {
            toasts.method_1999((class_368)new DeepslateToast());
        }
    }

    private void onPacket(PacketReceiveNettyEvent event) {
        class_310 mc = class_310.method_1551();
        if (event.packet instanceof class_2678) {
            mc.execute(() -> {
                if (this.isEnabled()) {
                    this.reset();
                }
            });
        } else {
            class_2596<?> class_25962 = event.packet;
            if (class_25962 instanceof class_2672) {
                class_2672 packet = (class_2672)class_25962;
                int chunkX = packet.method_11523();
                int chunkZ = packet.method_11524();
                class_6606 data = packet.method_38599();
                mc.execute(() -> {
                    if (this.isEnabled() && mc.field_1687 != null) {
                        this.baselineChunk(chunkX, chunkZ, data);
                    }
                });
            } else {
                class_2596<?> chunkX = event.packet;
                if (chunkX instanceof class_2676) {
                    class_2676 packet = (class_2676)chunkX;
                    int chunkX2 = packet.method_11558();
                    int chunkZ = packet.method_11554();
                    class_6606 data = packet.method_38600();
                    mc.execute(() -> {
                        if (this.isEnabled() && mc.field_1687 != null) {
                            this.detectChanges(chunkX2, chunkZ, data);
                        }
                    });
                }
            }
        }
    }

    @EventHandler
    public void onBlockUpdate(BlockUpdateEvent event) {
        if (event.oldState == event.newState || class_310.method_1551().field_1687 == null) {
            return;
        }
        this.rememberGoneLamp(event.pos, event.oldState, event.newState);
        this.detectRedstone(event.pos, event.oldState, event.newState);
    }

    @EventHandler(stage=Stage.POST)
    public void onTick(TickEvent event) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null) {
            this.reset();
            return;
        }
        if (mc.field_1724 != null && mc.field_1724.method_23318() < 0.0) {
            this.passedChunks.add(mc.field_1724.method_31476().method_8324());
        }
        this.forgetOldLamps();
        this.refreshShown(mc.field_1687);
    }

    private void refreshShown(class_638 level) {
        this.shownChunks.clear();
        LongListIterator longListIterator = this.flaggedChunks.iterator();
        while (longListIterator.hasNext()) {
            long key = (Long)longListIterator.next();
            if (!LevelUtil.isLoaded(level, class_1923.method_8325((long)key), class_1923.method_8332((long)key))) continue;
            this.shownChunks.add(key);
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        if (this.shownChunks.isEmpty() || class_310.method_1551().field_1687 == null) {
            return;
        }
        int fill = this.chunkColor.argb();
        this.chunkOverlay.draw((LongSet)this.shownChunks, fill, event.partialTick);
        if (this.tracers.get()) {
            this.chunkAreas.drawTracers((LongSet)this.shownChunks, 63.0, class_9848.method_61334((int)fill));
        }
    }

    private void flagChunk(long key) {
        this.flaggedChunks.addAndMoveToLast(key);
        this.shownChunks.add(key);
        while (this.flaggedChunks.size() > 256) {
            this.flaggedChunks.removeFirstLong();
        }
    }

    private void snapshotLoadedChunks() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) {
            return;
        }
        int centerX = mc.field_1724.method_31477() >> 4;
        int centerZ = mc.field_1724.method_31479() >> 4;
        for (int chunkX = centerX - 32; chunkX <= centerX + 32; ++chunkX) {
            for (int chunkZ = centerZ - 32; chunkZ <= centerZ + 32; ++chunkZ) {
                Map<Integer, byte[]> sections = this.snapshotChunk(chunkX, chunkZ);
                if (sections == null) continue;
                this.chunkLight.put(class_1923.method_8331((int)chunkX, (int)chunkZ), sections);
            }
        }
        this.trimChunkCache();
    }

    private Map<Integer, byte[]> snapshotChunk(int chunkX, int chunkZ) {
        class_638 level = class_310.method_1551().field_1687;
        if (level == null) {
            return null;
        }
        class_631 chunkSource = level.method_2935();
        if (!chunkSource.method_12123(chunkX, chunkZ)) {
            return null;
        }
        class_3562 blockLight = chunkSource.method_12130().method_15562(class_1944.field_9282);
        Map<Integer, byte[]> sections = this.createZeroedChunk();
        for (int sectionY = this.bottomSection(); sectionY <= this.topSection(); ++sectionY) {
            class_2804 layer = blockLight.method_15544(class_4076.method_18676((int)chunkX, (int)sectionY, (int)chunkZ));
            if (layer == null || layer.method_51379(0)) continue;
            sections.put(sectionY, Arrays.copyOf(layer.method_12137(), 2048));
        }
        return sections;
    }

    private void baselineChunk(int chunkX, int chunkZ, class_6606 data) {
        Map<Integer, byte[]> sections = this.createZeroedChunk();
        this.applyInitializedSections(data, sections, chunkX, chunkZ, false, null);
        this.applyUninitializedSections(data, sections, chunkX, chunkZ, false, null);
        this.chunkLight.put(class_1923.method_8331((int)chunkX, (int)chunkZ), sections);
        this.trimChunkCache();
    }

    private void detectChanges(int chunkX, int chunkZ, class_6606 data) {
        boolean compare;
        long key = class_1923.method_8331((int)chunkX, (int)chunkZ);
        this.checkedSections.clear();
        this.amethystSections.clear();
        this.oreSections.clear();
        this.lampSections.clear();
        Map<Integer, byte[]> sections = this.chunkLight.get(key);
        if (sections == null) {
            sections = this.snapshotChunk(chunkX, chunkZ);
        }
        boolean bl = compare = sections != null && !this.isOwnChunk(key);
        if (sections == null) {
            sections = this.createZeroedChunk();
        }
        ChangeSummary summary = new ChangeSummary();
        this.applyInitializedSections(data, sections, chunkX, chunkZ, compare, summary);
        this.applyUninitializedSections(data, sections, chunkX, chunkZ, compare, summary);
        this.chunkLight.put(key, sections);
        this.trimChunkCache();
        if (summary.count == 0) {
            return;
        }
        this.flagChunk(key);
        if (this.notificationsOn()) {
            ChallengeToast.addOrUpdate(new class_1799((class_1935)class_1802.field_8733), this.name(), (class_2561)class_2561.method_43470((String)(summary.x + ", " + summary.y + ", " + summary.z)), this.sound.get());
        }
    }

    private void rememberGoneLamp(class_2338 pos, class_2680 previous, class_2680 state) {
        if (!PrimeChunkFinderModule.isLamp(previous)) {
            return;
        }
        int light = previous.method_26213();
        if (PrimeChunkFinderModule.isLamp(state) && state.method_26213() >= light) {
            return;
        }
        this.goneLamps.put(pos.method_10063(), class_156.method_658() + 5000L << 4 | (long)light);
    }

    private void forgetOldLamps() {
        if (this.goneLamps.isEmpty()) {
            return;
        }
        long now = class_156.method_658();
        LongIterator it = this.goneLamps.values().iterator();
        while (it.hasNext()) {
            if (it.nextLong() >>> 4 >= now) continue;
            it.remove();
        }
    }

    private void detectRedstone(class_2338 pos, class_2680 previous, class_2680 state) {
        if ((double)pos.method_10264() >= 0.0 || !PrimeChunkFinderModule.isRedstone(state) && !PrimeChunkFinderModule.isRedstone(previous)) {
            return;
        }
        long key = class_1923.method_8331((int)(pos.method_10263() >> 4), (int)(pos.method_10260() >> 4));
        if (this.isOwnChunk(key)) {
            return;
        }
        this.flagChunk(key);
        long now = class_156.method_658();
        if (!this.notificationsOn() || now - this.redstoneNotified.get(key) < 15000L) {
            return;
        }
        this.redstoneNotified.put(key, now);
        ChallengeToast.addOrUpdate(new class_1799((class_1935)class_1802.field_8725), this.name(), (class_2561)class_2561.method_43470((String)("Redstone " + pos.method_10263() + ", " + pos.method_10264() + ", " + pos.method_10260())), this.sound.get());
    }

    private static boolean isRedstone(class_2680 state) {
        class_2248 block = state.method_26204();
        return block instanceof class_2457 || block instanceof class_2312 || block instanceof class_2459 || block instanceof class_2453 || block instanceof class_2426 || block instanceof class_2665 || block instanceof class_2671 || block instanceof class_2667 || block instanceof class_2315 || block instanceof class_2377 || block instanceof class_2428 || block instanceof class_2309 || block instanceof class_2401 || block instanceof class_2269 || block instanceof class_2231 || block instanceof class_2537 || block instanceof class_4850 || block instanceof class_8886 || block instanceof class_2436;
    }

    private boolean isOwnChunk(long key) {
        if (this.passedChunks.contains(key)) {
            return true;
        }
        class_310 mc = class_310.method_1551();
        return mc.field_1724 != null && mc.field_1724.method_31476().method_8324() == key;
    }

    private void applyInitializedSections(class_6606 data, Map<Integer, byte[]> sections, int chunkX, int chunkZ, boolean compare, ChangeSummary summary) {
        BitSet mask = data.method_38608();
        List updates = data.method_38610();
        int next = 0;
        int bit = mask.nextSetBit(0);
        while (bit >= 0 && next < updates.size()) {
            byte[] update = (byte[])updates.get(next++);
            int sectionY = this.lightSectionY(bit);
            if (this.isWorldSection(sectionY) && update.length == 2048) {
                this.updateSection(sections, chunkX, chunkZ, sectionY, Arrays.copyOf(update, 2048), compare, summary);
            }
            bit = mask.nextSetBit(bit + 1);
        }
    }

    private void applyUninitializedSections(class_6606 data, Map<Integer, byte[]> sections, int chunkX, int chunkZ, boolean compare, ChangeSummary summary) {
        BitSet empty = data.method_38609();
        int bit = empty.nextSetBit(0);
        while (bit >= 0) {
            int sectionY = this.lightSectionY(bit);
            if (this.isWorldSection(sectionY)) {
                this.updateSection(sections, chunkX, chunkZ, sectionY, ZERO_SECTION, compare, summary);
            }
            bit = empty.nextSetBit(bit + 1);
        }
    }

    private void updateSection(Map<Integer, byte[]> sections, int chunkX, int chunkZ, int sectionY, byte[] light, boolean compare, ChangeSummary summary) {
        byte[] previous = sections.get(sectionY);
        if (compare && previous != null && summary != null) {
            this.compareSection(previous, light, chunkX, chunkZ, sectionY, summary);
        }
        sections.put(sectionY, light);
    }

    private void compareSection(byte[] previous, byte[] light, int chunkX, int chunkZ, int sectionY, ChangeSummary summary) {
        for (int i = 0; i < 2048; ++i) {
            int was = previous[i] & 0xFF;
            int now = light[i] & 0xFF;
            int changed = was ^ now;
            if ((changed & 0xF) != 0) {
                this.recordChange(chunkX, chunkZ, sectionY, i << 1, was & 0xF, now & 0xF, summary);
            }
            if ((changed & 0xF0) == 0) continue;
            this.recordChange(chunkX, chunkZ, sectionY, (i << 1) + 1, was >>> 4 & 0xF, now >>> 4 & 0xF, summary);
        }
    }

    private void recordChange(int chunkX, int chunkZ, int sectionY, int index, int oldValue, int newValue, ChangeSummary summary) {
        int x = (chunkX << 4) + (index & 0xF);
        int y = (sectionY << 4) + (index >>> 8 & 0xF);
        int z = (chunkZ << 4) + (index >>> 4 & 0xF);
        int value = Math.max(oldValue, newValue);
        if (this.fromAmethyst(x, y, z, oldValue, newValue) || this.fromRedstoneOre(x, y, z, value) || this.fromLamp(x, y, z, value)) {
            return;
        }
        summary.add(x, y, z, oldValue, newValue);
    }

    private boolean fromLamp(int x, int y, int z, int value) {
        if (value > 14) {
            return false;
        }
        class_638 level = class_310.method_1551().field_1687;
        if (level == null) {
            return false;
        }
        int reach = 14 - value;
        for (int sx = x - reach >> 4; sx <= x + reach >> 4; ++sx) {
            for (int sy = y - reach >> 4; sy <= y + reach >> 4; ++sy) {
                for (int sz = z - reach >> 4; sz <= z + reach >> 4; ++sz) {
                    for (int lamp : this.lamps(level, sx, sy, sz)) {
                        int index = lamp >>> 4;
                        int lx = (sx << 4) + (index & 0xF);
                        int ly = (sy << 4) + (index >>> 8);
                        int lz = (sz << 4) + (index >>> 4 & 0xF);
                        if (Math.abs(lx - x) + Math.abs(ly - y) + Math.abs(lz - z) > (lamp & 0xF) - value) continue;
                        return true;
                    }
                }
            }
        }
        long now = class_156.method_658();
        for (Long2LongMap.Entry gone : this.goneLamps.long2LongEntrySet()) {
            long pos;
            int distance;
            long packed = gone.getLongValue();
            if (packed >>> 4 < now || (distance = Math.abs(class_2338.method_10061((long)(pos = gone.getLongKey())) - x) + Math.abs(class_2338.method_10071((long)pos) - y) + Math.abs(class_2338.method_10083((long)pos) - z)) > (int)(packed & 0xFL) - value) continue;
            return true;
        }
        return false;
    }

    private int[] lamps(class_638 level, int sectionX, int sectionY, int sectionZ) {
        long key = class_4076.method_18685((int)sectionX, (int)sectionY, (int)sectionZ);
        int[] cached = (int[])this.lampSections.get(key);
        if (cached != null) {
            return cached;
        }
        int[] found = PrimeChunkFinderModule.scanLamps(level, sectionX, sectionY, sectionZ);
        this.lampSections.put(key, (Object)found);
        return found;
    }

    private static int[] scanLamps(class_638 level, int sectionX, int sectionY, int sectionZ) {
        if (sectionY < level.method_32891() || sectionY > level.method_31597()) {
            return NO_LAMPS;
        }
        class_2818 chunk = level.method_2935().method_2857(sectionX, sectionZ, class_2806.field_12803, false);
        if (chunk == null) {
            return NO_LAMPS;
        }
        class_2826 section = chunk.method_38259(chunk.method_31603(sectionY));
        if (section.method_38292() || !section.method_12265().method_19526(PrimeChunkFinderModule::isLamp)) {
            return NO_LAMPS;
        }
        IntArrayList lamps = new IntArrayList();
        for (int index = 0; index < 4096; ++index) {
            class_2680 state = section.method_12254(index & 0xF, index >>> 8, index >>> 4 & 0xF);
            if (!PrimeChunkFinderModule.isLamp(state)) continue;
            lamps.add(index << 4 | state.method_26213());
        }
        return lamps.isEmpty() ? NO_LAMPS : lamps.toIntArray();
    }

    private static boolean isLamp(class_2680 state) {
        return state.method_27852(class_2246.field_10336) || state.method_27852(class_2246.field_10099) || state.method_27852(class_2246.field_22092) || state.method_27852(class_2246.field_22093) || state.method_27852(class_2246.field_61902) || state.method_27852(class_2246.field_61903) || class_5803.method_33618((class_2680)state);
    }

    private boolean fromAmethyst(int x, int y, int z, int oldValue, int newValue) {
        if (Math.max(oldValue, newValue) > 5) {
            return false;
        }
        class_638 level = class_310.method_1551().field_1687;
        if (level == null || !this.amethystSectionNear(level, x, y, z)) {
            return false;
        }
        for (int dx = -5; dx <= 5; ++dx) {
            int restX = 5 - Math.abs(dx);
            for (int dy = -restX; dy <= restX; ++dy) {
                int restY = restX - Math.abs(dy);
                for (int dz = -restY; dz <= restY; ++dz) {
                    if (!PrimeChunkFinderModule.isAmethyst(level.method_8320((class_2338)this.cursor.method_10103(x + dx, y + dy, z + dz)))) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private boolean amethystSectionNear(class_638 level, int x, int y, int z) {
        for (int sx = x - 5 >> 4; sx <= x + 5 >> 4; ++sx) {
            for (int sy = y - 5 >> 4; sy <= y + 5 >> 4; ++sy) {
                for (int sz = z - 5 >> 4; sz <= z + 5 >> 4; ++sz) {
                    long key = class_4076.method_18685((int)sx, (int)sy, (int)sz);
                    if (this.checkedSections.add(key) && PrimeChunkFinderModule.sectionHasAmethyst(level, sx, sy, sz)) {
                        this.amethystSections.add(key);
                    }
                    if (!this.amethystSections.contains(key)) continue;
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean sectionHasAmethyst(class_638 level, int sectionX, int sectionY, int sectionZ) {
        if (sectionY < level.method_32891() || sectionY > level.method_31597()) {
            return false;
        }
        class_2818 chunk = level.method_2935().method_2857(sectionX, sectionZ, class_2806.field_12803, false);
        if (chunk == null) {
            return false;
        }
        class_2826 section = chunk.method_38259(chunk.method_31603(sectionY));
        return !section.method_38292() && section.method_12265().method_19526(PrimeChunkFinderModule::isAmethyst);
    }

    private boolean fromRedstoneOre(int x, int y, int z, int value) {
        if (value > 9) {
            return false;
        }
        class_638 level = class_310.method_1551().field_1687;
        if (level == null) {
            return false;
        }
        int reach = 9 - value;
        for (int sx = x - reach >> 4; sx <= x + reach >> 4; ++sx) {
            for (int sy = y - reach >> 4; sy <= y + reach >> 4; ++sy) {
                for (int sz = z - reach >> 4; sz <= z + reach >> 4; ++sz) {
                    for (long ore : this.exposedOres(level, sx, sy, sz)) {
                        int distance = Math.abs(class_2338.method_10061((long)ore) - x) + Math.abs(class_2338.method_10071((long)ore) - y) + Math.abs(class_2338.method_10083((long)ore) - z);
                        if (distance > reach) continue;
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private long[] exposedOres(class_638 level, int sectionX, int sectionY, int sectionZ) {
        long key = class_4076.method_18685((int)sectionX, (int)sectionY, (int)sectionZ);
        long[] cached = (long[])this.oreSections.get(key);
        if (cached != null) {
            return cached;
        }
        long[] found = this.scanExposedOres(level, sectionX, sectionY, sectionZ);
        this.oreSections.put(key, (Object)found);
        return found;
    }

    private long[] scanExposedOres(class_638 level, int sectionX, int sectionY, int sectionZ) {
        if (sectionY < level.method_32891() || sectionY > level.method_31597()) {
            return NO_ORES;
        }
        class_2818 chunk = level.method_2935().method_2857(sectionX, sectionZ, class_2806.field_12803, false);
        if (chunk == null) {
            return NO_ORES;
        }
        class_2826 section = chunk.method_38259(chunk.method_31603(sectionY));
        if (section.method_38292() || !section.method_12265().method_19526(PrimeChunkFinderModule::isRedstoneOre)) {
            return NO_ORES;
        }
        LongArrayList ores = new LongArrayList();
        for (int ly = 0; ly < 16; ++ly) {
            for (int lz = 0; lz < 16; ++lz) {
                for (int lx = 0; lx < 16; ++lx) {
                    int wz;
                    int wy;
                    int wx;
                    if (!PrimeChunkFinderModule.isRedstoneOre(section.method_12254(lx, ly, lz)) || !this.exposed(level, wx = (sectionX << 4) + lx, wy = (sectionY << 4) + ly, wz = (sectionZ << 4) + lz)) continue;
                    ores.add(class_2338.method_10064((int)wx, (int)wy, (int)wz));
                }
            }
        }
        return ores.isEmpty() ? NO_ORES : ores.toLongArray();
    }

    private boolean exposed(class_638 level, int x, int y, int z) {
        for (class_2350 direction : class_2350.values()) {
            class_2680 neighbour = level.method_8320((class_2338)this.cursor.method_10103(x + direction.method_10148(), y + direction.method_10164(), z + direction.method_10165()));
            if (!neighbour.method_26215() && neighbour.method_26225()) continue;
            return true;
        }
        return false;
    }

    private static boolean isRedstoneOre(class_2680 state) {
        return state.method_27852(class_2246.field_10080) || state.method_27852(class_2246.field_29030);
    }

    private static boolean isAmethyst(class_2680 state) {
        return state.method_27852(class_2246.field_27161) || state.method_27852(class_2246.field_27162) || state.method_27852(class_2246.field_27163) || state.method_27852(class_2246.field_27164) || state.method_27852(class_2246.field_27160) || state.method_27852(class_2246.field_27159);
    }

    private Map<Integer, byte[]> createZeroedChunk() {
        HashMap<Integer, byte[]> sections = new HashMap<Integer, byte[]>();
        for (int sectionY = this.bottomSection(); sectionY <= this.topSection(); ++sectionY) {
            sections.put(sectionY, ZERO_SECTION);
        }
        return sections;
    }

    private int lightSectionY(int index) {
        class_638 level = class_310.method_1551().field_1687;
        if (level == null) {
            return Integer.MIN_VALUE;
        }
        return level.method_2935().method_12130().method_31929() + index;
    }

    private int bottomSection() {
        return class_310.method_1551().field_1687.method_32891();
    }

    private int topSection() {
        return class_310.method_1551().field_1687.method_31597();
    }

    private boolean isWorldSection(int sectionY) {
        return sectionY >= this.bottomSection() && sectionY <= this.topSection();
    }

    private void trimChunkCache() {
        while (this.chunkLight.size() > 5000) {
            Iterator<Long> keys = this.chunkLight.keySet().iterator();
            keys.next();
            keys.remove();
        }
    }

    private void reset() {
        this.chunkLight.clear();
        this.passedChunks.clear();
        this.flaggedChunks.clear();
        this.shownChunks.clear();
        this.redstoneNotified.clear();
        this.lampSections.clear();
        this.goneLamps.clear();
    }

    @Environment(value=EnvType.CLIENT)
    public static final class DeepslateToast
    extends ChallengeToast {
        private static final class_2561 TITLE = class_2561.method_43470((String)"Deepslate Level Only");
        private static final class_2561 MESSAGE = class_2561.method_43470((String)"Go below Y 0 first.");

        DeepslateToast() {
            super(new class_1799((class_1935)class_1802.field_28866), TITLE, MESSAGE, true);
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class ChangeSummary {
        int count;
        int x;
        int y;
        int z;
        int oldValue;
        int newValue;

        private ChangeSummary() {
        }

        void add(int x, int y, int z, int oldValue, int newValue) {
            if (this.count == 0 || Math.abs(newValue - oldValue) > Math.abs(this.newValue - this.oldValue)) {
                this.x = x;
                this.y = y;
                this.z = z;
                this.oldValue = oldValue;
                this.newValue = newValue;
            }
            ++this.count;
        }
    }
}

