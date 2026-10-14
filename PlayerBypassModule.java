/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap
 *  it.unimi.dsi.fastutil.longs.Long2ObjectMap$Entry
 *  it.unimi.dsi.fastutil.longs.LongCollection
 *  it.unimi.dsi.fastutil.longs.LongIterator
 *  it.unimi.dsi.fastutil.longs.LongOpenHashSet
 *  it.unimi.dsi.fastutil.longs.LongSet
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_12179
 *  net.minecraft.class_12180
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1923
 *  net.minecraft.class_1935
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_2561
 *  net.minecraft.class_310
 *  net.minecraft.class_634
 *  net.minecraft.class_9848
 */
package net.anubis.client.module.impl.misc;

import it.unimi.dsi.fastutil.longs.Long2FloatOpenHashMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.anubis.client.chat.ModuleChat;
import net.anubis.client.event.EventHandler;
import net.anubis.client.event.impl.Render3DEvent;
import net.anubis.client.gui.ChallengeToast;
import net.anubis.client.module.ClientModule;
import net.anubis.client.module.ModuleCategory;
import net.anubis.client.module.setting.BooleanSetting;
import net.anubis.client.module.setting.NumberSetting;
import net.anubis.client.util.ChunkAreas;
import net.anubis.client.util.FoglessLines;
import net.anubis.client.util.playerbypass.ChunkLightSnapshot;
import net.anubis.client.util.playerbypass.PlayerBypassLightTracker;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12179;
import net.minecraft.class_12180;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1923;
import net.minecraft.class_1935;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_634;
import net.minecraft.class_9848;

