/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1268
 *  net.minecraft.class_1657
 *  net.minecraft.class_310
 *  net.minecraft.class_746
 *  org.lwjgl.glfw.GLFW
 */
package net.anubis.client.module.impl.misc;

import net.anubis.client.event.EventHandler;
import net.anubis.client.event.impl.TickEvent;
import net.anubis.client.module.ClientModule;
import net.anubis.client.module.ModuleCategory;
import net.anubis.client.module.impl.misc.GrimRiptide;
import net.anubis.client.module.impl.misc.TridentUtilModule;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1268;
import net.minecraft.class_1657;
import net.minecraft.class_310;
import net.minecraft.class_746;
import org.lwjgl.glfw.GLFW;

@Environment(value=EnvType.CLIENT)
public final class RiptideModule
extends ClientModule {
    private int releasedTick = -100;

    public RiptideModule() {
        super("riptide", "Riptide", "Uses a riptide trident while you hold right-click.", ModuleCategory.DONUT);
    }

    @EventHandler
    public void onTick(TickEvent event) {
        class_310 mc = class_310.method_1551();
        class_746 player = mc.field_1724;
        if (player == null || mc.field_1761 == null || mc.field_1755 != null) {
            return;
        }
        if (GLFW.glfwGetMouseButton((long)mc.method_22683().method_4490(), (int)1) != 1) {
            return;
        }
        if (!GrimRiptide.clientAccepts()) {
            return;
        }
        class_1268 hand = RiptideModule.hand(player);
        if (hand == null) {
            return;
        }
        if (player.method_6115()) {
            if (!GrimRiptide.isRiptide(player.method_6030())) {
                return;
            }
            if (player.method_6048() < RiptideModule.chargeTicks()) {
                return;
            }
            if (player.field_6012 - this.releasedTick < 9) {
                return;
            }
            mc.field_1761.method_2897((class_1657)player);
            this.releasedTick = player.field_6012;
            return;
        }
        if (player.field_6012 - this.releasedTick < 9) {
            return;
        }
        mc.field_1761.method_2919((class_1657)player, hand);
    }

    private static int chargeTicks() {
        return Math.max(9, TridentUtilModule.minChargeTicks(10));
    }

    private static class_1268 hand(class_746 player) {
        if (GrimRiptide.isRiptide(player.method_6047())) {
            return class_1268.field_5808;
        }
        if (GrimRiptide.isRiptide(player.method_6079())) {
            return class_1268.field_5810;
        }
        return null;
    }
}

