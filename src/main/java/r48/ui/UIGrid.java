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
 * Created on 12/28/16.
 */
public class UIGrid extends UIPanel {
    public int tileCount;
    public int tileSize;

    public int bkgR = 0;
    public int bkgG = 0;
    public int bkgB = 0;

    // Region selection.
    public boolean canMultiSelect = false;
    private int selTile = 0;
    public int selWidth = 1;
    public int selHeight = 1;

    private int tmWidth = 8;

    public UIScrollbar uivScrollbar = new UIScrollbar(true);

    public Runnable onSelectionChange = null;

    public UIGrid(int tSize, int tCount) {
        tileSize = tSize;
        tileCount = tCount;
        allElements.add(uivScrollbar);
        setBounds(new Rect(0, 0, 320, 200));
    }

    private int getScrollOffset() {
        int totalRows = tileCount;
        if (totalRows % tmWidth > 0)
            totalRows += tmWidth;
        totalRows /= tmWidth;
        int screenRows = getBounds().height / tileSize;
        int extraRows = totalRows - screenRows;
        return ((int) Math.floor((uivScrollbar.scrollPoint * extraRows) + 0.5)) * tmWidth;
    }

    @Override
    public void updateAndRender(int ox, int oy, double deltaTime, boolean selected, IGrInDriver igd) {
        Rect r = getBounds();
        igd.clearRect(bkgR, bkgG, bkgB, ox, oy, r.width, r.height);
        super.updateAndRender(ox, oy, deltaTime, selected, igd);

        int mouseSel = -1;
        int mouseX = igd.getMouseX() - ox;
        int mouseY = igd.getMouseY() - oy;
        if (new Rect(0, 0, tileSize * tmWidth, r.height).contains(mouseX, mouseY)) {
            int tx = UIElement.sensibleCellDiv(mouseX, tileSize);
            int ty = UIElement.sensibleCellDiv(mouseY, tileSize);
            mouseSel = tx + (ty * tmWidth) + getScrollOffset();
        }

        int pi = 0;
        int visibleTiles = (((r.height + (tileSize - 1)) / tileSize) * tmWidth);
        int scrollOffset = getScrollOffset();
        for (int p = scrollOffset; p < scrollOffset + visibleTiles; p++) {
            int px = ((pi % tmWidth) * tileSize);
            int py = (UIGrid.sensibleCellDiv(pi, tmWidth) * tileSize);
            if (py >= r.height)
                break;
            if ((p < 0) || (p >= tileCount)) {
                pi++;
                // error
                igd.clearRect(128, 0, 0, ox + px, oy + py, tileSize, tileSize);
                continue;
            }
            if (p == selTile)
                igd.clearRect(128, 0, 128, ox + px, oy + py, tileSize, tileSize);
            drawTile(p, p == mouseSel, ox + px, oy + py, igd);
            if (p == selTile)
                igd.blitImage(36, 0, tileSize, tileSize, ox + px, oy + py, AppMain.layerTabs);
            pi++;
        }
        for (int ty = 0; ty < selHeight; ty++) {
            pi = (selTile - getScrollOffset()) + (ty * tmWidth);
            for (int tx = 0; tx < selWidth; tx++) {
                int px = (pi % tmWidth) * tileSize;
                int py = UIGrid.sensibleCellDiv(pi, tmWidth) * tileSize;
                if (py < 0)
                    continue;
                if (py >= r.height)
                    continue;
                igd.blitImage(36, 0, tileSize, tileSize, ox + px, oy + py, AppMain.layerTabs);
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
        int tiles = r.width / tileSize;
        if (tiles < 2)
            tiles = 2;
        int availableRows = r.height / tileSize;
        if (availableRows < 1)
            availableRows = 1;
        tmWidth = tiles;
        tmWidth -= (scrollBarW + (tileSize - 1)) / tileSize;
        Rect b = getBounds();
        uivScrollbar.setBounds(new Rect(b.width - scrollBarW, 0, scrollBarW, availableRows * tileSize));
        super.setBounds(new Rect(r.x, r.y, tiles * tileSize, availableRows * tileSize));
    }

    @Override
    public void handleClick(int x, int y, int button) {
        if (x < tileSize * tmWidth) {
            int tx = UIElement.sensibleCellDiv(x, tileSize);
            int ty = UIElement.sensibleCellDiv(y, tileSize);
            selTile = tx + (ty * tmWidth) + getScrollOffset();
            selWidth = 1;
            selHeight = 1;
            selectionChanged();
        }
        super.handleClick(x, y, button);
    }

    @Override
    public void handleDrag(int x, int y) {
        if (x < tileSize * tmWidth) {
            if (!canMultiSelect)
                return;
            int tx = UIElement.sensibleCellDiv(x, tileSize);
            int ty = UIElement.sensibleCellDiv(y, tileSize) + (getScrollOffset() / tmWidth);
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
            return;
        if (i >= tileCount)
            return;
        selTile = i;
        selWidth = 1;
        selHeight = 1;
        selectionChanged();
        // work out a general estimate
        double p = i / (double) tileCount;
        uivScrollbar.scrollPoint = p;
    }
}