@Environment(value=EnvType.CLIENT)
public final class PlayerBypassModule
extends ClientModule {
    private static final int RENDER_DISTANCE_CHUNKS = 16;
    private static final int ZONE_RADIUS = 5;
    private static final double MARKER_Y = 64.0;
    private static final float LINE_WIDTH = 1.5f;
    private static final float CENTER_FILL = 0.45f;
    private static final float RIM_FILL = 0.25f;
    private static final float EDGE_SHADE = 0.8f;
    private static final int BASE_COLOR = -2516480;
    private static final int SUSPECT_COLOR = -5766;
    private final NumberSetting opacity = this.addSetting("opacity", new NumberSetting("Opacity", 140.0f, 10.0f, 255.0f).step(1.0f));
    private final BooleanSetting tracers = this.addSetting("tracers", new BooleanSetting("Tracers", false));
    private final ChunkAreas chunkAreas = new ChunkAreas();
    private final ChunkAreas suspectAreas = new ChunkAreas();
    private final Long2FloatOpenHashMap baseWeight = new Long2FloatOpenHashMap();
    private final Long2FloatOpenHashMap suspectWeight = new Long2FloatOpenHashMap();
    private final LongOpenHashSet zone = new LongOpenHashSet();
    private final BooleanSetting sound = this.addSetting("sound", new BooleanSetting("Sound", true));
    private final BooleanSetting showCoordinates = this.addSetting("show-coordinates", new BooleanSetting("Show Coordinates", true));
    private final BooleanSetting disconnect = this.addSetting("disconnect-on-spotted", new BooleanSetting("Auto Disconnect", false));

    public PlayerBypassModule() {
        super("player-bypass", "Player Bypass", "Flags chunks with signs of underground player bases from light data.", ModuleCategory.BASE_RAID);
        BooleanSetting notifications = this.usesNotifications();
        this.sound.visibleWhen(notifications::get);
        this.showCoordinates.visibleWhen(() -> notifications.get() || this.disconnect.get());
    }

    @Override
    protected void onEnable() {
        PlayerBypassLightTracker.setSpottedListener(this::onChunkSpotted);
    }

    @Override
    protected void onDisable() {
        PlayerBypassLightTracker.setSpottedListener(null);
    }

    private void onChunkSpotted(int chunkX, int chunkZ) {
        class_634 connection;
        class_310 mc = class_310.method_1551();
        int x = (chunkX << 4) + 8;
        int z = (chunkZ << 4) + 8;
        boolean showPos = this.showCoordinates.get();
        if (this.notificationsOn()) {
            ChallengeToast.addOrUpdate(new class_1799((class_1935)class_1802.field_8575), "Base Detected", (class_2561)(showPos ? class_2561.method_43470((String)(x + ", " + z)) : class_2561.method_43473()), this.sound.get());
        }
        if (this.disconnect.get() && (connection = mc.method_1562()) != null) {
            ModuleChat.send((ClientModule)this, ModuleChat.body().text("Auto Disconnect ").warn("triggered"));
            connection.method_48296().method_10747((class_2561)class_2561.method_43470((String)("[Player Bypass] Base detected" + (String)(showPos ? " at " + x + ", " + z : ""))));
        }
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || mc.field_1724 == null) {
            return;
        }
        Long2ObjectMap<ChunkLightSnapshot> snapshots = PlayerBypassLightTracker.snapshots();
        if (snapshots.isEmpty()) {
            return;
        }
        int centerX = mc.field_1724.method_24515().method_10263() >> 4;
        int centerZ = mc.field_1724.method_24515().method_10260() >> 4;
        LongOpenHashSet bases = new LongOpenHashSet();
        LongOpenHashSet suspects = new LongOpenHashSet();
        for (Long2ObjectMap.Entry entry : snapshots.long2ObjectEntrySet()) {
            long key = entry.getLongKey();
            if (Math.abs(class_1923.method_8325((long)key) - centerX) > 16 || Math.abs(class_1923.method_8332((long)key) - centerZ) > 16 || !PlayerBypassLightTracker.isFlagged(key, (ChunkLightSnapshot)entry.getValue())) continue;
            (PlayerBypassLightTracker.isSuspect(key) ? suspects : bases).add(key);
        }
        if (bases.isEmpty() && suspects.isEmpty()) {
            return;
        }
        int base = -2516480;
        int suspect = -5766;
        PlayerBypassModule.spread(bases, this.baseWeight);
        PlayerBypassModule.spread(suspects, this.suspectWeight);
        FoglessLines lines = FoglessLines.forFrame(event.partialTick);
        this.drawZones(lines, base, suspect);
        this.drawSourceEdges(lines, bases, suspects, base, suspect);
        if (this.tracers.get()) {
            this.chunkAreas.drawTracers((LongSet)bases, 64.0, class_9848.method_61334((int)base));
            this.suspectAreas.drawTracers((LongSet)suspects, 64.0, class_9848.method_61334((int)suspect));
        }
    }

    private static void spread(LongOpenHashSet sources, Long2FloatOpenHashMap into) {
        into.clear();
        LongIterator longIterator = sources.iterator();
        while (longIterator.hasNext()) {
            long key = (Long)longIterator.next();
            int cx = class_1923.method_8325((long)key);
            int cz = class_1923.method_8332((long)key);
            for (int dx = -5; dx <= 5; ++dx) {
                for (int dz = -5; dz <= 5; ++dz) {
                    long area;
                    float weight = PlayerBypassModule.falloff(Math.max(Math.abs(dx), Math.abs(dz)));
                    if (!(weight > into.get(area = class_1923.method_8331((int)(cx + dx), (int)(cz + dz))))) continue;
                    into.put(area, weight);
                }
            }
        }
    }

    private static float falloff(int ring) {
        float t = (float)ring / 6.0f;
        return 1.0f - t * t * (3.0f - 2.0f * t);
    }

    private void drawZones(FoglessLines lines, int base, int suspect) {
        this.zone.clear();
        this.zone.addAll((LongCollection)this.baseWeight.keySet());
        this.zone.addAll((LongCollection)this.suspectWeight.keySet());
        float maxAlpha = Math.max(10, Math.min(255, this.opacity.getInt()));
        LongIterator longIterator = this.zone.iterator();
        while (longIterator.hasNext()) {
            long key = (Long)longIterator.next();
            float b = this.baseWeight.get(key);
            float s = this.suspectWeight.get(key);
            float mix = b * b / (b * b + s * s);
            int color = class_9848.method_61319((float)mix, (int)suspect, (int)base);
            float strength = 0.25f + 0.75f * Math.max(b, s);
            int fill = class_9848.method_61330((int)Math.round(maxAlpha * 0.45f * strength * class_9848.method_65100((int)color)), (int)color);
            int cx = class_1923.method_8325((long)key);
            int cz = class_1923.method_8332((long)key);
            double x0 = cx << 4;
            double z0 = cz << 4;
            double x1 = x0 + 16.0;
            double z1 = z0 + 16.0;
            class_12180.method_75541((class_238)new class_238(x0, 64.0, z0, x1, 64.0, z1), (class_12179)class_12179.method_75539((int)fill)).method_75533();
            int edge = class_9848.method_61330((int)Math.round(maxAlpha * class_9848.method_65100((int)color)), (int)class_9848.method_61321((int)color, (float)0.8f));
            if (!this.zone.contains(class_1923.method_8331((int)cx, (int)(cz - 1)))) {
                PlayerBypassModule.edge(lines, x0, z0, x1, z0, edge);
            }
            if (!this.zone.contains(class_1923.method_8331((int)cx, (int)(cz + 1)))) {
                PlayerBypassModule.edge(lines, x0, z1, x1, z1, edge);
            }
            if (!this.zone.contains(class_1923.method_8331((int)(cx - 1), (int)cz))) {
                PlayerBypassModule.edge(lines, x0, z0, x0, z1, edge);
            }
            if (this.zone.contains(class_1923.method_8331((int)(cx + 1), (int)cz))) continue;
            PlayerBypassModule.edge(lines, x1, z0, x1, z1, edge);
        }
    }

    private void drawSourceEdges(FoglessLines lines, LongOpenHashSet bases, LongOpenHashSet suspects, int base, int suspect) {
        int alpha = Math.max(20, Math.min(255, this.opacity.getInt()));
        PlayerBypassModule.drawSourceEdges(lines, bases, bases, suspects, class_9848.method_61330((int)Math.round((float)alpha * class_9848.method_65100((int)base)), (int)base));
        PlayerBypassModule.drawSourceEdges(lines, suspects, bases, suspects, class_9848.method_61330((int)Math.round((float)alpha * class_9848.method_65100((int)suspect)), (int)suspect));
    }

    private static void drawSourceEdges(FoglessLines lines, LongOpenHashSet chunks, LongOpenHashSet bases, LongOpenHashSet suspects, int color) {
        LongIterator longIterator = chunks.iterator();
        while (longIterator.hasNext()) {
            long key = (Long)longIterator.next();
            int cx = class_1923.method_8325((long)key);
            int cz = class_1923.method_8332((long)key);
            double x0 = cx << 4;
            double z0 = cz << 4;
            double x1 = x0 + 16.0;
            double z1 = z0 + 16.0;
            if (!PlayerBypassModule.isSource(bases, suspects, cx, cz - 1)) {
                PlayerBypassModule.edge(lines, x0, z0, x1, z0, color);
            }
            if (!PlayerBypassModule.isSource(bases, suspects, cx, cz + 1)) {
                PlayerBypassModule.edge(lines, x0, z1, x1, z1, color);
            }
            if (!PlayerBypassModule.isSource(bases, suspects, cx - 1, cz)) {
                PlayerBypassModule.edge(lines, x0, z0, x0, z1, color);
            }
            if (PlayerBypassModule.isSource(bases, suspects, cx + 1, cz)) continue;
            PlayerBypassModule.edge(lines, x1, z0, x1, z1, color);
        }
    }

    private static boolean isSource(LongOpenHashSet bases, LongOpenHashSet suspects, int cx, int cz) {
        long key = class_1923.method_8331((int)cx, (int)cz);
        return bases.contains(key) || suspects.contains(key);
    }

    private static void edge(FoglessLines lines, double ax, double az, double bx, double bz, int color) {
        lines.line(new class_243(ax, 64.0, az), new class_243(bx, 64.0, bz), color, 1.5f);
    }
}

