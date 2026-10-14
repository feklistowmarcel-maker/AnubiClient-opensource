/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.buffers.GpuBuffer
 *  com.mojang.blaze3d.buffers.GpuBuffer$MappedView
 *  com.mojang.blaze3d.buffers.GpuBufferSlice
 *  com.mojang.blaze3d.buffers.GpuFence
 *  com.mojang.blaze3d.opengl.GlStateManager
 *  com.mojang.blaze3d.systems.CommandEncoder
 *  com.mojang.blaze3d.systems.GpuDevice
 *  com.mojang.blaze3d.systems.RenderSystem
 *  com.mojang.blaze3d.textures.GpuTexture
 *  com.mojang.blaze3d.textures.GpuTextureView
 *  com.mojang.blaze3d.textures.TextureFormat
 *  imgui.ImDrawList
 *  it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ObjectIterator
 *  it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap
 *  it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1011$class_1012
 *  net.minecraft.class_10366
 *  net.minecraft.class_10444
 *  net.minecraft.class_10725
 *  net.minecraft.class_10868
 *  net.minecraft.class_11278
 *  net.minecraft.class_11515
 *  net.minecraft.class_11659
 *  net.minecraft.class_11661
 *  net.minecraft.class_11684
 *  net.minecraft.class_1747
 *  net.minecraft.class_1792
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_2680
 *  net.minecraft.class_308$class_11274
 *  net.minecraft.class_310
 *  net.minecraft.class_4587
 *  net.minecraft.class_4597
 *  net.minecraft.class_4597$class_4598
 *  net.minecraft.class_4608
 *  net.minecraft.class_4696
 *  net.minecraft.class_758$class_4596
 *  net.minecraft.class_9799
 *  org.joml.Matrix4fStack
 *  org.lwjgl.BufferUtils
 *  org.lwjgl.opengl.GL11
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.anubis.client.gui;

import com.example.client.mixin.AccessorGameRenderer;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.buffers.GpuFence;
import com.mojang.blaze3d.opengl.GlStateManager;
import com.mojang.blaze3d.systems.CommandEncoder;
import com.mojang.blaze3d.systems.GpuDevice;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTexture;
import com.mojang.blaze3d.textures.GpuTextureView;
import com.mojang.blaze3d.textures.TextureFormat;
import imgui.ImDrawList;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.Reference2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ReferenceOpenHashSet;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import net.anubis.client.gui.HudChrome;
import net.anubis.client.gui.ItemLook;
import net.anubis.client.imgui.ImGuiManager;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1011;
import net.minecraft.class_10366;
import net.minecraft.class_10444;
import net.minecraft.class_10725;
import net.minecraft.class_10868;
import net.minecraft.class_11278;
import net.minecraft.class_11515;
import net.minecraft.class_11659;
import net.minecraft.class_11661;
import net.minecraft.class_11684;
import net.minecraft.class_1747;
import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_2680;
import net.minecraft.class_308;
import net.minecraft.class_310;
import net.minecraft.class_4587;
import net.minecraft.class_4597;
import net.minecraft.class_4608;
import net.minecraft.class_4696;
import net.minecraft.class_758;
import net.minecraft.class_9799;
import org.joml.Matrix4fStack;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(value=EnvType.CLIENT)
public final class ItemIcons {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Anubis");
    private static final int SHEET_SIDE = 512;
    private static final int MIN_PX = 8;
    private static final int MAX_PX = 96;
    private static final int MAX_SHEETS = 8;
    private static final int BUSY_FRAMES = 2;
    private static final int IDLE_FRAMES = 600;
    private static final int PAINTS_PER_FRAME = 6;
    private static final Cell BLANK = new Cell();
    private static final Int2ObjectOpenHashMap<Sheet> SHEETS = new Int2ObjectOpenHashMap();
    private static final List<Sheet> RETIRED = new ArrayList<Sheet>();
    private static final Reference2ObjectOpenHashMap<class_1792, class_1799> PLAIN = new Reference2ObjectOpenHashMap();
    private static final ReferenceOpenHashSet<class_1792> BROKEN = new ReferenceOpenHashSet();
    private static final ItemLook MODEL = new ItemLook();
    private static final int[] VIEWPORT = new int[4];
    private static GpuTexture depth;
    private static GpuTextureView depthView;
    private static class_11278 projection;
    private static class_11661 submits;
    private static class_11684 features;
    private static class_4597.class_4598 buffers;
    private static GpuBuffer readback;
    private static ByteBuffer straightened;
    private static boolean straightenFailed;
    private static GpuTexture itemAtlas;
    private static int frame;
    private static int retiredFrame;
    private static int paintsLeft;
    private static boolean disabled;

    private ItemIcons() {
    }

