/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  imgui.ImDrawList
 *  imgui.ImFont
 *  net.fabricmc.api.EnvType
 *  net.fabricmc.api.Environment
 *  net.minecraft.class_310
 *  net.minecraft.class_746
 */
package net.anubis.client.gui;

import imgui.ImDrawList;
import imgui.ImFont;
import net.anubis.client.gui.DonutRegionMapData;
import net.anubis.client.gui.GuiPalette;
import net.anubis.client.gui.HudChrome;
import net.anubis.client.gui.HudLayoutStore;
import net.anubis.client.gui.HudRenderer;
import net.anubis.client.imgui.ImGuiManager;
import net.anubis.client.module.impl.client.RegionMapModule;
import net.anubis.client.module.setting.ColorSetting;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.class_310;
import net.minecraft.class_746;

@Environment(value=EnvType.CLIENT)
public final class RegionMapRenderer {
    private static final float PAD = 10.0f;
    private static final float INFO_GAP = 8.0f;
    private static final float INFO_LINE_H = 22.0f;
    private static final float LEGEND_GAP = 4.0f;
    private static final float LEGEND_ROW_H = 16.0f;
    private static final float LEGEND_SWATCH = 9.0f;
    private static final float LABEL_GAP = 8.0f;
    private static final String MINUS = "\u2212";
    private static final String COORDINATE_LABEL = "Coordinate";
    private static final String REGION_LABEL = "Region";

    private RegionMapRenderer() {
    }

    public static Size measureSize(RegionMapModule module, float scale) {
        float gridSize = RegionMapRenderer.gridSize(module, scale);
        float pad = 10.0f * scale;
        float contentW = Math.max(gridSize, RegionMapRenderer.measureInfoWidth(module, scale));
        return new Size(Math.round(contentW + pad * 2.0f), Math.round(gridSize + pad * 2.0f + 8.0f * scale + RegionMapRenderer.measureInfoHeight(module) * scale));
    }

    private static float gridSize(RegionMapModule module, float scale) {
        float cellSize = module.cellSize() * scale;
        float cellGap = module.cellGap() * scale;
        return 9.0f * (cellSize + cellGap) - cellGap;
    }

    private static float measureInfoWidth(RegionMapModule module, float scale) {
        ImGuiManager m = ImGuiManager.getInstance();
        int textPx = HudChrome.px(17.0f, scale);
        int labelPx = HudChrome.px(14.0f, scale);
        float labelCol = RegionMapRenderer.labelColumn(module, m.hudLabelFont(), labelPx) + 8.0f * scale;
        float values = module.showCoordinates() ? HudChrome.tabularWidth(m.hudTextFont(), textPx, "\u2212000000, \u2212000000") : 0.0f;
        float legend = 0.0f;
        for (String name : DonutRegionMapData.regionTypeNames()) {
            values = Math.max(values, HudChrome.width(m.hudTextFont(), textPx, "000  \u00b7  " + name));
            legend = Math.max(legend, 15.0f * scale + HudChrome.width(m.hudLabelFont(), labelPx, name));
        }
        values = Math.max(values, HudChrome.width(m.hudTextFont(), textPx, "000  \u00b7  Unknown"));
        return (float)Math.ceil(Math.max(labelCol + values, module.showLegend() ? legend : 0.0f));
    }

    public static void render(ImDrawList dl, RegionMapModule module, HudLayoutStore.HudElementState layout) {
        RegionMapRenderer.render(dl, module, layout, 1.0f);
    }

