/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_10799
 *  net.minecraft.class_1799
 *  net.minecraft.class_2561
 *  net.minecraft.class_2960
 *  net.minecraft.class_310
 *  net.minecraft.class_327
 *  net.minecraft.class_332
 *  net.minecraft.class_3414
 *  net.minecraft.class_3417
 *  net.minecraft.class_3532
 *  net.minecraft.class_368
 *  net.minecraft.class_368$class_369
 *  net.minecraft.class_374
 *  net.minecraft.class_5348
 *  net.minecraft.class_9334
 */
package net.anubis.client.gui;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_10799;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_3414;
import net.minecraft.class_3417;
import net.minecraft.class_3532;
import net.minecraft.class_368;
import net.minecraft.class_374;
import net.minecraft.class_5348;
import net.minecraft.class_9334;

@Environment(value=EnvType.CLIENT)
public class ChallengeToast
implements class_368 {
    private static final class_2960 BACKGROUND = class_2960.method_60656((String)"toast/advancement");
    private static final int TITLE_COLOR = -30465;
    private static final int MESSAGE_COLOR = -1;
    private static final int TEXT_X = 30;
    private static final int TEXT_PAD = 4;
    private static final int MAX_WIDTH = 240;
    private static final long DISPLAY_MILLIS = 5000L;
    private final Object token;
    private final class_1799 icon;
    private final boolean sound;
    private class_2561 title;
    private class_2561 message;
    private int width;
    private int count = 1;
    private boolean changed;
    private boolean hiding;
    private long changedAt;
    private class_368.class_369 wanted = class_368.class_369.field_2209;

    public ChallengeToast(class_1799 icon, class_2561 title, class_2561 message, boolean sound) {
        this(field_2208, icon, title, message, sound);
    }

    private ChallengeToast(Object token, class_1799 icon, class_2561 title, class_2561 message, boolean sound) {
        this.token = token;
        this.icon = icon.method_7972();
        this.icon.method_57379(class_9334.field_49641, (Object)true);
        this.title = title;
        this.message = message;
        this.sound = sound;
        this.fit();
    }

    public static void addOrUpdate(class_1799 icon, String title, class_2561 message, boolean sound) {
        ChallengeToast.addOrUpdate(icon, title, null, message, sound);
    }

    public static void addOrUpdate(class_1799 icon, String one, String many, class_2561 message, boolean sound) {
        class_310 mc = class_310.method_1551();
        if (!mc.method_18854()) {
            mc.execute(() -> ChallengeToast.addOrUpdate(icon, one, many, message, sound));
            return;
        }
        class_374 toasts = mc.method_1566();
        ChallengeToast toast = (ChallengeToast)toasts.method_1997(ChallengeToast.class, (Object)one);
        if (toast == null) {
            toasts.method_1999((class_368)new ChallengeToast(one, icon, (class_2561)class_2561.method_43470((String)one), message, sound));
            return;
        }
        ++toast.count;
        if (many != null) {
            toast.title = class_2561.method_43470((String)(toast.count + " " + many));
        }
        toast.message = message;
        toast.changed = true;
        toast.fit();
    }

    private void fit() {
        class_327 font = class_310.method_1551().field_1772;
        int text = Math.max(font.method_27525((class_5348)this.title), font.method_27525((class_5348)this.message));
        this.width = class_3532.method_15340((int)(30 + text + 4), (int)160, (int)240);
    }

    public Object method_1987() {
        return this.token;
    }

    public class_3414 method_66530() {
        return this.sound ? (class_3414)class_3417.field_14793.comp_349() : null;
    }

    public class_368.class_369 method_61988() {
        return this.wanted;
    }

    public int method_29049() {
        return this.width;
    }

    public void method_61989(class_374 toasts, long visibleFor) {
        if (this.hiding) {
            return;
        }
        if (this.changed) {
            this.changedAt = visibleFor;
            this.changed = false;
        }
        this.hiding = (double)(visibleFor - this.changedAt) >= 5000.0 * toasts.method_48221();
        this.wanted = this.hiding ? class_368.class_369.field_2209 : class_368.class_369.field_2210;
    }

    public void method_71995() {
        if (!this.changed) {
            return;
        }
        this.hiding = false;
        class_310.method_1551().method_1566().method_1999((class_368)this);
    }

    public void method_1986(class_332 graphics, class_327 font, long visibleFor) {
        int half = this.width / 2;
        ChallengeToast.drawBackground(graphics, 0, 0, half);
        ChallengeToast.drawBackground(graphics, 160 - (this.width - half), half, this.width - half);
        boolean single = this.message.getString().isEmpty();
        this.drawText(graphics, font, this.title, single ? 12 : 7, -30465);
        if (!single) {
            this.drawText(graphics, font, this.message, 18, -1);
        }
        graphics.method_51445(this.icon, 8, 8);
    }

    private static void drawBackground(class_332 graphics, int u, int x, int w) {
        graphics.method_70846(class_10799.field_56883, BACKGROUND, 160, 32, u, 0, x, 0, w, 32);
    }

    private void drawText(class_332 graphics, class_327 font, class_2561 text, int y, int color) {
        int room = this.width - 30 - 4;
        int textWidth = font.method_27525((class_5348)text);
        if (textWidth <= room) {
            graphics.method_51439(font, text, 30, y, color, false);
            return;
        }
        graphics.method_51448().pushMatrix();
        graphics.method_51448().translate(30.0f, (float)y);
        graphics.method_51448().scale((float)room / (float)textWidth, 1.0f);
        graphics.method_51439(font, text, 0, 0, color, false);
        graphics.method_51448().popMatrix();
    }
}

