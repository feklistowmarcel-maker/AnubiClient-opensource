/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_1297
 */
package net.anubis.client.event.impl;

import net.anubis.client.event.Event;
import net.anubis.client.event.EventType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1297;

@Environment(value=EnvType.CLIENT)
public final class WorldEntityRemoveEvent
extends Event {
    public static final EventType<WorldEntityRemoveEvent> TYPE = new EventType("WorldEntityRemoveEvent");
    public class_1297 entity;
    private static final WorldEntityRemoveEvent INSTANCE = new WorldEntityRemoveEvent();

    private WorldEntityRemoveEvent() {
    }

    public static void dispatch(class_1297 entity) {
        if (!TYPE.hasPreListeners()) {
            return;
        }
        WorldEntityRemoveEvent.INSTANCE.entity = entity;
        TYPE.dispatch(INSTANCE);
        WorldEntityRemoveEvent.INSTANCE.entity = null;
    }
}