    public static void render(ImDrawList dl, RegionMapModule module, HudLayoutStore.HudElementState layout, float fade) {
        class_310 mc = class_310.method_1551();
        class_746 player = mc.field_1724;
        if (player == null || dl == null || module == null || layout == null) {
            return;
        }
        float scale = HudRenderer.scaleOf(layout);
        float cellSize = module.cellSize() * scale;
        float cellGap = module.cellGap() * scale;
        float pad = 10.0f * scale;
        int mapSize = 9;
        float gridSize = RegionMapRenderer.gridSize(module, scale);
        float x = Math.round(layout.x);
        float y = Math.round(layout.y);
        float r = HudRenderer.hudRounding() * scale;
        float alpha = module.opacity() * fade;
        HudChrome.card(dl, HudRenderer.hudSurface(layout), x, y, layout.width, layout.height, r, alpha, module.shadow(), scale);
        float gridX = Math.round(x + (layout.width - gridSize) * 0.5f);
        float gridY = y + pad;
        float cellR = Math.min(Math.max(2.0f * scale, r - pad), cellSize * 0.3f);
        for (int row = 0; row < mapSize; ++row) {
            for (int col = 0; col < mapSize; ++col) {
                DonutRegionMapData.RegionInfo info = DonutRegionMapData.regionInfo(row * mapSize + col);
                if (info == null) continue;
                float cx = gridX + (float)col * (cellSize + cellGap);
                float cy = gridY + (float)row * (cellSize + cellGap);
                int color = HudChrome.scaleAlpha(DonutRegionMapData.regionColor(info.regionType), alpha);
                dl.addRectFilled(cx, cy, cx + cellSize, cy + cellSize, color, cellR);
                if (!module.showRegionIds()) continue;
                RegionMapRenderer.drawCenteredCellText(dl, info.regionId, cx, cy, cellSize, module.textScale(), alpha, scale);
            }
        }
        RegionMapRenderer.renderPlayerPin(dl, module, player, gridX, gridY, cellSize, cellGap, alpha, scale);
        RegionMapRenderer.renderInfoPanel(dl, module, player, x + pad, gridY + gridSize + 8.0f * scale, alpha, scale);
    }

    private static void drawCenteredCellText(ImDrawList dl, int regionId, float cx, float cy, float size, float textScale, float alpha, float scale) {
        ImFont font = ImGuiManager.getInstance().hudLabelFont();
        int fontPx = Math.max(8, HudChrome.px(14.0f * textScale * 0.85f, scale));
        String label = String.valueOf(regionId);
        float tx = cx + (size - HudChrome.width(font, fontPx, label)) * 0.5f;
        float ty = cy + (size - (float)fontPx) * 0.5f;
        HudChrome.text(dl, font, fontPx, tx, ty, HudChrome.scaleAlpha(HudChrome.TEXT_VALUE, alpha), label, true);
    }

    private static void renderPlayerPin(ImDrawList dl, RegionMapModule module, class_746 player, float gridX, float gridY, float cellSize, float cellGap, float alpha, float scale) {
        double pz;
        double px = player.method_23317();
        int[] grid = DonutRegionMapData.worldToGrid(px, pz = player.method_23321());
        if (grid[0] < 0 || grid[0] >= 9 || grid[1] < 0 || grid[1] >= 9) {
            return;
        }
        double[] cellPos = DonutRegionMapData.worldToCellPosition(px, pz);
        float centerX = gridX + (float)grid[0] * (cellSize + cellGap) + (float)(cellPos[0] * (double)cellSize);
        float centerY = gridY + (float)grid[1] * (cellSize + cellGap) + (float)(cellPos[1] * (double)cellSize);
        double angle = Math.toRadians((double)(-player.method_36454()) - 90.0);
        float arrowSize = 9.0f * module.playerScale() * scale;
        int color = HudChrome.scaleAlpha(RegionMapRenderer.toAbgr(module.playerIndicatorColor()), alpha);
        float tipX = centerX + (float)(Math.cos(angle) * (double)arrowSize);
        float tipY = centerY - (float)(Math.sin(angle) * (double)arrowSize);
        float leftX = centerX + (float)(Math.cos(angle + Math.toRadians(135.0)) * (double)arrowSize);
        float leftY = centerY - (float)(Math.sin(angle + Math.toRadians(135.0)) * (double)arrowSize);
        float rightX = centerX + (float)(Math.cos(angle - Math.toRadians(135.0)) * (double)arrowSize);
        float rightY = centerY - (float)(Math.sin(angle - Math.toRadians(135.0)) * (double)arrowSize);
        dl.addTriangleFilled(tipX, tipY, leftX, leftY, rightX, rightY, color);
        dl.addTriangle(tipX, tipY, leftX, leftY, rightX, rightY, HudChrome.scaleAlpha(-1728053248, alpha), 1.0f);
    }

