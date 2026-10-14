/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10185
 *  net.minecraft.class_238
 *  net.minecraft.class_243
 *  net.minecraft.class_2596
 *  net.minecraft.class_2828
 *  net.minecraft.class_310
 *  net.minecraft.class_3532
 *  net.minecraft.class_746
 */
package net.anubis.client.util.pvp;

import net.anubis.client.util.pvp.Rotation;
import net.anubis.client.util.pvp.RotationUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10185;
import net.minecraft.class_238;
import net.minecraft.class_243;
import net.minecraft.class_2596;
import net.minecraft.class_2828;
import net.minecraft.class_310;
import net.minecraft.class_3532;
import net.minecraft.class_746;

@Environment(value=EnvType.CLIENT)
public final class ServerRotation {
    private static Rotation requested;
    private static Rotation committed;
    private static Rotation server;
    private static boolean silent;
    private static class_746 owner;
    private static float ownYaw;
    private static float ownPitch;
    private static boolean swapped;
    private static Rotation walked;

    private ServerRotation() {
    }

    public static void request(Rotation rotation) {
        if (requested == null && committed == null) {
            requested = rotation;
        }
    }

    public static boolean pending() {
        return requested != null || committed != null;
    }

    public static Rotation sent() {
        return server;
    }

    public static boolean canRotate(class_746 player) {
        return !player.method_6128() && !player.method_6123() && !player.method_5765() && !player.method_5681() && player.field_3944.method_76760();
    }

    public static Rotation next(class_746 player, Rotation wanted) {
        Rotation base = server != null ? server : new Rotation(player.method_36454(), player.method_36455());
        double k = RotationUtils.mouseScale(class_310.method_1551(), player);
        float step = (float)k * 0.15f;
        long yawCounts = Math.round(class_3532.method_15393((float)(wanted.yaw() - base.yaw())) / step);
        long pitchCounts = Math.round((wanted.pitch() - base.pitch()) / step);
        float yaw = base.yaw() + (float)((double)yawCounts * k) * 0.15f;
        float pitch = class_3532.method_15363((float)(base.pitch() + (float)((double)pitchCounts * k) * 0.15f), (float)-90.0f, (float)90.0f);
        return new Rotation(yaw, pitch);
    }

    public static void startTick() {
        class_746 player = class_310.method_1551().field_1724;
        if (player != owner) {
            owner = player;
            server = null;
        }
        requested = null;
        committed = null;
        if (player == null || server == null || Math.abs(player.method_36454() - server.yaw()) <= 180.0f) {
            return;
        }
        float shift = server.yaw() + class_3532.method_15393((float)(player.method_36454() - server.yaw())) - player.method_36454();
        player.method_36456(player.method_36454() + shift);
        player.field_5982 += shift;
        player.field_3932 += shift;
        player.field_3931 += shift;
        player.field_6241 += shift;
        player.field_6259 += shift;
        player.field_6283 += shift;
        player.field_6220 += shift;
    }

    public static void noteSent(class_2596<?> packet) {
        class_2828 move;
        if (packet instanceof class_2828 && (move = (class_2828)packet).method_36172() && class_310.method_1551().method_18854()) {
            server = new Rotation(move.method_12271(0.0f), move.method_12270(0.0f));
            silent = swapped;
            owner = class_310.method_1551().field_1724;
        }
    }

    public static Rotation commit(class_746 player) {
        if (committed == null) {
            committed = requested != null && ServerRotation.canRotate(player) ? ServerRotation.next(player, requested) : new Rotation(player.method_36454(), player.method_36455());
        }
        return committed;
    }

    public static boolean isSwapped() {
        return swapped;
    }

    public static void applyForMovement(class_746 player) {
        Rotation target;
        if (swapped || !ServerRotation.canRotate(player)) {
            return;
        }
        Rotation rotation = committed != null ? committed : (target = requested != null ? ServerRotation.next(player, requested) : null);
        if (target == null || target.yaw() == player.method_36454() && target.pitch() == player.method_36455()) {
            return;
        }
        ownYaw = player.method_36454();
        ownPitch = player.method_36455();
        player.method_36456(target.yaw());
        player.method_36457(target.pitch());
        walked = target;
        swapped = true;
    }

