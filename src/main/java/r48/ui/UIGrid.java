/*
 * This is released into the public domain.
 * No warranty is provided, implied or otherwise.
 */

package r48.ui;

import gabien.IGrInDriver;
import gabien.ui.Rect;
import gabien.ui.UILabel;
import gabien.ui.UIPanel;
import gabien.ui.UIScrollbar;
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
        int pi = 0;
        for (int p = getScrollOffset(); p < tileCount; p++) {
            int px = ((pi % tmWidth) * tileSize);
            int py = ((pi / tmWidth) * tileSize);
            if (py >= r.height)
                break;
            drawTile(p, ox + px, oy + py, igd);
            if (p == selTile)
                igd.blitImage(36, 0, tileSize, tileSize, ox + px, oy + py, AppMain.layerTabs);
            pi++;
        }
        for (int ty = 0; ty < selHeight; ty++) {
            pi = (selTile - getScrollOffset()) + (ty * tmWidth);
            for (int tx = 0; tx < selWidth; tx++) {
                int px = (pi % tmWidth) * tileSize;
                int py = (pi / tmWidth) * tileSize;
                if (py < 0)
                    continue;
                if (py >= r.height)
                    continue;
                igd.blitImage(36, 0, tileSize, tileSize, ox + px, oy + py, AppMain.layerTabs);
                pi++;
            }
        }
    }

    protected void drawTile(int t, int x, int y, IGrInDriver igd) {
        UILabel.drawString(igd, x, y, Integer.toHexString(t), false, FontSizes.gridTextHeight);
    }

    @Override
    public void setBounds(Rect r) {
        setBoundsPass(r);
        setBoundsPass(r);
    }
    private void setBoundsPass(Rect r) {
        int scrollBarW = uivScrollbar.getBounds().width; // wait, this wasn't in UI lib at all. fixed.
        int tiles = r.width / tileSize;
        if (tiles < 2)
            tiles = 2;
        int availableRows = r.height / tileSize;
        if (availableRows < 1)
            availableRows = 1;
        tmWidth = tiles;
        int rows = tileCount;
        if (rows % tiles > 0)
            rows += tiles;
        rows /= tiles;
        allElements.clear();
        if (rows > availableRows) {
            tmWidth -= (scrollBarW + (tileSize - 1)) / tileSize;
            allElements.add(uivScrollbar);
        } else {
            allElements.remove(uivScrollbar);
        }
        Rect b = getBounds();
        uivScrollbar.setBounds(new Rect(b.width - scrollBarW, 0, scrollBarW, availableRows * tileSize));
        super.setBounds(new Rect(r.x, r.y, tiles * tileSize, availableRows * tileSize));
    }

    @Override
    public void handleClick(int x, int y, int button) {
        if (x < tileSize * tmWidth) {
            int tx = x / tileSize;
            int ty = y / tileSize;
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
            int tx = x / tileSize;
            int ty = (y / tileSize) + (getScrollOffset() / tmWidth);
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

    private void selectionChanged() {
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
        // work out a general estimate
        double p = i / (double) tileCount;
        uivScrollbar.scrollPoint = p;
    }
}
