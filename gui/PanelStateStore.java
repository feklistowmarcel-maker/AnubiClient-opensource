/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.GsonBuilder
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.fabricmc.loader.api.FabricLoader
 *  org.slf4j.Logger
 *  org.slf4j.LoggerFactory
 */
package net.anubis.client.gui;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.FileAttribute;
import java.util.LinkedHashSet;
import java.util.Set;
import net.anubis.client.module.ModuleCategory;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Environment(value=EnvType.CLIENT)
public final class PanelStateStore {
    private static final Logger LOGGER = LoggerFactory.getLogger((String)"Anubis");
    private static final PanelStateStore INSTANCE = new PanelStateStore();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final int CONFIG_VERSION = 1;
    private final Set<String> collapsed = new LinkedHashSet<String>();
    private boolean loaded;
    private boolean readOnly;

    private PanelStateStore() {
    }

    public static PanelStateStore getInstance() {
        return INSTANCE;
    }

    public synchronized boolean isCollapsed(ModuleCategory category) {
        this.ensureLoaded();
        return this.collapsed.contains(category.name());
    }

    public synchronized void setCollapsed(ModuleCategory category, boolean fold) {
        boolean changed;
        this.ensureLoaded();
        boolean bl = changed = fold ? this.collapsed.add(category.name()) : this.collapsed.remove(category.name());
        if (changed) {
            this.save();
        }
    }

    public synchronized void load() {
        if (this.loaded) {
            return;
        }
        this.loaded = true;
        this.collapsed.clear();
        Path path = this.configPath();
        if (!Files.isRegularFile(path, new LinkOption[0])) {
            return;
        }
        try (BufferedReader reader = Files.newBufferedReader(path);){
            int version;
            JsonObject root = JsonParser.parseReader((Reader)reader).getAsJsonObject();
            int n = version = root.has("version") ? root.get("version").getAsInt() : 0;
            if (version > 1) {
                this.readOnly = true;
                LOGGER.warn("[Anubis] ClickGUI state version {} is newer than supported {}; skipping load", (Object)version, (Object)1);
                return;
            }
            if (!root.has("collapsed") || !root.get("collapsed").isJsonArray()) {
                return;
            }
            for (JsonElement element : root.getAsJsonArray("collapsed")) {
                if (!element.isJsonPrimitive()) continue;
                this.collapsed.add(element.getAsString());
            }
        }
        catch (IOException | RuntimeException e) {
            LOGGER.warn("[Anubis] Failed to load the ClickGUI state; preserving the file", (Throwable)e);
            this.collapsed.clear();
            this.readOnly = true;
        }
    }

    public synchronized void save() {
        if (this.readOnly) {
            return;
        }
        JsonObject root = new JsonObject();
        root.addProperty("version", (Number)1);
        JsonArray list = new JsonArray();
        for (String id : this.collapsed) {
            list.add(id);
        }
        root.add("collapsed", (JsonElement)list);
        Path path = this.configPath();
        try {
            Files.createDirectories(path.getParent(), new FileAttribute[0]);
            Path tmp = path.resolveSibling(path.getFileName().toString() + ".tmp");
            try (BufferedWriter writer = Files.newBufferedWriter(tmp, new OpenOption[0]);){
                GSON.toJson((JsonElement)root, (Appendable)writer);
            }
            try {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            }
            catch (IOException atomicFail) {
                Files.move(tmp, path, StandardCopyOption.REPLACE_EXISTING);
            }
        }
        catch (IOException e) {
            LOGGER.warn("[Anubis] Failed to save the ClickGUI state", (Throwable)e);
        }
    }

    private void ensureLoaded() {
        if (!this.loaded) {
            this.load();
        }
    }

    private Path configPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("anubis").resolve("clickgui.json");
    }
}

