/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  imgui.ImDrawList
 *  imgui.ImFont
 *  imgui.ImGui
 *  imgui.ImGuiIO
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_310
 *  org.lwjgl.util.tinyfd.TinyFileDialogs
 */
package net.anubis.client.gui.mainmenu;

import imgui.ImDrawList;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiIO;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import net.anubis.client.account.Account;
import net.anubis.client.account.AccountType;
import net.anubis.client.account.Accounts;
import net.anubis.client.gui.EgyptDraw;
import net.anubis.client.gui.mainmenu.DuskScene;
import net.anubis.client.gui.mainmenu.DuskWidgets;
import net.anubis.client.gui.mainmenu.MenuFrame;
import net.anubis.client.gui.mainmenu.PlayerFaces;
import net.anubis.client.proxy.Proxies;
import net.anubis.client.proxy.Proxy;
import net.anubis.client.proxy.ProxyChecker;
import net.anubis.client.proxy.ProxyType;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import org.lwjgl.util.tinyfd.TinyFileDialogs;

@Environment(value=EnvType.CLIENT)
public final class ConnectionsPanel {
    private static final String NAME_CHARS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_";
    private static final int WINDOW_FLAGS = 447;
    private static final float ACCOUNTS_W = 820.0f;
    private static final float PROXIES_W = 860.0f;
    private static final float PAD = 24.0f;
    private static final float FIELD_H = 50.0f;
    private static final float ACCOUNT_ROW_H = 68.0f;
    private static final float PROXY_ROW_H = 46.0f;
    private static final float TAB_H = 40.0f;
    private static final float BAR_H = 30.0f;
    private static final float FACE = 46.0f;
    private static final float CHECK = 16.0f;
    private static final int ACCOUNT_ROWS = 6;
    private static final int PROXY_ROWS = 8;
    private static final float FADE = 0.15f;
    private static final float CONFIRM_FOR = 3.0f;
    static final int WINDOW = -15461610;
    private static final int ERROR = -1733516;
    private static final int GOOD = -6501498;
    private final Runnable onProxyChange;
    private final DuskWidgets ui = new DuskWidgets();
    private final MenuFrame frame = new MenuFrame();
    private boolean open;
    private boolean opening;
    private long openedAt;
    private Tab tab = Tab.ACCOUNTS;
    private String routeAtOpen = "";
    private Object armed;
    private float armedUntil;
    private AccountType adding = AccountType.CRACKED;
    private AccountType accountFilter;
    private final DuskWidgets.TextField crackedName = new DuskWidgets.TextField(16).placeholder("Username").allowedChars("abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_");
    private final DuskWidgets.TextField token = new DuskWidgets.TextField(4096).placeholder("Minecraft access token").password(true);
    private CompletableFuture<?> microsoft;
    private String accountError;
    private final DuskWidgets.TextField proxyInput = new DuskWidgets.TextField(512).placeholder("host:port or socks5://user:pass@host:port");
    private String proxyMessage;
    private boolean proxyError;
    private Filter filter = Filter.ALL;
    private ProxyType typeFilter;
    private Sort sort = Sort.ADDED;
    private final Set<Proxy> selected = new HashSet<Proxy>();
    private boolean fileDialog;

    public ConnectionsPanel(Runnable onProxyChange) {
        this.onProxyChange = onProxyChange;
    }

    public void open(Tab tab) {
        if (!this.open) {
            this.open = true;
            this.opening = true;
            this.openedAt = System.nanoTime();
            this.routeAtOpen = ConnectionsPanel.route();
        }
        this.tab = tab;
        this.armed = null;
        if (tab == Tab.PROXIES) {
            this.proxyInput.focus();
        } else if (this.adding != AccountType.MICROSOFT) {
            this.field(this.adding).focus();
        }
    }

    public boolean isOpen() {
        return this.open;
    }

