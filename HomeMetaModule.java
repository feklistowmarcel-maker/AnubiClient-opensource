/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_243
 *  net.minecraft.class_310
 */
package net.anubis.client.module.impl.misc;

import net.anubis.client.event.EventHandler;
import net.anubis.client.event.impl.TickEvent;
import net.anubis.client.module.ClientModule;
import net.anubis.client.module.ModuleCategory;
import net.anubis.client.module.setting.NumberSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_243;
import net.minecraft.class_310;

@Environment(value=EnvType.CLIENT)
public final class HomeMetaModule
extends ClientModule {
    private static final double TELEPORT_DISTANCE_SQ = 25.0;
    private final NumberSetting homeNumber = this.addSetting("home-number", new NumberSetting("Home Number", 3.0f, 1.0f, 10.0f).step(1.0f));
    private final NumberSetting delay = this.addSetting("delay", new NumberSetting("Delay (s)", 0.5f, 0.0f, 10.0f).step(0.5f));
    private final NumberSetting rtpTimeout = this.addSetting("rtp-max-timeout", new NumberSetting("RTP Timeout (s)", 10.0f, 1.0f, 30.0f).step(1.0f));
    private Step step;
    private long nextAtMs;
    private long rtpDeadlineMs;
    private class_243 rtpStart;
    private int home;

    public HomeMetaModule() {
        super("home-meta", "Home Meta", "Re-sets a home, RTPs away, then teleports back home.", ModuleCategory.BASE_RAID);
    }

    @Override
    public boolean persistEnabled() {
        return false;
    }

    @Override
    public boolean inConfigs() {
        return false;
    }

    @Override
    protected void onEnable() {
        if (class_310.method_1551().field_1724 == null) {
            this.requestDisable();
            return;
        }
        this.home = this.homeNumber.getInt();
        this.step = Step.DELHOME;
        this.nextAtMs = 0L;
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 == null || mc.method_1562() == null) {
            this.requestDisable();
            return;
        }
        long now = System.currentTimeMillis();
        if (now < this.nextAtMs) {
            return;
        }
        long delayMs = (long)((double)this.delay.get() * 1000.0);
        switch (this.step.ordinal()) {
            case 0: {
                HomeMetaModule.command(mc, "delhome " + this.home);
                this.step = Step.SETHOME;
                this.nextAtMs = now + delayMs;
                break;
            }
            case 1: {
                HomeMetaModule.command(mc, "sethome " + this.home);
                this.step = Step.RTP;
                this.nextAtMs = now + delayMs;
                break;
            }
            case 2: {
                this.rtpStart = mc.field_1724.method_73189();
                HomeMetaModule.command(mc, "rtp");
                this.rtpDeadlineMs = now + (long)((double)this.rtpTimeout.get() * 1000.0);
                this.step = Step.WAIT_TELEPORT;
                break;
            }
            case 3: {
                boolean moved;
                boolean bl = moved = mc.field_1724.method_73189().method_1025(this.rtpStart) > 25.0;
                if (!moved && now < this.rtpDeadlineMs) break;
                this.step = Step.HOME;
                this.nextAtMs = now + delayMs;
                break;
            }
            case 4: {
                HomeMetaModule.command(mc, "home " + this.home);
                this.requestDisable();
            }
        }
    }

    private static void command(class_310 mc, String command) {
        mc.method_1562().method_45730(command);
    }

    @Environment(value=EnvType.CLIENT)
    private static enum Step {
        DELHOME,
        SETHOME,
        RTP,
        WAIT_TELEPORT,
        HOME;

    }
}

