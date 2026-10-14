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
 *  net.minecraft.class_1041
 *  net.minecraft.class_310
 *  net.minecraft.class_500
 *  net.minecraft.class_5348
 */
package net.anubis.client.gui.mainmenu;

import com.example.client.mixin.AccessorMenusJoinMultiplayerScreen;
import imgui.ImDrawList;
import imgui.ImFont;
import imgui.ImGui;
import imgui.ImGuiIO;
import net.anubis.client.account.Account;
import net.anubis.client.account.Accounts;
import net.anubis.client.gui.EgyptDraw;
import net.anubis.client.gui.mainmenu.ConnectionsPanel;
import net.anubis.client.gui.mainmenu.DuskWidgets;
import net.anubis.client.gui.mainmenu.MenuFrame;
import net.anubis.client.gui.mainmenu.PlayerFaces;
import net.anubis.client.imgui.ImGuiManager;
import net.anubis.client.proxy.Proxies;
import net.anubis.client.proxy.Proxy;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_1041;
import net.minecraft.class_310;
import net.minecraft.class_500;
import net.minecraft.class_5348;

@Environment(value=EnvType.CLIENT)
public final class MultiplayerOverlay {
    private static final int HEADER_HEIGHT = 33;
    private static final float NOWHERE = -10000.0f;
    private static final float BUTTON_H = 62.0f;
    private static final float ICON = 34.0f;
    private static final float MARGIN = 16.0f;
    private final class_500 screen;
    private final ConnectionsPanel panel;
    private final DuskWidgets ui = new DuskWidgets();
    private final MenuFrame frame = new MenuFrame();
    private long shownAt;
    private boolean overButton;

    public MultiplayerOverlay(class_500 screen) {
        this.screen = screen;
        class_310 mc = class_310.method_1551();
        this.panel = new ConnectionsPanel(() -> mc.method_63588(((AccessorMenusJoinMultiplayerScreen)screen)::anubis$refreshServerList));
    }

    public void show() {
        this.shownAt = System.nanoTime();
        ImGuiManager.getInstance().activate(this::render);
    }

    public void hide() {
        ImGuiManager.getInstance().deactivate();
    }

    public boolean takesPointer() {
        return this.overButton || this.panel.isOpen();
    }

    public boolean keyPressed(int key) {
        return this.panel.keyPressed(key);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    private void render() {
        ImGuiIO io = ImGui.getIO();
        float width = io.getDisplaySizeX();
        float height = io.getDisplaySizeY();
        float scale = ImGuiManager.getInstance().uiScale();
        float dt = Math.max(0.001f, io.getDeltaTime());
        boolean free = !this.panel.isOpen();
        this.frame.begin(ImGui.getBackgroundDrawList(), ImGui.getForegroundDrawList(), width, height, scale, (float)(System.nanoTime() - this.shownAt) / 1.0E9f, dt, free ? io.getMousePosX() : -10000.0f, free ? io.getMousePosY() : -10000.0f, free && ImGui.isMouseClicked((int)0));
        this.ui.begin(this.frame);
        try {
            this.header();
        }
        finally {
            this.ui.end();
        }
        this.panel.render(width, height, scale, dt);
    }

    private void header() {
        class_1041 window = class_310.method_1551().method_22683();
        double gui = window.method_4495();
        float s = this.frame.scale();
        float a = this.frame.intro(0.0f);
        float h = 62.0f * s;
        float margin = 16.0f * s;
        float header = (float)(33.0 * gui);
        float y = Math.round((header - h) * 0.5f);
        ImFont font = this.frame.menuFont();
        float w = Math.max(this.frame.textWidth(font, "Accounts"), this.frame.textWidth(font, "Proxies")) + 82.0f * s;
        Proxy proxy = Proxies.get().enabled();
        float x = margin;
        boolean over = this.button(ConnectionsPanel.Tab.ACCOUNTS, "Accounts", null, x, y, w, h, s, a);
        this.overButton = over |= this.button(ConnectionsPanel.Tab.PROXIES, "Proxies", proxy, x + w + 8.0f * s, y, w, h, s, a);
        float titleRight = (float)((double)(window.method_4486() + class_310.method_1551().field_1772.method_27525((class_5348)this.screen.method_25440())) / 2.0 * gui);
        float right = this.frame.width() - margin;
        float room = right - titleRight - 3.0f * margin;
        if (room < 40.0f * s) {
            return;
        }
        ImFont nameFont = this.frame.menuFont();
        ImFont routeFont = this.frame.listFont();
        float lineGap = 4.0f * s;
        float total = this.frame.textHeight(nameFont) + lineGap + this.frame.textHeight(routeFont);
        float ty = Math.round((header - total) * 0.5f);
        String name = DuskWidgets.fit(nameFont, this.frame.displayName(), room);
        String route = DuskWidgets.fit(routeFont, proxy != null ? proxy.displayAddress() : "No proxy", room);
        ImDrawList dl = this.frame.bg();
        this.frame.text(dl, nameFont, right - this.frame.textWidth(nameFont, name), ty, -923950, a, name);
        this.frame.text(dl, routeFont, right - this.frame.textWidth(routeFont, route), ty + this.frame.textHeight(nameFont) + lineGap, proxy != null ? -1326998 : -7372954, a, route);
    }

    private boolean button(ConnectionsPanel.Tab tab, String label, Proxy proxy, float x, float y, float w, float h, float s, float a) {
        ImDrawList dl = this.frame.bg();
        boolean over = this.ui.hovered(x, y, w, h);
        if (this.ui.pressed(x, y, w, h)) {
            this.panel.open(tab);
        }
        float k = this.ui.hover((Object)tab, over);
        dl.addRectFilled(x, y, x + w, y + h, EgyptDraw.col(-15461610, 0.88f * a));
        if (k > 0.01f) {
            dl.addRectFilled(x, y, x + w, y + h, EgyptDraw.col(-923950, 0.06f * k * a));
        }
        dl.addRect(x, y, x + w, y + h, EgyptDraw.col(EgyptDraw.mix(-923950, -871878, k), (0.18f + 0.4f * k) * a), 0.0f, 0, Math.max(1.0f, s));
        float icon = 34.0f * s;
        float ix = x + 16.0f * s;
        float iy = y + (h - icon) * 0.5f;
        if (tab == ConnectionsPanel.Tab.ACCOUNTS) {
            Account active = Accounts.get().active();
            if (active != null) {
                PlayerFaces.draw(dl, active, ix, iy, icon, s, a);
            } else {
                PlayerFaces.draw(dl, class_310.method_1551().method_1548().method_44717(), false, ix, iy, icon, s, a);
            }
        } else {
            ConnectionsPanel.proxyDot(dl, ix + icon * 0.5f, iy + icon * 0.5f, proxy != null, 2.1f * s, a);
        }
        ImFont font = this.frame.menuFont();
        this.frame.text(dl, font, ix + icon + 12.0f * s, y + (h - this.frame.textHeight(font)) * 0.5f, EgyptDraw.mix(-923950, -11654, k), a, label);
        return over;
    }
}