    public boolean keyPressed(int key) {
        if (!this.open) {
            return false;
        }
        if (key == 256) {
            this.close();
        }
        return true;
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public void render(float width, float height, float scale, float dt) {
        if (!this.open) {
            return;
        }
        ImGuiIO io = ImGui.getIO();
        boolean click = !this.opening && ImGui.isMouseClicked((int)0);
        this.opening = false;
        ImGui.setNextWindowPos((float)0.0f, (float)0.0f);
        ImGui.setNextWindowSize((float)width, (float)height);
        ImGui.pushStyleVar((int)2, (float)0.0f, (float)0.0f);
        ImGui.pushStyleVar((int)4, (float)0.0f);
        ImGui.begin((String)"##anubis_connections", (int)447);
        ImGui.popStyleVar((int)2);
        try {
            this.frame.begin(ImGui.getWindowDrawList(), ImGui.getForegroundDrawList(), width, height, scale, (float)(System.nanoTime() - this.openedAt) / 1.0E9f, dt, io.getMousePosX(), io.getMousePosY(), click);
            this.ui.begin(this.frame);
            try {
                this.draw();
            }
            finally {
                this.ui.end();
            }
            this.drawTooltip();
        }
        finally {
            ImGui.end();
        }
    }

    private void close() {
        this.open = false;
        this.crackedName.setText("");
        this.token.setText("");
        this.proxyInput.setText("");
        this.selected.clear();
        if (this.microsoft != null) {
            Accounts.get().cancelMicrosoft();
        }
        if (!ConnectionsPanel.route().equals(this.routeAtOpen)) {
            this.onProxyChange.run();
        }
    }

    private void draw() {
        ImDrawList dl = this.frame.bg();
        float s = this.frame.scale();
        float sw = this.frame.width();
        float sh = this.frame.height();
        float fade = Math.min(1.0f, this.frame.time() / 0.15f);
        float a = 1.0f - (1.0f - fade) * (1.0f - fade);
        this.ui.alpha(a);
        DuskScene.veil(dl, sw, sh, 0.5f * a);
        boolean accounts = this.tab == Tab.ACCOUNTS;
        List<Object> shown = accounts ? List.of() : this.shownProxies();
        List<Object> listed = accounts ? this.shownAccounts() : List.of();
        float titleH = this.frame.textHeight(this.frame.itemFont());
        float lineH = this.frame.textHeight(this.frame.bodyFont());
        float listH = accounts ? (float)Math.max(3, Math.min(listed.size(), 6)) * 68.0f * s : (float)Math.max(1, Math.min(shown.size(), 8)) * 46.0f * s;
        float bars = accounts ? 96.0f * s : 60.0f * s + 22.0f * s;
        float w = Math.min((accounts ? 820.0f : 860.0f) * s, sw - 32.0f * s);
        float h = Math.min(24.0f * s + titleH + 16.0f * s + 50.0f * s + 12.0f * s + lineH + 18.0f * s + bars + listH + 16.0f * s, sh - 32.0f * s);
        float x = Math.round((sw - w) * 0.5f);
        float y = Math.round((sh - h) * 0.5f + (1.0f - a) * 6.0f * s);
        for (int i = 1; i <= 3; ++i) {
            float g = (float)i * 4.0f * s;
            dl.addRectFilled(x - g, y - g + 4.0f * s, x + w + g, y + h + g + 4.0f * s, EgyptDraw.col(-16777216, 0.08f * a));
        }
        dl.addRectFilled(x, y, x + w, y + h, EgyptDraw.col(-15461610, a));
        dl.addRect(x, y, x + w, y + h, EgyptDraw.col(-923950, 0.1f * a), 0.0f, 0, Math.max(1.0f, s));
        float pad = 24.0f * s;
        this.frame.text(dl, this.frame.itemFont(), x + pad, y + pad * 0.8f, -923950, a, accounts ? "Accounts" : "Proxies");
        this.closeCross(x + w - 44.0f * s, y + pad * 0.8f + titleH * 0.5f - 16.0f * s, 32.0f * s, s, a);
        float top = y + pad + titleH + 16.0f * s;
        float listBottom = y + h - 16.0f * s;
        if (accounts) {
            this.accounts(listed, x, top, w, listBottom, s, a);
        } else {
            this.proxies(shown, x, top, w, listBottom, s, a);
        }
    }

    private void closeCross(float x, float y, float size, float s, float a) {
        ImDrawList dl = this.frame.bg();
        boolean over = this.ui.hovered(x, y, size, size);
        if (this.ui.pressed(x, y, size, size)) {
            this.close();
        }
        float k = this.ui.hover("close", over);
        float cx = x + size * 0.5f;
        float cy = y + size * 0.5f;
        float c = 6.0f * s;
        int color = EgyptDraw.col(EgyptDraw.mix(-7372954, -923950, k), a);
        float line = Math.max(1.2f, 1.6f * s);
        dl.addLine(cx - c, cy - c, cx + c, cy + c, color, line);
        dl.addLine(cx - c, cy + c, cx + c, cy - c, color, line);
    }

    private List<Account> shownAccounts() {
        List<Account> all = Accounts.get().list();
        return this.accountFilter == null ? all : all.stream().filter(account -> account.type() == this.accountFilter).toList();
    }

    private void accounts(List<Account> listed, float x, float y, float w, float bottom, float s, float a) {
        String hint;
        float pad = 24.0f * s;
        float fx = x + pad;
        float fw = w - 2.0f * pad;
        boolean busy = Accounts.get().busy();
        this.addTabs(fx, y, s, a);
        y += 54.0f * s;
        float fh = 50.0f * s;
        ImFont font = this.frame.bodyFont();
        if (this.adding == AccountType.MICROSOFT) {
            String label = this.microsoft != null ? "Waiting for the browser..." : "Sign in with Microsoft";
            if (this.box("microsoft", label, fx, y, fw, fh, this.microsoft == null && !busy, a)) {
                this.signInMicrosoft();
            }
            hint = "Opens Microsoft's sign-in in your browser";
        } else {
            DuskWidgets.TextField field = this.field(this.adding);
            if (this.ui.textField(field, fx, y, fw, fh) && !field.text().isBlank() && !busy) {
                this.add(field);
            }
            hint = this.adding == AccountType.CRACKED ? "Press Enter to add. Joins servers that don't check accounts." : "Press Enter to add. A token works until it expires, about a day.";
        }
        y += fh + 12.0f * s;
        if (this.microsoft != null) {
            float cancelW = this.frame.textWidth(font, "Cancel");
            this.text(DuskWidgets.fit(font, Accounts.get().status(), fw - cancelW - 16.0f * s), fx, y, -923950, a);
            if (this.link("cancel", "Cancel", fx + fw - cancelW, y, true, a)) {
                Accounts.get().cancelMicrosoft();
            }
        } else if (busy) {
            this.text(DuskWidgets.fit(font, Accounts.get().status(), fw), fx, y, -7372954, a);
        } else if (this.accountError != null) {
            this.text(DuskWidgets.fit(font, this.accountError, fw), fx, y, -1733516, a);
        } else {
            this.text(DuskWidgets.fit(font, hint, fw), fx, y, -7372954, a);
        }
        this.accountFilters(fx, y += this.frame.textHeight(font) + 18.0f * s, s, a);
        float listH = bottom - (y += 42.0f * s);
        if (listed.isEmpty()) {
            this.centered((String)(this.accountFilter == null ? "No accounts yet" : "No " + this.accountFilter.label() + " accounts"), x, y, w, listH, a);
            return;
        }
        Account active = Accounts.get().active();
        float rowX = x + 8.0f * s;
        float rowW = w - 16.0f * s;
        float rowH = 68.0f * s;
        float scroll = this.ui.beginScroll("accounts", rowX, y, rowW, listH, (float)listed.size() * rowH);
        for (int i = 0; i < listed.size(); ++i) {
            Account account = listed.get(i);
            this.accountRow(account, account.equals(active), rowX, y - scroll + (float)i * rowH, rowW, rowH, s, a);
        }
        this.ui.endScroll();
    }

    private void addTabs(float x, float y, float s, float a) {
        ImDrawList dl = this.frame.bg();
        ImFont font = this.frame.menuFont();
        float h = 40.0f * s;
        float ty = y + (h - this.frame.textHeight(font)) * 0.5f;
        for (AccountType type : AccountType.values()) {
            String label = type.label();
            float tw = this.frame.textWidth(font, label);
            boolean over = this.ui.hovered(x - 6.0f * s, y, tw + 12.0f * s, h);
            if (this.ui.pressed(x - 6.0f * s, y, tw + 12.0f * s, h) && this.microsoft == null && type != this.adding) {
                this.adding = type;
                this.accountError = null;
                if (type != AccountType.MICROSOFT) {
                    this.field(type).focus();
                }
            }
            float k = this.ui.hover(new Key("add", label), over);
            this.frame.text(dl, font, x, ty, type == this.adding ? -923950 : EgyptDraw.mix(-7372954, -923950, k), a, label);
            if (type == this.adding) {
                dl.addRectFilled(x, y + h - 2.0f * s, x + tw, y + h, EgyptDraw.col(-871878, a));
            }
            x += tw + 30.0f * s;
        }
    }

    private void accountFilters(float x, float y, float s, float a) {
        List<Account> all = Accounts.get().list();
        if (this.chip("all-accounts", "All", all.size(), this.accountFilter == null, x, y, a)) {
            this.accountFilter = null;
        }
        x += this.chipWidth("All", all.size()) + 18.0f * s;
        for (AccountType type : AccountType.values()) {
            int count = (int)all.stream().filter(account -> account.type() == type).count();
            if (this.chip((Object)type, type.label(), count, this.accountFilter == type, x, y, a)) {
                this.accountFilter = type;
            }
            x += this.chipWidth(type.label(), count) + 18.0f * s;
        }
    }

    private void accountRow(Account account, boolean inUse, float x, float y, float w, float h, float s, float a) {
        ImDrawList dl = this.frame.bg();
        boolean over = this.ui.hovered(x, y, w, h);
        float k = this.ui.hover(account, over);
        if (k > 0.01f && !inUse) {
            dl.addRectFilled(x, y, x + w, y + h, EgyptDraw.col(-923950, 0.05f * k * a));
        }
        float face = 46.0f * s;
        float fx = x + 12.0f * s;
        PlayerFaces.draw(dl, account, fx, y + (h - face) * 0.5f, face, s, a);
        String name = account.name().isEmpty() ? account.type().label() + " account" : account.name();
        float right = this.removeCross(new Key(account, "remove"), x + w - 8.0f * s, y, h, k, s, a);
        if (right < 0.0f) {
            Accounts.get().remove(account);
            return;
        }
        if (inUse) {
            right = this.tag("Active", -1326998, right, y, h, a);
        }
        this.entry(this.frame.menuFont(), this.frame.listFont(), fx + face + 16.0f * s, y, h, right - 12.0f * s, name, account.type().label(), a);
        if (this.ui.pressed(x, y, w, h) && !inUse) {
            this.login(account);
        }
    }

    private DuskWidgets.TextField field(AccountType type) {
        return type == AccountType.SESSION ? this.token : this.crackedName;
    }

    private void add(DuskWidgets.TextField field) {
        CompletableFuture<Account> added = field == this.token ? Accounts.get().addSession(field.text()) : Accounts.get().addCracked(field.text());
        this.track(added);
        added.thenRun(() -> field.setText(""));
    }

    private void login(Account account) {
        if (!Accounts.get().busy()) {
            this.track(Accounts.get().login(account));
        }
    }

    private void signInMicrosoft() {
        CompletableFuture<Account> signIn = Accounts.get().addMicrosoft();
        this.microsoft = signIn;
        this.track(signIn);
        signIn.whenComplete((account, failure) -> {
            this.microsoft = null;
        });
    }

    private void track(CompletableFuture<?> action) {
        this.accountError = null;
        action.whenComplete((result, failure) -> {
            this.accountError = failure == null ? null : ConnectionsPanel.messageOf(failure);
        });
    }

    private static String messageOf(Throwable failure) {
        Throwable cause = failure instanceof CompletionException && failure.getCause() != null ? failure.getCause() : failure;
        String message = cause.getMessage();
        return message == null || message.isBlank() ? "Login failed" : message;
    }

    private List<Proxy> shownProxies() {
        ArrayList<Proxy> shown = new ArrayList<Proxy>();
        for (Proxy proxy : Proxies.get().list()) {
            if (!this.filter.matches(proxy) || this.typeFilter != null && proxy.type != this.typeFilter) continue;
            shown.add(proxy);
        }
        if (this.sort.order != null) {
            shown.sort(this.sort.order);
        }
        return shown;
    }

    private static int pingOrder(Proxy proxy) {
        if (proxy.ping >= 0) {
            return proxy.ping;
        }
        return proxy.ping == -2 ? 0x7FFFFFFE : Integer.MAX_VALUE;
    }

    private void proxies(List<Proxy> shown, float x, float y, float w, float bottom, float s, float a) {
        float px;
        float pad = 24.0f * s;
        float fx = x + pad;
        float fw = w - 2.0f * pad;
        List<Proxy> all = Proxies.get().list();
        this.selected.retainAll(new HashSet<Proxy>(all));
        float fh = 50.0f * s;
        float fieldW = fw - fh - 8.0f * s;
        if (this.ui.textField(this.proxyInput, fx, y, fieldW, fh) && !this.proxyInput.text().isBlank()) {
            int added = Proxies.get().importText(this.proxyInput.text());
            this.message(added == 0 ? "Not added: already saved, or not host:port" : null, added == 0);
            if (added > 0) {
                this.proxyInput.setText("");
            }
        }
        if (this.pasteButton(px = fx + fw - fh, y, fh, a)) {
            String clipboard = class_310.method_1551().field_1774.method_1460();
            this.imported(Proxies.get().importText(clipboard), clipboard.isBlank() ? "The clipboard is empty" : "Nothing new on the clipboard");
        }
        if (this.ui.hoverOf("paste") > 0.5f) {
            this.frame.tooltip("Paste proxies from the clipboard", px + fh * 0.5f, y + fh + 6.0f * s);
        }
        ImFont font = this.frame.bodyFont();
        float lx = fx;
        if (this.link("file", "Import file", lx, y += 62.0f * s, !this.fileDialog, a)) {
            this.importFile();
        }
        if (this.ui.hoverOf("file") > 0.5f) {
            this.frame.tooltip("A text file with one proxy per line", lx + this.frame.textWidth(font, "Import file") * 0.5f, y + this.frame.textHeight(font) + 8.0f * s);
        }
        lx += this.frame.textWidth(font, "Import file") + 24.0f * s;
        int pending = ProxyChecker.get().pending();
        if (pending > 0) {
            this.statusRight("Checking, " + pending + " left", false, lx, fx + fw, y, a);
        } else if (this.proxyMessage != null) {
            this.statusRight(this.proxyMessage, this.proxyError, lx, fx + fw, y, a);
        }
        this.filters(all, fx, y += this.frame.textHeight(font) + 18.0f * s, fw, s, a);
        this.actions(shown, all, fx, y += 30.0f * s + 10.0f * s, fw, s, a);
        float listH = bottom - (y += 30.0f * s + 12.0f * s);
        if (shown.isEmpty()) {
            this.centered(all.isEmpty() ? "No proxies yet" : "None match the filter", x, y, w, listH, a);
            return;
        }
        float rowX = x + 8.0f * s;
        float rowW = w - 16.0f * s;
        float rowH = 46.0f * s;
        float scroll = this.ui.beginScroll("proxies", rowX, y, rowW, listH, (float)shown.size() * rowH);
        int first = Math.max(0, (int)(scroll / rowH));
        int last = Math.min(shown.size(), (int)((scroll + listH) / rowH) + 1);
        for (int i = first; i < last; ++i) {
            this.proxyRow(shown.get(i), rowX, y - scroll + (float)i * rowH, rowW, rowH, s, a);
        }
        this.ui.endScroll();
    }

    private void filters(List<Proxy> all, float x, float y, float w, float s, float a) {
        float lw;
        int[] counts = new int[Filter.values().length];
        int[] types = new int[ProxyType.values().length];
        for (Proxy proxy : all) {
            Filter[] filterArray = Filter.values();
            int n = filterArray.length;
            for (int i = 0; i < n; ++i) {
                Filter each = filterArray[i];
                if (!each.matches(proxy)) continue;
                int n2 = each.ordinal();
                counts[n2] = counts[n2] + 1;
            }
            int n3 = proxy.type.ordinal();
            types[n3] = types[n3] + 1;
        }
        float cx = x;
        for (Filter filter : Filter.values()) {
            if (this.chip((Object)filter, filter.label, counts[filter.ordinal()], this.filter == filter, cx, y, a)) {
                this.filter = filter;
            }
            cx += this.chipWidth(filter.label, counts[filter.ordinal()]) + 18.0f * s;
        }
        this.frame.bg().addLine(cx += 10.0f * s, y + 4.0f * s, cx, y + 30.0f * s - 4.0f * s, EgyptDraw.col(-923950, 0.12f * a), Math.max(1.0f, s));
        cx += 20.0f * s;
        for (ProxyType proxyType : ProxyType.values()) {
            boolean on;
            boolean bl = on = this.typeFilter == proxyType;
            if (this.chip((Object)proxyType, proxyType.label(), types[proxyType.ordinal()], on, cx, y, a)) {
                this.typeFilter = on ? null : proxyType;
            }
            cx += this.chipWidth(proxyType.label(), types[proxyType.ordinal()]) + 18.0f * s;
        }
        ImFont imFont = this.frame.bodyFont();
        String label = "Sort: " + this.sort.label;
        if (this.link("sort", label, x + w - (lw = this.frame.textWidth(imFont, label)), y + (30.0f * s - this.frame.textHeight(imFont)) * 0.5f, true, a)) {
            this.sort = Sort.values()[(this.sort.ordinal() + 1) % Sort.values().length];
        }
    }

    private void actions(List<Proxy> shown, List<Proxy> all, float x, float y, float w, float s, float a) {
        String remove;
        Key removeSelected;
        boolean allShown;
        ImFont font = this.frame.bodyFont();
        float cy = y + 30.0f * s * 0.5f;
        float ty = cy - this.frame.textHeight(font) * 0.5f;
        boolean bl = allShown = !shown.isEmpty() && this.selected.containsAll(shown);
        if (this.checkbox("select-all", allShown, x + 12.0f * s, cy, s, a)) {
            if (allShown) {
                this.selected.removeAll(shown);
            } else {
                this.selected.addAll(shown);
            }
        }
        float lx = x + 12.0f * s + 16.0f * s + 18.0f * s;
        if (this.selected.isEmpty()) {
            String label;
            Key removeDead;
            List<Proxy> dead;
            if (this.link("check-all", "Check all", lx, ty, !shown.isEmpty(), a)) {
                ProxyChecker.get().check(shown);
            }
            if (!(dead = all.stream().filter(proxy -> proxy.ping == -1).toList()).isEmpty() && this.link(removeDead, label = this.armed(removeDead = new Key("bulk", "remove-dead")) ? "Remove " + dead.size() + " dead?" : "Remove dead", lx += this.frame.textWidth(font, "Check all") + 22.0f * s, ty, true, a, this.armed(removeDead) ? -1733516 : -7372954) && this.confirm(removeDead)) {
                Proxies.get().removeAll(dead);
                this.message("Removed " + dead.size() + " dead", false);
            }
            return;
        }
        List<Proxy> picked = List.copyOf(this.selected);
        String count = picked.size() + " selected";
        this.text(count, lx, ty, -923950, a);
        if (this.link("check-selected", "Check", lx += this.frame.textWidth(font, count) + 22.0f * s, ty, true, a)) {
            ProxyChecker.get().check(picked);
        }
        if (this.link("copy-selected", "Copy", lx += this.frame.textWidth(font, "Check") + 22.0f * s, ty, true, a)) {
            class_310.method_1551().field_1774.method_1455(String.join((CharSequence)"\n", picked.stream().map(ConnectionsPanel::line).toList()));
            this.message("Copied " + picked.size() + (picked.size() == 1 ? " proxy" : " proxies"), false);
        }
        if (this.link(removeSelected, remove = this.armed(removeSelected = new Key("bulk", "remove-selected")) ? "Remove " + picked.size() + "?" : "Remove", lx += this.frame.textWidth(font, "Copy") + 22.0f * s, ty, true, a, this.armed(removeSelected) ? -1733516 : -7372954) && this.confirm(removeSelected)) {
            Proxies.get().removeAll(picked);
            this.selected.clear();
            this.message("Removed " + picked.size(), false);
        }
        if (this.link("clear-selection", "Clear", lx += this.frame.textWidth(font, remove) + 22.0f * s, ty, true, a)) {
            this.selected.clear();
        }
    }

    private void proxyRow(Proxy proxy, float x, float y, float w, float h, float s, float a) {
        ImDrawList dl = this.frame.bg();
        boolean switchable = proxy.enabled || proxy.isValid();
        boolean over = this.ui.hovered(x, y, w, h);
        float k = this.ui.hover(proxy, over);
        boolean picked = this.selected.contains(proxy);
        if (picked) {
            dl.addRectFilled(x, y, x + w, y + h, EgyptDraw.col(-871878, 0.07f * a));
        } else if (k > 0.01f && switchable) {
            dl.addRectFilled(x, y, x + w, y + h, EgyptDraw.col(-923950, 0.05f * k * a));
        }
        float cy = y + h * 0.5f;
        if (this.checkbox(new Key(proxy, "select"), picked, x + 12.0f * s, cy, s, a)) {
            if (picked) {
                this.selected.remove(proxy);
            } else {
                this.selected.add(proxy);
            }
        }
        ConnectionsPanel.proxyDot(dl, x + 52.0f * s, cy, proxy.enabled, 1.3f * s, a);
        float right = this.removeCross(new Key(proxy, "remove"), x + w - 8.0f * s, y, h, k, s, a);
        if (right < 0.0f) {
            Proxies.get().remove(proxy);
            this.selected.remove(proxy);
            return;
        }
        if (proxy.enabled) {
            right = this.tag("In use", -1326998, right, y, h, a);
        }
        right = this.ping(proxy, right, y, h, a);
        String login = proxy.username.isEmpty() ? "" : " \u00b7 " + proxy.username;
        String detail = proxy.name.equals(proxy.displayAddress()) ? proxy.type.label() + login : proxy.type.label() + " \u00b7 " + proxy.displayAddress() + login;
        this.entry(this.frame.bodyFont(), this.frame.detailFont(), x + 74.0f * s, y, h, right - 12.0f * s, proxy.name, detail, a);
        if (this.ui.pressed(x, y, w, h) && switchable) {
            Proxies.get().setEnabled(proxy, !proxy.enabled);
            this.message(null, false);
        }
    }

    private float ping(Proxy proxy, float right, float y, float h, float a) {
        float column = right - 96.0f * this.frame.scale();
        if (ProxyChecker.get().isChecking(proxy)) {
            int dots = (int)(this.frame.time() * 3.0f) % 4;
            this.tag("Checking" + ".".repeat(dots) + " ".repeat(3 - dots), -7372954, right, y, h, a);
        } else if (proxy.ping >= 0) {
            this.tag(proxy.ping + " ms", -6501498, right, y, h, a);
        } else if (proxy.ping == -1) {
            this.tag("Dead", -1733516, right, y, h, a);
        }
        return column;
    }

    private static String line(Proxy proxy) {
        String login;
        String string = proxy.username.isEmpty() ? "" : (login = proxy.username + (String)(proxy.password.isEmpty() ? "" : ":" + proxy.password) + "@");
        String scheme = proxy.type == ProxyType.SOCKS4 ? "socks4://" : (login.isEmpty() ? "" : "socks5://");
        return scheme + login + proxy.displayAddress();
    }

    private void imported(int added, String none) {
        this.message((String)(added > 0 ? "Added " + added + (added == 1 ? " proxy" : " proxies") : none), added == 0);
    }

    private void importFile() {
        this.fileDialog = true;
        Thread thread = new Thread(() -> {
            String chosen = null;
            try {
                chosen = TinyFileDialogs.tinyfd_openFileDialog((CharSequence)"Import proxies", null, null, null, (boolean)false);
            }
            finally {
                String path = chosen;
                class_310.method_1551().execute(() -> {
                    this.fileDialog = false;
                    if (path != null) {
                        this.importFrom(Path.of(path, new String[0]));
                    }
                });
            }
        }, "Anubis proxy import");
        thread.setDaemon(true);
        thread.start();
    }

    private void importFrom(Path file) {
        try {
            this.imported(Proxies.get().importText(Files.readString(file)), "No proxies found in " + String.valueOf(file.getFileName()));
        }
        catch (IOException e) {
            this.message("Couldn't read " + String.valueOf(file.getFileName()), true);
        }
    }

    private void message(String text, boolean error) {
        this.proxyMessage = text;
        this.proxyError = error;
    }

    private static String route() {
        Proxy proxy = Proxies.get().enabled();
        if (proxy == null) {
            return "";
        }
        return String.valueOf((Object)proxy.type) + " " + proxy.displayAddress() + " " + proxy.username + " " + proxy.password;
    }

    private void entry(ImFont nameFont, ImFont detailFont, float x, float y, float h, float right, String name, String detail, float a) {
        float nh = this.frame.textHeight(nameFont);
        float gap = 3.0f * this.frame.scale();
        boolean oneLine = detail.isEmpty();
        float ty = oneLine ? y + (h - nh) * 0.5f : y + (h - nh - gap - this.frame.textHeight(detailFont)) * 0.5f;
        this.frame.text(this.frame.bg(), nameFont, x, ty, -923950, a, DuskWidgets.fit(nameFont, name, right - x));
        if (!oneLine) {
            this.frame.text(this.frame.bg(), detailFont, x, ty + nh + gap, -7372954, a, DuskWidgets.fit(detailFont, detail, right - x));
        }
    }

    private float tag(String text, int color, float right, float y, float h, float a) {
        ImFont font = this.frame.bodyFont();
        float x = right - this.frame.textWidth(font, text);
        this.frame.text(this.frame.bg(), font, x, y + (h - this.frame.textHeight(font)) * 0.5f, color, a, text);
        return x - 14.0f * this.frame.scale();
    }

    private float removeCross(Key key, float right, float y, float h, float k, float s, float a) {
        boolean over;
        ImDrawList dl = this.frame.bg();
        if (this.armed(key)) {
            ImFont font = this.frame.bodyFont();
            String text = "Remove?";
            float tw = this.frame.textWidth(font, text);
            float x = right - tw;
            float th = this.frame.textHeight(font);
            boolean over2 = this.ui.hovered(x - 6.0f * s, y, tw + 12.0f * s, h);
            if (this.ui.pressed(x - 6.0f * s, y, tw + 12.0f * s, h)) {
                this.armed = null;
                return -1.0f;
            }
            this.frame.text(dl, font, x, y + (h - th) * 0.5f, -1733516, over2 ? a : 0.85f * a, text);
            return x - 14.0f * s;
        }
        float size = 28.0f * s;
        float x = right - size;
        float cy = y + h * 0.5f;
        boolean bl = over = k > 0.5f && this.ui.hovered(x, cy - size * 0.5f, size, size);
        if (over && this.ui.pressed(x, cy - size * 0.5f, size, size)) {
            this.confirm(key);
        }
        float kc = this.ui.hover(key, over);
        if (k > 0.01f) {
            float cx = x + size * 0.5f;
            float c = 5.0f * s;
            float line = Math.max(1.2f, 1.5f * s);
            int color = EgyptDraw.col(EgyptDraw.mix(-7372954, -1733516, kc), k * a);
            dl.addLine(cx - c, cy - c, cx + c, cy + c, color, line);
            dl.addLine(cx - c, cy + c, cx + c, cy - c, color, line);
        }
        return x - 6.0f * s;
    }

    private boolean checkbox(Object key, boolean checked, float x, float cy, float s, float a) {
        ImDrawList dl = this.frame.bg();
        float reach = 8.0f * s;
        float size = 16.0f * s;
        float y = cy - size * 0.5f;
        boolean over = this.ui.hovered(x - reach, y - reach, size + 2.0f * reach, size + 2.0f * reach);
        boolean clicked = over && this.ui.pressed(x - reach, y - reach, size + 2.0f * reach, size + 2.0f * reach);
        float k = this.ui.hover(key, over);
        if (checked) {
            dl.addRectFilled(x, y, x + size, y + size, EgyptDraw.col(-871878, a));
            dl.pathClear();
            dl.pathLineTo(x + size * 0.22f, y + size * 0.52f);
            dl.pathLineTo(x + size * 0.42f, y + size * 0.72f);
            dl.pathLineTo(x + size * 0.78f, y + size * 0.3f);
            dl.pathStroke(EgyptDraw.col(-16777216, a), 0, Math.max(1.4f, 2.0f * s));
        } else {
            dl.addRectFilled(x, y, x + size, y + size, EgyptDraw.col(-16777216, 0.3f * a));
            dl.addRect(x, y, x + size, y + size, EgyptDraw.col(EgyptDraw.mix(-923950, -871878, k), (0.3f + 0.4f * k) * a), 0.0f, 0, Math.max(1.0f, s));
        }
        return clicked;
    }

    private float chipWidth(String label, int count) {
        return this.frame.textWidth(this.frame.bodyFont(), label) + 7.0f * this.frame.scale() + this.frame.textWidth(this.frame.bodyFont(), Integer.toString(count));
    }

    private boolean chip(Object key, String label, int count, boolean on, float x, float y, float a) {
        float h;
        float w;
        ImDrawList dl = this.frame.bg();
        ImFont font = this.frame.bodyFont();
        float s = this.frame.scale();
        boolean over = this.ui.hovered(x - 4.0f * s, y, (w = this.chipWidth(label, count)) + 8.0f * s, h = 30.0f * s);
        boolean clicked = over && this.ui.pressed(x - 4.0f * s, y, w + 8.0f * s, h);
        float k = this.ui.hover(key, over);
        float ty = y + (h - this.frame.textHeight(font)) * 0.5f;
        this.frame.text(dl, font, x, ty, on ? -1326998 : EgyptDraw.mix(-7372954, -923950, k), a, label);
        float lw = this.frame.textWidth(font, label) + 7.0f * s;
        this.frame.text(dl, font, x + lw, ty, -7372954, 0.8f * a, Integer.toString(count));
        return clicked;
    }

    private boolean box(Object key, String label, float x, float y, float w, float h, boolean enabled, float a) {
        ImDrawList dl = this.frame.bg();
        float s = this.frame.scale();
        float alpha = enabled ? a : 0.5f * a;
        boolean over = enabled && this.ui.hovered(x, y, w, h);
        boolean clicked = over && this.ui.pressed(x, y, w, h);
        float k = this.ui.hover(key, over);
        dl.addRectFilled(x, y, x + w, y + h, EgyptDraw.col(-16777216, 0.35f * alpha));
        if (k > 0.01f) {
            dl.addRectFilled(x, y, x + w, y + h, EgyptDraw.col(-871878, 0.08f * k * alpha));
        }
        dl.addRect(x, y, x + w, y + h, EgyptDraw.col(EgyptDraw.mix(-923950, -871878, k), (0.2f + 0.5f * k) * alpha), 0.0f, 0, Math.max(1.0f, s));
        ImFont font = this.frame.bodyFont();
        this.frame.text(dl, font, x + (w - this.frame.textWidth(font, label)) * 0.5f, y + (h - this.frame.textHeight(font)) * 0.5f, EgyptDraw.mix(-923950, -11654, k), alpha, label);
        return clicked;
    }

    private boolean pasteButton(float x, float y, float size, float a) {
        ImDrawList dl = this.frame.bg();
        float s = this.frame.scale();
        boolean over = this.ui.hovered(x, y, size, size);
        boolean clicked = over && this.ui.pressed(x, y, size, size);
        float k = this.ui.hover("paste", over);
        dl.addRectFilled(x, y, x + size, y + size, EgyptDraw.col(-16777216, 0.35f * a));
        if (k > 0.01f) {
            dl.addRectFilled(x, y, x + size, y + size, EgyptDraw.col(-871878, 0.08f * k * a));
        }
        dl.addRect(x, y, x + size, y + size, EgyptDraw.col(EgyptDraw.mix(-923950, -871878, k), (0.12f + 0.5f * k) * a), 0.0f, 0, Math.max(1.0f, s));
        float cx = x + size * 0.5f;
        float cy = y + size * 0.5f + s;
        float line = Math.max(1.2f, 1.5f * s);
        int color = EgyptDraw.col(EgyptDraw.mix(-7372954, -11654, k), a);
        dl.addRect(cx - 7.0f * s, cy - 8.0f * s, cx + 7.0f * s, cy + 9.0f * s, color, 2.0f * s, 0, line);
        dl.addRectFilled(cx - 3.5f * s, cy - 10.5f * s, cx + 3.5f * s, cy - 6.0f * s, color, s);
        dl.addLine(cx - 3.5f * s, cy - 1.0f * s, cx + 3.5f * s, cy - 1.0f * s, color, line);
        dl.addLine(cx - 3.5f * s, cy + 3.5f * s, cx + 2.0f * s, cy + 3.5f * s, color, line);
        return clicked;
    }

    private boolean link(Object key, String text, float x, float y, boolean enabled, float a) {
        return this.link(key, text, x, y, enabled, a, -7372954);
    }

    private boolean link(Object key, String text, float x, float y, boolean enabled, float a, int color) {
        ImFont font = this.frame.bodyFont();
        float s = this.frame.scale();
        float w = this.frame.textWidth(font, text);
        float h = this.frame.textHeight(font);
        boolean over = enabled && this.ui.hovered(x, y - 4.0f * s, w, h + 8.0f * s);
        boolean clicked = over && this.ui.pressed(x, y - 4.0f * s, w, h + 8.0f * s);
        float k = this.ui.hover(key, over);
        float alpha = enabled ? a : 0.4f * a;
        this.frame.text(this.frame.bg(), font, x, y, EgyptDraw.mix(color, -923950, k), alpha, text);
        if (k > 0.01f) {
            this.frame.bg().addLine(x, y + h + s, x + w * k, y + h + s, EgyptDraw.col(-923950, 0.6f * k * alpha), Math.max(1.0f, s));
        }
        return clicked;
    }

    private void statusRight(String status, boolean error, float left, float right, float y, float a) {
        ImFont font = this.frame.bodyFont();
        String shown = DuskWidgets.fit(font, status, right - left);
        this.text(shown, right - this.frame.textWidth(font, shown), y, error ? -1733516 : -7372954, a);
    }

    private void text(String text, float x, float y, int color, float a) {
        this.frame.text(this.frame.bg(), this.frame.bodyFont(), x, y, color, a, text);
    }

    private void centered(String text, float x, float y, float w, float h, float a) {
        ImFont font = this.frame.bodyFont();
        this.frame.text(this.frame.bg(), font, x + (w - this.frame.textWidth(font, text)) * 0.5f, y + (h - this.frame.textHeight(font)) * 0.5f, -7372954, a, text);
    }

    private void drawTooltip() {
        String text = this.frame.pendingTooltip();
        if (text == null) {
            return;
        }
        ImDrawList dl = this.frame.fg();
        ImFont font = this.frame.detailFont();
        float s = this.frame.scale();
        float w = this.frame.textWidth(font, text) + 18.0f * s;
        float h = this.frame.textHeight(font) + 10.0f * s;
        float x = Math.max(6.0f * s, Math.min(this.frame.width() - w - 6.0f * s, this.frame.tooltipX() - w * 0.5f));
        float y = this.frame.tooltipY();
        dl.addRectFilled(x, y, x + w, y + h, EgyptDraw.col(-16053748, 0.96f));
        dl.addRect(x, y, x + w, y + h, EgyptDraw.col(-923950, 0.14f), 0.0f, 0, Math.max(1.0f, s));
        this.frame.text(dl, font, x + 9.0f * s, y + 5.0f * s, -923950, 1.0f, text);
    }

    private boolean armed(Object key) {
        return key.equals(this.armed) && this.frame.time() < this.armedUntil;
    }

    private boolean confirm(Object key) {
        if (this.armed(key)) {
            this.armed = null;
            return true;
        }
        this.armed = key;
        this.armedUntil = this.frame.time() + 3.0f;
        return false;
    }

    static void proxyDot(ImDrawList dl, float cx, float cy, boolean on, float s, float a) {
        if (on) {
            dl.addCircleFilled(cx, cy, 7.0f * s, EgyptDraw.col(-871878, 0.16f * a), 20);
            dl.addCircleFilled(cx, cy, 3.5f * s, EgyptDraw.col(-871878, a), 12);
        } else {
            dl.addCircle(cx, cy, 3.5f * s, EgyptDraw.col(-7372954, 0.8f * a), 12, Math.max(1.0f, s));
        }
    }

    @Environment(value=EnvType.CLIENT)
    public static enum Tab {
        ACCOUNTS,
        PROXIES;

    }

    @Environment(value=EnvType.CLIENT)
    private static enum Filter {
        ALL("All"),
        WORKING("Working"),
        DEAD("Dead"),
        UNCHECKED("Unchecked");

        final String label;

        private Filter(String label) {
            this.label = label;
        }

        boolean matches(Proxy proxy) {
            return switch (this.ordinal()) {
                default -> throw new MatchException(null, null);
                case 0 -> true;
                case 1 -> {
                    if (proxy.ping >= 0) {
                        yield true;
                    }
                    yield false;
                }
                case 2 -> {
                    if (proxy.ping == -1) {
                        yield true;
                    }
                    yield false;
                }
                case 3 -> proxy.ping == -2;
            };
        }
    }

    @Environment(value=EnvType.CLIENT)
    private static enum Sort {
        ADDED("Added", null),
        PING("Ping", Comparator.comparingInt(ConnectionsPanel::pingOrder)),
        NAME("Name", Comparator.comparing(proxy -> proxy.name.toLowerCase(Locale.ROOT)));

        final String label;
        final Comparator<Proxy> order;

        private Sort(String label, Comparator<Proxy> order) {
            this.label = label;
            this.order = order;
        }
    }

    @Environment(value=EnvType.CLIENT)
    private record Key(Object owner, String what) {
    }
}