    public static class_10185 correctInput(class_10185 keys) {
        if (walked == null) {
            return keys;
        }
        int forward = ServerRotation.impulse(keys.comp_3159(), keys.comp_3160());
        int strafe = ServerRotation.impulse(keys.comp_3161(), keys.comp_3162());
        if (forward == 0 && strafe == 0) {
            return keys;
        }
        double[] wanted = ServerRotation.heading(ownYaw, strafe, forward);
        int bestForward = forward;
        int bestStrafe = strafe;
        double bestDot = Double.NEGATIVE_INFINITY;
        for (int f = -1; f <= 1; ++f) {
            for (int s = -1; s <= 1; ++s) {
                double[] way;
                double dot;
                if (f == 0 && s == 0 || !((dot = (way = ServerRotation.heading(walked.yaw(), s, f))[0] * wanted[0] + way[1] * wanted[1]) > bestDot)) continue;
                bestDot = dot;
                bestForward = f;
                bestStrafe = s;
            }
        }
        if (bestForward == forward && bestStrafe == strafe) {
            return keys;
        }
        return new class_10185(bestForward > 0, bestForward < 0, bestStrafe > 0, bestStrafe < 0, keys.comp_3163(), keys.comp_3164(), keys.comp_3165());
    }

    public static boolean misses(class_746 player, class_238 box, double range) {
        if (server == null || !ServerRotation.pending() || !ServerRotation.canRotate(player)) {
            return false;
        }
        class_243 eye = player.method_33571();
        if (box.method_1006(eye)) {
            return false;
        }
        Rotation look = committed != null ? committed : ServerRotation.next(player, requested);
        return !ServerRotation.hits(eye, box, range, look.yaw(), look.pitch()) && !ServerRotation.hits(eye, box, range, server.yaw(), look.pitch()) && !ServerRotation.hits(eye, box, range, server.yaw(), server.pitch());
    }

    public static boolean missesBlock(class_746 player, class_238 box, double range) {
        class_243 eye;
        if (server == null) {
            return false;
        }
        if (!silent) {
            Rotation look;
            if (!ServerRotation.pending() || !ServerRotation.canRotate(player)) {
                return false;
            }
            Rotation rotation = look = committed != null ? committed : ServerRotation.next(player, requested);
            if (look.yaw() == player.method_36454() && look.pitch() == player.method_36455()) {
                return false;
            }
        }
        return !box.method_1006(eye = player.method_33571()) && !ServerRotation.hits(eye, box, range, server.yaw(), server.pitch());
    }

    public static void afterSend(class_746 player) {
        if (swapped) {
            ServerRotation.restore(player);
        }
    }

    public static void finishTick(class_746 player) {
        if (swapped) {
            ServerRotation.restore(player);
        }
    }

    private static int impulse(boolean positive, boolean negative) {
        return positive == negative ? 0 : (positive ? 1 : -1);
    }

    private static double[] heading(float yaw, int strafe, int forward) {
        double sin = class_3532.method_15374((double)(yaw * ((float)Math.PI / 180)));
        double cos = class_3532.method_15362((double)(yaw * ((float)Math.PI / 180)));
        double x = (double)strafe * cos - (double)forward * sin;
        double z = (double)forward * cos + (double)strafe * sin;
        double length = Math.sqrt(x * x + z * z);
        return new double[]{x / length, z / length};
    }

    private static boolean hits(class_243 eye, class_238 box, double range, float yaw, float pitch) {
        class_243 direction = class_243.method_1030((float)pitch, (float)yaw);
        return box.method_992(eye, eye.method_1019(direction.method_1021(range))).isPresent();
    }

    private static void restore(class_746 player) {
        player.field_3932 += (ownYaw - walked.yaw()) * 0.5f;
        player.field_3916 += (ownPitch - walked.pitch()) * 0.5f;
        walked = null;
        player.method_36456(ownYaw);
        player.method_36457(ownPitch);
        swapped = false;
    }
}

