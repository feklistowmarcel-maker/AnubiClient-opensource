/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_12179
 *  net.minecraft.class_12180
 *  net.minecraft.class_1923
 *  net.minecraft.class_238
 *  net.minecraft.class_310
 *  net.minecraft.class_634
 */
package net.anubis.client.module.impl.misc;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import net.anubis.client.event.EventHandler;
import net.anubis.client.event.impl.Render3DEvent;
import net.anubis.client.module.ClientModule;
import net.anubis.client.module.ModuleCategory;
import net.anubis.client.module.setting.BooleanSetting;
import net.anubis.client.module.setting.ColorSetting;
import net.anubis.client.util.LevelUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12179;
import net.minecraft.class_12180;
import net.minecraft.class_1923;
import net.minecraft.class_238;
import net.minecraft.class_310;
import net.minecraft.class_634;

@Environment(value=EnvType.CLIENT)
public final class RenderUtilModule
extends ClientModule {
    private static final double MARKER_Y = 63.0;
    private static final float LINE_WIDTH = 1.5f;
    private static final int CYAN = 65535;
    private static final int SOURCE_FILL_ALPHA = 40;
    private final BooleanSetting clearOnToggle = this.addSetting("clear-chunks-on-toggle", new BooleanSetting("Clear On Toggle", true));
    private final BooleanSetting highlightSources = this.addSetting("render-flagged-chunk", new BooleanSetting("Highlight Source Chunks", true));
    private final ColorSetting fillColor = this.addSetting("side-color", new ColorSetting("Fill Color", 0x28FF2828));
    private final ColorSetting lineColor = this.addSetting("line-color", new ColorSetting("Line Color", -55256));
    private final Set<class_1923> flaggedChunks = new HashSet<class_1923>();
    private final Set<class_1923> sourceChunks = new HashSet<class_1923>();
    private class_634 session;

    public RenderUtilModule() {
        super("render-util", "Render Util", "Renders the chunks flagged by Quantum.", ModuleCategory.BASE_RAID);
    }

    @Override
    protected void onEnable() {
        if (this.clearOnToggle.get()) {
            this.flaggedChunks.clear();
            this.sourceChunks.clear();
        }
    }

    private void ensureSession() {
        class_634 connection = class_310.method_1551().method_1562();
        if (connection == this.session) {
            return;
        }
        this.session = connection;
        this.flaggedChunks.clear();
        this.sourceChunks.clear();
    }

    public boolean hasFlaggedChunks() {
        this.ensureSession();
        return !this.flaggedChunks.isEmpty();
    }

    public boolean isFlagged(class_1923 pos) {
        this.ensureSession();
        return this.flaggedChunks.contains(pos);
    }

    public void addFlaggedChunks(Collection<class_1923> chunks) {
        this.ensureSession();
        this.flaggedChunks.addAll(chunks);
    }

    public void clearFlaggedChunks() {
        this.ensureSession();
        this.flaggedChunks.clear();
    }

    public void addSourceChunk(class_1923 pos) {
        this.ensureSession();
        this.sourceChunks.add(pos);
    }

    @EventHandler
    public void onRender3D(Render3DEvent event) {
        this.ensureSession();
        class_310 mc = class_310.method_1551();
        if (mc.field_1687 == null || !LevelUtil.isOverworld(mc.field_1687)) {
            return;
        }
        class_12179 flagStyle = class_12179.method_75537((int)this.lineColor.argb(), (float)1.5f, (int)this.fillColor.argb());
        boolean sources = this.highlightSources.get();
        for (class_1923 pos : this.flaggedChunks) {
            if (sources && this.sourceChunks.contains(pos)) continue;
            class_12180.method_75541((class_238)RenderUtilModule.marker(pos), (class_12179)flagStyle).method_75533();
        }
        if (!sources) {
            return;
        }
        class_12179 sourceStyle = class_12179.method_75537((int)-16711681, (float)1.5f, (int)671154175);
        for (class_1923 pos : this.sourceChunks) {
            class_12180.method_75541((class_238)RenderUtilModule.marker(pos), (class_12179)sourceStyle).method_75533();
        }
    }

    private static class_238 marker(class_1923 pos) {
        double x = (double)pos.field_9181 * 16.0;
        double z = (double)pos.field_9180 * 16.0;
        return new class_238(x, 63.0, z, x + 16.0, 63.0, z + 16.0);
    }
}

