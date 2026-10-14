/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  it.unimi.dsi.fastutil.objects.Object2IntMap$Entry
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1799
 *  net.minecraft.class_1802
 *  net.minecraft.class_1893
 *  net.minecraft.class_2338
 *  net.minecraft.class_238
 *  net.minecraft.class_2680
 *  net.minecraft.class_310
 *  net.minecraft.class_3611
 *  net.minecraft.class_3612
 *  net.minecraft.class_6880
 *  net.minecraft.class_746
 */
package net.anubis.client.module.impl.misc;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1799;
import net.minecraft.class_1802;
import net.minecraft.class_1893;
import net.minecraft.class_2338;
import net.minecraft.class_238;
import net.minecraft.class_2680;
import net.minecraft.class_310;
import net.minecraft.class_3611;
import net.minecraft.class_3612;
import net.minecraft.class_6880;
import net.minecraft.class_746;

@Environment(value=EnvType.CLIENT)
public final class GrimRiptide {
    public static final int MIN_TICKS = 9;

    private GrimRiptide() {
    }

    public static boolean clientAccepts() {
        class_310 mc = class_310.method_1551();
        class_746 player = mc.field_1724;
        if (player == null || mc.field_1687 == null || !mc.method_18854()) {
            return false;
        }
        if (player.method_5721() || mc.field_1687.method_8419()) {
            return true;
        }
        class_238 box = player.method_5829().method_1014(0.1);
        class_2338 min = class_2338.method_49637((double)box.field_1323, (double)box.field_1322, (double)box.field_1321);
        class_2338 max = class_2338.method_49637((double)box.field_1320, (double)box.field_1325, (double)box.field_1324);
        for (class_2338 pos : class_2338.method_10097((class_2338)min, (class_2338)max)) {
            class_2680 state = mc.field_1687.method_8320(pos);
            if (state.method_26227().method_39360((class_3611)class_3612.field_15910)) {
                return true;
            }
            if (!state.method_26227().method_39360((class_3611)class_3612.field_15909)) continue;
            return true;
        }
        return false;
    }

    public static boolean isRiptide(class_1799 stack) {
        if (!stack.method_31574(class_1802.field_8547)) {
            return false;
        }
        for (Object2IntMap.Entry entry : stack.method_58657().method_57539()) {
            class_6880 enchantment = (class_6880)entry.getKey();
            if (!enchantment.method_40225(class_1893.field_9104) || entry.getIntValue() <= 0) continue;
            return true;
        }
        return false;
    }
}

