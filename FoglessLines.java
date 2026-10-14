/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_12179
 *  net.minecraft.class_12180
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 *  net.minecraft.class_4184
 *  org.joml.Vector3fc
 */
package net.anubis.client.util;

import com.example.client.mixin.AccessorGameRenderer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_12179;
import net.minecraft.class_12180;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_310;
import net.minecraft.class_4184;
import org.joml.Vector3fc;

@Environment(value=EnvType.CLIENT)
public final class FoglessLines {
    private static final double NEAR = 0.05;
    private final class_243 eye;
    private final Vector3fc forward;
    private final double pixelPerDepth;

    private FoglessLines(class_243 eye, Vector3fc forward, double pixelPerDepth) {
        this.eye = eye;
        this.forward = forward;
        this.pixelPerDepth = pixelPerDepth;
    }

    public static FoglessLines forFrame(float partialTick) {
        class_4184 camera = class_310.method_1551().field_1773.method_19418();
        return new FoglessLines(camera.method_71156(), camera.method_19335(), FoglessLines.pixelScale(partialTick));
    }

    public static double pixelScale(float partialTick) {
        class_310 mc = class_310.method_1551();
        class_4184 camera = mc.field_1773.method_19418();
        float fov = ((AccessorGameRenderer)mc.field_1773).anubis$getFov(camera, partialTick, true);
        return 2.0 * Math.tan(Math.toRadians(fov) * 0.5) / (double)Math.max(1, mc.method_22683().method_4506());
    }

    public static FoglessLines forView(class_243 eye, Vector3fc forward, double pixelPerDepth) {
        return new FoglessLines(eye, forward, pixelPerDepth);
    }

    public void line(class_243 from, class_243 to, int argb, float width) {
        double depthFrom = this.depth(from);
        double depthTo = this.depth(to);
        if (depthFrom < 0.05 && depthTo < 0.05) {
            return;
        }
        if (depthFrom < 0.05) {
            from = from.method_35590(to, (0.05 - depthFrom) / (depthTo - depthFrom));
            depthFrom = 0.05;
        } else if (depthTo < 0.05) {
            to = to.method_35590(from, (0.05 - depthTo) / (depthFrom - depthTo));
            depthTo = 0.05;
        }
        class_243 side = from.method_1020(this.eye).method_1036(to.method_1020(from));
        double length = side.method_1033();
        if (length < 1.0E-9) {
            return;
        }
        double scale = (double)width * 0.5 * this.pixelPerDepth / length;
        class_243 atFrom = side.method_1021(scale * depthFrom);
        class_243 atTo = side.method_1021(scale * depthTo);
        class_12180.method_75547((class_243)from.method_1019(atFrom), (class_243)to.method_1019(atTo), (class_243)to.method_1020(atTo), (class_243)from.method_1020(atFrom), (class_12179)class_12179.method_75539((int)argb)).method_75533();
    }

    public void box(class_238 box, int argb, float width) {
        double x0 = box.field_1323;
        double y0 = box.field_1322;
        double z0 = box.field_1321;
        double x1 = box.field_1320;
        double y1 = box.field_1325;
        double z1 = box.field_1324;
        for (double y : new double[]{y0, y1}) {
            this.line(new class_243(x0, y, z0), new class_243(x1, y, z0), argb, width);
            this.line(new class_243(x1, y, z0), new class_243(x1, y, z1), argb, width);
            this.line(new class_243(x1, y, z1), new class_243(x0, y, z1), argb, width);
            this.line(new class_243(x0, y, z1), new class_243(x0, y, z0), argb, width);
        }
        this.line(new class_243(x0, y0, z0), new class_243(x0, y1, z0), argb, width);
        this.line(new class_243(x1, y0, z0), new class_243(x1, y1, z0), argb, width);
        this.line(new class_243(x1, y0, z1), new class_243(x1, y1, z1), argb, width);
        this.line(new class_243(x0, y0, z1), new class_243(x0, y1, z1), argb, width);
    }

    private double depth(class_243 point) {
        return (point.field_1352 - this.eye.field_1352) * (double)this.forward.x() + (point.field_1351 - this.eye.field_1351) * (double)this.forward.y() + (point.field_1350 - this.eye.field_1350) * (double)this.forward.z();
    }
}

