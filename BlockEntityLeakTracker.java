/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2246
 *  net.minecraft.class_2248
 *  net.minecraft.class_2338
 *  net.minecraft.class_2591
 *  net.minecraft.class_2596
 *  net.minecraft.class_2622
 *  net.minecraft.class_2672
 *  net.minecraft.class_2678
 *  net.minecraft.class_2724
 *  net.minecraft.class_2960
 *  net.minecraft.class_6603
 *  net.minecraft.class_6880
 *  net.minecraft.class_7923
 */
package net.anubis.client.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2246;
import net.minecraft.class_2248;
import net.minecraft.class_2338;
import net.minecraft.class_2591;
import net.minecraft.class_2596;
import net.minecraft.class_2622;
import net.minecraft.class_2672;
import net.minecraft.class_2678;
import net.minecraft.class_2724;
import net.minecraft.class_2960;
import net.minecraft.class_6603;
import net.minecraft.class_6880;
import net.minecraft.class_7923;

@Environment(value=EnvType.CLIENT)
public final class BlockEntityLeakTracker {
    private static final Map<class_2338, Entry> FOUND = new ConcurrentHashMap<class_2338, Entry>();
    private static final ConcurrentLinkedQueue<class_2338> ADDED = new ConcurrentLinkedQueue();
    private static final AtomicInteger GENERATION = new AtomicInteger();
    private static volatile boolean recordAdditions;

    private BlockEntityLeakTracker() {
    }

    public static Map<class_2338, Entry> view() {
        return FOUND;
    }

    public static boolean contains(class_2338 pos) {
        return FOUND.containsKey(pos);
    }

    public static void clear() {
        FOUND.clear();
        ADDED.clear();
        GENERATION.incrementAndGet();
    }

    public static int generation() {
        return GENERATION.get();
    }

    public static void recordAdditions(boolean on) {
        recordAdditions = on;
        if (!on) {
            ADDED.clear();
        }
    }

    public static class_2338 pollAdded() {
        return ADDED.poll();
    }

    public static void ingest(class_2596<?> packet) {
        if (packet instanceof class_2672) {
            class_2672 chunkPacket = (class_2672)packet;
            class_6603 data = chunkPacket.method_38598();
            try {
                data.method_38587(chunkPacket.method_11523(), chunkPacket.method_11524()).accept((pos, type, tag) -> {
                    if (type != null) {
                        BlockEntityLeakTracker.put(pos, type);
                    }
                });
            }
            catch (Throwable throwable) {}
        } else if (packet instanceof class_2622) {
            class_2622 bePacket = (class_2622)packet;
            BlockEntityLeakTracker.put(bePacket.method_11293(), bePacket.method_11291());
        } else if (packet instanceof class_2678 || packet instanceof class_2724) {
            BlockEntityLeakTracker.clear();
        }
    }

    private static void put(class_2338 pos, class_2591<?> type) {
        class_2338 key = pos.method_10062();
        if (FOUND.putIfAbsent(key, new Entry(BlockEntityLeakTracker.blockFromType(type))) == null && recordAdditions) {
            ADDED.add(key);
        }
    }

    public static class_2248 blockFromType(class_2591<?> type) {
        class_2248 mapped;
        String path;
        class_2960 id = class_7923.field_41181.method_10221(type);
        if (id == null) {
            return class_2246.field_10124;
        }
        switch (path = id.method_12832()) {
            case "mob_spawner": {
                class_2248 class_22482 = class_2246.field_10260;
                break;
            }
            case "piston": {
                class_2248 class_22482 = class_2246.field_10008;
                break;
            }
            case "sign": {
                class_2248 class_22482 = class_2246.field_10121;
                break;
            }
            case "hanging_sign": {
                class_2248 class_22482 = class_2246.field_40262;
                break;
            }
            case "skull": {
                class_2248 class_22482 = class_2246.field_10481;
                break;
            }
            case "banner": {
                class_2248 class_22482 = class_2246.field_10154;
                break;
            }
            case "bed": {
                class_2248 class_22482 = class_2246.field_10069;
                break;
            }
            case "brushable_block": {
                class_2248 class_22482 = class_2246.field_42728;
                break;
            }
            default: {
                class_2248 class_22482 = mapped = null;
            }
        }
        if (mapped != null) {
            return mapped;
        }
        return class_7923.field_41175.method_10223(id).map(class_6880::comp_349).orElse(class_2246.field_10124);
    }

    @Environment(value=EnvType.CLIENT)
    public record Entry(class_2248 block) {
    }
}

