/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents
 *  net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
 *  net.minecraft.class_1923
 *  net.minecraft.class_2338
 *  net.minecraft.class_2338$class_2339
 *  net.minecraft.class_2678
 *  net.minecraft.class_2680
 *  net.minecraft.class_2818
 *  net.minecraft.class_2826
 *  net.minecraft.class_310
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.anubis.client.esp;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.LongPredicate;
import net.anubis.client.event.impl.BlockUpdateEvent;
import net.anubis.client.event.impl.PacketReceiveNettyEvent;
import net.anubis.client.module.impl.client.PerformanceModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.class_1923;
import net.minecraft.class_2338;
import net.minecraft.class_2678;
import net.minecraft.class_2680;
import net.minecraft.class_2818;
import net.minecraft.class_2826;
import net.minecraft.class_310;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(value=EnvType.CLIENT)
public final class ChunkScanner {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Anubis");
    private static final Object OWNER = new Object();
    private static final Consumer[] NONE = new Consumer[0];
    private static final List<Consumer> consumers = new ArrayList<Consumer>();
    private static Consumer[] snapshot = NONE;
    private static final ArrayDeque<Long> queue = new ArrayDeque();
    private static final Set<Long> queued = new HashSet<Long>();
    private static volatile boolean resetPending;

    private ChunkScanner() {
    }

    public static void init() {
        ClientChunkEvents.CHUNK_LOAD.register((level, chunk) -> ChunkScanner.enqueue(chunk.method_12004().method_8324()));
        ClientTickEvents.END_CLIENT_TICK.register(ChunkScanner::tick);
        BlockUpdateEvent.TYPE.listen(OWNER, ChunkScanner::onBlockUpdate);
        PacketReceiveNettyEvent.TYPE.listen(OWNER, event -> {
            if (event.packet instanceof class_2678) {
                resetPending = true;
            }
        });
    }

    public static void register(Consumer consumer) {
        if (consumers.contains(consumer)) {
            return;
        }
        consumers.add(consumer);
        snapshot = consumers.toArray(NONE);
        ChunkScanner.queueLoadedChunks();
    }

    public static void unregister(Consumer consumer) {
        consumers.remove(consumer);
        snapshot = consumers.toArray(NONE);
        if (consumers.isEmpty()) {
            queue.clear();
            queued.clear();
        }
    }

    public static int pending() {
        return queue.size();
    }

    public static void rescan() {
        ChunkScanner.queueLoadedChunks();
    }