    static float pixels(float size) {
        float snapped = size < 16.0f ? (float)Math.round(size) : 16.0f * (float)Math.floor(size / 16.0f + 0.25f);
        return Math.max(8.0f, Math.min(96.0f, snapped));
    }

    static void draw(ImDrawList dl, class_1792 item, float x, float y, float size, float alpha) {
        if (item == null || item == class_1802.field_8162) {
            return;
        }
        class_1799 plain = (class_1799)PLAIN.get((Object)item);
        if (plain == null) {
            plain = item.method_7854();
            PLAIN.put((Object)item, (Object)plain);
        }
        ItemIcons.draw(dl, plain, x, y, size, alpha);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void draw(ImDrawList dl, class_1799 stack, float x, float y, float size, float alpha) {
        Cell cell;
        if (stack == null || stack.method_7960() || alpha <= 0.004f) {
            return;
        }
        ItemIcons.startFrame();
        int px = (int)ItemIcons.pixels(size);
        float x0 = Math.round(x);
        float y0 = Math.round(y);
        try {
            cell = ItemIcons.cell(stack, px);
        }
        finally {
            MODEL.method_65605();
        }
        if (cell == null) {
            return;
        }
        if (cell.texture == 0) {
            float r = Math.max(1.0f, (float)px * 0.18f);
            dl.addRectFilled(x0 + 1.0f, y0 + 1.0f, x0 + (float)px - 1.0f, y0 + (float)px - 1.0f, HudChrome.scaleAlpha(0x1AFFFFFF, alpha), r);
            return;
        }
        dl.addImage((long)cell.texture, x0, y0, x0 + (float)px, y0 + (float)px, cell.u0, cell.v0, cell.u1, cell.v1, HudChrome.scaleAlpha(-1, alpha));
    }

    private static void startFrame() {
        GpuTexture atlas;
        int now = ImGuiManager.getInstance().frameCount();
        if (now == frame) {
            return;
        }
        frame = now;
        paintsLeft = 6;
        if (!RETIRED.isEmpty() && now != retiredFrame) {
            for (Sheet sheet : RETIRED) {
                sheet.release();
            }
            RETIRED.clear();
        }
        if ((atlas = ItemIcons.currentAtlas()) != itemAtlas) {
            itemAtlas = atlas;
            for (Sheet sheet : SHEETS.values()) {
                sheet.release();
            }
            SHEETS.clear();
            BROKEN.clear();
            return;
        }
        ObjectIterator objectIterator = SHEETS.values().iterator();
        while (objectIterator.hasNext()) {
            Sheet sheet = (Sheet)objectIterator.next();
            if (now - sheet.lastUsed <= 600) continue;
            sheet.release();
            objectIterator.remove();
        }
    }

    private static GpuTexture currentAtlas() {
        try {
            return class_310.method_1551().method_72703().method_73025(class_10725.field_64477).method_68004();
        }
        catch (RuntimeException notReady) {
            return null;
        }
    }

    private static Cell cell(class_1799 stack, int px) {
        Sheet sheet = ItemIcons.sheet(px);
        if (sheet == null) {
            return null;
        }
        sheet.lastUsed = frame;
        if (!ItemIcons.resolve(stack)) {
            return BLANK;
        }
        Cell cell = (Cell)sheet.cells.get(ItemIcons.MODEL.identity);
        if (cell != null) {
            return cell;
        }
        if (disabled) {
            return BLANK;
        }
        if (paintsLeft <= 0) {
            return null;
        }
        --paintsLeft;
        if (sheet.full()) {
            ItemIcons.retire(sheet);
            sheet = new Sheet(px);
            sheet.lastUsed = frame;
            SHEETS.put(px, (Object)sheet);
        }
        cell = ItemIcons.paint(sheet, ItemIcons.seeThrough(stack.method_7909()));
        sheet.cells.put(Arrays.asList(ItemIcons.MODEL.identity.toArray()), (Object)cell);
        return cell;
    }

    private static Sheet sheet(int px) {
        Sheet sheet = (Sheet)SHEETS.get(px);
        if (sheet != null) {
            return sheet;
        }
        if (SHEETS.size() >= 8) {
            Sheet oldest = ItemIcons.stalest();
            if (oldest == null || frame - oldest.lastUsed < 2) {
                return null;
            }
            ItemIcons.retire(oldest);
        }
        sheet = new Sheet(px);
        SHEETS.put(px, (Object)sheet);
        return sheet;
    }

    private static Sheet stalest() {
        Sheet oldest = null;
        for (Sheet sheet : SHEETS.values()) {
            if (oldest != null && sheet.lastUsed >= oldest.lastUsed) continue;
            oldest = sheet;
        }
        return oldest;
    }

    private static void retire(Sheet sheet) {
        SHEETS.remove(sheet.px);
        RETIRED.add(sheet);
        retiredFrame = frame;
    }

    private static boolean resolve(class_1799 stack) {
        if (BROKEN.contains((Object)stack.method_7909())) {
            return false;
        }
        if (MODEL.resolve(stack)) {
            return true;
        }
        BROKEN.add((Object)stack.method_7909());
        return false;
    }

    private static boolean seeThrough(class_1792 item) {
        if (!(item instanceof class_1747)) {
            return false;
        }
        class_1747 block = (class_1747)item;
        return class_4696.method_23679((class_2680)block.method_7711().method_9564()) == class_11515.field_60926;
    }

    private static Cell paint(Sheet sheet, boolean seeThrough) {
        if (MODEL.method_65606()) {
            return BLANK;
        }
        try {
            if (sheet.color == null) {
                ItemIcons.allocate(sheet);
            }
            int px = sheet.px;
            int perRow = 512 / px;
            int index = sheet.filled++;
            int left = index % perRow * px;
            int top = index / perRow * px;
            int glTop = 512 - top - px;
            ItemIcons.render(MODEL, sheet.color, sheet.colorView, depth, depthView, left, top, px);
            if (seeThrough) {
                ItemIcons.straighten(sheet.color, left, glTop, px);
            }
            Cell cell = new Cell();
            cell.texture = ((class_10868)sheet.color).method_68427();
            cell.u0 = (float)left / 512.0f;
            cell.u1 = (float)(left + px) / 512.0f;
            cell.v0 = (float)(512 - top) / 512.0f;
            cell.v1 = (float)glTop / 512.0f;
            return cell;
        }
        catch (RuntimeException failure) {
            disabled = true;
            LOGGER.warn("HUD item icons could not be drawn; showing plain slots instead", (Throwable)failure);
            return BLANK;
        }
    }

    private static void allocate(Sheet sheet) {
        GpuDevice device = RenderSystem.getDevice();
        if (depth == null) {
            depth = device.createTexture("Anubis HUD icon depth", 8, TextureFormat.DEPTH32, 512, 512, 1, 1);
            depthView = device.createTextureView(depth);
        }
        sheet.color = device.createTexture("Anubis HUD icons " + sheet.px, 15, TextureFormat.RGBA8, 512, 512, 1, 1);
        sheet.colorView = device.createTextureView(sheet.color);
        device.createCommandEncoder().clearColorAndDepthTextures(sheet.color, 0, depth, 1.0);
        GpuTexture gpuTexture = sheet.color;
        if (gpuTexture instanceof class_10868) {
            class_10868 gl = (class_10868)gpuTexture;
            ItemIcons.nearest(gl);
        }
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static void render(class_10444 model, GpuTexture target, GpuTextureView targetView, GpuTexture targetDepth, GpuTextureView targetDepthView, int x, int y, int cell) {
        class_310 mc = class_310.method_1551();
        int side = target.getWidth(0);
        int glY = side - y - cell;
        RenderSystem.getDevice().createCommandEncoder().clearColorAndDepthTextures(target, 0, targetDepth, 1.0, x, glY, cell, cell);
        if (features == null) {
            buffers = class_4597.method_22991((class_9799)new class_9799(262144));
            submits = new class_11661();
            features = new class_11684(submits, mc.method_1541(), buffers, mc.method_72703(), mc.method_22940().method_23003(), mc.method_22940().method_23001(), mc.field_1772);
        }
        GpuTextureView colorBefore = RenderSystem.outputColorTextureOverride;
        GpuTextureView depthBefore = RenderSystem.outputDepthTextureOverride;
        GpuBufferSlice lightsBefore = RenderSystem.getShaderLights();
        GpuBufferSlice fogBefore = RenderSystem.getShaderFog();
        GpuBufferSlice projectionBefore = RenderSystem.getProjectionMatrixBuffer();
        class_10366 projectionTypeBefore = RenderSystem.getProjectionType();
        GL11.glGetIntegerv((int)2978, (int[])VIEWPORT);
        GlStateManager._viewport((int)VIEWPORT[0], (int)VIEWPORT[1], (int)VIEWPORT[2], (int)VIEWPORT[3]);
        Matrix4fStack modelView = RenderSystem.getModelViewStack();
        modelView.pushMatrix();
        modelView.identity();
        try {
            RenderSystem.outputColorTextureOverride = targetView;
            RenderSystem.outputDepthTextureOverride = targetDepthView;
            if (projection == null) {
                projection = new class_11278("Anubis icons", -1000.0f, 1000.0f, true);
            }
            RenderSystem.setProjectionMatrix((GpuBufferSlice)projection.method_71092((float)side, (float)side), (class_10366)class_10366.field_54954);
            class_308.class_11274 light = model.method_65608() ? class_308.class_11274.field_60027 : class_308.class_11274.field_60026;
            mc.field_1773.method_71114().method_71034(light);
            RenderSystem.setShaderFog((GpuBufferSlice)((AccessorGameRenderer)mc.field_1773).anubis$getFogRenderer().method_71109(class_758.class_4596.field_60101));
            RenderSystem.enableScissorForRenderTypeDraws((int)x, (int)glY, (int)cell, (int)cell);
            class_4587 pose = new class_4587();
            pose.method_46416((float)x + (float)cell * 0.5f, (float)y + (float)cell * 0.5f, 0.0f);
            pose.method_22905((float)cell, (float)(-cell), (float)cell);
            try {
                model.method_65604(pose, (class_11659)submits, 0xF000F0, class_4608.field_21444, 0);
                features.method_73002();
            }
            finally {
                submits.method_72953();
                buffers.method_22993();
            }
        }
        finally {
            RenderSystem.disableScissorForRenderTypeDraws();
            RenderSystem.outputColorTextureOverride = colorBefore;
            RenderSystem.outputDepthTextureOverride = depthBefore;
            RenderSystem.setShaderLights((GpuBufferSlice)lightsBefore);
            RenderSystem.setShaderFog((GpuBufferSlice)fogBefore);
            RenderSystem.setProjectionMatrix((GpuBufferSlice)projectionBefore, (class_10366)projectionTypeBefore);
            modelView.popMatrix();
            GlStateManager._viewport((int)VIEWPORT[0], (int)VIEWPORT[1], (int)VIEWPORT[2], (int)VIEWPORT[3]);
        }
    }

    static void straighten(GpuTexture page, int x, int y, int cell) {
        if (straightenFailed) {
            return;
        }
        int bytes = cell * cell * 4;
        long whole = (long)page.getWidth(0) * (long)page.getHeight(0) * 4L;
        try {
            GpuDevice device = RenderSystem.getDevice();
            if (readback == null || readback.size() < whole) {
                if (readback != null) {
                    readback.close();
                }
                readback = device.createBuffer(() -> "Anubis icon readback", 9, whole);
            }
            if (straightened == null || straightened.capacity() < bytes) {
                straightened = BufferUtils.createByteBuffer((int)bytes);
            }
            CommandEncoder encoder = device.createCommandEncoder();
            encoder.copyTextureToBuffer(page, readback, 0L, () -> {}, 0, x, y, cell, cell);
            try (GpuFence copied = encoder.createFence();){
                GL11.glFlush();
                if (!copied.awaitCompletion(1000000000L)) {
                    throw new IllegalStateException("icon read-back timed out");
                }
            }
            straightened.clear().limit(bytes);
            try (GpuBuffer.MappedView view = encoder.mapBuffer(readback.slice(0L, (long)bytes), true, false);){
                ByteBuffer read = view.data();
                for (int i = 0; i < bytes; i += 4) {
                    int a = read.get(i + 3) & 0xFF;
                    for (int ch = 0; ch < 3; ++ch) {
                        int v = a == 0 ? 0 : Math.min(255, ((read.get(i + ch) & 0xFF) * 255 + a / 2) / a);
                        straightened.put(i + ch, (byte)v);
                    }
                    straightened.put(i + 3, (byte)a);
                }
            }
            encoder.writeToTexture(page, straightened, class_1011.class_1012.field_4997, 0, 0, x, y, cell, cell);
        }
        catch (RuntimeException e) {
            straightenFailed = true;
            LOGGER.warn("Couldn't read icons back; see-through ones stay darker", (Throwable)e);
        }
    }

    static void nearest(class_10868 gl) {
        int bound = GL11.glGetInteger((int)32873);
        GL11.glBindTexture((int)3553, (int)gl.method_68427());
        GL11.glTexParameteri((int)3553, (int)10241, (int)9728);
        GL11.glTexParameteri((int)3553, (int)10240, (int)9728);
        GL11.glBindTexture((int)3553, (int)bound);
    }

    static {
        frame = Integer.MIN_VALUE;
    }

    @Environment(value=EnvType.CLIENT)
    private static final class Cell {
        int texture;
        float u0;
        float v0;
        float u1;
        float v1;

        private Cell() {
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static final class Sheet {
        final int px;
        final Object2ObjectOpenHashMap<Object, Cell> cells = new Object2ObjectOpenHashMap();
        GpuTexture color;
        GpuTextureView colorView;
        int filled;
        int lastUsed;

        Sheet(int px) {
            this.px = px;
        }

        boolean full() {
            int perRow = 512 / this.px;
            return this.filled >= perRow * perRow;
        }

        void release() {
            if (this.colorView != null) {
                this.colorView.close();
            }
            if (this.color != null) {
                this.color.close();
            }
            this.colorView = null;
            this.color = null;
            this.cells.clear();
        }
    }
}

