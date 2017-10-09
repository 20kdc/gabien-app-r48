/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui;

import gabien.IGrInDriver;
import gabien.ui.*;
import r48.AppMain;
import r48.FontSizes;

/**
 * Notably, despite the name of "uiGridScaleTenths", this does NOT actually do the grid size adjustment.
 * It's too particular for that.
 * Created on 12/28/16.
 */
public class UIGrid extends UIPanel {
    public int tileCount;
    public int tileSizeW, tileSizeH;

    public int bkgR = 0;
    public int bkgG = 0;
    public int bkgB = 0;

    // Region selection.
    public boolean canMultiSelect = false;
    private int selTile = 0;
    public int selWidth = 1;
    public int selHeight = 1;

    private int tmWidth = 8;

    public UIScrollbar uivScrollbar = new UIScrollbar(true, FontSizes.gridScrollersize);

    public Runnable onSelectionChange = null;

    public UIGrid(int tSizeW, int tSizeH, int tCount) {
        tileSizeW = tSizeW;
        tileSizeH = tSizeH;
        tileCount = tCount;
        allElements.add(uivScrollbar);
        setBounds(new Rect(0, 0, 320, 200));
    }

    private int getScrollOffset() {
        if (tmWidth <= 0)
            return 0;
        int totalRows = tileCount;
        if (totalRows % tmWidth > 0)
            totalRows += tmWidth;
        totalRows /= tmWidth;
        int screenRows = getBounds().height / tileSizeH;
        int extraRows = totalRows - screenRows;
        return ((int) Math.floor((uivScrollbar.scrollPoint * extraRows) + 0.5)) * tmWidth;
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
        Rect r = getBounds();
        igd.clearRect(bkgR, bkgG, bkgB, ox, oy, r.width, r.height);
        super.updateAndRender(ox, oy, deltaTime, selected, igd);

        if (tmWidth <= 0)
            return;

        int mouseSel = -1;
        int mouseX = igd.getMouseX() - ox;
        int mouseY = igd.getMouseY() - oy;
        if (new Rect(0, 0, tileSizeW * tmWidth, r.height).contains(mouseX, mouseY)) {
            int tx = UIElement.sensibleCellDiv(mouseX, tileSizeW);
            int ty = UIElement.sensibleCellDiv(mouseY, tileSizeH);
            mouseSel = tx + (ty * tmWidth) + getScrollOffset();
        }

        int pi = 0;
        int visibleTiles = (((r.height + (tileSizeH - 1)) / tileSizeH) * tmWidth);
        int scrollOffset = getScrollOffset();
        for (int p = scrollOffset; p < scrollOffset + visibleTiles; p++) {
            int px = ((pi % tmWidth) * tileSizeW);
            int py = (UIGrid.sensibleCellDiv(pi, tmWidth) * tileSizeH);
            if (py >= r.height)
                break;
            if ((p < 0) || (p >= tileCount)) {
                pi++;
                // error
                igd.clearRect(128, 0, 0, ox + px, oy + py, tileSizeW, tileSizeH);
                continue;
            }
            if (p == selTile)
                igd.clearRect(128, 0, 128, ox + px, oy + py, tileSizeW, tileSizeH);
            drawTile(p, p == mouseSel, ox + px, oy + py, igd);
            if (p == selTile)
                Art.drawSelectionBox(ox + px, oy + py, tileSizeW, tileSizeH, FontSizes.getSpriteScale(), igd);
            pi++;
        }
        for (int ty = 0; ty < selHeight; ty++) {
            pi = (selTile - getScrollOffset()) + (ty * tmWidth);
            for (int tx = 0; tx < selWidth; tx++) {
                int px = (pi % tmWidth) * tileSizeW;
                int py = UIGrid.sensibleCellDiv(pi, tmWidth) * tileSizeH;
                if (py < 0)
                    continue;
                if (py >= r.height)
                    continue;
                Art.drawSelectionBox(ox + px, oy + py, tileSizeW, tileSizeH, FontSizes.getSpriteScale(), igd);
                pi++;
            }
        }
    }

    protected void drawTile(int t, boolean hover, int x, int y, IGrInDriver igd) {
        UILabel.drawString(igd, x, y, Integer.toHexString(t), false, FontSizes.gridTextHeight);
    }

    @Override
    public void setBounds(Rect r) {
        int scrollBarW = uivScrollbar.getBounds().width;
        int tiles = (r.width - scrollBarW) / tileSizeW;
        if (tiles < 2)
            tiles = 2;
        int availableRows = r.height / tileSizeH;
        if (availableRows < 1)
            availableRows = 1;
        tmWidth = tiles;
        uivScrollbar.setBounds(new Rect(r.width - scrollBarW, 0, scrollBarW, availableRows * tileSizeH));
        super.setBounds(new Rect(r.x, r.y, r.width, availableRows * tileSizeH));
    }

    @Override
    public void handleClick(int x, int y, int button) {
        if (x < tileSizeW * tmWidth) {
            int tx = UIElement.sensibleCellDiv(x, tileSizeW);
            int ty = UIElement.sensibleCellDiv(y, tileSizeH);
            selTile = tx + (ty * tmWidth) + getScrollOffset();
            selWidth = 1;
            selHeight = 1;
            selectionChanged();
        }
        super.handleClick(x, y, button);
    }

    @Override
    public void handleDrag(int x, int y) {
        if (tmWidth <= 0) {
            super.handleDrag(x, y);
            return;
        }
        if (x < tileSizeW * tmWidth) {
            if (!canMultiSelect)
                return;
            int tx = UIElement.sensibleCellDiv(x, tileSizeW);
            int ty = UIElement.sensibleCellDiv(y, tileSizeH) + (getScrollOffset() / tmWidth);
            int ox = selTile % tmWidth;
            int oy = selTile / tmWidth;
            selWidth = (tx - ox) + 1;
            if (selWidth < 1)
                selWidth = 1;
            selHeight = (ty - oy) + 1;
            if (selHeight < 1)
                selHeight = 1;
            selectionChanged();
        }
        super.handleDrag(x, y);
    }

    @Override
    public void handleMousewheel(int x, int y, boolean north) {
        uivScrollbar.handleMousewheel(x, y, north);
    }

    private void selectionChanged() {
        if (selTile < 0)
            selTile = 0;
        if (selTile >= tileCount)
            selTile = tileCount - 1;
        if (onSelectionChange != null)
            onSelectionChange.run();
    }

    public int getSelectStride() {
        return tmWidth;
    }

    public int getSelected() {
        return selTile;
    }

    public void setSelected(int i) {
        if (i < 0)
            i = 0;
        if (i >= tileCount)
            i = tileCount - 1;
        selTile = i;
        selWidth = 1;
        selHeight = 1;
        selectionChanged();
        // work out a general estimate
        uivScrollbar.scrollPoint = i / (double) tileCount;
    }
}