    private static void renderInfoPanel(ImDrawList dl, RegionMapModule module, class_746 player, float x, float y, float alpha, float scale) {
        ImGuiManager m = ImGuiManager.getInstance();
        ImFont textFont = m.hudTextFont();
        ImFont labelFont = m.hudLabelFont();
        int textPx = HudChrome.px(17.0f, scale);
        int labelPx = HudChrome.px(14.0f, scale);
        float labelOffset = HudChrome.baselineOffset(textFont, textPx, labelFont, labelPx);
        float valueX = x + RegionMapRenderer.labelColumn(module, labelFont, labelPx) + 8.0f * scale;
        float lineH = 22.0f * scale;
        float textInset = (float)Math.floor((lineH - (float)textPx) * 0.5f);
        float lineTop = y + textInset;
        if (module.showCoordinates()) {
            HudChrome.text(dl, labelFont, labelPx, x, lineTop + labelOffset, HudChrome.scaleAlpha(HudChrome.TEXT_LABEL, alpha), COORDINATE_LABEL, false);
            HudChrome.tabular(dl, textFont, textPx, valueX, lineTop, HudChrome.scaleAlpha(HudChrome.TEXT_VALUE, alpha), RegionMapRenderer.number((long)Math.floor(player.method_23317())) + ", " + RegionMapRenderer.number((long)Math.floor(player.method_23321())), false);
            lineTop += lineH;
        }
        int regionId = DonutRegionMapData.regionAt(player.method_23317(), player.method_23321());
        String regionName = DonutRegionMapData.regionTypeName(player.method_23317(), player.method_23321());
        HudChrome.text(dl, labelFont, labelPx, x, lineTop + labelOffset, HudChrome.scaleAlpha(HudChrome.TEXT_LABEL, alpha), REGION_LABEL, false);
        HudChrome.text(dl, textFont, textPx, valueX, lineTop, HudChrome.scaleAlpha(HudChrome.TEXT_NAME, alpha), regionId + "  \u00b7  " + regionName, false);
        if (!module.showLegend()) {
            return;
        }
        float legendY = y + lineH * (float)RegionMapRenderer.infoLines(module) + 4.0f * scale;
        float rowH = 16.0f * scale;
        float swatch = 9.0f * scale;
        String[] names = DonutRegionMapData.regionTypeNames();
        int[] colors = DonutRegionMapData.regionTypeColors();
        for (int i = 0; i < names.length; ++i) {
            float rowY = legendY + (float)i * rowH;
            float swatchY = rowY + (rowH - swatch) * 0.5f;
            dl.addRectFilled(x, swatchY, x + swatch, swatchY + swatch, HudChrome.scaleAlpha(colors[i], alpha), 2.0f * scale);
            HudChrome.text(dl, labelFont, labelPx, x + swatch + 6.0f * scale, rowY + (float)Math.floor((rowH - (float)labelPx) * 0.5f), HudChrome.scaleAlpha(HudChrome.TEXT_NAME, alpha), names[i], false);
        }
    }

    private static int infoLines(RegionMapModule module) {
        return module.showCoordinates() ? 2 : 1;
    }

    private static float labelColumn(RegionMapModule module, ImFont labelFont, int labelPx) {
        float region = HudChrome.width(labelFont, labelPx, REGION_LABEL);
        return module.showCoordinates() ? Math.max(HudChrome.width(labelFont, labelPx, COORDINATE_LABEL), region) : region;
    }

    private static float measureInfoHeight(RegionMapModule module) {
        float height = 22.0f * (float)RegionMapRenderer.infoLines(module);
        if (module.showLegend()) {
            height += 4.0f + (float)DonutRegionMapData.regionTypeNames().length * 16.0f;
        }
        return height;
    }

    private static String number(long value) {
        return value < 0L ? MINUS + -value : Long.toString(value);
    }

    private static int toAbgr(ColorSetting color) {
        return GuiPalette.abgr(color.argb());
    }

    @Environment(value=EnvType.CLIENT)
    public record Size(float width, float height) {
    }
}