    private static void queueLoadedChunks() {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) {
            return;
        }
        int cx = mc.field_1724.method_31477() >> 4;
        int cz = mc.field_1724.method_31479() >> 4;
        int radius = (Integer)mc.field_1690.method_42503().method_41753() + 1;
        ArrayList<long[]> loaded = new ArrayList<long[]>();
        for (int x = cx - radius; x <= cx + radius; ++x) {
            for (int z = cz - radius; z <= cz + radius; ++z) {
                if (!mc.field_1687.method_2935().method_12123(x, z)) continue;
                int dx = x - cx;
                int dz = z - cz;
                loaded.add(new long[]{class_1923.method_8331((int)x, (int)z), dx * dx + dz * dz});
            }
        }
        loaded.sort(Comparator.comparingLong(a -> a[1]));
        for (long[] entry : loaded) {
            ChunkScanner.enqueue(entry[0]);
        }
    }

    private static void enqueue(long chunkKey) {
        if (consumers.isEmpty() || !queued.add(chunkKey)) {
            return;
        }
        queue.add(chunkKey);
    }

    private static void tick(class_310 mc) {
        Long key;
        if (resetPending) {
            resetPending = false;
            queue.clear();
            queued.clear();
            for (Consumer consumer : snapshot) {
                ChunkScanner.call(consumer, Consumer::reset);
            }
            ChunkScanner.queueLoadedChunks();
        }
        if (mc.field_1687 == null || consumers.isEmpty()) {
            return;
        }
        long deadline = System.nanoTime() + PerformanceModule.scanBudgetNanos();
        while ((key = queue.poll()) != null) {
            queued.remove(key);
            class_2818 chunk = mc.field_1687.method_2935().method_21730(class_1923.method_8325((long)key), class_1923.method_8332((long)key));
            if (chunk != null) {
                ChunkScanner.scan(mc, chunk);
            }
            if (System.nanoTime() < deadline) continue;
            break;
        }
    }

    private static void scan(class_310 mc, class_2818 chunk) {
        int minX = chunk.method_12004().method_8326();
        int minZ = chunk.method_12004().method_8328();
        int minSection = mc.field_1687.method_8597().comp_651() >> 4;
        class_2826[] sections = chunk.method_12006();
        ArrayList<Consumer> interested = new ArrayList<Consumer>(consumers.size());
        for (int si = 0; si < sections.length; ++si) {
            class_2826 section = sections[si];
            if (section == null || section.method_38292()) continue;
            interested.clear();
            for (Consumer consumer : consumers) {
                if (!section.method_19523(consumer::wants)) continue;
                interested.add(consumer);
            }
            if (interested.isEmpty()) continue;
            int baseY = minSection + si << 4;
            for (int x = 0; x < 16; ++x) {
                for (int z = 0; z < 16; ++z) {
                    for (int y = 0; y < 16; ++y) {
                        class_2680 state = section.method_12254(x, y, z);
                        if (state.method_26215()) continue;
                        long pos = class_2338.method_10064((int)(minX + x), (int)(baseY + y), (int)(minZ + z));
                        for (Consumer consumer : interested) {
                            if (!consumer.wants(state)) continue;
                            ChunkScanner.found(consumer, pos, state);
                        }
                    }
                }
            }
        }
        long chunkKey = chunk.method_12004().method_8324();
        class_2338.class_2339 probe = new class_2338.class_2339();
        for (Consumer consumer : snapshot) {
            ChunkScanner.call(consumer, c -> c.chunkScanned(chunkKey, pos -> {
                class_2680 state = chunk.method_8320((class_2338)probe.method_16363(pos));
                return !state.method_26215() && c.wants(state);
            }));
        }
    }

    private static void onBlockUpdate(BlockUpdateEvent event) {
        Consumer[] current = snapshot;
        if (current.length == 0) {
            return;
        }
        long pos = event.pos.method_10063();
        class_2680 state = event.newState;
        for (Consumer consumer : current) {
            if (!state.method_26215() && consumer.wants(state)) {
                ChunkScanner.found(consumer, pos, state);
                continue;
            }
            ChunkScanner.removed(consumer, pos);
        }
    }

    private static void found(Consumer consumer, long pos, class_2680 state) {
        try {
            consumer.found(pos, state);
        }
        catch (RuntimeException e) {
            ChunkScanner.drop(consumer, e);
        }
    }

    private static void removed(Consumer consumer, long pos) {
        try {
            consumer.removed(pos);
        }
        catch (RuntimeException e) {
            ChunkScanner.drop(consumer, e);
        }
    }

    private static void call(Consumer consumer, java.util.function.Consumer<Consumer> action) {
        try {
            action.accept(consumer);
        }
        catch (RuntimeException e) {
            ChunkScanner.drop(consumer, e);
        }
    }

    private static void drop(Consumer consumer, RuntimeException e) {
        LOGGER.error("[Anubis] Chunk scan consumer {} failed; disabling it", (Object)consumer.getClass().getSimpleName(), (Object)e);
        consumers.remove(consumer);
        snapshot = consumers.toArray(NONE);
    }

    @Environment(value=EnvType.CLIENT)
    public static interface Consumer {
        public boolean wants(class_2680 var1);

        public void found(long var1, class_2680 var3);

        public void removed(long var1);

        public void reset();

        default public void chunkScanned(long chunkKey, LongPredicate stillThere) {
        }
    }
}

