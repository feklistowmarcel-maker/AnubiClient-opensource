/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_2246
 *  net.minecraft.class_2680
 *  net.minecraft.class_3481
 */
package net.anubis.client.module.impl.misc;

import net.anubis.client.module.ClientModule;
import net.anubis.client.module.ModuleCategory;
import net.anubis.client.module.setting.BooleanSetting;
import net.anubis.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_2246;
import net.minecraft.class_2680;
import net.minecraft.class_3481;

@Environment(value=EnvType.CLIENT)
public final class SpeedMineModule
extends ClientModule {
    private static volatile SpeedMineModule active;
    private final NumberSetting breakAt = this.addSetting("break-at", new NumberSetting("Break At", 0.7f, 0.3f, 1.0f).step(0.01f));
    private final BooleanSetting skipDelay = this.addSetting("skip-delay", new BooleanSetting("Skip Break Delay", true));
    private final BooleanSetting keepInstantDelay = this.addSetting("keep-instant-delay", new BooleanSetting("Keep Instant Delay", true));

    public SpeedMineModule() {
        super("speed-mine", "Donut Speed Mine", "Finishes breaking blocks before the full break time.", ModuleCategory.DONUT);
        this.keepInstantDelay.visibleWhen(this.skipDelay::get);
    }

    @Override
    protected void onEnable() {
        active = this;
    }

    @Override
    protected void onDisable() {
        active = null;
    }

    public static float scaleProgress(class_2680 state, float progress) {
        SpeedMineModule module = active;
        if (module == null || !SpeedMineModule.affects(state)) {
            return progress;
        }
        return progress / module.breakAt.get();
    }

    public static boolean dropsDelay(class_2680 state, float progress) {
        SpeedMineModule module = active;
        if (module == null || !module.skipDelay.get() || !SpeedMineModule.affects(state)) {
            return false;
        }
        return !module.keepInstantDelay.get() || progress < 1.0f;
    }

    private static boolean affects(class_2680 state) {
        if (state.method_26215() || state.method_27852(class_2246.field_10260)) {
            return false;
        }
        if (state.method_27852(class_2246.field_10255)) {
            return true;
        }
        return !state.method_26164(class_3481.field_33716) && !state.method_26164(class_3481.field_33713);
    }
}

